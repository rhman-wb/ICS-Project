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
     * 经营区域(农险产品通用字段)
     */
    @TableField("operating_region")
    @Schema(description = "经营区域", example = "西藏自治区")
    private String operatingRegion;

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
     * 文档数量
     */
    @TableField("document_count")
    @Schema(description = "文档数量", example = "4")
    private Integer documentCount;

    // ===============================
    // 备案产品模板新增字段
    // ===============================

    /**
     * 开发类型（备案产品）
     */
    @TableField("development_type")
    @Schema(description = "开发类型", example = "自主开发", allowableValues = {"自主开发", "使用示范条款及参考纯风险损失率", "仅使用示范条款", "联合开发"})
    private String developmentType;

    /**
     * 示范条款名称（备案产品）
     */
    @TableField("demonstration_clause")
    @Schema(description = "示范条款名称")
    private String demonstrationClause;

    /**
     * 经营范围（备案产品）
     */
    @TableField("business_scope")
    @Schema(description = "经营范围", example = "法人团体", allowableValues = {"法人团体", "机关团体", "个人", "个人/法人团体"})
    private String businessScope;

    /**
     * 经营区域1（备案产品）
     */
    @TableField("business_area1")
    @Schema(description = "经营区域1", example = "全国（不含港澳台）", allowableValues = {"全国（不含港澳台）", "全国（含港澳台）", "总公司", "省级分公司"})
    private String businessArea1;

    /**
     * 经营区域2（备案产品）
     */
    @TableField("business_area2")
    @Schema(description = "经营区域2")
    private String businessArea2;

    /**
     * 产品属性（备案产品）
     */
    @TableField("product_property")
    @Schema(description = "产品属性", example = "中长期产品", allowableValues = {"中长期产品", "一年期及一年期以下产品"})
    private String productProperty;

    /**
     * 基础费率（备案产品）
     */
    @TableField("basic_rate")
    @Schema(description = "基础费率（%）", example = "0.5")
    private String basicRate;

    /**
     * 基础费率表（备案产品）
     */
    @TableField("basic_rate_table")
    @Schema(description = "基础费率表")
    private String basicRateTable;

    /**
     * 是否有电子保单（备案产品）
     */
    @TableField("has_electronic_policy")
    @Schema(description = "是否有电子保单", example = "true")
    private Boolean hasElectronicPolicy;

    /**
     * 是否是国产加密算法（备案产品）
     */
    @TableField("has_national_encryption")
    @Schema(description = "是否是国产加密算法", example = "true")
    private Boolean hasNationalEncryption;

    /**
     * 保险期间（备案产品）
     */
    @TableField("insurance_period")
    @Schema(description = "保险期间", maxLength = 2000)
    private String insurancePeriod;

    /**
     * 保险责任（备案产品）
     */
    @TableField("insurance_responsibility")
    @Schema(description = "保险责任", maxLength = 2000)
    private String insuranceResponsibility;

    /**
     * 销售区域（备案产品）
     */
    @TableField("sales_area")
    @Schema(description = "销售区域")
    private String salesArea;

    /**
     * 销售渠道（备案产品）
     */
    @TableField("sales_channel")
    @Schema(description = "销售渠道")
    private String salesChannel;

    // ===============================
    // 农险产品模板新增字段
    // ===============================

    /**
     * 修订次数（农险产品）
     */
    @TableField("revision_count")
    @Schema(description = "修订次数", example = "1")
    private Integer revisionCount;

    /**
     * 保险标的（农险产品）
     */
    @TableField("insurance_target")
    @Schema(description = "保险标的", example = "羊")
    private String insuranceTarget;

    /**
     * 是否开办（农险产品）
     */
    @TableField("is_operated")
    @Schema(description = "是否开办", example = "true")
    private Boolean isOperated;

    /**
     * 开办日期（农险产品）
     */
    @TableField("operation_date")
    @Schema(description = "开办日期", example = "2024-01-01")
    private String operationDate;

    /**
     * 费率浮动区间（农险产品）
     */
    @TableField("rate_floating_range")
    @Schema(description = "费率浮动区间", example = "±20%")
    private String rateFloatingRange;

    /**
     * 费率浮动系数（农险产品）
     */
    @TableField("rate_floating_coefficient")
    @Schema(description = "费率浮动系数", example = "0.8-1.2")
    private String rateFloatingCoefficient;

    /**
     * 绝对免赔率（额）（农险产品）
     */
    @TableField("absolute_deductible")
    @Schema(description = "绝对免赔率（额）", example = "100元/亩")
    private String absoluteDeductible;

    /**
     * 相对免赔率（额）（农险产品）
     */
    @TableField("relative_deductible")
    @Schema(description = "相对免赔率（额）", example = "5%")
    private String relativeDeductible;

    /**
     * 备注栏（农险产品）
     */
    @TableField("remarks")
    @Schema(description = "备注栏", maxLength = 2000)
    private String remarks;

    // ===============================
    // 通用模板字段
    // ===============================

    /**
     * 模板类型标识
     */
    @TableField("template_type")
    @Schema(description = "模板类型", example = "FILING", allowableValues = {"FILING", "AGRICULTURAL"})
    private String templateType;

    /**
     * 原产品名称和编号/备案编号（修订产品）
     */
    @TableField("original_product_code")
    @Schema(description = "原产品名称和编号/备案编号")
    private String originalProductCode;

    /**
     * 是否使用示范条款
     */
    @TableField("uses_demonstration_clause")
    @Schema(description = "是否使用示范条款", example = "false")
    private Boolean usesDemonstrationClause;

    /**
     * 是否为政策性保险
     */
    @TableField("is_policy_insurance")
    @Schema(description = "是否为政策性保险", example = "true")
    private Boolean isPolicyInsurance;

    /**
     * 承保方式
     */
    @TableField("underwriting_method")
    @Schema(description = "承保方式", example = "个险")
    private String underwritingMethod;

    /**
     * 赔付方式
     */
    @TableField("claim_method")
    @Schema(description = "赔付方式", example = "定额赔付")
    private String claimMethod;

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