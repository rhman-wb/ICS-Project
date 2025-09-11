package com.insurance.audit.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.insurance.audit.common.dto.EnumDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum OperationType implements IEnum<String> {
    
    LOGIN("LOGIN", "登录"),
    LOGOUT("LOGOUT", "退出"),
    CREATE("CREATE", "创建"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除"),
    VIEW("VIEW", "查看"),
    EXPORT("EXPORT", "导出"),
    IMPORT("IMPORT", "导入"),
    UPLOAD("UPLOAD", "上传"),
    DOWNLOAD("DOWNLOAD", "下载"),
    APPROVE("APPROVE", "审批"),
    REJECT("REJECT", "拒绝"),
    OTHER("OTHER", "其他");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
    
    public static OperationType fromCode(String code) {
        for (OperationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown operation type code: " + code);
    }
    
    /**
     * 获取完整信息的DTO
     */
    public EnumDto toDto() {
        return EnumDto.builder()
            .code(this.code)
            .description(this.description)
            .build();
    }
}