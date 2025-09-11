package com.insurance.audit.common;

import com.insurance.audit.user.infrastructure.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试安全工具类
 * 提供测试环境下的安全上下文管理
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
public class TestSecurityUtils {
    
    /**
     * 设置默认用户的安全上下文
     */
    public static void setDefaultUserSecurityContext() {
        CustomUserDetails userDetails = TestDataFactory.createDefaultCustomUserDetails();
        setSecurityContext(userDetails);
    }
    
    /**
     * 设置管理员用户的安全上下文
     */
    public static void setAdminUserSecurityContext() {
        CustomUserDetails userDetails = TestDataFactory.createAdminUserDetails();
        setSecurityContext(userDetails);
    }
    
    /**
     * 设置指定用户的安全上下文
     */
    public static void setUserSecurityContext(String username, String... roles) {
        Collection<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(TestDataFactory.generateId())
                .username(username)
                .password(TestDataFactory.DEFAULT_ENCODED_PASSWORD)
                .realName("测试用户-" + username)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("test:view"))
                .build();
        
        setSecurityContext(userDetails);
    }
    
    /**
     * 设置安全上下文
     */
    public static void setSecurityContext(CustomUserDetails userDetails) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    
    /**
     * 清除安全上下文
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
    
    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }
    
    /**
     * 检查当前用户是否有指定角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }
    
    /**
     * 检查当前用户是否有指定权限
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getPermissions() != null && 
                   userDetails.getPermissions().contains(permission);
        }
        return false;
    }
    
    /**
     * 模拟匿名用户
     */
    public static void setAnonymousUser() {
        SecurityContextHolder.clearContext();
    }
    
    /**
     * 创建测试用的JWT令牌
     */
    public static String createTestJwtToken(String username) {
        return "Bearer " + TestDataFactory.DEFAULT_JWT_TOKEN;
    }
    
    /**
     * 创建测试用的刷新令牌
     */
    public static String createTestRefreshToken(String username) {
        return TestDataFactory.DEFAULT_REFRESH_TOKEN;
    }
    
    /**
     * 自定义注解：使用默认测试用户
     */
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(username = TestDataFactory.DEFAULT_USERNAME, roles = {"USER", "TEST"})
    public @interface WithDefaultTestUser {
    }
    
    /**
     * 自定义注解：使用管理员用户
     */
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    public @interface WithAdminUser {
    }
    
    /**
     * 自定义注解：使用自定义用户详情
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface WithCustomUserDetails {
        String username() default TestDataFactory.DEFAULT_USERNAME;
        String userId() default TestDataFactory.DEFAULT_USER_ID;
        String[] roles() default {"USER"};
        String[] permissions() default {"test:view"};
    }
    
    /**
     * 自定义用户详情安全上下文工厂
     */
    public static class CustomUserDetailsSecurityContextFactory implements WithSecurityContextFactory<WithCustomUserDetails> {
        
        @Override
        public SecurityContext createSecurityContext(WithCustomUserDetails annotation) {
            Collection<GrantedAuthority> authorities = Arrays.stream(annotation.roles())
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
            
            CustomUserDetails userDetails = CustomUserDetails.builder()
                    .userId(annotation.userId())
                    .username(annotation.username())
                    .password(TestDataFactory.DEFAULT_ENCODED_PASSWORD)
                    .realName("测试用户-" + annotation.username())
                    .enabled(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .accountNonLocked(true)
                    .authorities(authorities)
                    .permissions(Arrays.asList(annotation.permissions()))
                    .build();
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, userDetails.getPassword(), userDetails.getAuthorities());
            
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            return securityContext;
        }
    }
    
    /**
     * 验证安全上下文是否正确设置
     */
    public static boolean isSecurityContextSet() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }
    
    /**
     * 验证当前用户是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * 获取当前用户的所有角色
     */
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring(5))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    /**
     * 获取当前用户的所有权限
     */
    public static List<String> getCurrentUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getPermissions() != null ? userDetails.getPermissions() : List.of();
        }
        return List.of();
    }
}