package com.insurance.audit.common;

import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.domain.model.Permission;
import com.insurance.audit.user.infrastructure.security.CustomUserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock数据服务
 * 用于测试环境提供模拟数据
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class MockDataService {
    
    private final Map<String, User> userStorage = new ConcurrentHashMap<>();
    private final Map<String, Role> roleStorage = new ConcurrentHashMap<>();
    private final Map<String, Permission> permissionStorage = new ConcurrentHashMap<>();
    private final Map<String, CustomUserDetails> userDetailsStorage = new ConcurrentHashMap<>();
    
    /**
     * 初始化Mock数据
     */
    public void initMockData() {
        // 初始化用户数据
        User defaultUser = TestDataFactory.createDefaultUser();
        userStorage.put(defaultUser.getId(), defaultUser);
        userStorage.put(defaultUser.getUsername(), defaultUser);
        
        User lockedUser = TestDataFactory.createLockedUser();
        userStorage.put(lockedUser.getId(), lockedUser);
        userStorage.put(lockedUser.getUsername(), lockedUser);
        
        User inactiveUser = TestDataFactory.createInactiveUser();
        userStorage.put(inactiveUser.getId(), inactiveUser);
        userStorage.put(inactiveUser.getUsername(), inactiveUser);
        
        // 初始化角色数据
        Role adminRole = TestDataFactory.createAdminRole();
        roleStorage.put(adminRole.getId(), adminRole);
        roleStorage.put(adminRole.getCode(), adminRole);
        
        Role userRole = TestDataFactory.createUserRole();
        roleStorage.put(userRole.getId(), userRole);
        roleStorage.put(userRole.getCode(), userRole);
        
        Role defaultRole = TestDataFactory.createDefaultRole();
        roleStorage.put(defaultRole.getId(), defaultRole);
        roleStorage.put(defaultRole.getCode(), defaultRole);
        
        // 初始化权限数据
        List<Permission> permissions = TestDataFactory.createPermissionList();
        for (Permission permission : permissions) {
            permissionStorage.put(permission.getId(), permission);
            permissionStorage.put(permission.getCode(), permission);
        }
        
        // 初始化用户详情数据
        CustomUserDetails defaultUserDetails = TestDataFactory.createDefaultCustomUserDetails();
        userDetailsStorage.put(defaultUserDetails.getUsername(), defaultUserDetails);
        
        CustomUserDetails adminUserDetails = TestDataFactory.createAdminUserDetails();
        userDetailsStorage.put(adminUserDetails.getUsername(), adminUserDetails);
    }
    
    /**
     * 清空Mock数据
     */
    public void clearMockData() {
        userStorage.clear();
        roleStorage.clear();
        permissionStorage.clear();
        userDetailsStorage.clear();
    }
    
    /**
     * 根据ID查找用户
     */
    public Optional<User> findUserById(String id) {
        return Optional.ofNullable(userStorage.get(id));
    }
    
    /**
     * 根据用户名查找用户
     */
    public Optional<User> findUserByUsername(String username) {
        return Optional.ofNullable(userStorage.get(username));
    }
    
    /**
     * 保存用户
     */
    public User saveUser(User user) {
        userStorage.put(user.getId(), user);
        userStorage.put(user.getUsername(), user);
        return user;
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(String id) {
        User user = userStorage.get(id);
        if (user != null) {
            userStorage.remove(id);
            userStorage.remove(user.getUsername());
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userStorage.containsKey(username);
    }
    
    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        return userStorage.values().stream()
                .anyMatch(user -> email.equals(user.getEmail()));
    }
    
    /**
     * 检查手机号是否存在
     */
    public boolean existsByPhone(String phone) {
        return userStorage.values().stream()
                .anyMatch(user -> phone.equals(user.getPhone()));
    }
    
    /**
     * 获取所有用户
     */
    public List<User> findAllUsers() {
        return userStorage.values().stream()
                .filter(value -> value instanceof User)
                .map(value -> (User) value)
                .toList();
    }
    
    /**
     * 根据ID查找角色
     */
    public Optional<Role> findRoleById(String id) {
        return Optional.ofNullable(roleStorage.get(id));
    }
    
    /**
     * 根据编码查找角色
     */
    public Optional<Role> findRoleByCode(String code) {
        return Optional.ofNullable(roleStorage.get(code));
    }
    
    /**
     * 保存角色
     */
    public Role saveRole(Role role) {
        roleStorage.put(role.getId(), role);
        roleStorage.put(role.getCode(), role);
        return role;
    }
    
    /**
     * 删除角色
     */
    public void deleteRole(String id) {
        Role role = roleStorage.get(id);
        if (role != null) {
            roleStorage.remove(id);
            roleStorage.remove(role.getCode());
        }
    }
    
    /**
     * 获取所有角色
     */
    public List<Role> findAllRoles() {
        return roleStorage.values().stream()
                .filter(value -> value instanceof Role)
                .map(value -> (Role) value)
                .toList();
    }
    
    /**
     * 根据ID查找权限
     */
    public Optional<Permission> findPermissionById(String id) {
        return Optional.ofNullable(permissionStorage.get(id));
    }
    
    /**
     * 根据编码查找权限
     */
    public Optional<Permission> findPermissionByCode(String code) {
        return Optional.ofNullable(permissionStorage.get(code));
    }
    
    /**
     * 保存权限
     */
    public Permission savePermission(Permission permission) {
        permissionStorage.put(permission.getId(), permission);
        permissionStorage.put(permission.getCode(), permission);
        return permission;
    }
    
    /**
     * 删除权限
     */
    public void deletePermission(String id) {
        Permission permission = permissionStorage.get(id);
        if (permission != null) {
            permissionStorage.remove(id);
            permissionStorage.remove(permission.getCode());
        }
    }
    
    /**
     * 获取所有权限
     */
    public List<Permission> findAllPermissions() {
        return permissionStorage.values().stream()
                .filter(value -> value instanceof Permission)
                .map(value -> (Permission) value)
                .toList();
    }
    
    /**
     * 根据用户名查找用户详情
     */
    public Optional<CustomUserDetails> findUserDetailsByUsername(String username) {
        return Optional.ofNullable(userDetailsStorage.get(username));
    }
    
    /**
     * 保存用户详情
     */
    public CustomUserDetails saveUserDetails(CustomUserDetails userDetails) {
        userDetailsStorage.put(userDetails.getUsername(), userDetails);
        return userDetails;
    }
    
    /**
     * 删除用户详情
     */
    public void deleteUserDetails(String username) {
        userDetailsStorage.remove(username);
    }
    
    /**
     * 模拟数据库查询延迟
     */
    public void simulateDelay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 获取存储统计信息
     */
    public Map<String, Integer> getStorageStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("users", (int) userStorage.values().stream().filter(v -> v instanceof User).count());
        stats.put("roles", (int) roleStorage.values().stream().filter(v -> v instanceof Role).count());
        stats.put("permissions", (int) permissionStorage.values().stream().filter(v -> v instanceof Permission).count());
        stats.put("userDetails", userDetailsStorage.size());
        return stats;
    }
    
    /**
     * 重置为默认数据
     */
    public void resetToDefaults() {
        clearMockData();
        initMockData();
    }
}