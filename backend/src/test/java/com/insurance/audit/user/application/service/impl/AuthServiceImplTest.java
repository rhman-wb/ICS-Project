package com.insurance.audit.user.application.service.impl;

import com.insurance.audit.common.TestDataFactory;
import com.insurance.audit.common.TestSecurityUtils;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.util.JwtUtil;
import com.insurance.audit.user.application.service.UserService;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.mapper.UserMapper;
import com.insurance.audit.user.infrastructure.security.CustomUserDetails;
import com.insurance.audit.user.interfaces.dto.request.LoginRequest;
import com.insurance.audit.user.interfaces.dto.request.RefreshTokenRequest;
import com.insurance.audit.user.interfaces.dto.response.LoginResponse;
import com.insurance.audit.user.interfaces.dto.response.RefreshTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务实现类测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务实现类测试")
class AuthServiceImplTest {
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private UserService userService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        TestSecurityUtils.clearSecurityContext();
    }
    
    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() {
        // Given
        LoginRequest request = TestDataFactory.createDefaultLoginRequest();
        User user = TestDataFactory.createDefaultUser();
        CustomUserDetails userDetails = TestDataFactory.createDefaultCustomUserDetails();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userMapper.selectByUsername(request.getUsername())).thenReturn(user);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn(TestDataFactory.DEFAULT_JWT_TOKEN);
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn(TestDataFactory.DEFAULT_REFRESH_TOKEN);
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        
        // When
        LoginResponse response = authService.login(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TestDataFactory.DEFAULT_JWT_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(TestDataFactory.DEFAULT_REFRESH_TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userMapper).selectByUsername(request.getUsername());
        verify(userMapper).updateById(user);
        verify(jwtUtil).generateAccessToken(userDetails);
        verify(jwtUtil).generateRefreshToken(userDetails);
    }
    
    @Test
    @DisplayName("用户登录 - 用户名或密码错误")
    void login_BadCredentials() {
        // Given
        LoginRequest request = TestDataFactory.createDefaultLoginRequest();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("用户名或密码错误"));
        
        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userMapper, never()).selectByUsername(anyString());
    }
    
    @Test
    @DisplayName("用户登录 - 账户被禁用")
    void login_AccountDisabled() {
        // Given
        LoginRequest request = TestDataFactory.createDefaultLoginRequest();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("账户已被禁用"));
        
        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账户已被禁用");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
    
    @Test
    @DisplayName("用户登录 - 账户被锁定")
    void login_AccountLocked() {
        // Given
        LoginRequest request = TestDataFactory.createDefaultLoginRequest();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("账户已被锁定"));
        
        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账户已被锁定");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
    
    @Test
    @DisplayName("用户登录 - 记住我功能")
    void login_RememberMe() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username(TestDataFactory.DEFAULT_USERNAME)
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .rememberMe(true)
                .build();
        
        User user = TestDataFactory.createDefaultUser();
        CustomUserDetails userDetails = TestDataFactory.createDefaultCustomUserDetails();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userMapper.selectByUsername(request.getUsername())).thenReturn(user);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn(TestDataFactory.DEFAULT_JWT_TOKEN);
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn(TestDataFactory.DEFAULT_REFRESH_TOKEN);
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(604800L);
        
        // When
        LoginResponse response = authService.login(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        
        verify(valueOperations).set(
                eq("refresh_token:" + TestDataFactory.DEFAULT_REFRESH_TOKEN),
                eq(userDetails.getUserId()),
                eq(604800L),
                eq(TimeUnit.SECONDS)
        );
    }
    
    @Test
    @DisplayName("刷新令牌 - 成功")
    void refreshToken_Success() {
        // Given
        RefreshTokenRequest request = TestDataFactory.createDefaultRefreshTokenRequest();
        User user = TestDataFactory.createDefaultUser();
        CustomUserDetails userDetails = TestDataFactory.createDefaultCustomUserDetails();
        
        when(jwtUtil.validateRefreshToken(request.getRefreshToken())).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(request.getRefreshToken())).thenReturn(user.getUsername());
        when(valueOperations.get("refresh_token:" + request.getRefreshToken())).thenReturn(user.getId());
        when(userMapper.selectByUsername(user.getUsername())).thenReturn(user);
        when(jwtUtil.generateAccessToken(any(CustomUserDetails.class))).thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400L);
        
        // When
        RefreshTokenResponse response = authService.refreshToken(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        
        verify(jwtUtil).validateRefreshToken(request.getRefreshToken());
        verify(jwtUtil).getUsernameFromToken(request.getRefreshToken());
        verify(valueOperations).get("refresh_token:" + request.getRefreshToken());
        verify(userMapper).selectByUsername(user.getUsername());
    }
    
    @Test
    @DisplayName("刷新令牌 - 无效令牌")
    void refreshToken_InvalidToken() {
        // Given
        RefreshTokenRequest request = TestDataFactory.createDefaultRefreshTokenRequest();
        
        when(jwtUtil.validateRefreshToken(request.getRefreshToken())).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无效的刷新令牌");
        
        verify(jwtUtil).validateRefreshToken(request.getRefreshToken());
        verify(valueOperations, never()).get(anyString());
    }
    
    @Test
    @DisplayName("刷新令牌 - 令牌已被撤销")
    void refreshToken_TokenRevoked() {
        // Given
        RefreshTokenRequest request = TestDataFactory.createDefaultRefreshTokenRequest();
        
        when(jwtUtil.validateRefreshToken(request.getRefreshToken())).thenReturn(true);
        when(valueOperations.get("refresh_token:" + request.getRefreshToken())).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("刷新令牌已被撤销");
        
        verify(jwtUtil).validateRefreshToken(request.getRefreshToken());
        verify(valueOperations).get("refresh_token:" + request.getRefreshToken());
    }
    
    @Test
    @DisplayName("用户退出 - 成功")
    void logout_Success() {
        // Given
        TestSecurityUtils.setDefaultUserSecurityContext();
        String refreshToken = TestDataFactory.DEFAULT_REFRESH_TOKEN;
        
        when(jwtUtil.getJtiFromToken(anyString())).thenReturn("test-jti");
        when(jwtUtil.getExpirationFromToken(anyString())).thenReturn(
                new java.util.Date(System.currentTimeMillis() + 86400000));
        
        // When
        authService.logout(refreshToken);
        
        // Then
        verify(valueOperations).set(
                eq("blacklist:test-jti"),
                eq("true"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
        verify(redisTemplate).delete("refresh_token:" + refreshToken);
        
        TestSecurityUtils.clearSecurityContext();
    }
    
    @Test
    @DisplayName("用户退出 - 未登录")
    void logout_NotAuthenticated() {
        // Given
        TestSecurityUtils.clearSecurityContext();
        String refreshToken = TestDataFactory.DEFAULT_REFRESH_TOKEN;
        
        // When & Then
        assertThatThrownBy(() -> authService.logout(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户未登录");
    }
    
    @Test
    @DisplayName("检查令牌是否在黑名单 - 在黑名单中")
    void isTokenBlacklisted_InBlacklist() {
        // Given
        String token = TestDataFactory.DEFAULT_JWT_TOKEN;
        String jti = "test-jti";
        
        when(jwtUtil.getJtiFromToken(token)).thenReturn(jti);
        when(redisTemplate.hasKey("blacklist:" + jti)).thenReturn(true);
        
        // When
        boolean result = authService.isTokenBlacklisted(token);
        
        // Then
        assertThat(result).isTrue();
        verify(jwtUtil).getJtiFromToken(token);
        verify(redisTemplate).hasKey("blacklist:" + jti);
    }
    
    @Test
    @DisplayName("检查令牌是否在黑名单 - 不在黑名单中")
    void isTokenBlacklisted_NotInBlacklist() {
        // Given
        String token = TestDataFactory.DEFAULT_JWT_TOKEN;
        String jti = "test-jti";
        
        when(jwtUtil.getJtiFromToken(token)).thenReturn(jti);
        when(redisTemplate.hasKey("blacklist:" + jti)).thenReturn(false);
        
        // When
        boolean result = authService.isTokenBlacklisted(token);
        
        // Then
        assertThat(result).isFalse();
        verify(jwtUtil).getJtiFromToken(token);
        verify(redisTemplate).hasKey("blacklist:" + jti);
    }
    
    // 注意：recordSuccessfulLogin 和 recordFailedLogin 是私有方法，通过login方法间接测试
    // 注意：validatePassword 方法在AuthService接口中不存在，密码验证通过UserService进行
}