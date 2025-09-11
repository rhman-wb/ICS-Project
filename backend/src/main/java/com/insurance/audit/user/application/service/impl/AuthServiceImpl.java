package com.insurance.audit.user.application.service.impl;

import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.common.service.OperationLogService;
import com.insurance.audit.common.util.JwtUtil;
import com.insurance.audit.common.util.WebUtil;
import com.insurance.audit.user.application.service.AuthService;
import com.insurance.audit.user.domain.enums.UserStatus;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.mapper.UserMapper;
import com.insurance.audit.user.infrastructure.security.CustomUserDetails;
import com.insurance.audit.user.interfaces.dto.request.LoginRequest;
import com.insurance.audit.user.interfaces.dto.request.RefreshTokenRequest;
import com.insurance.audit.user.interfaces.dto.response.LoginResponse;
import com.insurance.audit.user.interfaces.dto.response.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * 
 * @author system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "true", matchIfMissing = true)
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OperationLogService operationLogService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    
    // Redis键前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String LOGIN_ATTEMPTS_PREFIX = "auth:attempts:";
    private static final String USER_SESSION_PREFIX = "auth:session:";
    
    // 配置常量
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String clientIp = WebUtil.getClientIp();
        String userAgent = WebUtil.getUserAgent();
        long startTime = System.currentTimeMillis();
        
        log.info("User login attempt - username: {}, clientIp: {}", username, clientIp);
        
        // 检查登录尝试次数
        checkLoginAttempts(username);
        
        // 查询用户信息（用于后续处理）
        User user = userMapper.findByUsername(username);
        
        try {
            // 检查用户状态
            if (user != null) {
                validateUserStatus(user);
            }
            
            // 手动校验密码，避免认证管理器配置差异导致的误判
            boolean reqMatches = user != null && passwordEncoder.matches(request.getPassword(), user.getPassword());
            // 仅用于开发排查：不输出明文，仅输出对 admin123 的匹配结果
            boolean admin123Matches = user != null && passwordEncoder.matches("admin123", user.getPassword());
            log.debug("Password match check - reqMatches={}, admin123Matches={}", reqMatches, admin123Matches);
            if (!reqMatches) {
                throw new BadCredentialsException("Invalid credentials");
            }
            
            // 通过 UserDetailsService 构造用户详情（含角色权限）
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
            
            // 清除登录失败记录
            clearLoginAttempts(username);
            
            // 更新用户登录信息
            updateUserLoginInfo(userDetails.getUserId(), clientIp);
            
            // 生成JWT令牌
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            // 缓存用户会话信息
            cacheUserSession(userDetails, accessToken, request.getRememberMe());
            
            // 记录登录成功日志
            long executionTime = System.currentTimeMillis() - startTime;
            operationLogService.recordLoginLog(userDetails.getUserId(), username, true, 
                    clientIp, userAgent, null);
            
            log.info("User login successful - userId: {}, username: {}, clientIp: {}, executionTime: {}ms", 
                    userDetails.getUserId(), username, clientIp, executionTime);
            
            // 构建响应
            return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .realName(userDetails.getRealName())
                .roles(userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .collect(Collectors.toList()))
                .permissions(userDetails.getPermissions())
                .loginTime(LocalDateTime.now())
                .build();
                
        } catch (BadCredentialsException e) {
            // 记录登录失败
            recordLoginFailure(username, user, clientIp, userAgent, "用户名或密码错误");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (DisabledException e) {
            recordLoginFailure(username, user, clientIp, userAgent, "用户已被禁用");
            throw new BusinessException(ErrorCode.USER_DISABLED);
        } catch (LockedException e) {
            recordLoginFailure(username, user, clientIp, userAgent, "用户账户已被锁定");
            throw new BusinessException(ErrorCode.USER_LOCKED);
        } catch (BusinessException e) {
            recordLoginFailure(username, user, clientIp, userAgent, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            recordLoginFailure(username, user, clientIp, userAgent, "登录失败: " + e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "登录失败，请稍后重试");
        }
    }
    
    @Override
    public void logout(String token) {
        String clientIp = WebUtil.getClientIp();
        String userAgent = WebUtil.getUserAgent();
        
        try {
            // 验证令牌
            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException(ErrorCode.TOKEN_INVALID);
            }
            
            // 获取令牌信息
            String jti = jwtUtil.getJtiFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 获取用户信息
            User user = userMapper.findByUsername(username);
            String userId = user != null ? user.getId() : null;
            
            // 将令牌加入黑名单
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + jti;
            long expiration = jwtUtil.getExpirationFromToken(token).getTime() - System.currentTimeMillis();
            
            if (expiration > 0) {
                redisTemplate.opsForValue().set(blacklistKey, username, expiration, TimeUnit.MILLISECONDS);
            }
            
            // 清除用户会话缓存
            clearUserSession(userId);
            
            // 记录退出日志
            operationLogService.recordLogoutLog(userId, username, clientIp, userAgent);
            
            log.info("User logout successful - username: {}, clientIp: {}", username, clientIp);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退出登录失败");
        }
    }
    
    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String clientIp = WebUtil.getClientIp();
        String userAgent = WebUtil.getUserAgent();
        
        try {
            // 验证刷新令牌
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                log.warn("Invalid refresh token from IP: {}", clientIp);
                throw new BusinessException(ErrorCode.TOKEN_INVALID, "无效的刷新令牌");
            }
            
            // 检查令牌是否在黑名单中
            if (isTokenBlacklisted(refreshToken)) {
                log.warn("Blacklisted refresh token used from IP: {}", clientIp);
                throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED, "令牌已失效");
            }
            
            // 获取用户信息
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            User user = userMapper.findByUsername(username);
            if (user == null) {
                log.warn("User not found for refresh token: {}", username);
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
            
            // 验证用户状态
            validateUserStatus(user);
            
            // 构建用户详情
            CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .enabled(user.isEnabled())
                .accountNonLocked(!user.isAccountLocked())
                .build();
            
            // 生成新的令牌
            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            // 将旧的刷新令牌加入黑名单
            String jti = jwtUtil.getJtiFromToken(refreshToken);
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + jti;
            long expiration = jwtUtil.getExpirationFromToken(refreshToken).getTime() - System.currentTimeMillis();
            if (expiration > 0) {
                redisTemplate.opsForValue().set(blacklistKey, username, expiration, TimeUnit.MILLISECONDS);
            }
            
            // 更新用户会话缓存
            cacheUserSession(userDetails, newAccessToken, false);
            
            log.info("Token refreshed successfully - userId: {}, username: {}, clientIp: {}", 
                    user.getId(), username, clientIp);
            
            return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .refreshTime(LocalDateTime.now())
                .build();
                
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Refresh token failed from IP: {}", clientIp, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "刷新令牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证用户状态
     */
    private void validateUserStatus(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 检查用户是否被禁用
        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        
        // 检查用户是否被锁定
        if (user.isAccountLocked()) {
            throw new BusinessException(ErrorCode.USER_LOCKED, 
                "账户已被锁定，请在" + LOCKOUT_DURATION_MINUTES + "分钟后重试");
        }
        
        // 检查用户状态
        if (UserStatus.LOCKED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_LOCKED);
        }
        
        if (UserStatus.INACTIVE.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
    }
    
    /**
     * 检查登录尝试次数
     */
    private void checkLoginAttempts(String username) {
        try {
            String key = LOGIN_ATTEMPTS_PREFIX + username;
            Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
            if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
                throw new BusinessException(ErrorCode.USER_LOCKED,
                        "登录失败次数过多，账户已被锁定" + LOCKOUT_DURATION_MINUTES + "分钟");
            }
        } catch (Exception e) {
            // 开发/容错：Redis 不可用时不阻断登录流程
            log.warn("Skip login-attempts check due to Redis error: {}", e.getMessage());
        }
    }
    
    /**
     * 记录登录失败
     */
    private void recordLoginFailure(String username, User user, String clientIp, 
                                   String userAgent, String errorMessage) {
        // 增加失败次数
        recordLoginAttempt(username);
        
        // 如果用户存在，更新数据库中的失败次数
        if (user != null) {
            try {
                user.recordFailedLogin();
                userMapper.updateById(user);
            } catch (Exception e) {
                log.error("Failed to update user login attempts for user: {}", username, e);
            }
        }
        
        // 记录日志
        String userId = user != null ? user.getId() : null;
        operationLogService.recordLoginLog(userId, username, false, clientIp, userAgent, errorMessage);
    }
    
    /**
     * 记录登录失败尝试（Redis）
     */
    private void recordLoginAttempt(String username) {
        try {
            String key = LOGIN_ATTEMPTS_PREFIX + username;
            Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
            attempts = attempts == null ? 1 : attempts + 1;
            redisTemplate.opsForValue().set(key, attempts, LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
            log.warn("Login attempt failed for user: {}, attempts: {}", username, attempts);
        } catch (Exception e) {
            log.error("Record login attempt failed due to Redis error: {}", e.getMessage());
        }
    }
    
    /**
     * 清除登录失败记录
     */
    private void clearLoginAttempts(String username) {
        try {
            String key = LOGIN_ATTEMPTS_PREFIX + username;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Clear login attempts skipped due to Redis error: {}", e.getMessage());
        }
    }
    
    /**
     * 更新用户登录信息
     */
    private void updateUserLoginInfo(String userId, String clientIp) {
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.recordSuccessfulLogin(clientIp);
                userMapper.updateById(user);
            }
        } catch (Exception e) {
            log.error("Failed to update user login info for user: {}", userId, e);
        }
    }
    
    /**
     * 缓存用户会话信息
     */
    private void cacheUserSession(CustomUserDetails userDetails, String accessToken, Boolean rememberMe) {
        try {
            String sessionKey = USER_SESSION_PREFIX + userDetails.getUserId();
            
            // 构建会话信息
            UserSessionInfo sessionInfo = UserSessionInfo.builder()
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .realName(userDetails.getRealName())
                .accessToken(accessToken)
                .loginTime(LocalDateTime.now())
                .clientIp(WebUtil.getClientIp())
                .userAgent(WebUtil.getUserAgent())
                .build();
            
            // 设置过期时间（记住我功能）
            int timeoutMinutes = Boolean.TRUE.equals(rememberMe) ? 
                SESSION_TIMEOUT_MINUTES * 24 * 7 : SESSION_TIMEOUT_MINUTES; // 7天 vs 30分钟
            
            redisTemplate.opsForValue().set(sessionKey, sessionInfo, timeoutMinutes, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            log.error("Failed to cache user session for user: {}", userDetails.getUserId(), e);
        }
    }
    
    /**
     * 清除用户会话缓存
     */
    private void clearUserSession(String userId) {
        if (userId != null) {
            try {
                String sessionKey = USER_SESSION_PREFIX + userId;
                redisTemplate.delete(sessionKey);
            } catch (Exception e) {
                log.error("Failed to clear user session for user: {}", userId, e);
            }
        }
    }
    
    @Override
    public boolean isSessionValid(String userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            String sessionKey = USER_SESSION_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
        } catch (Exception e) {
            log.error("Failed to check session validity for user: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public Object getUserSession(String userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            String sessionKey = USER_SESSION_PREFIX + userId;
            return redisTemplate.opsForValue().get(sessionKey);
        } catch (Exception e) {
            log.error("Failed to get user session for user: {}", userId, e);
            return null;
        }
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 验证令牌格式
            if (!jwtUtil.validateToken(token)) {
                return false;
            }
            
            // 检查令牌是否在黑名单中
            String jti = jwtUtil.getJtiFromToken(token);
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + jti;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
            
        } catch (Exception e) {
            log.error("Failed to check token blacklist status", e);
            return false;
        }
    }
    
    /**
     * 用户会话信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UserSessionInfo {
        private String userId;
        private String username;
        private String realName;
        private String accessToken;
        private LocalDateTime loginTime;
        private String clientIp;
        private String userAgent;
    }
}