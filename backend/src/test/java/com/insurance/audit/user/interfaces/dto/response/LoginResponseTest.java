package com.insurance.audit.user.interfaces.dto.response;

import com.insurance.audit.common.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 登录响应DTO测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@DisplayName("登录响应DTO测试")
class LoginResponseTest {
    
    @Test
    @DisplayName("创建登录响应 - 成功")
    void createLoginResponse_Success() {
        // Given
        String accessToken = TestDataFactory.DEFAULT_JWT_TOKEN;
        String refreshToken = TestDataFactory.DEFAULT_REFRESH_TOKEN;
        String tokenType = "Bearer";
        Long expiresIn = 86400L;
        String userId = TestDataFactory.DEFAULT_USER_ID;
        String username = TestDataFactory.DEFAULT_USERNAME;
        String realName = TestDataFactory.DEFAULT_REAL_NAME;
        List<String> roles = Arrays.asList("USER", "TEST");
        List<String> permissions = Arrays.asList("test:view", "test:edit");
        
        // When
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .userId(userId)
                .username(username)
                .realName(realName)
                .roles(roles)
                .permissions(permissions)
                .build();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenType()).isEqualTo(tokenType);
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getRealName()).isEqualTo(realName);
        assertThat(response.getRoles()).isEqualTo(roles);
        assertThat(response.getPermissions()).isEqualTo(permissions);
    }
    
    @Test
    @DisplayName("使用TestDataFactory创建默认登录响应 - 成功")
    void createDefaultLoginResponse_Success() {
        // When
        LoginResponse response = TestDataFactory.createDefaultLoginResponse();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TestDataFactory.DEFAULT_JWT_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(TestDataFactory.DEFAULT_REFRESH_TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUserId()).isEqualTo(TestDataFactory.DEFAULT_USER_ID);
        assertThat(response.getUsername()).isEqualTo(TestDataFactory.DEFAULT_USERNAME);
        assertThat(response.getRealName()).isEqualTo(TestDataFactory.DEFAULT_REAL_NAME);
        assertThat(response.getRoles()).hasSize(2);
        assertThat(response.getPermissions()).hasSize(2);
    }
    
    @Test
    @DisplayName("创建最小登录响应 - 成功")
    void createMinimalLoginResponse_Success() {
        // Given
        String accessToken = TestDataFactory.DEFAULT_JWT_TOKEN;
        String userId = TestDataFactory.DEFAULT_USER_ID;
        String username = TestDataFactory.DEFAULT_USERNAME;
        
        // When
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .userId(userId)
                .username(username)
                .build();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getRefreshToken()).isNull();
        assertThat(response.getTokenType()).isNull();
        assertThat(response.getExpiresIn()).isNull();
        assertThat(response.getRealName()).isNull();
        assertThat(response.getRoles()).isNull();
        assertThat(response.getPermissions()).isNull();
    }
    
    @Test
    @DisplayName("创建无角色权限的登录响应 - 成功")
    void createLoginResponseWithoutRolesAndPermissions_Success() {
        // Given
        String accessToken = TestDataFactory.DEFAULT_JWT_TOKEN;
        String userId = TestDataFactory.DEFAULT_USER_ID;
        String username = TestDataFactory.DEFAULT_USERNAME;
        
        // When
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .userId(userId)
                .username(username)
                .roles(Arrays.asList())
                .permissions(Arrays.asList())
                .build();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRoles()).isNotNull();
        assertThat(response.getRoles()).isEmpty();
        assertThat(response.getPermissions()).isNotNull();
        assertThat(response.getPermissions()).isEmpty();
    }
    
    @Test
    @DisplayName("创建管理员登录响应 - 成功")
    void createAdminLoginResponse_Success() {
        // Given
        String accessToken = TestDataFactory.DEFAULT_JWT_TOKEN;
        String refreshToken = TestDataFactory.DEFAULT_REFRESH_TOKEN;
        String userId = "admin-user-id";
        String username = "admin";
        String realName = "管理员";
        List<String> roles = Arrays.asList("ADMIN", "USER");
        List<String> permissions = Arrays.asList("*:*");
        
        // When
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(userId)
                .username(username)
                .realName(realName)
                .roles(roles)
                .permissions(permissions)
                .build();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getRealName()).isEqualTo(realName);
        assertThat(response.getRoles()).contains("ADMIN", "USER");
        assertThat(response.getPermissions()).contains("*:*");
    }
    
    @Test
    @DisplayName("测试equals和hashCode - 相同对象")
    void testEqualsAndHashCode_SameObject() {
        // Given
        LoginResponse response1 = TestDataFactory.createDefaultLoginResponse();
        LoginResponse response2 = TestDataFactory.createDefaultLoginResponse();
        
        // When & Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }
    
    @Test
    @DisplayName("测试equals和hashCode - 不同对象")
    void testEqualsAndHashCode_DifferentObject() {
        // Given
        LoginResponse response1 = TestDataFactory.createDefaultLoginResponse();
        LoginResponse response2 = LoginResponse.builder()
                .accessToken("different-token")
                .userId("different-user-id")
                .username("different-username")
                .build();
        
        // When & Then
        assertThat(response1).isNotEqualTo(response2);
        assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
    }
    
    @Test
    @DisplayName("测试toString方法 - 成功")
    void testToString_Success() {
        // Given
        LoginResponse response = TestDataFactory.createDefaultLoginResponse();
        
        // When
        String toString = response.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("LoginResponse");
        assertThat(toString).contains(TestDataFactory.DEFAULT_USERNAME);
        assertThat(toString).contains(TestDataFactory.DEFAULT_USER_ID);
    }
    
    @Test
    @DisplayName("测试Builder模式 - 成功")
    void testBuilderPattern_Success() {
        // When
        LoginResponse response = LoginResponse.builder()
                .accessToken(TestDataFactory.DEFAULT_JWT_TOKEN)
                .refreshToken(TestDataFactory.DEFAULT_REFRESH_TOKEN)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(TestDataFactory.DEFAULT_USER_ID)
                .username(TestDataFactory.DEFAULT_USERNAME)
                .realName(TestDataFactory.DEFAULT_REAL_NAME)
                .roles(Arrays.asList("USER"))
                .permissions(Arrays.asList("test:view"))
                .build();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getUsername()).isNotNull();
        assertThat(response.getRealName()).isNotNull();
        assertThat(response.getRoles()).hasSize(1);
        assertThat(response.getPermissions()).hasSize(1);
    }
    
    @Test
    @DisplayName("测试无参构造函数 - 成功")
    void testNoArgsConstructor_Success() {
        // When
        LoginResponse response = new LoginResponse();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRefreshToken()).isNull();
        assertThat(response.getTokenType()).isNull();
        assertThat(response.getExpiresIn()).isNull();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getRealName()).isNull();
        assertThat(response.getRoles()).isNull();
        assertThat(response.getPermissions()).isNull();
    }
    
    @Test
    @DisplayName("测试全参构造函数 - 成功")
    void testAllArgsConstructor_Success() {
        // Given
        String accessToken = TestDataFactory.DEFAULT_JWT_TOKEN;
        String refreshToken = TestDataFactory.DEFAULT_REFRESH_TOKEN;
        String tokenType = "Bearer";
        Long expiresIn = 86400L;
        String userId = TestDataFactory.DEFAULT_USER_ID;
        String username = TestDataFactory.DEFAULT_USERNAME;
        String realName = TestDataFactory.DEFAULT_REAL_NAME;
        List<String> roles = Arrays.asList("USER");
        List<String> permissions = Arrays.asList("test:view");
        
        // When
        LoginResponse response = new LoginResponse(
                accessToken, refreshToken, tokenType, expiresIn,
                userId, username, realName, roles, permissions, LocalDateTime.now()
        );
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenType()).isEqualTo(tokenType);
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getRealName()).isEqualTo(realName);
        assertThat(response.getRoles()).isEqualTo(roles);
        assertThat(response.getPermissions()).isEqualTo(permissions);
    }
}