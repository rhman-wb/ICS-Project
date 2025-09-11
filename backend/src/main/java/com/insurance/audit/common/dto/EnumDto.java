package com.insurance.audit.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用枚举DTO - 用于需要同时返回code和description的场景
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@Schema(description = "枚举数据传输对象")
public class EnumDto {
    
    @Schema(description = "枚举代码", example = "ACTIVE")
    private String code;
    
    @Schema(description = "枚举描述", example = "激活")
    private String description;
}