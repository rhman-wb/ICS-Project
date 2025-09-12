package com.insurance.audit.product.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品查询请求DTO
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "产品查询请求")
public class ProductQueryRequest {

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    private Integer size = 10;

    /**
     * 文件名关键字
     */
    @Schema(description = "文件名关键字")
    private String fileName;

    /**
     * 报送类型
     */
    @Schema(description = "报送类型", allowableValues = {"新产品", "修订产品"})
    private String reportType;

    /**
     * 开发类型
     */
    @Schema(description = "开发类型", allowableValues = {"自主开发", "合作开发"})
    private String developmentType;

    /**
     * 产品类别
     */
    @Schema(description = "产品类别", allowableValues = {"种植险", "养殖险", "不区分"})
    private String productCategory;

    /**
     * 主附险
     */
    @Schema(description = "主附险", allowableValues = {"主险", "附险"})
    private String primaryAdditional;

    /**
     * 修订类型
     */
    @Schema(description = "修订类型", allowableValues = {"条款修订", "费率修订", "条款费率修订"})
    private String revisionType;

    /**
     * 经营区域
     */
    @Schema(description = "经营区域")
    private String operatingRegion;

    /**
     * 年度
     */
    @Schema(description = "年度")
    private String year;

    /**
     * 产品类型
     */
    @Schema(description = "产品类型", allowableValues = {"AGRICULTURAL", "FILING"})
    private String productType;

    /**
     * 产品状态
     */
    @Schema(description = "产品状态", allowableValues = {"DRAFT", "SUBMITTED", "APPROVED", "REJECTED"})
    private String status;
}