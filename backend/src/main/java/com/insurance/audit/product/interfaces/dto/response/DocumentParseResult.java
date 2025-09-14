package com.insurance.audit.product.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文档解析结果响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档解析结果响应")
public class DocumentParseResult {

    /**
     * 解析是否成功
     */
    @Schema(description = "解析是否成功", example = "true")
    private Boolean success;

    /**
     * 错误信息（失败时）
     */
    @Schema(description = "错误信息", example = "文件格式不支持")
    private String errorMessage;

    /**
     * 解析时间
     */
    @Schema(description = "解析时间")
    private LocalDateTime parseTime;

    /**
     * 文件信息
     */
    @Schema(description = "文件信息")
    private FileInfo fileInfo;

    /**
     * 解析出的产品基本信息
     */
    @Schema(description = "解析出的产品基本信息")
    private ProductBasicInfo productInfo;

    /**
     * 解析出的原始数据（键值对形式）
     */
    @Schema(description = "解析出的原始数据")
    private Map<String, Object> rawData;

    /**
     * 解析警告信息
     */
    @Schema(description = "解析警告信息")
    private List<String> warnings;

    /**
     * 文件信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "文件信息")
    public static class FileInfo {

        /**
         * 文件名
         */
        @Schema(description = "文件名", example = "产品信息登记表.xlsx")
        private String fileName;

        /**
         * 文件大小（字节）
         */
        @Schema(description = "文件大小", example = "51200")
        private Long fileSize;

        /**
         * 文件类型
         */
        @Schema(description = "文件类型", example = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        private String contentType;

        /**
         * 工作表数量
         */
        @Schema(description = "工作表数量", example = "3")
        private Integer sheetCount;

        /**
         * 数据行数
         */
        @Schema(description = "数据行数", example = "25")
        private Integer dataRowCount;
    }

    /**
     * 产品基本信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "产品基本信息")
    public static class ProductBasicInfo {

        /**
         * 产品名称
         */
        @Schema(description = "产品名称", example = "中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险")
        private String productName;

        /**
         * 报送类型
         */
        @Schema(description = "报送类型", example = "新产品")
        private String reportType;

        /**
         * 产品性质
         */
        @Schema(description = "产品性质", example = "政策性农险")
        private String productNature;

        /**
         * 年度
         */
        @Schema(description = "年度", example = "2024")
        private Integer year;

        /**
         * 产品类别
         */
        @Schema(description = "产品类别", example = "养殖险")
        private String productCategory;

        /**
         * 经营区域
         */
        @Schema(description = "经营区域", example = "西藏自治区")
        private String operatingRegion;

        /**
         * 开发方式
         */
        @Schema(description = "开发方式", example = "自主开发")
        private String developmentMethod;

        /**
         * 主附险
         */
        @Schema(description = "主附险", example = "主险")
        private String primaryAdditional;

        /**
         * 修订类型（修订产品）
         */
        @Schema(description = "修订类型", example = "条款修订")
        private String revisionType;

        /**
         * 原产品名称和编号（修订产品）
         */
        @Schema(description = "原产品名称和编号")
        private String originalProductName;

        /**
         * 示范条款名称（备案产品）
         */
        @Schema(description = "示范条款名称")
        private String demonstrationClauseName;

        /**
         * 经营范围（备案产品）
         */
        @Schema(description = "经营范围")
        private String operatingScope;

        /**
         * 销售推广名称（备案产品）
         */
        @Schema(description = "销售推广名称")
        private String salesPromotionName;
    }
}