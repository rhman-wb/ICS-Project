package com.insurance.audit.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 分页请求基类
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页请求")
public class PageRequest {
    
    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 1000, message = "每页大小不能超过1000")
    @Schema(description = "每页大小", example = "20", defaultValue = "20")
    private Integer size = 20;
    
    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createdAt")
    private String sortBy;
    
    /**
     * 排序方向
     */
    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
    
    /**
     * 获取偏移量
     */
    public long getOffset() {
        return (long) (page - 1) * size;
    }
    
    /**
     * 获取限制数量
     */
    public int getLimit() {
        return size;
    }
}