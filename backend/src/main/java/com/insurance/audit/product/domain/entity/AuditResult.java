package com.insurance.audit.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.insurance.audit.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 审核结果实体
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("audit_results")
@Schema(description = "审核结果实体")
public class AuditResult extends BaseEntity {

    /**
     * 关联文档ID
     */
    @TableField("document_id")
    @Schema(description = "关联文档ID", example = "doc_001")
    private String documentId;

    /**
     * 规则ID
     */
    @TableField("rule_id")
    @Schema(description = "规则ID", example = "CC001")
    private String ruleId;

    /**
     * 规则类型
     */
    @TableField("rule_type")
    @Schema(description = "规则类型", example = "格式规则")
    private String ruleType;

    /**
     * 适用章节
     */
    @TableField("applicable_chapter")
    @Schema(description = "适用章节", example = "总则")
    private String applicableChapter;

    /**
     * 规则来源
     */
    @TableField("rule_source")
    @Schema(description = "规则来源", example = "行政监管措施决定书（京银保监发〔2022〕36号）")
    private String ruleSource;

    /**
     * 原文内容
     */
    @TableField("original_content")
    @Schema(description = "原文内容", example = "第一条 保险责任")
    private String originalContent;

    /**
     * 修改建议
     */
    @TableField("suggestion")
    @Schema(description = "修改建议", example = "标题格式应为：第一条【保险责任】，需要在标题中添加中括号")
    private String suggestion;

    /**
     * 严重程度
     */
    @TableField("severity")
    @Schema(description = "严重程度", example = "WARNING")
    private AuditSeverity severity;

    /**
     * 在文档中的起始位置
     */
    @TableField("position_start")
    @Schema(description = "在文档中的起始位置", example = "100")
    private Integer positionStart;

    /**
     * 在文档中的结束位置
     */
    @TableField("position_end")
    @Schema(description = "在文档中的结束位置", example = "200")
    private Integer positionEnd;

    /**
     * 章节编号
     */
    @TableField("chapter_number")
    @Schema(description = "章节编号", example = "第一条")
    private String chapterNumber;

    /**
     * 页码
     */
    @TableField("page_number")
    @Schema(description = "页码", example = "1")
    private Integer pageNumber;

    /**
     * 行号
     */
    @TableField("line_number")
    @Schema(description = "行号", example = "5")
    private Integer lineNumber;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "审核时间", example = "2024-09-12 10:30:00")
    private LocalDateTime auditTime;

    /**
     * 关联的文档对象
     */
    @TableField(exist = false)
    @Schema(description = "关联的文档对象")
    private Document document;

    /**
     * 审核严重程度枚举
     */
    public enum AuditSeverity {
        /**
         * 错误
         */
        ERROR("错误"),
        /**
         * 警告
         */
        WARNING("警告"),
        /**
         * 信息
         */
        INFO("信息");

        private final String description;

        AuditSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}