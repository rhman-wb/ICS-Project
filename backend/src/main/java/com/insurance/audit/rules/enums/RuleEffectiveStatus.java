package com.insurance.audit.rules.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则有效状态枚举
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Getter
@AllArgsConstructor
public enum RuleEffectiveStatus implements IEnum<String> {

    /**
     * 激活/生效
     */
    ACTIVE("ACTIVE", "激活"),

    /**
     * 有效
     */
    EFFECTIVE("EFFECTIVE", "有效"),

    /**
     * 无效
     */
    INACTIVE("INACTIVE", "无效"),

    /**
     * 待开发上线
     */
    PENDING_DEPLOYMENT("PENDING_DEPLOYMENT", "待开发上线"),

    /**
     * 已下线
     */
    OFFLINE("OFFLINE", "已下线"),

    /**
     * 测试中
     */
    TESTING("TESTING", "测试中"),

    /**
     * 暂停状态
     */
    SUSPENDED("SUSPENDED", "暂停"),

    /**
     * 过期状态
     */
    EXPIRED("EXPIRED", "过期");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    @Override
    public String getValue() {
        return this.code;
    }

    public static RuleEffectiveStatus fromCode(String code) {
        for (RuleEffectiveStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown rule effective status code: " + code);
    }
}