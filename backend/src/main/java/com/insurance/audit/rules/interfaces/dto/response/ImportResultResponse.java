package com.insurance.audit.rules.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 导入结果响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Schema(description = "导入结果响应")
public class ImportResultResponse {

    @Schema(description = "批次ID")
    private String batchId;

    @Schema(description = "导入状态", example = "SUCCESS")
    private String status; // SUCCESS, FAILED, PARTIAL_SUCCESS, PROCESSING

    @Schema(description = "总行数")
    private Integer totalRows;

    @Schema(description = "成功行数")
    private Integer successRows;

    @Schema(description = "有效行数（别名）")
    private Integer validRows;

    @Schema(description = "失败行数")
    private Integer failureRows;

    @Schema(description = "失败行数（别名）")
    private Integer failedRows;

    @Schema(description = "无效行数（别名）")
    private Integer invalidRows;

    @Schema(description = "跳过行数")
    private Integer skippedRows;

    @Schema(description = "预览行数")
    private Integer previewRows;

    @Schema(description = "验证状态")
    private String validationStatus;

    @Schema(description = "导入状态（别名）")
    private String importStatus;

    @Schema(description = "回滚状态")
    private String rollbackStatus;

    @Schema(description = "回滚原因")
    private String rollbackReason;

    @Schema(description = "处理时间（毫秒）")
    private Long processingTimeMs;

    @Schema(description = "导入开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @Schema(description = "导入结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    @Schema(description = "导入模式", example = "INSERT")
    private String importMode;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "成功的规则ID列表")
    private List<String> successRuleIds;

    @Schema(description = "错误详情")
    private List<ImportError> errors;

    @Schema(description = "警告信息")
    private List<ImportWarning> warnings;

    @Schema(description = "数据预览")
    private List<Map<String, Object>> dataPreview;

    @Schema(description = "预览数据（别名）")
    private List<Object> previewData;

    @Schema(description = "结果摘要")
    private String resultSummary;

    @Schema(description = "下载链接")
    private Map<String, String> downloadLinks;

    @Data
    @Schema(description = "导入错误")
    public static class ImportError {
        @Schema(description = "行号")
        private Integer rowNumber;

        @Schema(description = "列名")
        private String columnName;

        @Schema(description = "错误类型")
        private String errorType;

        @Schema(description = "错误消息")
        private String errorMessage;

        @Schema(description = "原始值")
        private String originalValue;

        @Schema(description = "建议值")
        private String suggestedValue;

        // 无参构造函数
        public ImportError() {}

        // 三参数构造函数
        public ImportError(Integer rowNumber, String errorType, String errorMessage) {
            this.rowNumber = rowNumber;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
        }
    }

    @Data
    @Schema(description = "导入警告")
    public static class ImportWarning {
        @Schema(description = "行号")
        private Integer rowNumber;

        @Schema(description = "列名")
        private String columnName;

        @Schema(description = "警告类型")
        private String warningType;

        @Schema(description = "警告消息")
        private String warningMessage;

        @Schema(description = "原始值")
        private String originalValue;
    }

    public static ImportResultResponse success(String batchId, int total, int success) {
        ImportResultResponse response = new ImportResultResponse();
        response.setBatchId(batchId);
        response.setStatus("SUCCESS");
        response.setTotalRows(total);
        response.setSuccessRows(success);
        response.setFailureRows(total - success);
        response.setSkippedRows(0);
        return response;
    }

    public static ImportResultResponse failure(String batchId, List<ImportError> errors) {
        ImportResultResponse response = new ImportResultResponse();
        response.setBatchId(batchId);
        response.setStatus("FAILED");
        response.setErrors(errors);
        return response;
    }
}