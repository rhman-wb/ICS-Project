package com.insurance.audit.user.interfaces.dto.request;

import com.insurance.audit.common.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * 刷新令牌请求DTO测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@DisplayName("刷新令牌请求DTO测试")
class RefreshTokenRequestTest {
    
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();
    
    @Test
    @DisplayName("创建刷新令牌请求 - 成功")
    void createRefreshTokenRequest_Success() {
        // Given
        String refreshToken = TestDataFactory.DEFAULT_REFRESH_TOKEN;
        
        // When
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
        
        // Then
        assertThat(request).isNotNull();
        assertThat(request.getRefreshToken()).isEqualTo(refreshToken);
    }
    
    @Test
    @DisplayName("使用TestDataFactory创建默认刷新令牌请求 - 成功")
    void createDefaultRefreshTokenRequest_Success() {
        // When
        RefreshTokenRequest request = TestDataFactory.createDefaultRefreshTokenRequest();
        
        // Then
        assertThat(request).isNotNull();
        assertThat(request.getRefreshToken()).isEqualTo(TestDataFactory.DEFAULT_REFRESH_TOKEN);
    }
    
    @Test
    @DisplayName("验证刷新令牌请求 - 成功")
    void validateRefreshTokenRequest_Success() {
        // Given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(TestDataFactory.DEFAULT_REFRESH_TOKEN)
                .build();
        
        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("验证刷新令牌请求 - 刷新令牌为空")
    void validateRefreshTokenRequest_RefreshTokenBlank() {
        // Given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("")
                .build();
        
        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("刷新令牌不能为空");
    }
    
    @Test
    @DisplayName("验证刷新令牌请求 - 刷新令牌为null")
    void validateRefreshTokenRequest_RefreshTokenNull() {
        // Given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(null)
                .build();
        
        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("刷新令牌不能为空");
    }
    
    @Test
    @DisplayName("验证刷新令牌请求 - 刷新令牌为空白字符")
    void validateRefreshTokenRequest_RefreshTokenWhitespace() {
        // Given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("   ")
                .build();
        
        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("刷新令牌不能为空");
    }
    
    @Test
    @DisplayName("创建有效的JWT格式刷新令牌请求 - 成功")
    void createValidJwtFormatRefreshTokenRequest_Success() {
        // Given
        String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInR5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNzA0NjcyMDAwLCJleHAiOjE3MDUyNzY4MDB9.test";
        
        // When
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(jwtToken)
                .build();
        
        // Then
        assertThat(request).isNotNull();
        assertThat(request.getRefreshToken()).isEqualTo(jwtToken);
        
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("测试equals和hashCode - 相同对象")
    void testEqualsAndHashCode_SameObject() {
        // Given
        RefreshTokenRequest request1 = TestDataFactory.createDefaultRefreshTokenRequest();
        RefreshTokenRequest request2 = TestDataFactory.createDefaultRefreshTokenRequest();
        
        // When & Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }
    
    @Test
    @DisplayName("测试equals和hashCode - 不同对象")
    void testEqualsAndHashCode_DifferentObject() {
        // Given
        RefreshTokenRequest request1 = TestDataFactory.createDefaultRefreshTokenRequest();
        RefreshTokenRequest request2 = RefreshTokenRequest.builder()
                .refreshToken("different-refresh-token")
                .build();
        
        // When & Then
        assertThat(request1).isNotEqualTo(request2);
        assertThat(request1.hashCode()).isNotEqualTo(request2.hashCode());
    }
    
    @Test
    @DisplayName("测试toString方法 - 成功")
    void testToString_Success() {
        // Given
        RefreshTokenRequest request = TestDataFactory.createDefaultRefreshTokenRequest();
        
        // When
        String toString = request.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("RefreshTokenRequest");
        // 注意：出于安全考虑，toString可能不包含实际的token值
    }
    
    @Test
    @DisplayName("测试Builder模式 - 成功")
    void testBuilderPattern_Success() {
        // When
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(TestDataFactory.DEFAULT_REFRESH_TOKEN)
                .build();
        
        // Then
        assertThat(request).isNotNull();
        assertThat(request.getRefreshToken()).isEqualTo(TestDataFactory.DEFAULT_REFRESH_TOKEN);
    }
    
    @Test
    @DisplayName("测试无参构造函数 - 成功")
    void testNoArgsConstructor_Success() {
        // When
        RefreshTokenRequest request = new RefreshTokenRequest();
        
        // Then
        assertThat(request).isNotNull();
        assertThat(request.getRefreshToken()).isNull();
    }
    
    @Test
    @DisplayName("测试全参构造函数 - 成功")
    void testAllArgsConstructor_Success() {
        // Given
        String refreshToken = TestDataFactory.DEFAULT_REFRESH_TOKEN;
        
        // When
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        // Then
        assertThat(request).isNotNull();
        assertThat(request.getRefreshToken()).isEqualTo(refreshToken);
    }
    
    @Test
    @DisplayName("测试setter方法 - 成功")
    void testSetterMethod_Success() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        String refreshToken = TestDataFactory.DEFAULT_REFRESH_TOKEN;
        
        // When
        request.setRefreshToken(refreshToken);
        
        // Then
        assertThat(request.getRefreshToken()).isEqualTo(refreshToken);
    }
    
    @Test
    @DisplayName("创建长刷新令牌请求 - 成功")
    void createLongRefreshTokenRequest_Success() {
        // Given
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longToken.append("a");
        }
        String refreshToken = longToken.toString();
        
        // When
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
        
        // Then
        assertThat(request).isNotNull();
        assertThat(request.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(request.getRefreshToken()).hasSize(1000);
        
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}