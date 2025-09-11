package com.insurance.audit.user.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.insurance.audit.common.dto.EnumDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色类型枚举
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum RoleType implements IEnum<String> {
    
    ADMIN("ADMIN", "管理员"),
    BUSINESS_USER("BUSINESS_USER", "业务用户"),
    AUDIT_USER("AUDIT_USER", "审核用户");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
    
    public static RoleType fromCode(String code) {
        for (RoleType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown role type code: " + code);
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