package com.insurance.audit.user.infrastructure.security;

import com.insurance.audit.common.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 自定义用户详情测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@DisplayName("自定义用户详情测试")
class CustomUserDetailsTest {
    
    @Test
    @DisplayName("创建用户详情 - 成功")
    void createCustomUserDetails_Success() {
        // Given
        String userId = TestDataFactory.DEFAULT_USER_ID;
        String username = TestDataFactory.DEFAULT_USERNAME;
        String password = TestDataFactory.DEFAULT_ENCODED_PASSWORD;
        String realName = TestDataFactory.DEFAULT_REAL_NAME;
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_TEST")
        );
        List<String> permissions = Arrays.asList("test:view", "test:edit");
        
        // When
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(userId)
                .username(username)
                .password(password)
                .realName(realName)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(permissions)
                .build();
        
        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUserId()).isEqualTo(userId);
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(userDetails.getRealName()).isEqualTo(realName);
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.getAuthorities()).isEqualTo(authorities);
        assertThat(userDetails.getPermissions()).isEqualTo(permissions);
    }
    
    @Test
    @DisplayName("使用TestDataFactory创建默认用户详情 - 成功")
    void createDefaultCustomUserDetails_Success() {
        // When
        CustomUserDetails userDetails = TestDataFactory.createDefaultCustomUserDetails();
        
        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUserId()).isEqualTo(TestDataFactory.DEFAULT_USER_ID);
        assertThat(userDetails.getUsername()).isEqualTo(TestDataFactory.DEFAULT_USERNAME);
        assertThat(userDetails.getPassword()).isEqualTo(TestDataFactory.DEFAULT_ENCODED_PASSWORD);
        assertThat(userDetails.getRealName()).isEqualTo(TestDataFactory.DEFAULT_REAL_NAME);
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getPermissions()).hasSize(2);
    }
    
    @Test
    @DisplayName("使用TestDataFactory创建管理员用户详情 - 成功")
    void createAdminCustomUserDetails_Success() {
        // When
        CustomUserDetails userDetails = TestDataFactory.createAdminUserDetails();
        
        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUserId()).isEqualTo("admin-user-id");
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getRealName()).isEqualTo("管理员");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "ROLE_USER");
        assertThat(userDetails.getPermissions()).contains("*:*");
    }
    
    @Test
    @DisplayName("创建禁用用户详情 - 成功")
    void createDisabledCustomUserDetails_Success() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        // When
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(TestDataFactory.generateId())
                .username("disableduser")
                .password(TestDataFactory.DEFAULT_ENCODED_PASSWORD)
                .realName("禁用用户")
                .enabled(false)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("test:view"))
                .build();
        
        // Then
        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
    }
    
    @Test
    @DisplayName("创建账户过期用户详情 - 成功")
    void createExpiredAccountCustomUserDetails_Success() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        // When
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(TestDataFactory.generateId())
                .username("expireduser")
                .password(TestDataFactory.DEFAULT_ENCODED_PASSWORD)
                .realName("过期用户")
                .enabled(true)
                .accountNonExpired(false)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("test:view"))
                .build();
        
        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isFalse();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
    }
    
    @Test
    @DisplayName("创建凭据过期用户详情 - 成功")
    void createCredentialsExpiredCustomUserDetails_Success() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        // When
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(TestDataFactory.generateId())
                .username("credentialsexpireduser")
                .password(TestDataFactory.DEFAULT_ENCODED_PASSWORD)
                .realName("凭据过期用户")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(false)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("test:view"))
                .build();
        
        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isFalse();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
    }
    
    @Test
    @DisplayName("创建锁定用户详情 - 成功")
    void createLockedCustomUserDetails_Success() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        // When
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(TestDataFactory.generateId())
                .username("lockeduser")
                .password(TestDataFactory.DEFAULT_ENCODED_PASSWORD)
                .realName("锁定用户")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(false)
                .authorities(authorities)
                .permissions(Arrays.asList("test:view"))
                .build();
        
        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isFalse();
    }
    
    @Test
    @DisplayName("创建无权限用户详情 - 成功")
    void createNoPermissionsCustomUserDetails_Success() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        // When
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(TestDataFactory.generateId())
                .username("nopermissionsuser")
                .password(TestDataFactory.DEFAULT_ENCODED_PASSWORD)
                .realName("无权限用户")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(null)
                .build();
        
        // Then
        assertThat(userDetails.getPermissions()).isNull();
        assertThat(userDetails.getAuthorities()).hasSize(1);
    }
    
    @Test
    @DisplayName("创建空权限列表用户详情 - 成功")
    void createEmptyPermissionsCustomUserDetails_Success() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        // When
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(TestDataFactory.generateId())
                .username("emptypermissionsuser")
                .password(TestDataFactory.DEFAULT_ENCODED_PASSWORD)
                .realName("空权限用户")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList())
                .build();
        
        // Then
        assertThat(userDetails.getPermissions()).isNotNull();
        assertThat(userDetails.getPermissions()).isEmpty();
    }
    
    @Test
    @DisplayName("测试equals和hashCode - 相同对象")
    void testEqualsAndHashCode_SameObject() {
        // Given
        CustomUserDetails userDetails1 = TestDataFactory.createDefaultCustomUserDetails();
        CustomUserDetails userDetails2 = TestDataFactory.createDefaultCustomUserDetails();
        
        // When & Then
        assertThat(userDetails1).isEqualTo(userDetails2);
        assertThat(userDetails1.hashCode()).isEqualTo(userDetails2.hashCode());
    }
    
    @Test
    @DisplayName("测试equals和hashCode - 不同对象")
    void testEqualsAndHashCode_DifferentObject() {
        // Given
        CustomUserDetails userDetails1 = TestDataFactory.createDefaultCustomUserDetails();
        CustomUserDetails userDetails2 = TestDataFactory.createAdminUserDetails();
        
        // When & Then
        assertThat(userDetails1).isNotEqualTo(userDetails2);
        assertThat(userDetails1.hashCode()).isNotEqualTo(userDetails2.hashCode());
    }
    
    @Test
    @DisplayName("测试toString方法 - 成功")
    void testToString_Success() {
        // Given
        CustomUserDetails userDetails = TestDataFactory.createDefaultCustomUserDetails();
        
        // When
        String toString = userDetails.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("CustomUserDetails");
        assertThat(toString).contains(TestDataFactory.DEFAULT_USERNAME);
        assertThat(toString).contains(TestDataFactory.DEFAULT_USER_ID);
    }
}