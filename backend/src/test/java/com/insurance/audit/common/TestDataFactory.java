package com.insurance.audit.common;

import com.insurance.audit.user.domain.enums.PermissionStatus;
import com.insurance.audit.user.domain.enums.PermissionType;
import com.insurance.audit.user.domain.enums.RoleStatus;
import com.insurance.audit.user.domain.enums.UserStatus;
import com.insurance.audit.user.domain.model.Permission;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.security.CustomUserDetails;
import com.insurance.audit.user.interfaces.dto.request.ChangePasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.CreateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.LoginRequest;
import com.insurance.audit.user.interfaces.dto.request.RefreshTokenRequest;
import com.insurance.audit.user.interfaces.dto.request.ResetPasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.UpdateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.UserProfileRequest;
import com.insurance.audit.user.interfaces.dto.request.UserQueryRequest;
import com.insurance.audit.user.interfaces.dto.response.LoginResponse;
import com.insurance.audit.user.interfaces.dto.response.UserProfileResponse;
import com.insurance.audit.user.interfaces.dto.response.UserResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 测试数据工厂
 * 提供统一的测试数据创建方法
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
public class TestDataFactory {
    
    // 常量定义
    public static final String DEFAULT_USER_ID = "test-user-id-001";
    public static final String DEFAULT_USERNAME = "testuser";
    public static final String DEFAULT_PASSWORD = "password123";
    public static final String DEFAULT_ENCODED_PASSWORD = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjxSOpE6.jVgKlW";
    public static final String DEFAULT_REAL_NAME = "测试用户";
    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_PHONE = "13800138000";
    public static final String DEFAULT_AVATAR = "https://example.com/avatar.jpg";
    public static final String DEFAULT_CLIENT_IP = "192.168.1.100";
    
    public static final String DEFAULT_ROLE_ID = "test-role-id-001";
    public static final String DEFAULT_ROLE_NAME = "测试角色";
    public static final String DEFAULT_ROLE_CODE = "TEST_ROLE";
    
    public static final String DEFAULT_PERMISSION_ID = "test-permission-id-001";
    public static final String DEFAULT_PERMISSION_NAME = "测试权限";
    public static final String DEFAULT_PERMISSION_CODE = "test:view";
    
    public static final String DEFAULT_JWT_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwNDY3MjAwMCwiZXhwIjoxNzA0NzU4NDAwfQ.test";
    public static final String DEFAULT_REFRESH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInR5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNzA0NjcyMDAwLCJleHAiOjE3MDUyNzY4MDB9.test";
    
