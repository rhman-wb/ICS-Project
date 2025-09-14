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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/v1/documents")
@RequiredArgsConstructor
@Validated
@Tag(name = "文档管理", description = "文档上传、解析和管理相关API")
public class DocumentController {

    private final FileStorageService fileStorageService;
    private final DocumentParsingService documentParsingService;

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "TERMS", "FEASIBILITY_REPORT", "ACTUARIAL_REPORT", "RATE_TABLE", "REGISTRATION", "REGISTRATION_FORM"
    );

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/msword", // .doc
            "application/pdf" // .pdf
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".xlsx", ".xls", ".docx", ".doc", ".pdf");

    private static final Map<String, String> CONTENT_TYPE_TO_FILETYPE;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("application/pdf", "PDF");
        map.put("application/msword", "DOC");
        map.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "DOCX");
        map.put("application/vnd.ms-excel", "XLS");
        map.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "XLSX");
        CONTENT_TYPE_TO_FILETYPE = Collections.unmodifiableMap(map);
    }

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

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

        log.info("上传文档: 文件={} 类型={} 产品ID={}", file != null ? file.getOriginalFilename() : null, documentType, productId);

        try {
            ApiResponse<String> validationResult = validateUploadRequest(file, documentType, productId);
            if (!validationResult.getSuccess()) {
                return ApiResponse.error(validationResult.getMessage());
            }

            String safeProductId = normalizeProductId(productId);
            String subPath = "documents/" + safeProductId;
            String filePath = fileStorageService.storeFile(file, subPath);

            String normalizedDocType = normalizeDocumentType(documentType);
            String fileTypeLabel = guessFileType(file);
            DocumentResponse response = DocumentResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .fileType(fileTypeLabel)
                    .documentType(normalizedDocType)
                    .productId(safeProductId)
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

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "解析产品信息登记表", description = "解析Excel格式的产品信息登记表，提取产品基本信息")
    @PreAuthorize("hasAuthority('document:parse')")
    public ApiResponse<DocumentParseResult> parseDocument(
            @Parameter(description = "Excel文件", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("解析产品信息登记表: {}", file != null ? file.getOriginalFilename() : null);
        try {
            if (file == null || file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                return ApiResponse.error("文件大小不能超过50MB");
            }

            DocumentParseResult result = documentParsingService.parseProductRegistrationForm(file);
            if (Boolean.TRUE.equals(result.getSuccess())) {
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

    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "批量上传文档", description = "批量上传多个文档文件")
    @PreAuthorize("hasAuthority('document:upload')")
    public ApiResponse<List<DocumentResponse>> batchUploadDocuments(
            @Parameter(description = "文档文件数组", required = true)
            @RequestParam("files") MultipartFile[] files,

            @Parameter(description = "产品ID", required = true)
            @RequestParam("productId") String productId) {

        if (files == null || files.length == 0) {
            return ApiResponse.error("请选择要上传的文件");
        }
        if (files.length > 10) {
            return ApiResponse.error("单次最多只能上传10个文件");
        }

        try {
            String safeProductId = normalizeProductId(productId);
            List<DocumentResponse> results = new ArrayList<>();
            for (MultipartFile file : files) {
                ApiResponse<String> vr = validateUploadRequest(file, null, safeProductId);
                if (!vr.getSuccess()) {
                    return ApiResponse.error(vr.getMessage());
                }
                String subPath = "documents/" + safeProductId;
                String filePath = fileStorageService.storeFile(file, subPath);
                String fileTypeLabel = guessFileType(file);
                DocumentResponse resp = DocumentResponse.builder()
                        .fileName(file.getOriginalFilename())
                        .filePath(filePath)
                        .fileSize(file.getSize())
                        .fileType(fileTypeLabel)
                        .documentType(null)
                        .productId(safeProductId)
                        .uploadStatus("UPLOADED")
                        .uploadStatusDescription("已上传")
                        .auditStatus("PENDING")
                        .auditStatusDescription("待审核")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                results.add(resp);
            }
            return ApiResponse.success(results, "批量上传成功");
        } catch (Exception e) {
            log.error("批量上传异常", e);
            return ApiResponse.error("批量上传异常: " + e.getMessage());
        }
    }

    @GetMapping("/supported-formats")
    @Operation(summary = "获取支持的文件格式", description = "获取系统支持的文档格式和类型信息")
    public ApiResponse<SupportedFormats> getSupportedFormats() {
        List<String> parseFormats = Arrays.asList(documentParsingService.getSupportedFormats());
        SupportedFormats formats = SupportedFormats.builder()
                .documentTypes(Arrays.asList("TERMS", "FEASIBILITY_REPORT", "ACTUARIAL_REPORT", "RATE_TABLE", "REGISTRATION_FORM"))
                .fileTypes(ALLOWED_FILE_TYPES)
                .allowedUploadTypes(ALLOWED_EXTENSIONS)
                .maxFileSize(MAX_FILE_SIZE)
                .parseFormats(parseFormats)
                .parsableFormats(parseFormats)
                .build();
        return ApiResponse.success(formats, "获取支持格式成功");
    }

    private ApiResponse<String> validateUploadRequest(MultipartFile file, String documentType, String productId) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ApiResponse.error("文件大小不能超过50MB");
        }

        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename();
        if (contentType != null) {
            if (!ALLOWED_FILE_TYPES.contains(contentType)) {
                return ApiResponse.error("不支持的文件类型: " + contentType);
            }
        } else if (originalName != null) {
            String lower = originalName.toLowerCase();
            boolean ok = ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
            if (!ok) {
                return ApiResponse.error("不支持的文件扩展名: " + originalName);
            }
        }

        if (documentType != null) {
            String normalized = normalizeDocumentType(documentType);
            if (!ALLOWED_DOCUMENT_TYPES.contains(normalized)) {
                return ApiResponse.error("不支持的文档类型: " + documentType);
            }
        }

        if (productId == null || productId.trim().isEmpty()) {
            return ApiResponse.error("产品ID不能为空");
        }
        if (!Pattern.matches("^[A-Za-z0-9_-]+$", productId)) {
            return ApiResponse.error("产品ID格式不合法");
        }

        return ApiResponse.success("验证通过");
    }

    private String normalizeProductId(String productId) {
        if (productId == null) return "";
        return productId.replaceAll("[^A-Za-z0-9_-]", "");
    }

    private String normalizeDocumentType(String docType) {
        if (docType == null) return null;
        String upper = docType.trim().toUpperCase();
        if ("REGISTRATION_FORM".equals(upper) || "REGISTRATION".equals(upper)) {
            return "REGISTRATION_FORM";
        }
        return upper;
    }

    private String guessFileType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct != null && CONTENT_TYPE_TO_FILETYPE.containsKey(ct)) {
            return CONTENT_TYPE_TO_FILETYPE.get(ct);
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.endsWith(".pdf")) return "PDF";
            if (lower.endsWith(".docx")) return "DOCX";
            if (lower.endsWith(".doc")) return "DOC";
            if (lower.endsWith(".xlsx")) return "XLSX";
            if (lower.endsWith(".xls")) return "XLS";
        }
        return "UNKNOWN";
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SupportedFormats {
        private List<String> documentTypes;
        private List<String> fileTypes;
        private List<String> allowedUploadTypes;
        private Long maxFileSize;
        private List<String> parseFormats;
        private List<String> parsableFormats;
    }
}

