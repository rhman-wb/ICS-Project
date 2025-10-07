package com.insurance.audit.rules.interfaces.web;

import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.rules.application.service.RuleImportService;
import com.insurance.audit.rules.interfaces.dto.request.ImportValidationRequest;
import com.insurance.audit.rules.interfaces.dto.response.ImportResultResponse;
import com.insurance.audit.rules.interfaces.dto.response.ImportTemplateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 规则导入控制器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Slf4j
@RestController
@RequestMapping("/v1/rules/import")
@RequiredArgsConstructor
@Validated
@Tag(name = "规则导入", description = "规则批量导入相关API")
public class RuleImportController {

    private final RuleImportService ruleImportService;

    @GetMapping("/template")
    @Operation(summary = "下载导入模板", description = "下载规则导入的Excel模板文件")
    @PreAuthorize("hasAuthority('RULE_IMPORT')")
    public ResponseEntity<Resource> downloadTemplate(
            @Parameter(description = "规则类型") @RequestParam(required = false) String ruleType) {

        log.info("下载导入模板，规则类型: {}", ruleType);

        Resource templateResource = ruleImportService.generateTemplate(ruleType);

        String filename = String.format("rule_import_template_%s.xlsx",
                                       ruleType != null ? ruleType.toLowerCase() : "all");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, com.insurance.audit.common.util.HttpHeaderUtils.contentDispositionAttachment(filename))
                .body(templateResource);
    }

    @GetMapping("/template/info")
    @Operation(summary = "获取模板信息", description = "获取导入模板的字段信息和要求")
    @PreAuthorize("hasAuthority('RULE_IMPORT')")
    public ApiResponse<ImportTemplateResponse> getTemplateInfo(
            @Parameter(description = "规则类型") @RequestParam(required = false) String ruleType) {

        log.info("获取模板信息，规则类型: {}", ruleType);

        ImportTemplateResponse response = ruleImportService.getTemplateInfo(ruleType);

        return ApiResponse.success(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "验证导入文件", description = "验证上传的Excel文件格式和内容，不实际导入")
    @PreAuthorize("hasAuthority('RULE_IMPORT')")
    public ApiResponse<ImportResultResponse> validateFile(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "验证参数") @Valid @ModelAttribute ImportValidationRequest request) {

        log.info("验证导入文件，文件名: {}, 大小: {}MB", file.getOriginalFilename(),
                file.getSize() / 1024.0 / 1024.0);

        ImportResultResponse response = ruleImportService.validateImportFile(file, request);

        return ApiResponse.success(response);
    }

    @PostMapping
    @Operation(summary = "批量导入规则", description = "从Excel文件批量导入规则数据")
    @PreAuthorize("hasAuthority('RULE_IMPORT')")
    public ApiResponse<ImportResultResponse> importRules(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "导入模式") @RequestParam(defaultValue = "INSERT") String importMode,
            @Parameter(description = "是否跳过错误") @RequestParam(defaultValue = "false") boolean skipErrors,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "500") int batchSize) {

        log.info("批量导入规则，文件名: {}, 导入模式: {}, 跳过错误: {}, 批次大小: {}",
                file.getOriginalFilename(), importMode, skipErrors, batchSize);

        ImportResultResponse response = ruleImportService.importRules(file, importMode, skipErrors, batchSize);

        return ApiResponse.success(response);
    }

    @PostMapping("/preview")
    @Operation(summary = "预览导入数据", description = "预览Excel文件中的数据，不实际导入")
    @PreAuthorize("hasAuthority('RULE_IMPORT')")
    public ApiResponse<ImportResultResponse> previewImport(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "预览行数") @RequestParam(defaultValue = "10") int previewRows) {

        log.info("预览导入数据，文件名: {}, 预览行数: {}", file.getOriginalFilename(), previewRows);

        ImportResultResponse response = ruleImportService.previewImport(file, previewRows);

        return ApiResponse.success(response);
    }

    @GetMapping("/history")
    @Operation(summary = "获取导入历史", description = "获取规则导入的历史记录")
    @PreAuthorize("hasAuthority('RULE_IMPORT')")
    public ApiResponse<List<Object>> getImportHistory(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") int size) {

        log.info("获取导入历史，页码: {}, 页面大小: {}", page, size);

        List<Object> importHistory = ruleImportService.getImportHistory(page, size);

        return ApiResponse.success(importHistory);
    }

    @GetMapping("/result/{batchId}")
    @Operation(summary = "获取导入结果", description = "根据批次ID获取导入结果详情")
    @PreAuthorize("hasAuthority('RULE_IMPORT')")
    public ApiResponse<ImportResultResponse> getImportResult(
            @Parameter(description = "批次ID") @PathVariable String batchId) {

        log.info("获取导入结果，批次ID: {}", batchId);

        ImportResultResponse response = ruleImportService.getImportResult(batchId);

        return ApiResponse.success(response);
    }

    @GetMapping("/result/{batchId}/download")
    @Operation(summary = "下载导入结果报告", description = "下载导入结果的详细报告")
    @PreAuthorize("hasAuthority('RULE_IMPORT')")
    public ResponseEntity<Resource> downloadImportReport(
            @Parameter(description = "批次ID") @PathVariable String batchId) {

        log.info("下载导入结果报告，批次ID: {}", batchId);

        Resource reportResource = ruleImportService.generateImportReport(batchId);

        String filename = String.format("import_report_%s.xlsx", batchId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, com.insurance.audit.common.util.HttpHeaderUtils.contentDispositionAttachment(filename))
                .body(reportResource);
    }

    @DeleteMapping("/result/{batchId}")
    @Operation(summary = "删除导入批次", description = "删除指定批次的导入记录和数据")
    @PreAuthorize("hasAuthority('RULE_ADMIN')")
    public ApiResponse<Void> deleteImportBatch(
            @Parameter(description = "批次ID") @PathVariable String batchId,
            @Parameter(description = "是否删除数据") @RequestParam(defaultValue = "false") boolean deleteData) {

        log.info("删除导入批次，批次ID: {}, 删除数据: {}", batchId, deleteData);

        boolean success = ruleImportService.deleteImportBatch(batchId, deleteData);

        if (success) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error("删除导入批次失败");
        }
    }

    @PostMapping("/rollback/{batchId}")
    @Operation(summary = "回滚导入数据", description = "回滚指定批次导入的数据")
    @PreAuthorize("hasAuthority('RULE_ADMIN')")
    public ApiResponse<ImportResultResponse> rollbackImport(
            @Parameter(description = "批次ID") @PathVariable String batchId,
            @Parameter(description = "回滚原因") @RequestParam String reason) {

        log.info("回滚导入数据，批次ID: {}, 原因: {}", batchId, reason);

        ImportResultResponse response = ruleImportService.rollbackImport(batchId, reason);

        return ApiResponse.success(response);
    }
}