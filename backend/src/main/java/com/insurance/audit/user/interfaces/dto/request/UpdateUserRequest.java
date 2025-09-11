package com.insurance.audit.user.interfaces.dto.request;

import com.insurance.audit.user.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 更新用户请求
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户请求")
public class UpdateUserRequest {
    
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
     * 用户状态
     */
    @Schema(description = "用户状态")
    private UserStatus status;
    
    /**
     * 角色ID列表
     */
    @Schema(description = "角色ID列表", example = "[\"role1\", \"role2\"]")
    private List<String> roleIds;
}