package com.insurance.audit.user.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户个人资料请求
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户个人资料请求")
public class UserProfileRequest {
    
    /**
     * 真实姓名
     */
    @Size(max = 100, message = "真实姓名长度不能超过100字符")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100字符")
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(max = 20, message = "手机号长度不能超过20字符")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255字符")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    /**
     * 个人简介（兼容性字段）
     */
    @Size(max = 500, message = "个人简介长度不能超过500字符")
    @Schema(description = "个人简介", example = "这是我的个人简介")
    private String bio;
    
    /**
     * 获取个人简介（兼容性方法）
     */
    public String getBio() {
        return this.bio;
    }
    
    /**
     * 设置个人简介（兼容性方法）
     */
    public void setBio(String bio) {
        this.bio = bio;
    }
}