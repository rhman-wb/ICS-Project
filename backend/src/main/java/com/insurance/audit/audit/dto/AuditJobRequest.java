package com.insurance.audit.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 检核作业请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "检核作业请求")
public class AuditJobRequest {

    /**
     * 作业名称
     */
    @Schema(description = "作业名称", example = "产品条款检核")
    private String jobName;

    /**
     * 规则集ID
     */
    @Schema(description = "规则集ID", example = "ruleset-001")
    private String ruleSetId;

    /**
     * 产品文档ID列表
     */
    @Schema(description = "产品文档ID列表")
    private List<String> documentIds;

    /**
     * 作业描述
     */
    @Schema(description = "作业描述", example = "检核产品条款是否符合监管要求")
    private String description;

    /**
     * 并发度
     */
    @Schema(description = "并发度", example = "5")
    private Integer concurrency = 5;

    /**
     * 是否异步执行
     */
    @Schema(description = "是否异步执行", example = "true")
    private Boolean async = true;

    /**
     * 回调URL（异步执行时使用）
     */
    @Schema(description = "回调URL", example = "http://localhost:8080/callback")
    private String callbackUrl;
}