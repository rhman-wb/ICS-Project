package com.insurance.audit.rules.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 排序方向枚举
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Getter
@AllArgsConstructor
public enum SortDirection implements IEnum<String> {

    /**
     * 升序
     */
    ASC("ASC", "升序"),

    /**
     * 降序
     */
    DESC("DESC", "降序");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    @Override
    public String getValue() {
        return this.code;
    }

    public static SortDirection fromCode(String code) {
        for (SortDirection direction : values()) {
            if (direction.getCode().equals(code)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown sort direction code: " + code);
    }
}