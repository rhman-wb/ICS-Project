package com.insurance.audit.rules.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 批量操作响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Schema(description = "批量操作响应")
public class BatchOperationResponse {

    @Schema(description = "总请求数量")
    private Integer totalRequested;

    @Schema(description = "成功处理数量")
    private Integer successCount;

    @Schema(description = "失败处理数量")
    private Integer failureCount;

    @Schema(description = "操作是否整体成功")
    private Boolean success;

    @Schema(description = "成功处理的ID列表")
    private List<String> successIds;

    @Schema(description = "失败处理的ID列表")
    private List<String> failureIds;

    @Schema(description = "失败详情")
    private Map<String, String> failureDetails;

    @Schema(description = "批次ID")
    private String batchId;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "处理时间（毫秒）")
    private Long processingTimeMs;

    @Schema(description = "操作结果摘要")
    private String resultSummary;

    public static BatchOperationResponse success(int total, int success, List<String> successIds) {
        BatchOperationResponse response = new BatchOperationResponse();
        response.setTotalRequested(total);
        response.setSuccessCount(success);
        response.setFailureCount(total - success);
        response.setSuccess(success > 0);
        response.setSuccessIds(successIds);
        return response;
    }

    public static BatchOperationResponse failure(int total, Map<String, String> failures) {
        BatchOperationResponse response = new BatchOperationResponse();
        response.setTotalRequested(total);
        response.setSuccessCount(0);
        response.setFailureCount(total);
        response.setSuccess(false);
        response.setFailureDetails(failures);
        return response;
    }
}