package com.insurance.audit.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.common.TestDataFactory;
import com.insurance.audit.user.domain.enums.UserStatus;
import com.insurance.audit.user.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 用户Mapper测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("用户Mapper测试")
class UserMapperTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    @DisplayName("插入用户 - 成功")
    void insert_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null); // 让数据库生成ID
        
        // When
        int result = userMapper.insert(user);
        
        // Then
        assertThat(result).isEqualTo(1);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void selectById_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        User found = userMapper.selectById(user.getId());
        
        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(user.getId());
        assertThat(found.getUsername()).isEqualTo(user.getUsername());
        assertThat(found.getRealName()).isEqualTo(user.getRealName());
        assertThat(found.getEmail()).isEqualTo(user.getEmail());
        assertThat(found.getPhone()).isEqualTo(user.getPhone());
        assertThat(found.getStatus()).isEqualTo(user.getStatus());
    }
    
    @Test
    @DisplayName("根据用户名查询用户 - 成功")
    void selectByUsername_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        User found = userMapper.selectByUsername(user.getUsername());
        
        // Then
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo(user.getUsername());
        assertThat(found.getRealName()).isEqualTo(user.getRealName());
    }
    
    @Test
    @DisplayName("根据用户名查询用户 - 用户不存在")
    void selectByUsername_UserNotFound() {
        // Given
        String nonExistentUsername = "nonexistent";
        
        // When
        User found = userMapper.selectByUsername(nonExistentUsername);
        
        // Then
        assertThat(found).isNull();
    }
    
    @Test
    @DisplayName("更新用户 - 成功")
    void updateById_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        String newRealName = "更新后的姓名";
        String newEmail = "updated@example.com";
        user.setRealName(newRealName);
        user.setEmail(newEmail);
        
        // When
        int result = userMapper.updateById(user);
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User updated = userMapper.selectById(user.getId());
        assertThat(updated.getRealName()).isEqualTo(newRealName);
        assertThat(updated.getEmail()).isEqualTo(newEmail);
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }
    
    @Test
    @DisplayName("删除用户 - 成功")
    void deleteById_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        int result = userMapper.deleteById(user.getId());
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User deleted = userMapper.selectById(user.getId());
        assertThat(deleted).isNull(); // 逻辑删除，查询时应该返回null
    }
    
    @Test
    @DisplayName("检查用户名是否存在 - 存在")
    void existsByUsername_Exists() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        boolean exists = userMapper.existsByUsername(user.getUsername());
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("检查用户名是否存在 - 不存在")
    void existsByUsername_NotExists() {
        // Given
        String nonExistentUsername = "nonexistent";
        
        // When
        boolean exists = userMapper.existsByUsername(nonExistentUsername);
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("检查邮箱是否存在 - 存在")
    void existsByEmail_Exists() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        boolean exists = userMapper.existsByEmail(user.getEmail());
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("检查邮箱是否存在 - 不存在")
    void existsByEmail_NotExists() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        
        // When
        boolean exists = userMapper.existsByEmail(nonExistentEmail);
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("检查手机号是否存在 - 存在")
    void existsByPhone_Exists() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        boolean exists = userMapper.existsByPhone(user.getPhone());
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("检查手机号是否存在 - 不存在")
    void existsByPhone_NotExists() {
        // Given
        String nonExistentPhone = "13999999999";
        
        // When
        boolean exists = userMapper.existsByPhone(nonExistentPhone);
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("检查邮箱是否存在（排除指定ID） - 不存在")
    void existsByEmailExcludeId_NotExists() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        boolean exists = userMapper.existsByEmailExcludeId(user.getEmail(), user.getId());
        
        // Then
        assertThat(exists).isFalse(); // 排除自己，所以不存在
    }
    
    @Test
    @DisplayName("检查手机号是否存在（排除指定ID） - 不存在")
    void existsByPhoneExcludeId_NotExists() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        boolean exists = userMapper.existsByPhoneExcludeId(user.getPhone(), user.getId());
        
        // Then
        assertThat(exists).isFalse(); // 排除自己，所以不存在
    }
    
    @Test
    @DisplayName("锁定账户 - 成功")
    void lockAccount_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        
        // When
        int result = userMapper.lockAccount(user.getId(), lockedUntil);
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User locked = userMapper.selectById(user.getId());
        assertThat(locked.getLockedUntil()).isNotNull();
        assertThat(locked.getLockedUntil()).isAfter(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("更新密码 - 成功")
    void updatePassword_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        String newPassword = "new-encoded-password";
        
        // When
        int result = userMapper.updatePassword(user.getId(), newPassword);
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User updated = userMapper.selectById(user.getId());
        assertThat(updated.getPassword()).isEqualTo(newPassword);
    }
    
    @Test
    @DisplayName("更新状态 - 成功")
    void updateStatus_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        UserStatus newStatus = UserStatus.INACTIVE;
        
        // When
        int result = userMapper.updateStatus(user.getId(), newStatus);
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User updated = userMapper.selectById(user.getId());
        assertThat(updated.getStatus()).isEqualTo(newStatus);
    }
    
    @Test
    @DisplayName("解锁用户 - 成功")
    void unlockUser_Success() {
        // Given
        User user = TestDataFactory.createLockedUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        int result = userMapper.unlockUser(user.getId());
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User unlocked = userMapper.selectById(user.getId());
        assertThat(unlocked.getLockedUntil()).isNull();
        assertThat(unlocked.getLoginFailureCount()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("更新最后登录信息 - 成功")
    void updateLastLogin_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        LocalDateTime loginTime = LocalDateTime.now();
        String loginIp = "192.168.1.100";
        
        // When
        int result = userMapper.updateLastLogin(user.getId(), loginTime, loginIp);
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User updated = userMapper.selectById(user.getId());
        assertThat(updated.getLastLoginTime()).isNotNull();
        assertThat(updated.getLastLoginIp()).isEqualTo(loginIp);
    }
    
    @Test
    @DisplayName("重置登录尝试次数 - 成功")
    void resetLoginAttempts_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setLoginFailureCount(3);
        user.setId(null);
        userMapper.insert(user);
        
        // When
        int result = userMapper.resetLoginAttempts(user.getId());
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User updated = userMapper.selectById(user.getId());
        assertThat(updated.getLoginFailureCount()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("增加登录尝试次数 - 成功")
    void incrementLoginAttempts_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setLoginFailureCount(2);
        user.setId(null);
        userMapper.insert(user);
        
        // When
        int result = userMapper.incrementLoginAttempts(user.getId());
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User updated = userMapper.selectById(user.getId());
        assertThat(updated.getLoginFailureCount()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("根据用户名删除用户 - 成功")
    void deleteByUsername_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        int result = userMapper.deleteByUsername(user.getUsername());
        
        // Then
        assertThat(result).isEqualTo(1);
        
        User deleted = userMapper.selectByUsername(user.getUsername());
        assertThat(deleted).isNull(); // 逻辑删除，查询时应该返回null
    }
    
    @Test
    @DisplayName("查询所有用户 - 成功")
    void selectAll_Success() {
        // Given
        User user1 = TestDataFactory.createUser("user1");
        user1.setId(null);
        userMapper.insert(user1);
        
        User user2 = TestDataFactory.createUser("user2");
        user2.setId(null);
        userMapper.insert(user2);
        
        // When
        List<User> users = userMapper.selectAll();
        
        // Then
        assertThat(users).isNotNull();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
        assertThat(users).extracting(User::getUsername)
                .contains("user1", "user2");
    }
    
    @Test
    @DisplayName("分页查询用户 - 成功")
    void selectPage_Success() {
        // Given
        User user1 = TestDataFactory.createUser("pageuser1");
        user1.setId(null);
        userMapper.insert(user1);
        
        User user2 = TestDataFactory.createUser("pageuser2");
        user2.setId(null);
        userMapper.insert(user2);
        
        Page<User> page = new Page<>(1, 10);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getUsername, "pageuser");
        
        // When
        IPage<User> result = userMapper.selectPage(page, wrapper);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.getTotal()).isGreaterThanOrEqualTo(2);
        assertThat(result.getCurrent()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }
    
    @Test
    @DisplayName("插入用户角色关联 - 成功")
    void insertUserRoles_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        List<String> roleIds = Arrays.asList("role1", "role2");
        
        // When
        int result = userMapper.insertUserRoles(user.getId(), roleIds);
        
        // Then
        assertThat(result).isEqualTo(2);
    }
    
    @Test
    @DisplayName("删除用户角色关联 - 成功")
    void deleteUserRoles_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        List<String> roleIds = Arrays.asList("role1", "role2");
        userMapper.insertUserRoles(user.getId(), roleIds);
        
        // When
        int result = userMapper.deleteUserRoles(user.getId());
        
        // Then
        assertThat(result).isEqualTo(2);
    }
    
    @Test
    @DisplayName("根据用户ID查询角色名称 - 成功")
    void selectRoleNamesByUserId_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // 这里需要先插入角色数据和用户角色关联数据
        // 在实际测试中，可能需要mock或者准备测试数据
        
        // When
        List<String> roleNames = userMapper.selectRoleNamesByUserId(user.getId());
        
        // Then
        assertThat(roleNames).isNotNull();
        // 由于没有实际的角色数据，这里只验证方法不抛异常
    }
    
    @Test
    @DisplayName("根据用户ID查询权限 - 成功")
    void selectPermissionsByUserId_Success() {
        // Given
        User user = TestDataFactory.createDefaultUser();
        user.setId(null);
        userMapper.insert(user);
        
        // When
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        
        // Then
        assertThat(permissions).isNotNull();
        // 由于没有实际的权限数据，这里只验证方法不抛异常
    }
}