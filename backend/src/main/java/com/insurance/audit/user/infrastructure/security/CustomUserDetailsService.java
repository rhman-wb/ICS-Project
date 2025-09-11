package com.insurance.audit.user.infrastructure.security;

import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.mapper.RoleMapper;
import com.insurance.audit.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "true", matchIfMissing = true)
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        User user = userMapper.findByUsername(username);
        if (user == null) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 检查用户状态
        if (!user.isEnabled()) {
            throw new RuntimeException("用户已被禁用: " + username);
        }
        
        if (user.isAccountLocked()) {
            throw new RuntimeException("用户账户已被锁定: " + username);
        }
        
        // 查询用户角色（角色表字段与实体字段 name/code 已一致）
        List<Role> roles = roleMapper.findByUserId(user.getId());
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
        
        return CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .realName(user.getRealName())
                .enabled(user.isEnabled())
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(!user.isAccountLocked())
                .authorities(authorities)
                .permissions(List.of()) // TODO: 实现权限查询
                .build();
    }
}