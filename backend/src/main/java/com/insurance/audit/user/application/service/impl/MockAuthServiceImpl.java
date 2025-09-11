package com.insurance.audit.user.application.service.impl;

import com.insurance.audit.user.application.service.AuthService;
import com.insurance.audit.user.interfaces.dto.request.LoginRequest;
import com.insurance.audit.user.interfaces.dto.request.RefreshTokenRequest;
import com.insurance.audit.user.interfaces.dto.response.LoginResponse;
import com.insurance.audit.user.interfaces.dto.response.RefreshTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Mock认证服务实现类 - 用于debug环境
 * 
 * @author system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "false")
public class MockAuthServiceImpl implements AuthService {
    
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Mock login for user: {}", request.getUsername());
        
        // 返回模拟的登录响应
        return LoginResponse.builder()
            .accessToken("mock-access-token-" + UUID.randomUUID().toString())
            .refreshToken("mock-refresh-token-" + UUID.randomUUID().toString())
            .tokenType("Bearer")
            .expiresIn(3600L)
            .userId("mock-user-id")
            .username(request.getUsername())
            .realName("Mock User")
            .roles(Arrays.asList("USER"))
            .permissions(Arrays.asList("READ", "WRITE"))
            .loginTime(LocalDateTime.now())
            .build();
    }
    
    @Override
    public void logout(String token) {
        log.info("Mock logout for token: {}", token);
        // Mock实现，不做实际操作
    }
    
    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Mock refresh token");
        
        return RefreshTokenResponse.builder()
            .accessToken("mock-new-access-token-" + UUID.randomUUID().toString())
            .refreshToken("mock-new-refresh-token-" + UUID.randomUUID().toString())
            .tokenType("Bearer")
            .expiresIn(3600L)
            .refreshTime(LocalDateTime.now())
            .build();
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        log.debug("Mock check token blacklist: {}", token);
        return false; // Mock实现总是返回false
    }
    
    @Override
    public boolean isSessionValid(String userId) {
        log.debug("Mock check session valid: {}", userId);
        return true; // Mock实现总是返回true
    }
    
    @Override
    public Object getUserSession(String userId) {
        log.debug("Mock get user session: {}", userId);
        return "mock-session-" + userId;
    }
}