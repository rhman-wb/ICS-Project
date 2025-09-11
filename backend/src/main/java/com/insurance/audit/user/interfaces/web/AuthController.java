package com.insurance.audit.user.interfaces.web;

import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.user.application.service.AuthService;
import com.insurance.audit.user.interfaces.dto.request.LoginRequest;
import com.insurance.audit.user.interfaces.dto.request.RefreshTokenRequest;
import com.insurance.audit.user.interfaces.dto.response.LoginResponse;
import com.insurance.audit.user.interfaces.dto.response.RefreshTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关API")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户通过用户名和密码登录系统")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "用户名或密码错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "账户被锁定"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "系统内部错误")
    })
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("User login attempt: username={}, ip={}", 
            request.getUsername(), getClientIpAddress(httpRequest));
        
        LoginResponse response = authService.login(request);
        
        log.info("User login successful: username={}, userId={}", 
            request.getUsername(), response.getUserId());
        
        return ApiResponse.success(response);
    }
    
    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出登录，清除会话信息")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "退出成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授权"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "系统内部错误")
    })
    public ApiResponse<Void> logout(
            @Parameter(description = "Authorization头部", required = true)
            HttpServletRequest request) {
        
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            log.warn("Logout attempt without token from IP: {}", getClientIpAddress(request));
            return ApiResponse.error(400, "缺少认证令牌");
        }
        
        authService.logout(token);
        
        log.info("User logout successful, token invalidated");
        
        return ApiResponse.success("退出登录成功");
    }
    
    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "刷新成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "刷新令牌无效"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "系统内部错误")
    })
    public ApiResponse<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Token refresh attempt from IP: {}", getClientIpAddress(httpRequest));
        
        RefreshTokenResponse response = authService.refreshToken(request);
        
        log.info("Token refresh successful");
        
        return ApiResponse.success(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授权"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "系统内部错误")
    })
    public ApiResponse<Object> getCurrentUser() {
        // TODO: 实现获取当前用户信息
        // 这个功能将在用户管理API接口任务中实现
        return ApiResponse.success("当前用户信息获取功能待实现");
    }
    
    /**
     * 从请求中提取JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}