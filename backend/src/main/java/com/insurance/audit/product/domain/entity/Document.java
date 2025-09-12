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

import java.util.List;

/**
 * 文档实体
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
@TableName("documents")
@Schema(description = "文档实体")
public class Document extends BaseEntity {

    /**
     * 文件名称
     */
    @TableField("file_name")
    @Schema(description = "文件名称", example = "中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险条款.docx")
    private String fileName;

    /**
     * 文件路径
     */
    @TableField("file_path")
    @Schema(description = "文件路径", example = "/uploads/2024/09/product_001/terms.docx")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    @Schema(description = "文件大小（字节）", example = "2048576")
    private Long fileSize;

    /**
     * 文件类型
     */
    @TableField("file_type")
    @Schema(description = "文件类型", example = "DOCX", allowableValues = {"DOCX", "PDF", "XLS", "XLSX"})
    private String fileType;

    /**
     * 要件类型
     */
    @TableField("document_type")
    @Schema(description = "要件类型", example = "TERMS")
    private DocumentType documentType;

    /**
     * 关联产品ID
     */
    @TableField("product_id")
    @Schema(description = "关联产品ID", example = "product_001")
    private String productId;

    /**
     * 上传状态
     */
    @TableField("upload_status")
    @Schema(description = "上传状态", example = "UPLOADED")
    private UploadStatus uploadStatus;

    /**
     * 审核状态
     */
    @TableField("audit_status")
    @Schema(description = "审核状态", example = "PENDING")
    private AuditStatus auditStatus;

    /**
     * 关联的产品对象
     */
    @TableField(exist = false)
    @Schema(description = "关联的产品对象")
    private Product product;

    /**
     * 审核结果列表
     */
    @TableField(exist = false)
    @Schema(description = "审核结果列表")
    private List<AuditResult> auditResults;

    /**
     * 文档章节列表
     */
    @TableField(exist = false)
    @Schema(description = "文档章节列表")
    private List<DocumentChapter> chapters;

    /**
     * 要件类型枚举
     */
    public enum DocumentType {
        /**
         * 产品信息登记表
         */
        REGISTRATION("产品信息登记表"),
        /**
         * 条款
         */
        TERMS("条款"),
        /**
         * 可行性报告
         */
        FEASIBILITY_REPORT("可行性报告"),
        /**
         * 精算报告
         */
        ACTUARIAL_REPORT("精算报告"),
        /**
         * 费率表
         */
        RATE_TABLE("费率表");

        private final String description;

        DocumentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 上传状态枚举
     */
    public enum UploadStatus {
        /**
         * 已上传
         */
        UPLOADED("已上传"),
        /**
         * 处理中
         */
        PROCESSING("处理中"),
        /**
         * 已处理
         */
        PROCESSED("已处理"),
        /**
         * 失败
         */
        FAILED("失败");

        private final String description;

        UploadStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 审核状态枚举
     */
    public enum AuditStatus {
        /**
         * 待审核
         */
        PENDING("待审核"),
        /**
         * 审核中
         */
        PROCESSING("审核中"),
        /**
         * 审核完成
         */
        COMPLETED("审核完成"),
        /**
         * 审核失败
         */
        FAILED("审核失败");

        private final String description;

        AuditStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}