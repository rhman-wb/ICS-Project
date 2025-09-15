package com.insurance.audit.rules.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则类型枚举
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Getter
@AllArgsConstructor
public enum RuleType implements IEnum<String> {

    /**
     * 单句规则
     */
    SINGLE("SINGLE", "单句规则"),

    /**
     * 双句规则
     */
    DOUBLE("DOUBLE", "双句规则"),

    /**
     * 格式规则
     */
    FORMAT("FORMAT", "格式规则"),

    /**
     * 高级规则
     */
    ADVANCED("ADVANCED", "高级规则");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    @Override
    public String getValue() {
        return this.code;
    }

    public static RuleType fromCode(String code) {
        for (RuleType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown rule type code: " + code);
    }
}