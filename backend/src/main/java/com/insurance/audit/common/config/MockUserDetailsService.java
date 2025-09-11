package com.insurance.audit.common.config;

import com.insurance.audit.user.infrastructure.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Mock用户详情服务 - 用于调试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "false")
@Slf4j
public class MockUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Mock UserDetailsService loading user: {}", username);
        
        // 创建一个Mock用户
        return CustomUserDetails.builder()
                .userId("mock-user-id")
                .username(username)
                .password("$2a$10$mockPasswordHash") // Mock密码哈希
                .realName("Mock User")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .permissions(List.of())
                .build();
    }
}