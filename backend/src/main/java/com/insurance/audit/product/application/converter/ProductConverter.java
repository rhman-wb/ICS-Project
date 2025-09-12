package com.insurance.audit.product.application.converter;

import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.interfaces.dto.request.CreateProductRequest;
import com.insurance.audit.product.interfaces.dto.request.UpdateProductRequest;
import com.insurance.audit.product.interfaces.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品转换器
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Component
public class ProductConverter {

    /**
     * 将创建请求转换为实体
     * 
     * @param request 创建请求
     * @return 产品实体
     */
    public Product toEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .productName(request.getProductName())
                .reportType(request.getReportType())
                .productNature(request.getProductNature())
                .year(request.getYear())
                .revisionType(request.getRevisionType())
                .originalProductName(request.getOriginalProductName())
                .developmentMethod(request.getDevelopmentMethod())
                .primaryAdditional(request.getPrimaryAdditional())
                .primarySituation(request.getPrimarySituation())
                .productCategory(request.getProductCategory())
                .operatingRegion(request.getOperatingRegion())
                .operatingScope(request.getOperatingScope())
                .demonstrationClauseName(request.getDemonstrationClauseName())
                .operatingRegion1(request.getOperatingRegion1())
                .operatingRegion2(request.getOperatingRegion2())
                .salesPromotionName(request.getSalesPromotionName())
                .productType(Product.ProductType.valueOf(request.getProductType()))
                .status(Product.ProductStatus.DRAFT)
                .build();
    }

    /**
     * 将更新请求转换为实体
     * 
     * @param existing 现有实体
     * @param request 更新请求
     * @return 更新后的产品实体
     */
    public Product toEntity(Product existing, UpdateProductRequest request) {
        if (existing == null || request == null) {
            return existing;
        }

        Product.ProductBuilder builder = existing.toBuilder();

        if (request.getProductName() != null) {
            builder.productName(request.getProductName());
        }
        if (request.getReportType() != null) {
            builder.reportType(request.getReportType());
        }
        if (request.getProductNature() != null) {
            builder.productNature(request.getProductNature());
        }
        if (request.getYear() != null) {
            builder.year(request.getYear());
        }
        if (request.getRevisionType() != null) {
            builder.revisionType(request.getRevisionType());
        }
        if (request.getOriginalProductName() != null) {
            builder.originalProductName(request.getOriginalProductName());
        }
        if (request.getDevelopmentMethod() != null) {
            builder.developmentMethod(request.getDevelopmentMethod());
        }
        if (request.getPrimaryAdditional() != null) {
            builder.primaryAdditional(request.getPrimaryAdditional());
        }
        if (request.getPrimarySituation() != null) {
            builder.primarySituation(request.getPrimarySituation());
        }
        if (request.getProductCategory() != null) {
            builder.productCategory(request.getProductCategory());
        }
        if (request.getOperatingRegion() != null) {
            builder.operatingRegion(request.getOperatingRegion());
        }
        if (request.getOperatingScope() != null) {
            builder.operatingScope(request.getOperatingScope());
        }
        if (request.getDemonstrationClauseName() != null) {
            builder.demonstrationClauseName(request.getDemonstrationClauseName());
        }
        if (request.getOperatingRegion1() != null) {
            builder.operatingRegion1(request.getOperatingRegion1());
        }
        if (request.getOperatingRegion2() != null) {
            builder.operatingRegion2(request.getOperatingRegion2());
        }
        if (request.getSalesPromotionName() != null) {
            builder.salesPromotionName(request.getSalesPromotionName());
        }
        if (request.getStatus() != null) {
            builder.status(Product.ProductStatus.valueOf(request.getStatus()));
        }

        return builder.build();
    }

    /**
     * 将实体转换为响应DTO
     * 
     * @param entity 产品实体
     * @return 产品响应DTO
     */
    public ProductResponse toResponse(Product entity) {
        if (entity == null) {
            return null;
        }

        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(entity.getId())
                .productName(entity.getProductName())
                .reportType(entity.getReportType())
                .productNature(entity.getProductNature())
                .year(entity.getYear())
                .revisionType(entity.getRevisionType())
                .originalProductName(entity.getOriginalProductName())
                .developmentMethod(entity.getDevelopmentMethod())
                .primaryAdditional(entity.getPrimaryAdditional())
                .primarySituation(entity.getPrimarySituation())
                .productCategory(entity.getProductCategory())
                .operatingRegion(entity.getOperatingRegion())
                .operatingScope(entity.getOperatingScope())
                .demonstrationClauseName(entity.getDemonstrationClauseName())
                .operatingRegion1(entity.getOperatingRegion1())
                .operatingRegion2(entity.getOperatingRegion2())
                .salesPromotionName(entity.getSalesPromotionName())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getProductType() != null) {
            builder.productType(entity.getProductType().name())
                    .productTypeDescription(entity.getProductType().getDescription());
        }

        if (entity.getStatus() != null) {
            builder.status(entity.getStatus().name())
                    .statusDescription(entity.getStatus().getDescription());
        }

        // 设置文档数量
        if (entity.getDocuments() != null) {
            builder.documentCount(entity.getDocuments().size());
        } else {
            builder.documentCount(0);
        }

        // 设置检核状态（根据文档的审核状态来确定）
        builder.auditStatus(determineAuditStatus(entity));

        return builder.build();
    }

    /**
     * 将实体列表转换为响应DTO列表
     * 
     * @param entities 产品实体列表
     * @return 产品响应DTO列表
     */
    public List<ProductResponse> toResponseList(List<Product> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据产品的文档审核状态确定整体审核状态
     * 
     * @param product 产品实体
     * @return 审核状态描述
     */
    private String determineAuditStatus(Product product) {
        if (product.getDocuments() == null || product.getDocuments().isEmpty()) {
            return "未上传文档";
        }

        boolean hasCompleted = false;
        boolean hasPending = false;
        boolean hasProcessing = false;

        for (var document : product.getDocuments()) {
            switch (document.getAuditStatus()) {
                case COMPLETED:
                    hasCompleted = true;
                    break;
                case PENDING:
                    hasPending = true;
                    break;
                case PROCESSING:
                    hasProcessing = true;
                    break;
                case FAILED:
                    return "审核失败";
                default:
                    break;
            }
        }

        if (hasProcessing) {
            return "审核中";
        }
        if (hasPending) {
            return "待审核";
        }
        if (hasCompleted) {
            return "已完成";
        }

        return "未开始";
    }
}