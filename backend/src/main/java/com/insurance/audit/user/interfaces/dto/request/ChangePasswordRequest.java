package com.insurance.audit.user.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {
    
    /**
     * 当前密码
     */
    @NotBlank(message = "当前密码不能为空")
    @Size(max = 255, message = "当前密码长度不能超过255字符")
    @Schema(description = "当前密码", required = true, example = "oldPassword123")
    private String currentPassword;
    
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
     * 获取旧密码（兼容性方法）
     */
    public String getOldPassword() {
        return this.currentPassword;
    }
    
    /**
     * 设置旧密码（兼容性方法）
     */
    public void setOldPassword(String oldPassword) {
        this.currentPassword = oldPassword;
    }
}