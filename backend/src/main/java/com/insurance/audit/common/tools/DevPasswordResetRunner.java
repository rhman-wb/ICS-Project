package com.insurance.audit.common.tools;

import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 开发环境工具：一次性重置 admin 密码为 admin123
 */
@Slf4j
@Component
@Profile({"dev", "debug"})
@ConditionalOnProperty(name = "app.tools.reset-admin-password", havingValue = "true")
@RequiredArgsConstructor
public class DevPasswordResetRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            User user = userMapper.findByUsername("admin");
            if (user == null) {
                log.warn("Dev reset password skipped: admin user not found");
                return;
            }
            String newHash = passwordEncoder.encode("admin123");
            user.setPassword(newHash);
            user.setLoginFailureCount(0);
            user.setLockedUntil(null);
            userMapper.updateById(user);
            log.warn("[DEV ONLY] Admin password has been reset to 'admin123'. Please disable app.tools.reset-admin-password after login.");
        } catch (Exception e) {
            log.error("Dev reset password failed", e);
        }
    }
}


