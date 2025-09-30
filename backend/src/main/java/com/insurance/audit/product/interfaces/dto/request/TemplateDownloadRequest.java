package com.insurance.audit.product.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 模板下载请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板下载请求")
public class TemplateDownloadRequest {

    /**
     * 模板类型
     */
    @NotBlank(message = "模板类型不能为空")
    @Pattern(regexp = "^(FILING|AGRICULTURAL)$", message = "模板类型必须是FILING或AGRICULTURAL")
    @Schema(description = "模板类型", example = "AGRICULTURAL",
            allowableValues = {"FILING", "AGRICULTURAL"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateType;

    /**
     * 模板版本（可选）
     */
    @Schema(description = "模板版本，不指定时使用最新版本", example = "1.0")
    private String version;
}