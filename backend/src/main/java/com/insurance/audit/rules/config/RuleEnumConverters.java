package com.insurance.audit.rules.config;

import com.insurance.audit.rules.enums.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * String到Enum的转换器配置
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
public class RuleEnumConverters {

    @Component
    public static class StringToRuleSourceConverter implements Converter<String, RuleSource> {
        @Override
        public RuleSource convert(@NonNull String source) {
            try {
                return RuleSource.fromCode(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Component
    public static class StringToRuleTypeConverter implements Converter<String, RuleType> {
        @Override
        public RuleType convert(@NonNull String source) {
            try {
                return RuleType.fromCode(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Component
    public static class StringToRuleAuditStatusConverter implements Converter<String, RuleAuditStatus> {
        @Override
        public RuleAuditStatus convert(@NonNull String source) {
            try {
                return RuleAuditStatus.fromCode(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Component
    public static class StringToRuleEffectiveStatusConverter implements Converter<String, RuleEffectiveStatus> {
        @Override
        public RuleEffectiveStatus convert(@NonNull String source) {
            try {
                return RuleEffectiveStatus.fromCode(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Component
    public static class StringToRuleSortFieldConverter implements Converter<String, RuleSortField> {
        @Override
        public RuleSortField convert(@NonNull String source) {
            try {
                return RuleSortField.fromCode(source);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Component
    public static class StringToSortDirectionConverter implements Converter<String, SortDirection> {
        @Override
        public SortDirection convert(@NonNull String source) {
            try {
                return SortDirection.fromCode(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}