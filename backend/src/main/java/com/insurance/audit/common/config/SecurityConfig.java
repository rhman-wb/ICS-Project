package com.insurance.audit.common.config;

import com.insurance.audit.user.infrastructure.security.JwtAuthenticationEntryPoint;
import com.insurance.audit.user.infrastructure.security.JwtAuthenticationFilter;
import com.insurance.audit.user.infrastructure.security.JwtAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security配置
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
@Profile("!debug")
@ConditionalOnProperty(name = "app.security.disabled", havingValue = "false", matchIfMissing = true)
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    
    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);  // 平衡安全性和性能
    }
    
    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }
    
    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT不需要CSRF保护）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 禁用CORS（如需要可单独配置）
            .cors(AbstractHttpConfigurer::disable)
            
            // 会话管理 - 无状态
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 请求授权配置
            .authorizeHttpRequests(auth -> auth
                // 公开接口 - 使用AntPathRequestMatcher避免MVC依赖
                .requestMatchers(new AntPathRequestMatcher("/v1/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v1/health/**")).permitAll()
                // 兼容在有 context-path=/api 时的完整路径匹配
                .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/health/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                
                // Swagger文档
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-resources/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/favicon.ico")).permitAll()
                
                // 静态资源
                .requestMatchers(new AntPathRequestMatcher("/static/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
                
                // 管理员接口
                .requestMatchers(new AntPathRequestMatcher("/v1/admin/**")).hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/admin/**")).hasRole("ADMIN")
                
                // 用户接口
                .requestMatchers(new AntPathRequestMatcher("/v1/user/**")).hasAnyRole("USER", "ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/user/**")).hasAnyRole("USER", "ADMIN")
                
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            
            // 异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            
            // 认证提供者
            .authenticationProvider(authenticationProvider())
            
            // JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 安全头配置
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            );
        
        return http.build();
    }
}