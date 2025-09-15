package com.insurance.audit.rules.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则来源枚举
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Getter
@AllArgsConstructor
public enum RuleSource implements IEnum<String> {

    /**
     * 系统规则
     */
    SYSTEM("SYSTEM", "系统规则"),

    /**
     * 自定义规则
     */
    CUSTOM("CUSTOM", "自定义规则"),

    /**
     * 导入规则
     */
    IMPORTED("IMPORTED", "导入规则"),

    /**
     * 模板规则
     */
    TEMPLATE("TEMPLATE", "模板规则");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    @Override
    public String getValue() {
        return this.code;
    }

    public static RuleSource fromCode(String code) {
        for (RuleSource source : values()) {
            if (source.getCode().equals(code)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown rule source code: " + code);
    }
}