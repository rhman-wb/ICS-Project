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
 * 用户响应
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户响应")
public class UserResponse {
    
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
     * 登录失败次数
     */
    @Schema(description = "登录失败次数", example = "0")
    private Integer loginFailureCount;
    
    /**
     * 登录尝试次数（兼容性字段）
     */
    @Schema(description = "登录尝试次数", example = "0")
    private Integer loginAttempts;
    
    /**
     * 锁定截止时间
     */
    @Schema(description = "锁定截止时间", example = "2024-01-01T12:30:00")
    private LocalDateTime lockedUntil;
    
    /**
     * 是否被锁定
     */
    @Schema(description = "是否被锁定", example = "false")
    private Boolean locked;
    
    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
    
    /**
     * 角色列表
     */
    @Schema(description = "角色列表")
    private List<RoleInfo> roles;
    
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
    
    /**
     * 获取登录尝试次数（兼容性方法）
     */
    public Integer getLoginAttempts() {
        return this.loginAttempts != null ? this.loginAttempts : this.loginFailureCount;
    }
    
    /**
     * 设置登录尝试次数（兼容性方法）
     */
    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
        this.loginFailureCount = loginAttempts;
    }
    
    /**
     * 从User实体创建UserResponse
     */
    public static UserResponse fromUser(com.insurance.audit.user.domain.model.User user) {
        UserResponseBuilder builder = UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .avatar(user.getAvatar())
            .status(user.getStatus())
            .lastLoginTime(user.getLastLoginTime())
            .lastLoginIp(user.getLastLoginIp())
            .loginFailureCount(user.getLoginFailureCount())
            .loginAttempts(user.getLoginAttempts())
            .lockedUntil(user.getLockedUntil())
            .locked(user.isAccountLocked())
            .enabled(user.isEnabled())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt());
            
        // 转换角色列表
        if (user.getRoles() != null) {
            List<RoleInfo> roleInfos = user.getRoles().stream()
                .map(role -> RoleInfo.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .code(role.getCode())
                    .description(role.getDescription())
                    .build())
                .collect(java.util.stream.Collectors.toList());
            builder.roles(roleInfos);
        }
        
        return builder.build();
    }
    
    /**
     * 角色信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色信息")
    public static class RoleInfo {
        
        /**
         * 角色ID
         */
        @Schema(description = "角色ID", example = "role123")
        private String id;
        
        /**
         * 角色名称
         */
        @Schema(description = "角色名称", example = "管理员")
        private String name;
        
        /**
         * 角色编码
         */
        @Schema(description = "角色编码", example = "ADMIN")
        private String code;
        
        /**
         * 角色描述
         */
        @Schema(description = "角色描述", example = "系统管理员")
        private String description;
    }
}