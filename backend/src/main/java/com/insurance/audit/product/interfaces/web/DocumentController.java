package com.insurance.audit.product.interfaces.web;

import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.product.application.service.DocumentParsingService;
import com.insurance.audit.product.application.service.FileStorageService;
import com.insurance.audit.product.interfaces.dto.response.DocumentParseResult;
import com.insurance.audit.product.interfaces.dto.response.DocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 文档管理控制器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-14
 */
@Slf4j
@RestController
@RequestMapping("/v1/documents")
@RequiredArgsConstructor
@Validated
@Tag(name = "文档管理", description = "文档上传、解析和管理相关API")
public class DocumentController {

    private final FileStorageService fileStorageService;
    private final DocumentParsingService documentParsingService;

    // 支持的文档类型
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "TERMS", "FEASIBILITY_REPORT", "ACTUARIAL_REPORT", "RATE_TABLE", "REGISTRATION_FORM"
    );

    // 支持的文件类型
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/msword", // .doc
            "application/pdf" // .pdf
    );

    // 最大文件大小 (50MB)
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    // ==================== 文档上传功能 ====================

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文档", description = "上传产品要件文档，支持条款、可行性报告、精算报告、费率表等")
    @PreAuthorize("hasAuthority('document:upload')")
    public ApiResponse<DocumentResponse> uploadDocument(
            @Parameter(description = "文档文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "文档类型", example = "TERMS", required = true)
            @RequestParam("documentType") String documentType,

            @Parameter(description = "产品ID", required = true)
            @RequestParam("productId") String productId,

            @Parameter(description = "文档描述")
            @RequestParam(value = "description", required = false) String description) {

        log.info("上传文档: 文件名={}, 类型={}, 产品ID={}",
                file.getOriginalFilename(), documentType, productId);

        try {
            // 验证参数
            ApiResponse<String> validationResult = validateUploadRequest(file, documentType, productId);
            if (!validationResult.getSuccess()) {
                return ApiResponse.error(validationResult.getMessage());
            }

            // 存储文件
            String subPath = "documents/" + productId;
            String filePath = fileStorageService.storeFile(file, subPath);

            // 构建响应
            DocumentResponse response = DocumentResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .documentType(documentType)
                    .productId(productId)
                    .uploadStatus("UPLOADED")
                    .uploadStatusDescription("已上传")
                    .auditStatus("PENDING")
                    .auditStatusDescription("待审核")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            return ApiResponse.success(response, "文档上传成功");

        } catch (IOException e) {
            log.error("文档上传失败", e);
            return ApiResponse.error("文档上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文档上传异常", e);
            return ApiResponse.error("文档上传异常: " + e.getMessage());
        }
    }

    // ==================== 文档解析功能 ====================

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "解析产品信息登记表", description = "解析Excel格式的产品信息登记表，提取产品基本信息")
    @PreAuthorize("hasAuthority('document:parse')")
    public ApiResponse<DocumentParseResult> parseDocument(
            @Parameter(description = "Excel文件", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("解析产品信息登记表: {}", file.getOriginalFilename());

        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ApiResponse.error("文件大小不能超过50MB");
            }

            // 解析文档
            DocumentParseResult result = documentParsingService.parseProductRegistrationForm(file);

            if (result.getSuccess()) {
                return ApiResponse.success(result, "文档解析成功");
            } else {
                return ApiResponse.error(result.getErrorMessage());
            }

        } catch (IOException e) {
            log.error("文档解析失败", e);
            return ApiResponse.error("文档解析失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文档解析异常", e);
            return ApiResponse.error("文档解析异常: " + e.getMessage());
        }
    }

    // ==================== 批量上传功能 ====================

    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "批量上传文档", description = "批量上传多个文档文件")
    @PreAuthorize("hasAuthority('document:upload')")
    public ApiResponse<List<DocumentResponse>> batchUploadDocuments(
            @Parameter(description = "文档文件数组", required = true)
            @RequestParam("files") MultipartFile[] files,

            @Parameter(description = "产品ID", required = true)
            @RequestParam("productId") String productId) {

        log.info("批量上传文档: 文件数量={}, 产品ID={}", files.length, productId);

        if (files.length == 0) {
            return ApiResponse.error("请选择要上传的文件");
        }

        if (files.length > 10) {
            return ApiResponse.error("单次最多只能上传10个文件");
        }

        try {
            // TODO: 实现批量上传逻辑
            // 这里可以异步处理多个文件上传

            return ApiResponse.error("批量上传功能正在开发中");

        } catch (Exception e) {
            log.error("批量上传异常", e);
            return ApiResponse.error("批量上传异常: " + e.getMessage());
        }
    }

    // ==================== 获取支持的格式信息 ====================

    @GetMapping("/supported-formats")
    @Operation(summary = "获取支持的文件格式", description = "获取系统支持的文档格式和类型信息")
    public ApiResponse<SupportedFormats> getSupportedFormats() {

        SupportedFormats formats = SupportedFormats.builder()
                .documentTypes(ALLOWED_DOCUMENT_TYPES)
                .fileTypes(ALLOWED_FILE_TYPES)
                .maxFileSize(MAX_FILE_SIZE)
                .parseFormats(Arrays.asList(documentParsingService.getSupportedFormats()))
                .build();

        return ApiResponse.success(formats, "获取支持格式成功");
    }

    // ==================== 私有方法 ====================

    /**
     * 验证上传请求
     */
    private ApiResponse<String> validateUploadRequest(MultipartFile file, String documentType, String productId) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ApiResponse.error("文件大小不能超过50MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_FILE_TYPES.contains(contentType)) {
            return ApiResponse.error("不支持的文件类型: " + contentType);
        }

        // 验证文档类型
        if (!ALLOWED_DOCUMENT_TYPES.contains(documentType)) {
            return ApiResponse.error("不支持的文档类型: " + documentType);
        }

        // 验证产品ID
        if (productId == null || productId.trim().isEmpty()) {
            return ApiResponse.error("产品ID不能为空");
        }

        return ApiResponse.success("验证通过");
    }

    // ==================== 内部类 ====================

    /**
     * 支持格式信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SupportedFormats {
        private List<String> documentTypes;
        private List<String> fileTypes;
        private Long maxFileSize;
        private List<String> parseFormats;
    }
}