package com.insurance.audit.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作状态枚举
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum OperationStatus implements IEnum<String> {
    
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    PENDING("PENDING", "处理中");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
    
    public static OperationStatus fromCode(String code) {
        for (OperationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown operation status code: " + code);
    }
}