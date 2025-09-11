package com.insurance.audit.user.interfaces.dto.response;

import com.insurance.audit.user.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户登录响应
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应")
public class LoginResponse {
    
    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * 令牌过期时间（秒）
     */
    @Schema(description = "令牌过期时间（秒）", example = "3600")
    private Long expiresIn;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "user123")
    private String userId;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;
    
    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", example = "张三")
    private String realName;
    
    /**
     * 角色列表
     */
    @Schema(description = "角色列表")
    private List<String> roles;
    
    /**
     * 权限列表
     */
    @Schema(description = "权限列表")
    private List<String> permissions;
    
    /**
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2024-01-01T12:00:00")
    private LocalDateTime loginTime;
    
    /**
     * 获取用户ID（兼容性方法）
     */
    public String getUserId() {
        return this.userId;
    }
    
    /**
     * 设置用户ID（兼容性方法）
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}