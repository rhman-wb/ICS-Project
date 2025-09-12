package com.insurance.audit.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.insurance.audit.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 文档章节实体
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
@TableName("document_chapters")
@Schema(description = "文档章节实体")
public class DocumentChapter extends BaseEntity {

    /**
     * 关联文档ID
     */
    @TableField("document_id")
    @Schema(description = "关联文档ID", example = "doc_001")
    private String documentId;

    /**
     * 章节名称
     */
    @TableField("chapter_name")
    @Schema(description = "章节名称", example = "总则")
    private String chapterName;

    /**
     * 章节编号
     */
    @TableField("chapter_number")
    @Schema(description = "章节编号", example = "第一条")
    private String chapterNumber;

    /**
     * 父章节ID
     */
    @TableField("parent_id")
    @Schema(description = "父章节ID")
    private String parentId;

    /**
     * 章节级别
     */
    @TableField("level")
    @Schema(description = "章节级别", example = "1")
    private Integer level;

    /**
     * 排序号
     */
    @TableField("sort_order")
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    /**
     * 内容起始位置
     */
    @TableField("content_start")
    @Schema(description = "内容起始位置", example = "0")
    private Integer contentStart;

    /**
     * 内容结束位置
     */
    @TableField("content_end")
    @Schema(description = "内容结束位置", example = "500")
    private Integer contentEnd;

    /**
     * 起始页码
     */
    @TableField("page_start")
    @Schema(description = "起始页码", example = "1")
    private Integer pageStart;

    /**
     * 结束页码
     */
    @TableField("page_end")
    @Schema(description = "结束页码", example = "1")
    private Integer pageEnd;

    /**
     * 关联的文档对象
     */
    @TableField(exist = false)
    @Schema(description = "关联的文档对象")
    private Document document;
}