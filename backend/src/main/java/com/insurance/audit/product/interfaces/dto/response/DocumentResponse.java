package com.insurance.audit.product.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档响应DTO
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档响应")
public class DocumentResponse {

    /**
     * 文档ID
     */
    @Schema(description = "文档ID", example = "doc_001")
    private String id;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称", example = "中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险条款.docx")
    private String fileName;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径", example = "/uploads/2024/09/product_001/terms.docx")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）", example = "2048576")
    private Long fileSize;

    /**
     * 格式化的文件大小
     */
    @Schema(description = "格式化的文件大小", example = "2.0 MB")
    private String formattedFileSize;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型", example = "DOCX")
    private String fileType;

    /**
     * 要件类型
     */
    @Schema(description = "要件类型", example = "TERMS")
    private String documentType;

    /**
     * 要件类型描述
     */
    @Schema(description = "要件类型描述", example = "条款")
    private String documentTypeDescription;

    /**
     * 关联产品ID
     */
    @Schema(description = "关联产品ID", example = "product_001")
    private String productId;

    /**
     * 上传状态
     */
    @Schema(description = "上传状态", example = "UPLOADED")
    private String uploadStatus;

    /**
     * 上传状态描述
     */
    @Schema(description = "上传状态描述", example = "已上传")
    private String uploadStatusDescription;

    /**
     * 审核状态
     */
    @Schema(description = "审核状态", example = "PENDING")
    private String auditStatus;

    /**
     * 审核状态描述
     */
    @Schema(description = "审核状态描述", example = "待审核")
    private String auditStatusDescription;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2024-09-12 10:30:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2024-09-12 10:30:00")
    private LocalDateTime updatedAt;

    /**
     * 审核结果列表
     */
    @Schema(description = "审核结果列表")
    private List<AuditResultResponse> auditResults;

    /**
     * 审核结果数量
     */
    @Schema(description = "审核结果数量", example = "3")
    private Integer auditResultCount;

    /**
     * 错误数量
     */
    @Schema(description = "错误数量", example = "1")
    private Integer errorCount;

    /**
     * 警告数量
     */
    @Schema(description = "警告数量", example = "2")
    private Integer warningCount;

    /**
     * 信息数量
     */
    @Schema(description = "信息数量", example = "0")
    private Integer infoCount;
}