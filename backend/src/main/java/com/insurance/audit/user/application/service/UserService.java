package com.insurance.audit.user.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.domain.model.Permission;
import com.insurance.audit.user.interfaces.dto.request.CreateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.UpdateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.ChangePasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.ResetPasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.UserQueryRequest;
import com.insurance.audit.user.interfaces.dto.request.UserProfileRequest;
import com.insurance.audit.user.interfaces.dto.response.UserResponse;
import com.insurance.audit.user.interfaces.dto.response.UserProfileResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户管理服务接口
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface UserService {
    
    // ==================== 用户CRUD操作 ====================
    
    /**
     * 创建用户
     * 
     * @param request 创建用户请求
     * @return 用户响应
     */
    UserResponse createUser(@Valid CreateUserRequest request);
    
    /**
     * 根据ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户响应
     */
    UserResponse getUserById(@NotBlank String userId);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户响应
     */
    UserResponse getUserByUsername(@NotBlank String username);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param request 更新用户请求
     * @return 用户响应
     */
    UserResponse updateUser(@NotBlank String userId, @Valid UpdateUserRequest request);
    
    /**
     * 删除用户（软删除）
     * 
     * @param userId 用户ID
     */
    void deleteUser(@NotBlank String userId);
    
    /**
     * 批量删除用户
     * 
     * @param userIds 用户ID列表
     */
    void batchDeleteUsers(@NotEmpty List<String> userIds);
    
    /**
     * 获取所有用户列表
     * 
     * @return 用户列表
     */
    List<UserResponse> getAllUsers();
    
    /**
     * 分页查询用户列表
     * 
     * @param request 查询请求
     * @return 分页用户列表
     */
    Page<UserResponse> getUserList(@Valid UserQueryRequest request);
    
    // ==================== 密码管理功能 ====================
    
    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param request 修改密码请求
     */
    void changePassword(@NotBlank String userId, @Valid ChangePasswordRequest request);
    
    /**
     * 重置密码
     * 
     * @param request 重置密码请求
     */
    void resetPassword(@Valid ResetPasswordRequest request);
    
    /**
     * 管理员重置用户密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void adminResetPassword(@NotBlank String userId, @NotBlank String newPassword);
    
    // ==================== 角色分配功能 ====================
    
    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(@NotBlank String userId, @NotEmpty List<String> roleIds);
    
    /**
     * 移除用户角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void removeRoles(@NotBlank String userId, @NotEmpty List<String> roleIds);
    
    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(@NotBlank String userId);
    
    /**
     * 设置用户角色（覆盖原有角色）
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void setUserRoles(@NotBlank String userId, List<String> roleIds);
    
    // ==================== 权限验证功能 ====================
    
    /**
     * 检查用户是否具有特定权限
     * 
     * @param userId 用户ID
     * @param permissionName 权限名称
     * @return 是否具有权限
     */
    boolean hasPermission(@NotBlank String userId, @NotBlank String permissionName);
    
    /**
     * 检查用户是否具有特定角色
     * 
     * @param userId 用户ID
     * @param roleName 角色名称
     * @return 是否具有角色
     */
    boolean hasRole(@NotBlank String userId, @NotBlank String roleName);
    
    /**
     * 获取用户权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getUserPermissions(@NotBlank String userId);
    
    /**
     * 检查用户是否具有任一权限
     * 
     * @param userId 用户ID
     * @param permissionNames 权限名称列表
     * @return 是否具有任一权限
     */
    boolean hasAnyPermission(@NotBlank String userId, @NotEmpty List<String> permissionNames);
    
    /**
     * 检查用户是否具有所有权限
     * 
     * @param userId 用户ID
     * @param permissionNames 权限名称列表
     * @return 是否具有所有权限
     */
    boolean hasAllPermissions(@NotBlank String userId, @NotEmpty List<String> permissionNames);
    
    // ==================== 用户状态管理 ====================
    
    /**
     * 启用用户
     * 
     * @param userId 用户ID
     */
    void enableUser(@NotBlank String userId);
    
    /**
     * 禁用用户
     * 
     * @param userId 用户ID
     */
    void disableUser(@NotBlank String userId);
    
    /**
     * 锁定用户
     * 
     * @param userId 用户ID
     * @param lockMinutes 锁定时长（分钟）
     */
    void lockUser(@NotBlank String userId, @NotNull Integer lockMinutes);
    
    /**
     * 解锁用户
     * 
     * @param userId 用户ID
     */
    void unlockUser(@NotBlank String userId);
    
    // ==================== 用户个人资料管理 ====================
    
    /**
     * 获取当前用户个人资料
     * 
     * @param userId 用户ID
     * @return 用户个人资料响应
     */
    UserProfileResponse getCurrentUserProfile(@NotBlank String userId);
    
    /**
     * 更新用户个人资料
     * 
     * @param userId 用户ID
     * @param request 用户个人资料更新请求
     * @return 更新后的用户个人资料响应
     */
    UserProfileResponse updateUserProfile(@NotBlank String userId, @Valid UserProfileRequest request);
    
    // ==================== 用户验证功能 ====================
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(@NotBlank String username);
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(@NotBlank String email);
    
    /**
     * 验证用户密码
     * 
     * @param userId 用户ID
     * @param rawPassword 原始密码
     * @return 是否正确
     */
    boolean validatePassword(@NotBlank String userId, @NotBlank String rawPassword);
}