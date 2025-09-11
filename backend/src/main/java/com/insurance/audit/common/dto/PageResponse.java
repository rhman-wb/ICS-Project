package com.insurance.audit.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应格式
 * 
 * @param <T> 数据类型
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应格式")
public class PageResponse<T> {
    
    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> records;
    
    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Long total;
    
    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer current;
    
    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer size;
    
    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "5")
    private Integer pages;
}