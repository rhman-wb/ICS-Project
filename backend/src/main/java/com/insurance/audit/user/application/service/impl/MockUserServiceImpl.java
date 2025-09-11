package com.insurance.audit.user.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.user.application.service.UserService;
import com.insurance.audit.user.domain.enums.UserStatus;
import com.insurance.audit.user.domain.model.Permission;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.interfaces.dto.request.ChangePasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.CreateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.ResetPasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.UpdateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.UserQueryRequest;
import com.insurance.audit.user.interfaces.dto.request.UserProfileRequest;
import com.insurance.audit.user.interfaces.dto.response.UserResponse;
import com.insurance.audit.user.interfaces.dto.response.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Mock用户服务实现类 - 用于debug环境
 * 
 * @author system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "false")
public class MockUserServiceImpl implements UserService {
    
    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Mock creating user: {}", request.getUsername());
        
        return UserResponse.builder()
            .id("mock-user-" + UUID.randomUUID().toString())
            .username(request.getUsername())
            .realName(request.getRealName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .status(UserStatus.ACTIVE)
            .enabled(true)
            .locked(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Override
    public UserResponse getUserById(String userId) {
        log.info("Mock getting user by id: {}", userId);
        
        return UserResponse.builder()
            .id(userId)
            .username("mock-user")
            .realName("Mock User")
            .email("mock@example.com")
            .phone("13800138000")
            .status(UserStatus.ACTIVE)
            .enabled(true)
            .locked(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Override
    public UserResponse getUserByUsername(String username) {
        log.info("Mock getting user by username: {}", username);
        
        return UserResponse.builder()
            .id("mock-user-id")
            .username(username)
            .realName("Mock User")
            .email("mock@example.com")
            .phone("13800138000")
            .status(UserStatus.ACTIVE)
            .enabled(true)
            .locked(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Override
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        log.info("Mock updating user: {}", userId);
        
        return UserResponse.builder()
            .id(userId)
            .username("mock-user")
            .realName(request.getRealName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .status(request.getStatus())
            .enabled(true)
            .locked(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Override
    public void deleteUser(String userId) {
        log.info("Mock deleting user: {}", userId);
        // Mock实现，不做实际操作
    }
    
    @Override
    public void batchDeleteUsers(List<String> userIds) {
        log.info("Mock batch deleting users: {}", userIds);
        // Mock实现，不做实际操作
    }
    
    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Mock getting all users");
        
        return Arrays.asList(
            UserResponse.builder()
                .id("mock-user-1")
                .username("mock-user-1")
                .realName("Mock User 1")
                .email("mock1@example.com")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .locked(false)
                .createdAt(LocalDateTime.now())
                .build(),
            UserResponse.builder()
                .id("mock-user-2")
                .username("mock-user-2")
                .realName("Mock User 2")
                .email("mock2@example.com")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .locked(false)
                .createdAt(LocalDateTime.now())
                .build()
        );
    }
    
    @Override
    public Page<UserResponse> getUserList(UserQueryRequest request) {
        log.info("Mock getting user list");
        
        Page<UserResponse> page = new Page<>();
        page.setCurrent(request.getCurrent());
        page.setSize(request.getSize());
        page.setTotal(2);
        page.setPages(1);
        page.setRecords(getAllUsers());
        
        return page;
    }
    
    @Override
    public void changePassword(String userId, ChangePasswordRequest request) {
        log.info("Mock changing password for user: {}", userId);
        // Mock实现，不做实际操作
    }
    
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Mock resetting password for: {}", request.getUsernameOrEmail());
        // Mock实现，不做实际操作
    }
    
    @Override
    public void adminResetPassword(String userId, String newPassword) {
        log.info("Mock admin resetting password for user: {}", userId);
        // Mock实现，不做实际操作
    }
    
    @Override
    public void assignRoles(String userId, List<String> roleIds) {
        log.info("Mock assigning roles to user: {}, roles: {}", userId, roleIds);
        // Mock实现，不做实际操作
    }
    
    @Override
    public void removeRoles(String userId, List<String> roleIds) {
        log.info("Mock removing roles from user: {}, roles: {}", userId, roleIds);
        // Mock实现，不做实际操作
    }
    
    @Override
    public List<Role> getUserRoles(String userId) {
        log.info("Mock getting user roles: {}", userId);
        
        Role mockRole = Role.builder()
            .id("mock-role-id")
            .name("USER")
            .code("USER")
            .description("Mock User Role")
            .createdAt(LocalDateTime.now())
            .build();
        
        return Arrays.asList(mockRole);
    }
    
    @Override
    public void setUserRoles(String userId, List<String> roleIds) {
        log.info("Mock setting roles for user: {}, roles: {}", userId, roleIds);
        // Mock实现，不做实际操作
    }
    
    @Override
    public boolean hasPermission(String userId, String permissionName) {
        log.debug("Mock checking permission: {} for user: {}", permissionName, userId);
        return true; // Mock实现总是返回true
    }
    
    @Override
    public boolean hasRole(String userId, String roleName) {
        log.debug("Mock checking role: {} for user: {}", roleName, userId);
        return true; // Mock实现总是返回true
    }
    
    @Override
    public List<Permission> getUserPermissions(String userId) {
        log.info("Mock getting user permissions: {}", userId);
        
        Permission mockPermission = Permission.builder()
            .id("mock-permission-id")
            .name("READ")
            .code("READ")
            .createdAt(LocalDateTime.now())
            .build();
        
        return Arrays.asList(mockPermission);
    }
    
    @Override
    public boolean hasAnyPermission(String userId, List<String> permissionNames) {
        log.debug("Mock checking any permissions: {} for user: {}", permissionNames, userId);
        return true; // Mock实现总是返回true
    }
    
    @Override
    public boolean hasAllPermissions(String userId, List<String> permissionNames) {
        log.debug("Mock checking all permissions: {} for user: {}", permissionNames, userId);
        return true; // Mock实现总是返回true
    }
    
    @Override
    public void enableUser(String userId) {
        log.info("Mock enabling user: {}", userId);
        // Mock实现，不做实际操作
    }
    
    @Override
    public void disableUser(String userId) {
        log.info("Mock disabling user: {}", userId);
        // Mock实现，不做实际操作
    }
    
    @Override
    public void lockUser(String userId, Integer lockMinutes) {
        log.info("Mock locking user: {} for {} minutes", userId, lockMinutes);
        // Mock实现，不做实际操作
    }
    
    @Override
    public void unlockUser(String userId) {
        log.info("Mock unlocking user: {}", userId);
        // Mock实现，不做实际操作
    }
    
    @Override
    public boolean existsByUsername(String username) {
        log.debug("Mock checking username exists: {}", username);
        return false; // Mock实现总是返回false
    }
    
    @Override
    public boolean existsByEmail(String email) {
        log.debug("Mock checking email exists: {}", email);
        return false; // Mock实现总是返回false
    }
    
    @Override
    public boolean validatePassword(String userId, String rawPassword) {
        log.debug("Mock validating password for user: {}", userId);
        return true; // Mock实现总是返回true
    }
    
    @Override
    public UserProfileResponse getCurrentUserProfile(String userId) {
        log.info("Mock getting current user profile: {}", userId);
        
        return UserProfileResponse.builder()
            .id(userId)
            .username("mock-user")
            .realName("Mock User")
            .email("mock@example.com")
            .phone("13800138000")
            .bio("This is a mock user profile")
            .status(UserStatus.ACTIVE)
            .lastLoginTime(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .roles(Collections.singletonList("USER"))
            .permissions(Arrays.asList("READ", "WRITE"))
            .build();
    }
    
    @Override
    public UserProfileResponse updateUserProfile(String userId, UserProfileRequest request) {
        log.info("Mock updating user profile: {}", userId);
        
        return UserProfileResponse.builder()
            .id(userId)
            .username("mock-user")
            .realName(request.getRealName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .bio(request.getBio())
            .status(UserStatus.ACTIVE)
            .lastLoginTime(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .roles(Collections.singletonList("USER"))
            .permissions(Arrays.asList("READ", "WRITE"))
            .build();
    }
}