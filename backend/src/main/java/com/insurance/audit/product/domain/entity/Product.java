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
 * 产品实体
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
@TableName("products")
@Schema(description = "产品实体")
public class Product extends BaseEntity {

    /**
     * 产品名称
     */
    @TableField("product_name")
    @Schema(description = "产品名称", example = "中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险")
    private String productName;

    /**
     * 报送类型
     */
    @TableField("report_type")
    @Schema(description = "报送类型", example = "修订产品", allowableValues = {"新产品", "修订产品"})
    private String reportType;

    /**
     * 产品性质
     */
    @TableField("product_nature")
    @Schema(description = "产品性质", example = "政策性农险")
    private String productNature;

    /**
     * 年度
     */
    @TableField("year")
    @Schema(description = "年度", example = "2022")
    private Integer year;

    /**
     * 修订类型
     */
    @TableField("revision_type")
    @Schema(description = "修订类型", example = "条款修订", allowableValues = {"条款修订", "费率修订", "条款费率修订"})
    private String revisionType;

    /**
     * 原产品名称和编号/备案编号
     */
    @TableField("original_product_name")
    @Schema(description = "原产品名称和编号/备案编号")
    private String originalProductName;

    /**
     * 开发方式/开发类型
     */
    @TableField("development_method")
    @Schema(description = "开发方式/开发类型", example = "自主开发", allowableValues = {"自主开发", "合作开发"})
    private String developmentMethod;

    /**
     * 主附险
     */
    @TableField("primary_additional")
    @Schema(description = "主附险", example = "主险", allowableValues = {"主险", "附险"})
    private String primaryAdditional;

    /**
     * 主险情况
     */
    @TableField("primary_situation")
    @Schema(description = "主险情况")
    private String primarySituation;

    /**
     * 产品类别
     */
    @TableField("product_category")
    @Schema(description = "产品类别", example = "种植险", allowableValues = {"种植险", "养殖险", "不区分"})
    private String productCategory;

    /**
     * 经营区域
     */
    @TableField("operating_region")
    @Schema(description = "经营区域", example = "西藏自治区")
    private String operatingRegion;

    /**
     * 经营范围（备案产品）
     */
    @TableField("operating_scope")
    @Schema(description = "经营范围（备案产品）")
    private String operatingScope;

    /**
     * 示范条款名称（备案产品）
     */
    @TableField("demonstration_clause_name")
    @Schema(description = "示范条款名称（备案产品）")
    private String demonstrationClauseName;

    /**
     * 经营区域1（备案产品）
     */
    @TableField("operating_region1")
    @Schema(description = "经营区域1（备案产品）")
    private String operatingRegion1;

    /**
     * 经营区域2（备案产品）
     */
    @TableField("operating_region2")
    @Schema(description = "经营区域2（备案产品）")
    private String operatingRegion2;

    /**
     * 销售推广名称（备案产品）
     */
    @TableField("sales_promotion_name")
    @Schema(description = "销售推广名称（备案产品）")
    private String salesPromotionName;

    /**
     * 产品类型
     */
    @TableField("product_type")
    @Schema(description = "产品类型", example = "AGRICULTURAL", allowableValues = {"AGRICULTURAL", "FILING"})
    private ProductType productType;

    /**
     * 产品状态
     */
    @TableField("status")
    @Schema(description = "产品状态", example = "DRAFT", allowableValues = {"DRAFT", "SUBMITTED", "APPROVED", "REJECTED"})
    private ProductStatus status;

    /**
     * 关联的文档列表
     */
    @TableField(exist = false)
    @Schema(description = "关联的文档列表")
    private List<Document> documents;

    /**
     * 产品类型枚举
     */
    public enum ProductType {
        /**
         * 农险产品
         */
        AGRICULTURAL("农险产品"),
        /**
         * 备案产品
         */
        FILING("备案产品");

        private final String description;

        ProductType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 产品状态枚举
     */
    public enum ProductStatus {
        /**
         * 草稿
         */
        DRAFT("草稿"),
        /**
         * 已提交
         */
        SUBMITTED("已提交"),
        /**
         * 已审批
         */
        APPROVED("已审批"),
        /**
         * 已拒绝
         */
        REJECTED("已拒绝");

        private final String description;

        ProductStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}