package com.insurance.audit.product.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 模板文件解析请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板文件解析请求")
public class TemplateParseRequest {

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
     * 上传的模板文件（当使用form-data时）
     */
    @Schema(description = "上传的模板文件（Excel格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    /**
     * 是否验证数据
     */
    @Schema(description = "是否验证数据", example = "true", defaultValue = "true")
    private Boolean validateData = true;

    /**
     * 是否自动修复数据
     */
    @Schema(description = "是否自动修复数据", example = "false", defaultValue = "false")
    private Boolean autoFix = false;
}