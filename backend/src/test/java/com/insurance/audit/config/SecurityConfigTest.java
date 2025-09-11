package com.insurance.audit.config;

import com.insurance.audit.common.config.SecurityConfig;
import com.insurance.audit.user.infrastructure.security.CustomUserDetailsService;
import com.insurance.audit.user.infrastructure.security.JwtAccessDeniedHandler;
import com.insurance.audit.user.infrastructure.security.JwtAuthenticationEntryPoint;
import com.insurance.audit.user.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security配置测试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootTest(classes = SecurityConfig.class)
@ActiveProfiles("test")
class SecurityConfigTest {
    
    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @MockBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private CustomUserDetailsService userDetailsService;
    
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    void testPasswordEncoder() {
        assertNotNull(passwordEncoder);
        
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }
    
    @Test
    void testPasswordEncoderStrength() {
        String password = "password123";
        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);
        
        // BCrypt应该每次生成不同的哈希值
        assertNotEquals(encoded1, encoded2);
        
        // 但都应该能匹配原密码
        assertTrue(passwordEncoder.matches(password, encoded1));
        assertTrue(passwordEncoder.matches(password, encoded2));
    }
}