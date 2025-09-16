package com.insurance.audit.rules.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则排序字段枚举
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Getter
@AllArgsConstructor
public enum RuleSortField implements IEnum<String> {

    /**
     * 规则编号
     */
    RULE_NUMBER("ruleNumber", "规则编号"),

    /**
     * 规则名称
     */
    RULE_NAME("ruleName", "规则名称"),

    /**
     * 创建时间
     */
    CREATED_AT("createdAt", "创建时间"),

    /**
     * 最后更新时间
     */
    LAST_UPDATED_AT("lastUpdatedAt", "最后更新时间"),

    /**
     * 提交时间
     */
    SUBMITTED_AT("submittedAt", "提交时间"),

    /**
     * 审核时间
     */
    AUDITED_AT("auditedAt", "审核时间"),

    /**
     * 生效时间
     */
    EFFECTIVE_TIME("effectiveTime", "生效时间"),

    /**
     * 失效时间
     */
    EXPIRY_TIME("expiryTime", "失效时间");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    @Override
    public String getValue() {
        return this.code;
    }

    public static RuleSortField fromCode(String code) {
        for (RuleSortField field : values()) {
            if (field.getCode().equals(code)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown rule sort field code: " + code);
    }
}