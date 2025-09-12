package com.insurance.audit.product.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核结果响应DTO
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核结果响应")
public class AuditResultResponse {

    /**
     * 审核结果ID
     */
    @Schema(description = "审核结果ID", example = "audit_001")
    private String id;

    /**
     * 关联文档ID
     */
    @Schema(description = "关联文档ID", example = "doc_001")
    private String documentId;

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "CC001")
    private String ruleId;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型", example = "格式规则")
    private String ruleType;

    /**
     * 适用章节
     */
    @Schema(description = "适用章节", example = "总则")
    private String applicableChapter;

    /**
     * 规则来源
     */
    @Schema(description = "规则来源", example = "行政监管措施决定书（京银保监发〔2022〕36号）")
    private String ruleSource;

    /**
     * 原文内容
     */
    @Schema(description = "原文内容", example = "第一条 保险责任")
    private String originalContent;

    /**
     * 修改建议
     */
    @Schema(description = "修改建议", example = "标题格式应为：第一条【保险责任】，需要在标题中添加中括号")
    private String suggestion;

    /**
     * 严重程度
     */
    @Schema(description = "严重程度", example = "WARNING")
    private String severity;

    /**
     * 严重程度描述
     */
    @Schema(description = "严重程度描述", example = "警告")
    private String severityDescription;

    /**
     * 在文档中的起始位置
     */
    @Schema(description = "在文档中的起始位置", example = "100")
    private Integer positionStart;

    /**
     * 在文档中的结束位置
     */
    @Schema(description = "在文档中的结束位置", example = "200")
    private Integer positionEnd;

    /**
     * 章节编号
     */
    @Schema(description = "章节编号", example = "第一条")
    private String chapterNumber;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNumber;

    /**
     * 行号
     */
    @Schema(description = "行号", example = "5")
    private Integer lineNumber;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "审核时间", example = "2024-09-12 10:30:00")
    private LocalDateTime auditTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2024-09-12 10:30:00")
    private LocalDateTime createdAt;
}