package com.insurance.audit.user.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.insurance.audit.common.base.BaseEntity;
import com.insurance.audit.user.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("users")
@Schema(description = "用户实体")
public class User extends BaseEntity {
    
    /**
     * 用户名
     */
    @TableField("username")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50字符")
    @Schema(description = "用户名", required = true, example = "admin")
    private String username;
    
    /**
     * 密码
     */
    @TableField("password")
    @NotBlank(message = "密码不能为空")
    @Size(max = 255, message = "密码长度不能超过255字符")
    @Schema(description = "密码", required = true)
    private String password;
    
    /**
     * 真实姓名
     */
    @TableField("real_name")
    @Size(max = 100, message = "真实姓名长度不能超过100字符")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;
    
    /**
     * 邮箱
     */
    @TableField("email")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100字符")
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
    
    /**
     * 手机号
     */
    @TableField("phone")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(max = 20, message = "手机号长度不能超过20字符")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    /**
     * 头像URL
     */
    @TableField("avatar")
    @Size(max = 255, message = "头像URL长度不能超过255字符")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    /**
     * 用户状态
     */
    @TableField("status")
    @NotNull(message = "用户状态不能为空")
    @Schema(description = "用户状态", required = true)
    @lombok.Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    @Size(max = 50, message = "最后登录IP长度不能超过50字符")
    @Schema(description = "最后登录IP", example = "192.168.1.1")
    private String lastLoginIp;
    
    /**
     * 登录失败次数
     */
    @TableField("login_failure_count")
    @Schema(description = "登录失败次数", example = "0")
    @lombok.Builder.Default
    private Integer loginFailureCount = 0;
    
    /**
     * 锁定截止时间
     */
    @TableField("locked_until")
    @Schema(description = "锁定截止时间")
    private LocalDateTime lockedUntil;
    
    /**
     * 用户角色列表（不存储在数据库中）
     */
    @TableField(exist = false)
    @Schema(description = "用户角色列表")
    private List<Role> roles;

    /**
     * 用户权限列表（不存储在数据库中）
     */
    @TableField(exist = false)
    @Schema(description = "用户权限列表")
    private List<Permission> permissions;
    
    /**
     * 记录成功登录
     */
    public void recordSuccessfulLogin(String clientIp) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = clientIp;
        this.loginFailureCount = 0;
        this.lockedUntil = null;
    }
    
    /**
     * 记录失败登录
     */
    public void recordFailedLogin() {
        if (this.loginFailureCount == null) {
            this.loginFailureCount = 0;
        }
        this.loginFailureCount++;
        if (this.loginFailureCount >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }
    
    /**
     * 检查账户是否被锁定
     */
    public boolean isAccountLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(LocalDateTime.now());
    }
    
    /**
     * 检查账户是否启用
     */
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * 获取登录失败次数（兼容性方法）
     */
    public Integer getLoginAttempts() {
        return this.loginFailureCount;
    }
    
    /**
     * 设置登录失败次数（兼容性方法）
     */
    public void setLoginAttempts(Integer loginAttempts) {
        this.loginFailureCount = loginAttempts;
    }
    
    /**
     * 获取个人简介（兼容性方法）
     */
    public String getBio() {
        return this.avatar; // 将avatar字段作为bio使用
    }
    
    /**
     * 设置个人简介（兼容性方法）
     */
    public void setBio(String bio) {
        this.avatar = bio; // 将bio设置到avatar字段
    }
    
    /**
     * 自定义Builder类，添加兼容性方法
     */
    public static abstract class UserBuilder<C extends User, B extends UserBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
        
        /**
         * 设置登录失败次数（兼容性方法）
         */
        public B loginAttempts(Integer loginAttempts) {
            return loginFailureCount(loginAttempts);
        }
        
        /**
         * 设置个人简介（兼容性方法）
         */
        public B bio(String bio) {
            return avatar(bio);
        }
    }
}