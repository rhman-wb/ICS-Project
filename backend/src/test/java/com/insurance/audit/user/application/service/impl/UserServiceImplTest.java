package com.insurance.audit.user.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.common.TestDataFactory;
import com.insurance.audit.common.TestSecurityUtils;
import com.insurance.audit.common.dto.PageResponse;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.user.domain.enums.UserStatus;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.mapper.RoleMapper;
import com.insurance.audit.user.infrastructure.mapper.UserMapper;
import com.insurance.audit.user.interfaces.dto.request.ChangePasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.CreateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.ResetPasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.UpdateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.UserProfileRequest;
import com.insurance.audit.user.interfaces.dto.request.UserQueryRequest;
import com.insurance.audit.user.interfaces.dto.response.UserProfileResponse;
import com.insurance.audit.user.interfaces.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务实现类测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务实现类测试")
class UserServiceImplTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private RoleMapper roleMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @BeforeEach
    void setUp() {
        TestSecurityUtils.clearSecurityContext();
    }
    
    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        CreateUserRequest request = TestDataFactory.createDefaultCreateUserRequest();
        User savedUser = TestDataFactory.createUser(request.getUsername());
        Role role = TestDataFactory.createDefaultRole();
        
        when(userMapper.existsByUsername(request.getUsername())).thenReturn(false);
        when(userMapper.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.existsByPhone(request.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(TestDataFactory.DEFAULT_ENCODED_PASSWORD);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(roleMapper.selectById(anyString())).thenReturn(role);
        when(userMapper.selectById(anyString())).thenReturn(savedUser);
        
        // When
        UserResponse response = userService.createUser(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(request.getUsername());
        assertThat(response.getRealName()).isEqualTo(request.getRealName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getPhone()).isEqualTo(request.getPhone());
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        
        verify(userMapper).existsByUsername(request.getUsername());
        verify(userMapper).existsByEmail(request.getEmail());
        verify(userMapper).existsByPhone(request.getPhone());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userMapper).insert(any(User.class));
        verify(userMapper).insertUserRoles(anyString(), eq(request.getRoleIds()));
    }
    
    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_UsernameExists() {
        // Given
        CreateUserRequest request = TestDataFactory.createDefaultCreateUserRequest();
        
        when(userMapper.existsByUsername(request.getUsername())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名已存在");
        
        verify(userMapper).existsByUsername(request.getUsername());
        verify(userMapper, never()).insert(any(User.class));
    }
    
    @Test
    @DisplayName("创建用户 - 邮箱已存在")
    void createUser_EmailExists() {
        // Given
        CreateUserRequest request = TestDataFactory.createDefaultCreateUserRequest();
        
        when(userMapper.existsByUsername(request.getUsername())).thenReturn(false);
        when(userMapper.existsByEmail(request.getEmail())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("邮箱已存在");
        
        verify(userMapper).existsByUsername(request.getUsername());
        verify(userMapper).existsByEmail(request.getEmail());
        verify(userMapper, never()).insert(any(User.class));
    }
    
    @Test
    @DisplayName("创建用户 - 手机号已存在")
    void createUser_PhoneExists() {
        // Given
        CreateUserRequest request = TestDataFactory.createDefaultCreateUserRequest();
        
        when(userMapper.existsByUsername(request.getUsername())).thenReturn(false);
        when(userMapper.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.existsByPhone(request.getPhone())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("手机号已存在");
        
        verify(userMapper).existsByUsername(request.getUsername());
        verify(userMapper).existsByEmail(request.getEmail());
        verify(userMapper).existsByPhone(request.getPhone());
        verify(userMapper, never()).insert(any(User.class));
    }
    
    @Test
    @DisplayName("根据ID获取用户 - 成功")
    void getUserById_Success() {
        // Given
        String userId = TestDataFactory.DEFAULT_USER_ID;
        User user = TestDataFactory.createDefaultUser();
        List<String> roleNames = Arrays.asList("USER", "TEST");
        
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.selectRoleNamesByUserId(userId)).thenReturn(roleNames);
        
        // When
        UserResponse response = userService.getUserById(userId);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getRoles()).isEqualTo(roleNames);
        
        verify(userMapper).selectById(userId);
        verify(userMapper).selectRoleNamesByUserId(userId);
    }
    
    @Test
    @DisplayName("根据ID获取用户 - 用户不存在")
    void getUserById_UserNotFound() {
        // Given
        String userId = "non-existent-id";
        
        when(userMapper.selectById(userId)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        
        verify(userMapper).selectById(userId);
    }
    
    @Test
    @DisplayName("更新用户 - 成功")
    void updateUser_Success() {
        // Given
        String userId = TestDataFactory.DEFAULT_USER_ID;
        UpdateUserRequest request = TestDataFactory.createDefaultUpdateUserRequest();
        User existingUser = TestDataFactory.createDefaultUser();
        User updatedUser = TestDataFactory.createDefaultUser();
        updatedUser.setRealName(request.getRealName());
        updatedUser.setEmail(request.getEmail());
        updatedUser.setPhone(request.getPhone());
        updatedUser.setStatus(request.getStatus());
        
        when(userMapper.selectById(userId)).thenReturn(existingUser);
        when(userMapper.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.existsByPhone(request.getPhone())).thenReturn(false);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(userMapper.selectById(userId)).thenReturn(updatedUser);
        when(userMapper.selectRoleNamesByUserId(userId)).thenReturn(Arrays.asList("USER"));
        
        // When
        UserResponse response = userService.updateUser(userId, request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRealName()).isEqualTo(request.getRealName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getPhone()).isEqualTo(request.getPhone());
        assertThat(response.getStatus()).isEqualTo(request.getStatus());
        
        verify(userMapper, times(2)).selectById(userId);
        verify(userMapper).updateById(any(User.class));
        verify(userMapper).deleteUserRoles(userId);
        verify(userMapper).insertUserRoles(userId, request.getRoleIds());
    }
    
    @Test
    @DisplayName("更新用户 - 用户不存在")
    void updateUser_UserNotFound() {
        // Given
        String userId = "non-existent-id";
        UpdateUserRequest request = TestDataFactory.createDefaultUpdateUserRequest();
        
        when(userMapper.selectById(userId)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        
        verify(userMapper).selectById(userId);
        verify(userMapper, never()).updateById(any(User.class));
    }
    
    @Test
    @DisplayName("删除用户 - 成功")
    void deleteUser_Success() {
        // Given
        String userId = TestDataFactory.DEFAULT_USER_ID;
        User user = TestDataFactory.createDefaultUser();
        
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.deleteById(userId)).thenReturn(1);
        
        // When
        userService.deleteUser(userId);
        
        // Then
        verify(userMapper).selectById(userId);
        verify(userMapper).deleteById(userId);
        verify(userMapper).deleteUserRoles(userId);
    }
    
    @Test
    @DisplayName("删除用户 - 用户不存在")
    void deleteUser_UserNotFound() {
        // Given
        String userId = "non-existent-id";
        
        when(userMapper.selectById(userId)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        
        verify(userMapper).selectById(userId);
        verify(userMapper, never()).deleteById(userId);
    }
    
    @Test
    @DisplayName("分页查询用户 - 成功")
    void getUserList_Success() {
        // Given
        UserQueryRequest request = TestDataFactory.createDefaultUserQueryRequest();
        List<User> users = TestDataFactory.createUserList(5);
        Page<User> page = new Page<>(request.getPage(), request.getSize());
        page.setRecords(users);
        page.setTotal(5);
        
        when(userMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(userMapper.selectRoleNamesByUserId(anyString())).thenReturn(Arrays.asList("USER"));
        
        // When
        Page<UserResponse> response = userService.getUserList(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRecords()).hasSize(5);
        assertThat(response.getTotal()).isEqualTo(5);
        assertThat(response.getCurrent()).isEqualTo(request.getCurrent());
        assertThat(response.getSize()).isEqualTo(request.getSize());
        
        verify(userMapper).selectPage(any(IPage.class), any(LambdaQueryWrapper.class));
        verify(userMapper, times(5)).selectRoleNamesByUserId(anyString());
    }
    
    @Test
    @DisplayName("获取用户资料 - 成功")
    void getUserProfile_Success() {
        // Given
        TestSecurityUtils.setDefaultUserSecurityContext();
        String userId = TestDataFactory.DEFAULT_USER_ID;
        User user = TestDataFactory.createDefaultUser();
        List<String> roleNames = Arrays.asList("USER", "TEST");
        List<String> permissions = Arrays.asList("test:view", "test:edit");
        
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.selectRoleNamesByUserId(userId)).thenReturn(roleNames);
        when(userMapper.selectPermissionsByUserId(userId)).thenReturn(permissions);
        
        // When
        UserProfileResponse response = userService.getCurrentUserProfile(userId);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getRoles()).isEqualTo(roleNames);
        assertThat(response.getPermissions()).isEqualTo(permissions);
        
        verify(userMapper).selectById(userId);
        verify(userMapper).selectRoleNamesByUserId(userId);
        verify(userMapper).selectPermissionsByUserId(userId);
        
        TestSecurityUtils.clearSecurityContext();
    }
    
    @Test
    @DisplayName("获取用户资料 - 未登录")
    void getUserProfile_NotAuthenticated() {
        // Given
        TestSecurityUtils.clearSecurityContext();
        
        // When & Then
        assertThatThrownBy(() -> userService.getCurrentUserProfile("invalid-user-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
    }
    
    @Test
    @DisplayName("更新用户资料 - 成功")
    void updateUserProfile_Success() {
        // Given
        TestSecurityUtils.setDefaultUserSecurityContext();
        String userId = TestDataFactory.DEFAULT_USER_ID;
        UserProfileRequest request = TestDataFactory.createDefaultUserProfileRequest();
        User existingUser = TestDataFactory.createDefaultUser();
        User updatedUser = TestDataFactory.createDefaultUser();
        updatedUser.setRealName(request.getRealName());
        updatedUser.setEmail(request.getEmail());
        updatedUser.setPhone(request.getPhone());
        updatedUser.setAvatar(request.getAvatar());
        
        when(userMapper.selectById(userId)).thenReturn(existingUser);
        when(userMapper.existsByEmailExcludeId(request.getEmail(), userId)).thenReturn(false);
        when(userMapper.existsByPhoneExcludeId(request.getPhone(), userId)).thenReturn(false);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(userMapper.selectById(userId)).thenReturn(updatedUser);
        when(userMapper.selectRoleNamesByUserId(userId)).thenReturn(Arrays.asList("USER"));
        when(userMapper.selectPermissionsByUserId(userId)).thenReturn(Arrays.asList("test:view"));
        
        // When
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRealName()).isEqualTo(request.getRealName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getPhone()).isEqualTo(request.getPhone());
        assertThat(response.getAvatar()).isEqualTo(request.getAvatar());
        
        verify(userMapper, times(2)).selectById(userId);
        verify(userMapper).updateById(any(User.class));
        
        TestSecurityUtils.clearSecurityContext();
    }
    
    @Test
    @DisplayName("修改密码 - 成功")
    void changePassword_Success() {
        // Given
        TestSecurityUtils.setDefaultUserSecurityContext();
        String userId = TestDataFactory.DEFAULT_USER_ID;
        ChangePasswordRequest request = TestDataFactory.createDefaultChangePasswordRequest();
        User user = TestDataFactory.createDefaultUser();
        
        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("new-encoded-password");
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        
        // When
        userService.changePassword(userId, request);
        
        // Then
        verify(userMapper).selectById(userId);
        verify(passwordEncoder).matches(request.getOldPassword(), user.getPassword());
        verify(passwordEncoder).encode(request.getNewPassword());
        verify(userMapper).updateById(any(User.class));
        
        TestSecurityUtils.clearSecurityContext();
    }
    
    @Test
    @DisplayName("修改密码 - 原密码错误")
    void changePassword_WrongOldPassword() {
        // Given
        TestSecurityUtils.setDefaultUserSecurityContext();
        String userId = TestDataFactory.DEFAULT_USER_ID;
        ChangePasswordRequest request = TestDataFactory.createDefaultChangePasswordRequest();
        User user = TestDataFactory.createDefaultUser();
        
        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("原密码错误");
        
        verify(userMapper).selectById(userId);
        verify(passwordEncoder).matches(request.getOldPassword(), user.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).updateById(any(User.class));
        
        TestSecurityUtils.clearSecurityContext();
    }
    
    @Test
    @DisplayName("重置密码 - 成功")
    void resetPassword_Success() {
        // Given
        ResetPasswordRequest request = TestDataFactory.createDefaultResetPasswordRequest();
        User user = TestDataFactory.createDefaultUser();
        
        when(userMapper.selectById(request.getUserId())).thenReturn(user);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("reset-encoded-password");
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        
        // When
        userService.resetPassword(request);
        
        // Then
        verify(userMapper).selectById(request.getUserId());
        verify(passwordEncoder).encode(request.getNewPassword());
        verify(userMapper).updateById(any(User.class));
    }
    
    @Test
    @DisplayName("重置密码 - 用户不存在")
    void resetPassword_UserNotFound() {
        // Given
        ResetPasswordRequest request = TestDataFactory.createDefaultResetPasswordRequest();
        
        when(userMapper.selectById(request.getUserId())).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        
        verify(userMapper).selectById(request.getUserId());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).updateById(any(User.class));
    }
    
    @Test
    @DisplayName("获取所有用户 - 成功")
    void getAllUsers_Success() {
        // Given
        List<User> users = TestDataFactory.createUserList(3);
        
        when(userMapper.selectAll()).thenReturn(users);
        when(userMapper.selectRoleNamesByUserId(anyString())).thenReturn(Arrays.asList("USER"));
        
        // When
        List<UserResponse> responses = userService.getAllUsers();
        
        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(3);
        
        verify(userMapper).selectAll();
        verify(userMapper, times(3)).selectRoleNamesByUserId(anyString());
    }
}