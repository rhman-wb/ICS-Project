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
 * 产品响应DTO
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "产品响应")
public class ProductResponse {

    /**
     * 产品ID
     */
    @Schema(description = "产品ID", example = "product_001")
    private String id;

    /**
     * 产品名称
     */
    @Schema(description = "产品名称", example = "中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险")
    private String productName;

    /**
     * 报送类型
     */
    @Schema(description = "报送类型", example = "修订产品")
    private String reportType;

    /**
     * 产品性质
     */
    @Schema(description = "产品性质", example = "政策性农险")
    private String productNature;

    /**
     * 年度
     */
    @Schema(description = "年度", example = "2022")
    private Integer year;

    /**
     * 修订类型
     */
    @Schema(description = "修订类型", example = "条款修订")
    private String revisionType;

    /**
     * 原产品名称和编号/备案编号
     */
    @Schema(description = "原产品名称和编号/备案编号")
    private String originalProductName;

    /**
     * 开发方式/开发类型
     */
    @Schema(description = "开发方式/开发类型", example = "自主开发")
    private String developmentMethod;

    /**
     * 主附险
     */
    @Schema(description = "主附险", example = "主险")
    private String primaryAdditional;

    /**
     * 主险情况
     */
    @Schema(description = "主险情况")
    private String primarySituation;

    /**
     * 产品类别
     */
    @Schema(description = "产品类别", example = "种植险")
    private String productCategory;

    /**
     * 经营区域
     */
    @Schema(description = "经营区域", example = "西藏自治区")
    private String operatingRegion;

    /**
     * 经营范围（备案产品）
     */
    @Schema(description = "经营范围（备案产品）")
    private String operatingScope;

    /**
     * 示范条款名称（备案产品）
     */
    @Schema(description = "示范条款名称（备案产品）")
    private String demonstrationClauseName;

    /**
     * 经营区域1（备案产品）
     */
    @Schema(description = "经营区域1（备案产品）")
    private String operatingRegion1;

    /**
     * 经营区域2（备案产品）
     */
    @Schema(description = "经营区域2（备案产品）")
    private String operatingRegion2;

    /**
     * 销售推广名称（备案产品）
     */
    @Schema(description = "销售推广名称（备案产品）")
    private String salesPromotionName;

    /**
     * 产品类型
     */
    @Schema(description = "产品类型", example = "AGRICULTURAL")
    private String productType;

    /**
     * 产品类型描述
     */
    @Schema(description = "产品类型描述", example = "农险产品")
    private String productTypeDescription;

    /**
     * 产品状态
     */
    @Schema(description = "产品状态", example = "DRAFT")
    private String status;

    /**
     * 产品状态描述
     */
    @Schema(description = "产品状态描述", example = "草稿")
    private String statusDescription;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2024-09-12 10:30:00")
    private LocalDateTime createdAt;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private String updatedBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2024-09-12 10:30:00")
    private LocalDateTime updatedAt;

    /**
     * 文档列表
     */
    @Schema(description = "文档列表")
    private List<DocumentResponse> documents;

    /**
     * 文档数量
     */
    @Schema(description = "文档数量", example = "4")
    private Integer documentCount;

    /**
     * 检核状态
     */
    @Schema(description = "检核状态", example = "已完成")
    private String auditStatus;
}