package com.insurance.audit.common.util;

import com.insurance.audit.common.TestDataFactory;
import com.insurance.audit.user.infrastructure.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT工具类测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT工具类测试")
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    private final String testSecret = "test-jwt-secret-key-for-insurance-audit-system-2024";
    private final Long testExpiration = 3600L; // 1小时
    private final Long testRefreshExpiration = 86400L; // 24小时
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", testRefreshExpiration);
    }
    
    @Test
    @DisplayName("生成JWT令牌 - 成功")
    void generateToken_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        
        // When
        String token = jwtUtil.generateToken(username);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo(username);
    }
    
    @Test
    @DisplayName("生成JWT令牌（带声明） - 成功")
    void generateTokenWithClaims_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", TestDataFactory.DEFAULT_USER_ID);
        claims.put("role", "USER");
        
        // When
        String token = jwtUtil.generateToken(username, claims);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo(username);
        
        Claims tokenClaims = ReflectionTestUtils.invokeMethod(jwtUtil, "getAllClaimsFromToken", token);
        assertThat(tokenClaims.get("userId")).isEqualTo(TestDataFactory.DEFAULT_USER_ID);
        assertThat(tokenClaims.get("role")).isEqualTo("USER");
    }
    
    @Test
    @DisplayName("生成刷新令牌 - 成功")
    void generateRefreshToken_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        
        // When
        String refreshToken = jwtUtil.generateRefreshToken(username);
        
        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(jwtUtil.getUsernameFromToken(refreshToken)).isEqualTo(username);
        
        Claims claims = ReflectionTestUtils.invokeMethod(jwtUtil, "getAllClaimsFromToken", refreshToken);
        assertThat(claims.get("type")).isEqualTo("refresh");
    }
    
    @Test
    @DisplayName("从令牌获取用户名 - 成功")
    void getUsernameFromToken_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        
        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        
        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }
    
    @Test
    @DisplayName("从令牌获取过期时间 - 成功")
    void getExpirationDateFromToken_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        
        // When
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        Date expirationFromToken = jwtUtil.getExpirationFromToken(token);
        
        // Then
        assertThat(expirationDate).isNotNull();
        assertThat(expirationFromToken).isNotNull();
        assertThat(expirationDate).isEqualTo(expirationFromToken);
        assertThat(expirationDate).isAfter(new Date());
    }
    
    @Test
    @DisplayName("从令牌获取JTI - 成功")
    void getJtiFromToken_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        
        // When
        String jti = jwtUtil.getJtiFromToken(token);
        
        // Then
        assertThat(jti).isNotNull();
        assertThat(jti).isNotEmpty();
    }
    
    @Test
    @DisplayName("检查令牌是否过期 - 未过期")
    void isTokenExpired_NotExpired() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        
        // When
        Boolean isExpired = jwtUtil.isTokenExpired(token);
        
        // Then
        assertThat(isExpired).isFalse();
    }
    
    @Test
    @DisplayName("检查令牌是否过期 - 已过期")
    void isTokenExpired_Expired() {
        // Given
        // 设置很短的过期时间
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        
        // 等待令牌过期
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        Boolean isExpired = jwtUtil.isTokenExpired(token);
        
        // Then
        assertThat(isExpired).isTrue();
    }
    
    @Test
    @DisplayName("验证令牌（带用户名） - 成功")
    void validateTokenWithUsername_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        
        // When
        Boolean isValid = jwtUtil.validateToken(token, username);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("验证令牌（带用户名） - 用户名不匹配")
    void validateTokenWithUsername_UsernameMismatch() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        String differentUsername = "differentuser";
        
        // When
        Boolean isValid = jwtUtil.validateToken(token, differentUsername);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("验证令牌（不检查用户名） - 成功")
    void validateToken_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        
        // When
        Boolean isValid = jwtUtil.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("验证令牌（不检查用户名） - 无效令牌")
    void validateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("验证刷新令牌 - 成功")
    void validateRefreshToken_Success() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String refreshToken = jwtUtil.generateRefreshToken(username);
        
        // When
        Boolean isValid = jwtUtil.validateRefreshToken(refreshToken);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("验证刷新令牌 - 普通令牌")
    void validateRefreshToken_AccessToken() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String accessToken = jwtUtil.generateToken(username);
        
        // When
        Boolean isValid = jwtUtil.validateRefreshToken(accessToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("生成访问令牌（使用UserDetails） - 成功")
    void generateAccessToken_WithUserDetails_Success() {
        // Given
        CustomUserDetails userDetails = TestDataFactory.createDefaultCustomUserDetails();
        
        // When
        String token = jwtUtil.generateAccessToken(userDetails);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo(userDetails.getUsername());
        
        Claims claims = ReflectionTestUtils.invokeMethod(jwtUtil, "getAllClaimsFromToken", token);
        assertThat(claims.get("type")).isEqualTo("access");
    }
    
    @Test
    @DisplayName("生成刷新令牌（使用UserDetails） - 成功")
    void generateRefreshTokenWithUserDetails_Success() {
        // Given
        CustomUserDetails userDetails = TestDataFactory.createDefaultCustomUserDetails();
        
        // When
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(jwtUtil.getUsernameFromToken(refreshToken)).isEqualTo(userDetails.getUsername());
        
        Claims claims = ReflectionTestUtils.invokeMethod(jwtUtil, "getAllClaimsFromToken", refreshToken);
        assertThat(claims.get("type")).isEqualTo("refresh");
    }
    
    @Test
    @DisplayName("获取访问令牌过期时间 - 成功")
    void getAccessTokenExpiration_Success() {
        // When
        Long expiration = jwtUtil.getAccessTokenExpiration();
        
        // Then
        assertThat(expiration).isEqualTo(testExpiration);
    }
    
    @Test
    @DisplayName("获取刷新令牌过期时间 - 成功")
    void getRefreshTokenExpiration_Success() {
        // When
        Long expiration = jwtUtil.getRefreshTokenExpiration();
        
        // Then
        assertThat(expiration).isEqualTo(testRefreshExpiration);
    }
    
    @Test
    @DisplayName("解析恶意令牌 - 抛出异常")
    void parseMaliciousToken_ThrowsException() {
        // Given
        String maliciousToken = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJhdHRhY2tlciJ9.";
        
        // When & Then
        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(maliciousToken))
                .isInstanceOf(MalformedJwtException.class);
    }
    
    @Test
    @DisplayName("解析空令牌 - 抛出异常")
    void parseNullToken_ThrowsException() {
        // Given
        String nullToken = null;
        
        // When & Then
        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(nullToken))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("解析空字符串令牌 - 抛出异常")
    void parseEmptyToken_ThrowsException() {
        // Given
        String emptyToken = "";
        
        // When & Then
        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(emptyToken))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("令牌签名验证 - 不同密钥")
    void tokenSignatureVerification_DifferentSecret() {
        // Given
        String username = TestDataFactory.DEFAULT_USERNAME;
        String token = jwtUtil.generateToken(username);
        
        // 更改密钥
        ReflectionTestUtils.setField(jwtUtil, "secret", "different-secret-key");
        
        // When & Then
        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(token))
                .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }
}