package com.insurance.audit.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

/**
 * MyBatis Plus配置
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@MapperScan("com.insurance.audit.**.mapper")
@Slf4j
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "true", matchIfMissing = false)
public class MyBatisPlusConfig {
    
    /**
     * MyBatis Plus插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 最大分页数量限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }
    
    /**
     * 元数据填充器 - 启用自动填充 createdAt/updatedAt/createdBy/updatedBy
     */
    @Bean
    @ConditionalOnProperty(name = "app.mybatis.meta-handler.enabled", havingValue = "true", matchIfMissing = true)
    public MetaObjectHandler metaObjectHandler() {
        return new MyMetaObjectHandler();
    }
    
    /**
     * 自动填充处理器
     */
    @Slf4j
    public static class MyMetaObjectHandler implements MetaObjectHandler {
        
        @Override
        public void insertFill(MetaObject metaObject) {
            log.debug("开始插入填充...");
            
            // 填充创建时间
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            
            // 填充创建人（从当前登录用户获取）
            String currentUserId = getCurrentUserId();
            this.strictInsertFill(metaObject, "createdBy", String.class, currentUserId);
            this.strictInsertFill(metaObject, "updatedBy", String.class, currentUserId);
            
            // 填充默认值
            this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
            this.strictInsertFill(metaObject, "version", Integer.class, 0);
        }
        
        @Override
        public void updateFill(MetaObject metaObject) {
            log.debug("开始更新填充...");
            
            // 填充更新时间
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            
            // 填充更新人
            String currentUserId = getCurrentUserId();
            this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUserId);
        }
        
        private String getCurrentUserId() {
            try {
                // 从Spring Security上下文获取当前用户ID
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                    return authentication.getName();
                }
            } catch (Exception e) {
                log.warn("Failed to get current user ID: {}", e.getMessage());
            }
            return "system";
        }
    }
}