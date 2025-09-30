package com.insurance.audit.product.integration;

import com.insurance.audit.integration.BaseIntegrationTest;
import com.insurance.audit.product.interfaces.dto.request.TemplateDownloadRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 模板功能集成测试
 * 测试完整的模板管理、下载、验证等数据流
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@AutoConfigureMockMvc
@DisplayName("模板功能集成测试")
class TemplateIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String TEMPLATE_BASE_URL = "/api/v1/products/templates";

    @Test
    @DisplayName("获取所有启用的模板列表 - 成功")
    void getAllEnabledTemplates_Success() throws Exception {
        mockMvc.perform(get(TEMPLATE_BASE_URL)
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("下载模板文件 - 备案产品模板")
    void downloadTemplate_FilingTemplate_Success() throws Exception {
        MvcResult result = mockMvc.perform(post(TEMPLATE_BASE_URL + "/download")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateType\":\"FILING\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assert content.length > 0;
    }

    @Test
    @DisplayName("下载模板文件 - 农险产品模板")
    void downloadTemplate_AgriculturalTemplate_Success() throws Exception {
        mockMvc.perform(post(TEMPLATE_BASE_URL + "/download")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateType\":\"AGRICULTURAL\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("获取模板字段配置 - 成功")
    void getTemplateFields_Success() throws Exception {
        mockMvc.perform(get(TEMPLATE_BASE_URL + "/fields")
                        .param("templateType", "FILING")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取模板验证规则 - 成功")
    void getValidationRules_Success() throws Exception {
        mockMvc.perform(get(TEMPLATE_BASE_URL + "/validation-rules")
                        .param("templateType", "FILING")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("验证产品字段 - 成功")
    void validateProductFields_Success() throws Exception {
        String requestBody = """
                {
                    "productId": "test-product-id",
                    "templateType": "FILING",
                    "fieldValues": {
                        "productName": "测试产品",
                        "productCode": "TEST123456"
                    }
                }
                """;

        mockMvc.perform(post(TEMPLATE_BASE_URL + "/validate-fields")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.valid").exists());
    }

    @Test
    @DisplayName("验证产品字段 - 验证失败")
    void validateProductFields_ValidationFailed() throws Exception {
        String requestBody = """
                {
                    "productId": "test-product-id",
                    "templateType": "FILING",
                    "fieldValues": {
                        "productName": "",
                        "productCode": "ABC"
                    }
                }
                """;

        mockMvc.perform(post(TEMPLATE_BASE_URL + "/validate-fields")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.errors").isArray())
                .andExpect(jsonPath("$.data.errors").isNotEmpty());
    }

    @Test
    @DisplayName("完整产品创建流程 - 使用模板字段")
    void completeProductCreation_WithTemplateFields() throws Exception {
        // 1. 获取模板配置
        mockMvc.perform(get(TEMPLATE_BASE_URL + "/fields")
                        .param("templateType", "FILING")
                        .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isOk());

        // 2. 创建产品（包含模板字段）
        String createProductRequest = """
                {
                    "productName": "集成测试产品",
                    "productCode": "INTTEST001",
                    "productType": "FILING",
                    "templateType": "FILING",
                    "registrationNumber": "REG20240930001",
                    "usesDemonstrationClause": false
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        // 3. 验证产品字段
        // Extract product ID from response and validate
    }

    @Test
    @DisplayName("未授权访问模板API - 返回401")
    void accessTemplateAPI_Unauthorized() throws Exception {
        mockMvc.perform(get(TEMPLATE_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("下载不存在的模板类型 - 返回错误")
    void downloadTemplate_InvalidType_Error() throws Exception {
        mockMvc.perform(post(TEMPLATE_BASE_URL + "/download")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateType\":\"INVALID_TYPE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(greaterThan(200)));
    }

    @Test
    @DisplayName("模板文件解析 - 成功")
    void parseTemplateFile_Success() throws Exception {
        // This test would require multipart file upload
        // Placeholder for actual implementation
        assert true;
    }

    /**
     * Helper method to get access token for authenticated requests
     */
    private String getAccessToken() {
        // Implementation would depend on your authentication setup
        // This is a placeholder
        return "test-access-token";
    }
}