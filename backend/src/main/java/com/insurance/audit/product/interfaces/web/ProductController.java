package com.insurance.audit.product.interfaces.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.common.dto.PageResponse;
import com.insurance.audit.product.application.converter.ProductConverter;
import com.insurance.audit.product.application.service.ProductService;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;
import com.insurance.audit.product.interfaces.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;

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
            @Parameter(description = "排序方向", allowableValues = {"ASC", "DESC"})
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
}