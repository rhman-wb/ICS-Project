package com.insurance.audit.user.infrastructure.security;

import com.insurance.audit.user.domain.model.Permission;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.mapper.PermissionMapper;
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
    private final PermissionMapper permissionMapper;
    
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
        
        // 查询用户角色
        List<Role> roles = roleMapper.findByUserId(user.getId());

        // 查询用户权限
        List<Permission> permissions = permissionMapper.findByUserId(user.getId());

        // 构建权限列表：包含角色权限(ROLE_前缀)和细粒度权限码
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        // 添加权限码到authorities（支持@PreAuthorize("hasAuthority('RULE_VIEW')")）
        List<GrantedAuthority> permissionAuthorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                .collect(Collectors.toList());
        authorities.addAll(permissionAuthorities);

        // 构建权限码列表供前端使用
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getCode)
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
                .permissions(permissionCodes) // 设置权限码列表
                .build();
    }
}