    /**
     * 创建默认用户
     */
    public static User createDefaultUser() {
        return User.builder()
                .id(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName(DEFAULT_REAL_NAME)
                .email(DEFAULT_EMAIL)
                .phone(DEFAULT_PHONE)
                .avatar(DEFAULT_AVATAR)
                .status(UserStatus.ACTIVE)
                .loginFailureCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建用户（指定用户名）
     */
    public static User createUser(String username) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("用户-" + username)
                .email(username + "@example.com")
                .phone("138" + String.format("%08d", username.hashCode() % 100000000))
                .status(UserStatus.ACTIVE)
                .loginFailureCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建锁定的用户
     */
    public static User createLockedUser() {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username("lockeduser")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("锁定用户")
                .email("locked@example.com")
                .phone("13800138001")
                .status(UserStatus.LOCKED)
                .loginFailureCount(5)
                .lockedUntil(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建停用的用户
     */
    public static User createInactiveUser() {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username("inactiveuser")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("停用用户")
                .email("inactive@example.com")
                .phone("13800138002")
                .status(UserStatus.INACTIVE)
                .loginFailureCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建默认角色
     */
    public static Role createDefaultRole() {
        return Role.builder()
                .id(DEFAULT_ROLE_ID)
                .name(DEFAULT_ROLE_NAME)
                .code(DEFAULT_ROLE_CODE)
                .description("测试角色描述")
                .status(RoleStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建管理员角色
     */
    public static Role createAdminRole() {
        return Role.builder()
                .id(UUID.randomUUID().toString())
                .name("管理员")
                .code("ADMIN")
                .description("系统管理员")
                .status(RoleStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建普通用户角色
     */
    public static Role createUserRole() {
        return Role.builder()
                .id(UUID.randomUUID().toString())
                .name("普通用户")
                .code("USER")
                .description("普通用户")
                .status(RoleStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建默认权限
     */
    public static Permission createDefaultPermission() {
        return Permission.builder()
                .id(DEFAULT_PERMISSION_ID)
                .name(DEFAULT_PERMISSION_NAME)
                .code(DEFAULT_PERMISSION_CODE)
                .type(PermissionType.MENU)
                .path("/test")
                .component("TestComponent")
                .icon("test")
                .sortOrder(1)
                .status(PermissionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建菜单权限
     */
    public static Permission createMenuPermission(String name, String code, String path) {
        return Permission.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .code(code)
                .type(PermissionType.MENU)
                .path(path)
                .component(name + "Component")
                .icon(code.toLowerCase())
                .sortOrder(1)
                .status(PermissionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建按钮权限
     */
    public static Permission createButtonPermission(String name, String code) {
        return Permission.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .code(code)
                .type(PermissionType.BUTTON)
                .sortOrder(1)
                .status(PermissionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建API权限
     */
    public static Permission createApiPermission(String name, String code, String path) {
        return Permission.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .code(code)
                .type(PermissionType.API)
                .path(path)
                .sortOrder(1)
                .status(PermissionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建自定义用户详情
     */
    public static CustomUserDetails createDefaultCustomUserDetails() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_TEST")
        );
        
        return CustomUserDetails.builder()
                .userId(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName(DEFAULT_REAL_NAME)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("test:view", "test:edit"))
                .build();
    }
    
    /**
     * 创建管理员用户详情
     */
    public static CustomUserDetails createAdminUserDetails() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        return CustomUserDetails.builder()
                .userId("admin-user-id")
                .username("admin")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("管理员")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("*:*"))
                .build();
    }
    
    /**
     * 创建登录请求
     */
    public static LoginRequest createDefaultLoginRequest() {
        return LoginRequest.builder()
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_PASSWORD)
                .rememberMe(false)
                .build();
    }
    
    /**
     * 创建登录请求（指定用户名和密码）
     */
    public static LoginRequest createLoginRequest(String username, String password) {
        return LoginRequest.builder()
                .username(username)
                .password(password)
                .rememberMe(false)
                .build();
    }
    
    /**
     * 创建登录响应
     */
    public static LoginResponse createDefaultLoginResponse() {
        return LoginResponse.builder()
                .accessToken(DEFAULT_JWT_TOKEN)
                .refreshToken(DEFAULT_REFRESH_TOKEN)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .realName(DEFAULT_REAL_NAME)
                .roles(Arrays.asList("USER", "TEST"))
                .permissions(Arrays.asList("test:view", "test:edit"))
                .build();
    }
    
    /**
     * 创建刷新令牌请求
     */
    public static RefreshTokenRequest createDefaultRefreshTokenRequest() {
        return RefreshTokenRequest.builder()
                .refreshToken(DEFAULT_REFRESH_TOKEN)
                .build();
    }
    
    /**
     * 创建用户创建请求
     */
    public static CreateUserRequest createDefaultCreateUserRequest() {
        return CreateUserRequest.builder()
                .username("newuser")
                .password("newpassword123")
                .realName("新用户")
                .email("newuser@example.com")
                .phone("13800138003")
                .roleIds(Arrays.asList(DEFAULT_ROLE_ID))
                .build();
    }
    
    /**
     * 创建用户更新请求
     */
    public static UpdateUserRequest createDefaultUpdateUserRequest() {
        return UpdateUserRequest.builder()
                .realName("更新后的用户")
                .email("updated@example.com")
                .phone("13800138004")
                .status(UserStatus.ACTIVE)
                .roleIds(Arrays.asList(DEFAULT_ROLE_ID))
                .build();
    }
    
    /**
     * 创建用户资料请求
     */
    public static UserProfileRequest createDefaultUserProfileRequest() {
        return UserProfileRequest.builder()
                .realName("更新资料")
                .email("profile@example.com")
                .phone("13800138005")
                .avatar("https://example.com/new-avatar.jpg")
                .build();
    }
    
    /**
     * 创建修改密码请求
     */
    public static ChangePasswordRequest createDefaultChangePasswordRequest() {
        return ChangePasswordRequest.builder()
                .currentPassword(DEFAULT_PASSWORD)
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();
    }
    
    /**
     * 创建重置密码请求
     */
    public static ResetPasswordRequest createDefaultResetPasswordRequest() {
        return ResetPasswordRequest.builder()
                .userId(DEFAULT_USER_ID)
                .newPassword("resetpassword123")
                .confirmPassword("resetpassword123")
                .build();
    }
    
    /**
     * 创建用户查询请求
     */
    public static UserQueryRequest createDefaultUserQueryRequest() {
        return UserQueryRequest.builder()
                .username("test")
                .realName("测试")
                .email("test")
                .status(UserStatus.ACTIVE)
                .page(1)
                .size(10)
                .build();
    }
    
    /**
     * 创建用户响应
     */
    public static UserResponse createDefaultUserResponse() {
        return UserResponse.builder()
                .id(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .realName(DEFAULT_REAL_NAME)
                .email(DEFAULT_EMAIL)
                .phone(DEFAULT_PHONE)
                .avatar(DEFAULT_AVATAR)
                .status(UserStatus.ACTIVE)
                .lastLoginTime(LocalDateTime.now().minusHours(1))
                .lastLoginIp(DEFAULT_CLIENT_IP)
                .roles(Arrays.asList(
                    UserResponse.RoleInfo.builder()
                        .id("role1")
                        .name("用户")
                        .code("USER")
                        .description("普通用户")
                        .build(),
                    UserResponse.RoleInfo.builder()
                        .id("role2")
                        .name("测试")
                        .code("TEST")
                        .description("测试角色")
                        .build()
                ))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建用户资料响应
     */
    public static UserProfileResponse createDefaultUserProfileResponse() {
        return UserProfileResponse.builder()
                .id(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .realName(DEFAULT_REAL_NAME)
                .email(DEFAULT_EMAIL)
                .phone(DEFAULT_PHONE)
                .avatar(DEFAULT_AVATAR)
                .status(UserStatus.ACTIVE)
                .lastLoginTime(LocalDateTime.now().minusHours(1))
                .lastLoginIp(DEFAULT_CLIENT_IP)
                .roles(Arrays.asList("USER", "TEST"))
                .permissions(Arrays.asList("test:view", "test:edit"))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建用户列表
     */
    public static List<User> createUserList(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(createUser("user" + i));
        }
        return users;
    }
    
    /**
     * 创建角色列表
     */
    public static List<Role> createRoleList() {
        return Arrays.asList(
                createAdminRole(),
                createUserRole(),
                createDefaultRole()
        );
    }
    
    /**
     * 创建权限列表
     */
    public static List<Permission> createPermissionList() {
        return Arrays.asList(
                createMenuPermission("用户管理", "user:view", "/user"),
                createButtonPermission("用户新增", "user:create"),
                createButtonPermission("用户编辑", "user:edit"),
                createButtonPermission("用户删除", "user:delete"),
                createApiPermission("用户API", "user:api", "/api/users/*")
        );
    }
    
    /**
     * 生成随机ID
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 生成随机用户名
     */
    public static String generateUsername() {
        return "user" + System.currentTimeMillis();
    }
    
    /**
     * 生成随机邮箱
     */
    public static String generateEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }
    
    /**
     * 生成随机手机号
     */
    public static String generatePhone() {
        return "138" + String.format("%08d", (int) (Math.random() * 100000000));
    }
    
    /**
     * 创建测试JWT令牌
     */
    public static String createTestJwtToken(String username) {
        // 返回一个模拟的JWT令牌，实际测试中可能需要真实的JWT工具类
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIiICsgdXNlcm5hbWUgKyAiIiwiaWF0IjoxNzA0NjcyMDAwLCJleHAiOjE3MDQ3NTg0MDB9.test-" + username;
    }
}