package com.insurance.audit.user.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.common.enums.OperationStatus;
import com.insurance.audit.common.enums.OperationType;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.common.service.OperationLogService;
import com.insurance.audit.common.util.WebUtil;
import com.insurance.audit.user.application.service.UserService;
import com.insurance.audit.user.domain.enums.UserStatus;
import com.insurance.audit.user.domain.model.Permission;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.mapper.PermissionMapper;
import com.insurance.audit.user.infrastructure.mapper.RoleMapper;
import com.insurance.audit.user.infrastructure.mapper.UserMapper;
import com.insurance.audit.user.interfaces.dto.request.ChangePasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.CreateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.ResetPasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.UpdateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.UserQueryRequest;
import com.insurance.audit.user.interfaces.dto.request.UserProfileRequest;
import com.insurance.audit.user.interfaces.dto.response.UserResponse;
import com.insurance.audit.user.interfaces.dto.response.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "true", matchIfMissing = true)
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;
    
    // ==================== 用户CRUD操作 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.getUsername());
        
        // 验证用户名是否已存在
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "用户名已存在: " + request.getUsername());
        }
        
        // 验证邮箱是否已存在
        if (StringUtils.hasText(request.getEmail()) && userMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "邮箱已存在: " + request.getEmail());
        }
        
        // 验证角色是否存在
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            validateRolesExist(request.getRoleIds());
        }
        
        // 创建用户实体
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(request.getStatus())
                .loginAttempts(0)
                .build();
        
        // 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建用户失败");
        }
        
        // 分配角色
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            userMapper.batchInsertUserRoles(user.getId(), request.getRoleIds());
        }
        
        // 记录操作日志
        recordUserOperation(user.getId(), request.getUsername(), OperationType.CREATE, 
                "创建用户", OperationStatus.SUCCESS, null);
        
        log.info("User created successfully: {}", user.getId());
        return convertToUserResponse(user);
    }
    
    @Override
    public UserResponse getUserById(String userId) {
        User user = userMapper.findUserWithRolesById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        return convertToUserResponse(user);
    }
    
    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userMapper.findUserWithRolesByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + username);
        }
        return convertToUserResponse(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        log.info("Updating user: {}", userId);
        
        // 检查用户是否存在
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 验证邮箱是否已被其他用户使用
        if (StringUtils.hasText(request.getEmail()) && 
            userMapper.existsByEmailExcludeUser(request.getEmail(), userId)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "邮箱已被其他用户使用: " + request.getEmail());
        }
        
        // 更新用户信息
        User user = User.builder()
                .realName(request.getRealName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(request.getStatus())
                .build();
        user.setId(userId);
        
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新用户失败");
        }
        
        // 记录操作日志
        recordUserOperation(userId, existingUser.getUsername(), OperationType.UPDATE, 
                "更新用户信息", OperationStatus.SUCCESS, null);
        
        log.info("User updated successfully: {}", userId);
        return getUserById(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 软删除用户
        int result = userMapper.deleteById(userId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "删除用户失败");
        }
        
        // 清除用户角色关联
        userMapper.deleteAllUserRoles(userId);
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.DELETE, 
                "删除用户", OperationStatus.SUCCESS, null);
        
        log.info("User deleted successfully: {}", userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<String> userIds) {
        log.info("Batch deleting users: {}", userIds);
        
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        
        // 批量软删除用户
        int result = userMapper.batchDeleteUsers(userIds);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "批量删除用户失败");
        }
        
        // 清除用户角色关联
        for (String userId : userIds) {
            userMapper.deleteAllUserRoles(userId);
        }
        
        // 记录操作日志
        recordUserOperation(null, null, OperationType.DELETE, 
                "批量删除用户: " + userIds.size() + "个", OperationStatus.SUCCESS, null);
        
        log.info("Users batch deleted successfully: {}", userIds.size());
    }
    
    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        
        // 查询所有用户（不包括已删除的）
        List<User> users = userMapper.selectAll();
        
        // 转换为响应对象
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<UserResponse> getUserList(UserQueryRequest request) {
        Page<User> page = new Page<>(request.getCurrent(), request.getSize());
        Page<User> userPage = userMapper.selectUserPage(page, request);
        
        // 转换为响应对象
        Page<UserResponse> responsePage = new Page<>();
        responsePage.setCurrent(userPage.getCurrent());
        responsePage.setSize(userPage.getSize());
        responsePage.setTotal(userPage.getTotal());
        responsePage.setPages(userPage.getPages());
        
        List<UserResponse> userResponses = userPage.getRecords().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(userResponses);
        
        return responsePage;
    }
    
    // ==================== 密码管理功能 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);
        
        // 验证新密码和确认密码是否一致
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "新密码和确认密码不一致");
        }
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "原密码不正确");
        }
        
        // 更新密码
        User updateUser = User.builder()
                .password(passwordEncoder.encode(request.getNewPassword()))
                .build();
        updateUser.setId(userId);
        
        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "修改密码失败");
        }
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.UPDATE, 
                "修改密码", OperationStatus.SUCCESS, null);
        
        log.info("Password changed successfully for user: {}", userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for: {}", request.getUsernameOrEmail());
        
        // 验证新密码和确认密码是否一致
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "新密码和确认密码不一致");
        }
        
        // 查找用户（通过用户名或邮箱）
        User user = userMapper.findByUsername(request.getUsernameOrEmail());
        if (user == null) {
            user = userMapper.findByEmail(request.getUsernameOrEmail()).orElse(null);
        }
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + request.getUsernameOrEmail());
        }
        
        // TODO: 验证验证码（这里简化处理，实际应该验证短信或邮箱验证码）
        // validateVerificationCode(user, request.getVerificationCode());
        
        // 更新密码
        User updateUser = User.builder()
                .password(passwordEncoder.encode(request.getNewPassword()))
                .build();
        updateUser.setId(user.getId());
        
        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "重置密码失败");
        }
        
        // 记录操作日志
        recordUserOperation(user.getId(), user.getUsername(), OperationType.UPDATE, 
                "重置密码", OperationStatus.SUCCESS, null);
        
        log.info("Password reset successfully for user: {}", user.getId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminResetPassword(String userId, String newPassword) {
        log.info("Admin resetting password for user: {}", userId);
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 更新密码
        User updateUser = User.builder()
                .password(passwordEncoder.encode(newPassword))
                .build();
        updateUser.setId(userId);
        
        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "重置密码失败");
        }
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.UPDATE, 
                "管理员重置密码", OperationStatus.SUCCESS, null);
        
        log.info("Password reset by admin successfully for user: {}", userId);
    }
    
    // ==================== 角色分配功能 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(String userId, List<String> roleIds) {
        log.info("Assigning roles to user: {}, roles: {}", userId, roleIds);
        
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 验证角色是否存在
        validateRolesExist(roleIds);
        
        // 分配角色
        int result = userMapper.batchInsertUserRoles(userId, roleIds);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "分配角色失败");
        }
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.UPDATE, 
                "分配角色: " + roleIds.size() + "个", OperationStatus.SUCCESS, null);
        
        log.info("Roles assigned successfully to user: {}", userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoles(String userId, List<String> roleIds) {
        log.info("Removing roles from user: {}, roles: {}", userId, roleIds);
        
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 移除角色
        int result = userMapper.batchDeleteUserRoles(userId, roleIds);
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.UPDATE, 
                "移除角色: " + roleIds.size() + "个", OperationStatus.SUCCESS, null);
        
        log.info("Roles removed successfully from user: {}", userId);
    }
    
    @Override
    public List<Role> getUserRoles(String userId) {
        return roleMapper.findByUserId(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setUserRoles(String userId, List<String> roleIds) {
        log.info("Setting roles for user: {}, roles: {}", userId, roleIds);
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 清除原有角色
        userMapper.deleteAllUserRoles(userId);
        
        // 分配新角色
        if (!CollectionUtils.isEmpty(roleIds)) {
            validateRolesExist(roleIds);
            userMapper.batchInsertUserRoles(userId, roleIds);
        }
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.UPDATE, 
                "设置角色: " + (CollectionUtils.isEmpty(roleIds) ? 0 : roleIds.size()) + "个", 
                OperationStatus.SUCCESS, null);
        
        log.info("Roles set successfully for user: {}", userId);
    }
    
    // ==================== 权限验证功能 ====================
    
    @Override
    public boolean hasPermission(String userId, String permissionName) {
        return userMapper.hasPermission(userId, permissionName);
    }
    
    @Override
    public boolean hasRole(String userId, String roleName) {
        return userMapper.hasRole(userId, roleName);
    }
    
    @Override
    public List<Permission> getUserPermissions(String userId) {
        return permissionMapper.findByUserId(userId);
    }
    
    @Override
    public boolean hasAnyPermission(String userId, List<String> permissionNames) {
        if (CollectionUtils.isEmpty(permissionNames)) {
            return false;
        }
        
        return permissionNames.stream()
                .anyMatch(permission -> hasPermission(userId, permission));
    }
    
    @Override
    public boolean hasAllPermissions(String userId, List<String> permissionNames) {
        if (CollectionUtils.isEmpty(permissionNames)) {
            return true;
        }
        
        return permissionNames.stream()
                .allMatch(permission -> hasPermission(userId, permission));
    }
    
    // ==================== 用户状态管理 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(String userId) {
        updateUserStatus(userId, UserStatus.ACTIVE, "启用用户");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(String userId) {
        updateUserStatus(userId, UserStatus.INACTIVE, "禁用用户");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockUser(String userId, Integer lockMinutes) {
        log.info("Locking user: {} for {} minutes", userId, lockMinutes);
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 锁定用户
        User updateUser = User.builder()
                .status(UserStatus.LOCKED)
                .lockedUntil(LocalDateTime.now().plusMinutes(lockMinutes))
                .build();
        updateUser.setId(userId);
        
        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "锁定用户失败");
        }
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.UPDATE, 
                "锁定用户: " + lockMinutes + "分钟", OperationStatus.SUCCESS, null);
        
        log.info("User locked successfully: {}", userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockUser(String userId) {
        log.info("Unlocking user: {}", userId);
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 解锁用户
        User updateUser = User.builder()
                .status(UserStatus.ACTIVE)
                .lockedUntil(null)
                .loginAttempts(0)
                .build();
        updateUser.setId(userId);
        
        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "解锁用户失败");
        }
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.UPDATE, 
                "解锁用户", OperationStatus.SUCCESS, null);
        
        log.info("User unlocked successfully: {}", userId);
    }
    
    // ==================== 用户验证功能 ====================
    
    @Override
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }
    
    @Override
    public boolean validatePassword(String userId, String rawPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
    
    // ==================== 用户个人资料管理 ====================
    
    @Override
    public UserProfileResponse getCurrentUserProfile(String userId) {
        log.info("Getting user profile: {}", userId);
        
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 查询用户角色
        List<Role> roles = roleMapper.findByUserId(userId);
        user.setRoles(roles);

        // 查询用户权限
        List<Permission> permissions = getUserPermissions(userId);
        user.setPermissions(permissions);
        
        return convertToUserProfileResponse(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse updateUserProfile(String userId, UserProfileRequest request) {
        log.info("Updating user profile: {}", userId);
        
        // 检查用户是否存在
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(request.getEmail()) && 
            !Objects.equals(request.getEmail(), existingUser.getEmail())) {
            if (userMapper.existsByEmail(request.getEmail())) {
                throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "邮箱已被其他用户使用: " + request.getEmail());
            }
        }
        
        // 检查手机号是否被其他用户使用
        if (StringUtils.hasText(request.getPhone()) && 
            !Objects.equals(request.getPhone(), existingUser.getPhone())) {
            if (userMapper.existsByPhone(request.getPhone())) {
                throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "手机号已被其他用户使用: " + request.getPhone());
            }
        }
        
        // 更新用户信息
        User updateUser = User.builder()
                .realName(request.getRealName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .bio(request.getBio())
                .build();
        updateUser.setId(userId);
        
        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新用户资料失败");
        }
        
        // 记录操作日志
        recordUserOperation(userId, existingUser.getUsername(), OperationType.UPDATE, 
                "更新个人资料", OperationStatus.SUCCESS, null);
        
        log.info("User profile updated successfully: {}", userId);
        
        // 返回更新后的用户资料
        return getCurrentUserProfile(userId);
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 更新用户状态
     */
    private void updateUserStatus(String userId, UserStatus status, String operation) {
        log.info("{}: {}", operation, userId);
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
        }
        
        // 更新状态
        User updateUser = User.builder()
                .status(status)
                .build();
        updateUser.setId(userId);
        
        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, operation + "失败");
        }
        
        // 记录操作日志
        recordUserOperation(userId, user.getUsername(), OperationType.UPDATE, 
                operation, OperationStatus.SUCCESS, null);
        
        log.info("{} successfully: {}", operation, userId);
    }
    
    /**
     * 验证角色是否存在
     */
    private void validateRolesExist(List<String> roleIds) {
        for (String roleId : roleIds) {
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在: " + roleId);
            }
        }
    }
    
    /**
     * 转换为用户响应对象
     */
    private UserResponse convertToUserResponse(User user) {
        if (user == null) {
            return null;
        }
        
        // 使用DTO类中的静态方法进行转换，避免重复代码
        return UserResponse.fromUser(user);
    }
    
    /**
     * 转换为用户个人资料响应对象
     */
    private UserProfileResponse convertToUserProfileResponse(User user) {
        if (user == null) {
            return null;
        }
        
        // 使用DTO类中的静态方法进行转换，避免重复代码
        return UserProfileResponse.fromUser(user);
    }
    
    /**
     * 记录用户操作日志
     */
    private void recordUserOperation(String userId, String username, OperationType operationType,
                                   String operationDesc, OperationStatus status, String errorMessage) {
        try {
            String clientIp = WebUtil.getClientIp();
            String userAgent = WebUtil.getUserAgent();
            
            operationLogService.recordOperation(userId, username, operationType, operationDesc,
                    status, clientIp, userAgent, null, errorMessage);
        } catch (Exception e) {
            log.error("Failed to record user operation log", e);
        }
    }
}