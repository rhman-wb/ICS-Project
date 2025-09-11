package com.insurance.audit.user.application.service;

import com.insurance.audit.user.interfaces.dto.request.LoginRequest;
import com.insurance.audit.user.interfaces.dto.request.RefreshTokenRequest;
import com.insurance.audit.user.interfaces.dto.response.LoginResponse;
import com.insurance.audit.user.interfaces.dto.response.RefreshTokenResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 认证服务接口
 * 
 * @author system
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(@Valid LoginRequest request);
    
    /**
     * 用户退出登录
     * 
     * @param token JWT令牌
     */
    void logout(@NotBlank String token);
    
    /**
     * 刷新访问令牌
     * 
     * @param request 刷新令牌请求
     * @return 刷新令牌响应
     */
    RefreshTokenResponse refreshToken(@Valid RefreshTokenRequest request);
    
    /**
     * 检查令牌是否在黑名单中
     * 
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    boolean isTokenBlacklisted(@NotBlank String token);
    
    /**
     * 检查用户会话是否有效
     * 
     * @param userId 用户ID
     * @return 是否有效
     */
    boolean isSessionValid(@NotBlank String userId);
    
    /**
     * 获取用户会话信息
     * 
     * @param userId 用户ID
     * @return 会话信息
     */
    Object getUserSession(@NotBlank String userId);
}