package com.insurance.audit.user.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {
    
    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "user123")
    private String userId;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 255, message = "新密码长度必须在6-255字符之间")
    @Schema(description = "新密码", required = true, example = "newPassword123")
    private String newPassword;
    
    /**
     * 确认新密码
     */
    @NotBlank(message = "确认新密码不能为空")
    @Size(min = 6, max = 255, message = "确认新密码长度必须在6-255字符之间")
    @Schema(description = "确认新密码", required = true, example = "newPassword123")
    private String confirmPassword;
    
    /**
     * 重置令牌（可选，用于邮箱重置密码场景）
     */
    @Schema(description = "重置令牌", example = "reset-token-123")
    private String resetToken;
    
    /**
     * 用户名或邮箱（兼容性字段）
     */
    @Schema(description = "用户名或邮箱", example = "admin@example.com")
    private String usernameOrEmail;
    
    /**
     * 获取用户名或邮箱（兼容性方法）
     */
    public String getUsernameOrEmail() {
        return this.usernameOrEmail;
    }
    
    /**
     * 设置用户名或邮箱（兼容性方法）
     */
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
}