package com.insurance.audit.product.interfaces.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.common.dto.PageResponse;
import com.insurance.audit.product.application.converter.ProductConverter;
import com.insurance.audit.product.application.service.ProductService;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.interfaces.dto.request.CreateProductRequest;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;
import com.insurance.audit.product.interfaces.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品管理控制器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Slf4j
@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "产品管理", description = "产品管理相关API")
public class ProductController {

    private final ProductService productService;
    private final ProductConverter productConverter;

    // ==================== 产品列表和查询功能 ====================

    @GetMapping
    @Operation(summary = "获取产品列表", description = "根据条件分页查询产品列表")
    @PreAuthorize("hasAuthority('product:view')")
    public ApiResponse<PageResponse<ProductResponse>> getProductList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "文件名关键字")
            @RequestParam(value = "fileName", required = false) String fileName,
            @Parameter(description = "报送类型")
            @RequestParam(value = "reportType", required = false) String reportType,
            @Parameter(description = "开发类型")
            @RequestParam(value = "developmentType", required = false) String developmentType,
            @Parameter(description = "产品类别")
            @RequestParam(value = "productCategory", required = false) String productCategory,
            @Parameter(description = "主附险")
            @RequestParam(value = "primaryAdditional", required = false) String primaryAdditional,
            @Parameter(description = "修订类型")
            @RequestParam(value = "revisionType", required = false) String revisionType,
            @Parameter(description = "经营区域")
            @RequestParam(value = "operatingRegion", required = false) String operatingRegion,
            @Parameter(description = "年度")
            @RequestParam(value = "year", required = false) Integer year,
            @Parameter(description = "产品类型")
            @RequestParam(value = "productType", required = false) String productType,
            @Parameter(description = "产品状态")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "排序字段")
            @RequestParam(value = "sortField", required = false) String sortField,
            @Parameter(description = "排序方向 (ASC, DESC)")
            @RequestParam(value = "sortOrder", required = false) String sortOrder) {

        log.info("Getting product list with filters - page: {}, size: {}, fileName: {}", page, size, fileName);

        try {
            // 构建查询请求
            ProductQueryRequest queryRequest = ProductQueryRequest.builder()
                    .page(page)
                    .size(size)
                    .fileName(fileName)
                    .reportType(reportType)
                    .developmentType(developmentType)
                    .productCategory(productCategory)
                    .primaryAdditional(primaryAdditional)
                    .revisionType(revisionType)
                    .operatingRegion(operatingRegion)
                    .year(year)
                    .productType(productType)
                    .status(status)
                    .sortField(sortField)
                    .sortOrder(sortOrder)
                    .build();

            // 执行查询
            IPage<Product> productPage = productService.getProductPage(queryRequest);

            // 转换为响应DTO
            List<ProductResponse> productResponseList = productConverter.toResponseList(productPage.getRecords());

            // 构建分页响应
            PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                    .records(productResponseList)
                    .total(productPage.getTotal())
                    .current((int) productPage.getCurrent())
                    .size((int) productPage.getSize())
                    .pages((int) productPage.getPages())
                    .build();

            return ApiResponse.success(pageResponse, "查询产品列表成功");

        } catch (Exception e) {
            log.error("查询产品列表失败", e);
            return ApiResponse.error("查询产品列表失败: " + e.getMessage());
        }
    }

    // ==================== 产品创建功能 ====================

    @PostMapping
    @Operation(summary = "创建产品", description = "创建新的产品记录")
    @PreAuthorize("hasAuthority('product:create')")
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        log.info("Creating product with name: {}, type: {}", request.getProductName(), request.getProductType());

        try {
            // 转换请求为实体
            Product product = productConverter.toEntity(request);

            // 创建产品
            Product createdProduct = productService.createProduct(product);

            // 转换为响应DTO
            ProductResponse response = productConverter.toResponse(createdProduct);

            return ApiResponse.success(response, "产品创建成功");

        } catch (Exception e) {
            log.error("产品创建失败", e);
            return ApiResponse.error("产品创建失败: " + e.getMessage());
        }
    }

    // ==================== 产品提交功能 ====================

    @PutMapping("/{productId}/submit")
    @Operation(summary = "提交产品", description = "将产品提交进行审核")
    @PreAuthorize("hasAuthority('product:submit')")
    public ApiResponse<ProductResponse> submitProduct(
            @Parameter(description = "产品ID", required = true)
            @PathVariable("productId") String productId) {

        log.info("提交产品进行审核, productId: {}", productId);

        try {
            // 提交产品
            Product submittedProduct = productService.submitProduct(productId);

            // 转换为响应DTO
            ProductResponse response = productConverter.toResponse(submittedProduct);

            return ApiResponse.success(response, "产品提交成功");

        } catch (Exception e) {
            log.error("产品提交失败, productId: {}", productId, e);
            return ApiResponse.error("产品提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/{productId}")
    @Operation(summary = "获取产品详情", description = "根据ID获取产品详细信息")
    @PreAuthorize("hasAuthority('product:view')")
    public ApiResponse<ProductResponse> getProductDetail(
            @Parameter(description = "产品ID", required = true)
            @PathVariable("productId") String productId) {

        log.info("获取产品详情, productId: {}", productId);

        try {
            // 获取产品详情
            Product product = productService.getProductById(productId);

            if (product == null) {
                return ApiResponse.error("产品不存在");
            }

            // 转换为响应DTO
            ProductResponse response = productConverter.toResponse(product);

            return ApiResponse.success(response, "获取产品详情成功");

        } catch (Exception e) {
            log.error("获取产品详情失败, productId: {}", productId, e);
            return ApiResponse.error("获取产品详情失败: " + e.getMessage());
        }
    }
}