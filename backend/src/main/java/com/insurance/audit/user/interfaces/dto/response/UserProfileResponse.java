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
 * 用户个人资料响应
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户个人资料响应")
public class UserProfileResponse {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "user123")
    private String id;
    
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
     * 邮箱
     */
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
    
    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    /**
     * 个人简介
     */
    @Schema(description = "个人简介", example = "这是我的个人简介")
    private String bio;
    
    /**
     * 从User实体创建UserProfileResponse
     */
    public static UserProfileResponse fromUser(com.insurance.audit.user.domain.model.User user) {
        UserProfileResponseBuilder builder = UserProfileResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .avatar(user.getAvatar())
            .bio(user.getBio())
            .status(user.getStatus())
            .lastLoginTime(user.getLastLoginTime())
            .lastLoginIp(user.getLastLoginIp())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt());
            
        // 转换角色列表
        if (user.getRoles() != null) {
            List<String> roleNames = user.getRoles().stream()
                .map(com.insurance.audit.user.domain.model.Role::getName)
                .collect(java.util.stream.Collectors.toList());
            builder.roles(roleNames);
        }

        // 转换权限列表
        if (user.getPermissions() != null) {
            List<String> permissionCodes = user.getPermissions().stream()
                .map(com.insurance.audit.user.domain.model.Permission::getCode)
                .collect(java.util.stream.Collectors.toList());
            builder.permissions(permissionCodes);
        }
        
        return builder.build();
    }
    
    /**
     * 用户状态
     */
    @Schema(description = "用户状态")
    private UserStatus status;
    
    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间", example = "2024-01-01T12:00:00")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP", example = "192.168.1.1")
    private String lastLoginIp;
    
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
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;
    

}