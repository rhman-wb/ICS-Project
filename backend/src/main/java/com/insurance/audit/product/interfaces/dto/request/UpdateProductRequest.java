package com.insurance.audit.product.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

/**
 * 产品更新请求DTO
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "产品更新请求")
public class UpdateProductRequest {

    /**
     * 产品名称
     */
    @Size(max = 200, message = "产品名称长度不能超过200个字符")
    @Schema(description = "产品名称", example = "中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险")
    private String productName;

    /**
     * 报送类型
     */
    @Schema(description = "报送类型", example = "修订产品", allowableValues = {"新产品", "修订产品"})
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
    @Schema(description = "修订类型", example = "条款修订", allowableValues = {"条款修订", "费率修订", "条款费率修订"})
    private String revisionType;

    /**
     * 原产品名称和编号/备案编号
     */
    @Schema(description = "原产品名称和编号/备案编号")
    private String originalProductName;

    /**
     * 开发方式/开发类型
     */
    @Schema(description = "开发方式/开发类型", example = "自主开发", allowableValues = {"自主开发", "合作开发"})
    private String developmentMethod;

    /**
     * 主附险
     */
    @Schema(description = "主附险", example = "主险", allowableValues = {"主险", "附险"})
    private String primaryAdditional;

    /**
     * 主险情况
     */
    @Schema(description = "主险情况")
    private String primarySituation;

    /**
     * 产品类别
     */
    @Schema(description = "产品类别", example = "种植险", allowableValues = {"种植险", "养殖险", "不区分"})
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
     * 产品状态
     */
    @Schema(description = "产品状态", example = "DRAFT", allowableValues = {"DRAFT", "SUBMITTED", "APPROVED", "REJECTED"})
    private String status;
}