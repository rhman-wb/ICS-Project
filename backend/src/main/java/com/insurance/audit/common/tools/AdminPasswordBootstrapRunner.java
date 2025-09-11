package com.insurance.audit.common.tools;

import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 一次性初始化/修复管理员密码（可在任意环境开启，具备幂等性）
 * 通过开关 app.security.admin.bootstrap.enabled 控制是否生效；
 * 需要提供 app.security.admin.initial-password（建议通过环境变量注入）。
 * 执行成功后，会在 system_configs 写入标记，后续启动不会再次修改。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.security.admin.bootstrap.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AdminPasswordBootstrapRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SystemConfigMapper systemConfigMapper;

    @Value("${app.security.admin.initial-password:}")
    private String initialPassword;

    private static final String BOOTSTRAP_FLAG_KEY = "security.admin.password_initialized";

    @Override
    public void run(String... args) {
        try {
            // 幂等：若已初始化过，直接跳过
            String flag = systemConfigMapper.getValue(BOOTSTRAP_FLAG_KEY);
            if (flag != null && flag.startsWith("true")) {
                log.info("Admin password bootstrap skipped (already initialized). Flag={}", flag);
                return;
            }

            if (initialPassword == null || initialPassword.trim().isEmpty()) {
                log.warn("Admin password bootstrap skipped: initial password not provided.");
                return;
            }

            User admin = userMapper.findByUsername("admin");
            if (admin == null) {
                log.warn("Admin password bootstrap skipped: admin user not found.");
                return;
            }

            admin.setPassword(passwordEncoder.encode(initialPassword));
            admin.setLoginFailureCount(0);
            admin.setLockedUntil(null);
            userMapper.updateById(admin);

            String value = "true@" + LocalDateTime.now();
            systemConfigMapper.upsertValue(BOOTSTRAP_FLAG_KEY, value, uuid());
            log.warn("[ADMIN BOOTSTRAP] Admin password has been initialized. Flag set to {}", value);
        } catch (Exception e) {
            log.error("Admin password bootstrap failed", e);
        }
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Mapper
    public interface SystemConfigMapper {
        @Select("SELECT config_value FROM system_configs WHERE config_key = #{key} AND is_deleted = 0 LIMIT 1")
        String getValue(@Param("key") String key);

        @Insert("INSERT INTO system_configs (id, config_key, config_value, config_type, description, is_system, created_by, created_at) " +
                "VALUES (#{id}, #{key}, #{value}, 'STRING', 'security bootstrap flag', 1, 'system', NOW()) " +
                "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), updated_by='system', updated_at=NOW()")
        int upsertValue(@Param("key") String key, @Param("value") String value, @Param("id") String id);
    }
}


