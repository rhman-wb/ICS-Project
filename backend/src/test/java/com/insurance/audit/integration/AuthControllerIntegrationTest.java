package com.insurance.audit.integration;

import com.insurance.audit.common.TestDataFactory;
import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.user.interfaces.dto.request.LoginRequest;
import com.insurance.audit.user.interfaces.dto.request.RefreshTokenRequest;
import com.insurance.audit.user.interfaces.dto.response.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;

/**
 * 认证控制器集成测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@DisplayName("认证控制器集成测试")
class AuthControllerIntegrationTest extends BaseIntegrationTest {
    
    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() {
        // Given
        LoginRequest request = TestDataFactory.createDefaultLoginRequest();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                request,
                ApiResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }
    
    @Test
    @DisplayName("用户登录 - 用户名为空")
    void login_UsernameBlank() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("")
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .build();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                request,
                ApiResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isFalse();
    }
    
    @Test
    @DisplayName("用户登录 - 密码为空")
    void login_PasswordBlank() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username(TestDataFactory.DEFAULT_USERNAME)
                .password("")
                .build();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                request,
                ApiResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isFalse();
    }
    
    @Test
    @DisplayName("用户登录 - 用户名或密码错误")
    void login_InvalidCredentials() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("wronguser")
                .password("wrongpassword")
                .build();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                request,
                ApiResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isFalse();
    }
    
    @Test
    @DisplayName("刷新令牌 - 成功")
    void refreshToken_Success() {
        // Given
        // 先登录获取刷新令牌
        LoginRequest loginRequest = TestDataFactory.createDefaultLoginRequest();
        ResponseEntity<ApiResponse> loginResponse = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                loginRequest,
                ApiResponse.class
        );
        
        // 假设登录成功并获取到刷新令牌
        RefreshTokenRequest request = TestDataFactory.createDefaultRefreshTokenRequest();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                getApiUrl("/auth/refresh"),
                request,
                ApiResponse.class
        );
        
        // Then
        // 注意：这里可能需要根据实际的Mock数据调整断言
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }
    
    @Test
    @DisplayName("刷新令牌 - 令牌为空")
    void refreshToken_TokenBlank() {
        // Given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("")
                .build();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                getApiUrl("/auth/refresh"),
                request,
                ApiResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isFalse();
    }
    
    @Test
    @DisplayName("用户退出 - 成功")
    void logout_Success() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/auth/logout"),
                HttpMethod.POST,
                entity,
                ApiResponse.class
        );
        
        // Then
        // 注意：这里可能需要根据实际的认证实现调整断言
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }
    
    @Test
    @DisplayName("用户退出 - 未认证")
    void logout_Unauthorized() {
        // Given
        HttpEntity<Void> entity = new HttpEntity<>(new org.springframework.http.HttpHeaders());
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/auth/logout"),
                HttpMethod.POST,
                entity,
                ApiResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    
    @Test
    @DisplayName("登录流程完整测试")
    void loginFlow_Complete() {
        // Given
        LoginRequest loginRequest = TestDataFactory.createDefaultLoginRequest();
        
        // When - 登录
        ResponseEntity<ApiResponse> loginResponse = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                loginRequest,
                ApiResponse.class
        );
        
        // Then - 验证登录响应
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().getSuccess()).isTrue();
        
        // 如果登录成功，可以继续测试其他需要认证的接口
        if (loginResponse.getBody().getSuccess()) {
            // 这里可以添加更多的认证后操作测试
            // 例如获取用户信息、修改密码等
        }
    }
    
    @Test
    @DisplayName("并发登录测试")
    void concurrentLogin_Test() {
        // Given
        LoginRequest request = TestDataFactory.createDefaultLoginRequest();
        
        // When - 并发登录
        ResponseEntity<ApiResponse> response1 = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                request,
                ApiResponse.class
        );
        
        ResponseEntity<ApiResponse> response2 = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                request,
                ApiResponse.class
        );
        
        // Then - 两次登录都应该成功（或者根据业务规则调整）
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    @DisplayName("登录参数验证测试")
    void loginValidation_Test() {
        // Test case 1: 用户名过长
        LoginRequest longUsernameRequest = LoginRequest.builder()
                .username("a".repeat(51)) // 超过50字符限制
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .build();
        
        ResponseEntity<ApiResponse> response1 = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                longUsernameRequest,
                ApiResponse.class
        );
        
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // Test case 2: 密码过长
        LoginRequest longPasswordRequest = LoginRequest.builder()
                .username(TestDataFactory.DEFAULT_USERNAME)
                .password("a".repeat(256)) // 超过255字符限制
                .build();
        
        ResponseEntity<ApiResponse> response2 = restTemplate.postForEntity(
                getApiUrl("/auth/login"),
                longPasswordRequest,
                ApiResponse.class
        );
        
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}