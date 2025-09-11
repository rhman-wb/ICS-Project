package com.insurance.audit.user.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.insurance.audit.common.dto.EnumDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum UserStatus implements IEnum<String> {
    
    ACTIVE("ACTIVE", "激活"),
    INACTIVE("INACTIVE", "停用"),
    LOCKED("LOCKED", "锁定");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
    
    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status code: " + code);
    }
    
    /**
     * 获取完整信息的DTO（用于下拉框等场景）
     */
    public EnumDto toDto() {
        return EnumDto.builder()
            .code(this.code)
            .description(this.description)
            .build();
    }
}