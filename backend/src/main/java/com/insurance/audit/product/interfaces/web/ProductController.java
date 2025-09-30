package com.insurance.audit.product.interfaces.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.common.dto.PageResponse;
import com.insurance.audit.product.application.converter.ProductConverter;
import com.insurance.audit.product.application.service.DocumentParsingService;
import com.insurance.audit.product.application.service.FieldValidationService;
import com.insurance.audit.product.application.service.ProductService;
import com.insurance.audit.product.application.service.TemplateService;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.domain.entity.ProductTemplate;
import com.insurance.audit.product.interfaces.dto.TemplateFieldConfig;
import com.insurance.audit.product.interfaces.dto.ValidationRule;
import com.insurance.audit.product.interfaces.dto.request.CreateProductRequest;
import com.insurance.audit.product.interfaces.dto.request.FieldValidationRequest;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;
import com.insurance.audit.product.interfaces.dto.response.DocumentParseResult;
import com.insurance.audit.product.interfaces.dto.response.FieldValidationResultResponse;
import com.insurance.audit.product.interfaces.dto.response.FieldValidationRuleResponse;
import com.insurance.audit.product.interfaces.dto.response.ProductResponse;
import com.insurance.audit.product.interfaces.dto.response.TemplateConfigResponse;
import com.insurance.audit.product.interfaces.dto.response.TemplateInfoResponse;
import com.insurance.audit.product.interfaces.dto.response.TemplateParseResultResponse;
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
    private final TemplateService templateService;
    private final FieldValidationService fieldValidationService;
    private final DocumentParsingService documentParsingService;

    // ==================== 产品列表和查询功能====================

    @GetMapping
    @Operation(summary = "获取产品列表", description = "根据条件分页查询产品列表")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW','product:view') or hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('PRODUCT_CREATE','product:create') or hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('PRODUCT_SUBMIT','product:submit') or hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW','product:view') or hasRole('ADMIN')")
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

    // ==================== 模板管理功能 ====================

    @GetMapping("/templates")
    @Operation(summary = "获取可用模板列表", description = "获取所有启用的产品模板列表")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW','product:view') or hasRole('ADMIN')")
    public ApiResponse<List<TemplateInfoResponse>> getAvailableTemplates() {
        log.info("获取可用模板列表");

        try {
            List<ProductTemplate> templates = templateService.getEnabledTemplates();
            List<TemplateInfoResponse> responses = templates.stream()
                    .map(this::convertToTemplateInfoResponse)
                    .toList();

            return ApiResponse.success(responses, "获取模板列表成功");

        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return ApiResponse.error("获取模板列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{templateType}")
    @Operation(summary = "获取模板配置", description = "根据模板类型获取完整的模板配置信息")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW','product:view') or hasRole('ADMIN')")
    public ApiResponse<TemplateConfigResponse> getTemplateConfig(
            @Parameter(description = "模板类型 (FILING/AGRICULTURAL)", required = true)
            @PathVariable("templateType") String templateType) {

        log.info("获取模板配置, templateType: {}", templateType);

        try {
            // 验证模板类型
            if (!templateService.isValidTemplateType(templateType)) {
                return ApiResponse.error("无效的模板类型: " + templateType);
            }

            // 获取模板配置
            ProductTemplate template = templateService.getTemplateConfig(templateType);
            if (template == null) {
                return ApiResponse.error("模板不存在: " + templateType);
            }

            // 获取字段配置和验证规则
            List<TemplateFieldConfig> fields = templateService.getTemplateFields(templateType);
            List<ValidationRule> validationRules = templateService.getValidationRules(templateType);

            // 构建响应
            TemplateConfigResponse response = TemplateConfigResponse.builder()
                    .templateInfo(convertToTemplateInfoResponse(template))
                    .fields(fields)
                    .validationRules(convertToFieldValidationRuleResponses(validationRules))
                    .build();

            return ApiResponse.success(response, "获取模板配置成功");

        } catch (Exception e) {
            log.error("获取模板配置失败, templateType: {}", templateType, e);
            return ApiResponse.error("获取模板配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{templateType}/download")
    @Operation(summary = "下载模板文件", description = "根据模板类型下载对应的Excel模板文件")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW','product:view') or hasRole('ADMIN')")
    public ApiResponse<Map<String, String>> downloadTemplate(
            @Parameter(description = "模板类型 (FILING/AGRICULTURAL)", required = true)
            @PathVariable("templateType") String templateType) {

        log.info("下载模板文件, templateType: {}", templateType);

        try {
            // 验证模板类型
            if (!templateService.isValidTemplateType(templateType)) {
                return ApiResponse.error("无效的模板类型: " + templateType);
            }

            // 获取模板元数据
            ProductTemplate template = templateService.getTemplateMetadata(templateType);
            if (template == null || template.getFilePath() == null) {
                return ApiResponse.error("模板文件不存在");
            }

            // 构建下载信息
            Map<String, String> downloadInfo = new HashMap<>();
            downloadInfo.put("templateName", template.getTemplateName());
            downloadInfo.put("fileName", getFileNameFromPath(template.getFilePath()));
            downloadInfo.put("filePath", template.getFilePath());
            downloadInfo.put("version", template.getVersion());
            downloadInfo.put("downloadUrl", "/v1/products/templates/" + templateType + "/file");

            return ApiResponse.success(downloadInfo, "获取模板下载信息成功");

        } catch (Exception e) {
            log.error("获取模板下载信息失败, templateType: {}", templateType, e);
            return ApiResponse.error("获取模板下载信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{templateType}/fields")
    @Operation(summary = "获取模板字段配置", description = "获取指定模板的所有字段配置信息")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW','product:view') or hasRole('ADMIN')")
    public ApiResponse<List<TemplateFieldConfig>> getTemplateFields(
            @Parameter(description = "模板类型 (FILING/AGRICULTURAL)", required = true)
            @PathVariable("templateType") String templateType) {

        log.info("获取模板字段配置, templateType: {}", templateType);

        try {
            // 验证模板类型
            if (!templateService.isValidTemplateType(templateType)) {
                return ApiResponse.error("无效的模板类型: " + templateType);
            }

            List<TemplateFieldConfig> fields = templateService.getTemplateFields(templateType);
            return ApiResponse.success(fields, "获取字段配置成功");

        } catch (Exception e) {
            log.error("获取字段配置失败, templateType: {}", templateType, e);
            return ApiResponse.error("获取字段配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{templateType}/validation-rules")
    @Operation(summary = "获取模板验证规则", description = "获取指定模板的所有字段验证规则")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW','product:view') or hasRole('ADMIN')")
    public ApiResponse<List<FieldValidationRuleResponse>> getTemplateValidationRules(
            @Parameter(description = "模板类型 (FILING/AGRICULTURAL)", required = true)
            @PathVariable("templateType") String templateType) {

        log.info("获取模板验证规则, templateType: {}", templateType);

        try {
            // 验证模板类型
            if (!templateService.isValidTemplateType(templateType)) {
                return ApiResponse.error("无效的模板类型: " + templateType);
            }

            List<ValidationRule> rules = templateService.getValidationRules(templateType);
            List<FieldValidationRuleResponse> responses = convertToFieldValidationRuleResponses(rules);

            return ApiResponse.success(responses, "获取验证规则成功");

        } catch (Exception e) {
            log.error("获取验证规则失败, templateType: {}", templateType, e);
            return ApiResponse.error("获取验证规则失败: " + e.getMessage());
        }
    }

    // ==================== 字段验证功能 ====================

    @PostMapping("/validate-fields")
    @Operation(summary = "验证产品字段", description = "根据模板规则验证产品字段数据")
    @PreAuthorize("hasAnyAuthority('PRODUCT_CREATE','product:create','PRODUCT_EDIT','product:edit') or hasRole('ADMIN')")
    public ApiResponse<FieldValidationResultResponse> validateFields(
            @Valid @RequestBody FieldValidationRequest request) {

        log.info("验证产品字段, templateType: {}, fieldCount: {}",
                request.getTemplateType(), request.getFieldData().size());

        try {
            // 验证模板类型
            if (!templateService.isValidTemplateType(request.getTemplateType())) {
                return ApiResponse.error("无效的模板类型: " + request.getTemplateType());
            }

            // 执行字段验证
            FieldValidationService.FieldValidationResult validationResult =
                    fieldValidationService.validateFields(request.getFieldData(), request.getTemplateType());

            // 转换为响应DTO
            FieldValidationResultResponse response = convertToFieldValidationResultResponse(validationResult);

            return ApiResponse.success(response,
                    validationResult.isValid() ? "字段验证通过" : "字段验证失败");

        } catch (Exception e) {
            log.error("字段验证失败", e);
            return ApiResponse.error("字段验证失败: " + e.getMessage());
        }
    }

    // ==================== 模板解析功能 ====================

    @PostMapping("/parse-template")
    @Operation(summary = "解析模板文件", description = "上传并解析Excel模板文件，提取产品信息")
    @PreAuthorize("hasAnyAuthority('PRODUCT_CREATE','product:create') or hasRole('ADMIN')")
    public ApiResponse<TemplateParseResultResponse> parseTemplate(
            @Parameter(description = "模板类型", required = true)
            @RequestParam("templateType") String templateType,
            @Parameter(description = "模板文件", required = true)
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @Parameter(description = "是否验证数据")
            @RequestParam(value = "validateData", defaultValue = "true") Boolean validateData) {

        log.info("解析模板文件, templateType: {}, fileName: {}", templateType, file.getOriginalFilename());

        try {
            // 验证模板类型
            if (!templateService.isValidTemplateType(templateType)) {
                return ApiResponse.error("无效的模板类型: " + templateType);
            }

            // 验证文件格式
            if (!documentParsingService.isSupportedFormat(file)) {
                return ApiResponse.error("不支持的文件格式，仅支持: " +
                        String.join(", ", documentParsingService.getSupportedFormats()));
            }

            // 解析文件
            DocumentParseResult parseResult = documentParsingService.parseProductRegistrationForm(file);

            // 如果需要验证，则执行字段验证
            FieldValidationResultResponse validationResult = null;
            if (validateData && parseResult.isSuccess()) {
                FieldValidationService.FieldValidationResult fieldValidation =
                        fieldValidationService.validateFields(parseResult.getParsedData(), templateType);
                validationResult = convertToFieldValidationResultResponse(fieldValidation);
            }

            // 构建响应
            TemplateParseResultResponse response = TemplateParseResultResponse.builder()
                    .success(parseResult.isSuccess())
                    .message(parseResult.getMessage())
                    .parsedData(parseResult.getParsedData())
                    .parsedFieldCount(parseResult.getParsedData() != null ? parseResult.getParsedData().size() : 0)
                    .validationResult(validationResult)
                    .build();

            return ApiResponse.success(response, "模板解析完成");

        } catch (Exception e) {
            log.error("模板解析失败", e);
            return ApiResponse.error("模板解析失败: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private TemplateInfoResponse convertToTemplateInfoResponse(ProductTemplate template) {
        return TemplateInfoResponse.builder()
                .id(template.getId())
                .templateType(template.getTemplateType())
                .templateName(template.getTemplateName())
                .description(template.getDescription())
                .filePath(template.getFilePath())
                .fileName(getFileNameFromPath(template.getFilePath()))
                .version(template.getVersion())
                .enabled(template.getEnabled())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private List<FieldValidationRuleResponse> convertToFieldValidationRuleResponses(List<ValidationRule> rules) {
        return rules.stream()
                .map(rule -> FieldValidationRuleResponse.builder()
                        .fieldName(rule.getFieldName())
                        .ruleType(rule.getRuleType())
                        .parameters(rule.getParameters())
                        .errorMessage(rule.getErrorMessage())
                        .required(rule.isRequired())
                        .build())
                .toList();
    }

    private FieldValidationResultResponse convertToFieldValidationResultResponse(
            FieldValidationService.FieldValidationResult validationResult) {

        List<FieldValidationResultResponse.FieldValidationDetail> details =
                validationResult.getErrors().stream()
                .map(error -> FieldValidationResultResponse.FieldValidationDetail.builder()
                        .fieldName(error.getFieldName())
                        .valid(false)
                        .value(error.getRejectedValue())
                        .errorMessage(error.getMessage())
                        .build())
                .toList();

        return FieldValidationResultResponse.builder()
                .valid(validationResult.isValid())
                .message(validationResult.isValid() ? "所有字段验证通过" : "部分字段验证失败")
                .validatedFieldCount(validationResult.getValidatedFields() != null ?
                        validationResult.getValidatedFields().size() : 0)
                .passedFieldCount(validationResult.getValidatedFields() != null ?
                        validationResult.getValidatedFields().size() - validationResult.getErrors().size() : 0)
                .failedFieldCount(validationResult.getErrors() != null ? validationResult.getErrors().size() : 0)
                .fieldValidations(details)
                .build();
    }

    private String getFileNameFromPath(String filePath) {
        if (filePath == null) {
            return null;
        }
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
}