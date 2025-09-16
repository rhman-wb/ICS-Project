package com.insurance.audit.common.config;

import com.insurance.audit.rules.config.RuleEnumConverters;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 注册规则枚举转换器
        registry.addConverter(new RuleEnumConverters.StringToRuleSourceConverter());
        registry.addConverter(new RuleEnumConverters.StringToRuleTypeConverter());
        registry.addConverter(new RuleEnumConverters.StringToRuleAuditStatusConverter());
        registry.addConverter(new RuleEnumConverters.StringToRuleEffectiveStatusConverter());
        registry.addConverter(new RuleEnumConverters.StringToRuleSortFieldConverter());
        registry.addConverter(new RuleEnumConverters.StringToSortDirectionConverter());
    }
}