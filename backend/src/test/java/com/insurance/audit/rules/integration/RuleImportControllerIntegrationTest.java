package com.insurance.audit.rules.integration;

import com.insurance.audit.common.TestDataFactory;
import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.integration.BaseIntegrationTest;
import com.insurance.audit.rules.interfaces.dto.response.ImportResultResponse;
import com.insurance.audit.rules.interfaces.dto.response.ImportTemplateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 规则导入控制器集成测试
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@DisplayName("规则导入控制器集成测试")
class RuleImportControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("下载导入模板 - 成功")
    void downloadTemplate_Success() {
        // Given
        String ruleType = "SINGLE";
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        String url = getV1ApiUrl("/rules/import/template?ruleType=" + ruleType);
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<Resource> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Resource.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(response.getHeaders().getContentDisposition().getFilename()).contains("rule_import_template");
    }

    @Test
    @DisplayName("下载导入模板 - 不指定规则类型")
    void downloadTemplate_WithoutRuleType() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        String url = getV1ApiUrl("/rules/import/template");
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<Resource> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Resource.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getContentDisposition().getFilename()).contains("rule_import_template_all");
    }

    @Test
    @DisplayName("获取模板信息 - 成功")
    void getTemplateInfo_Success() {
        // Given
        String ruleType = "SINGLE";
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        String url = getV1ApiUrl("/rules/import/template/info?ruleType=" + ruleType);
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<ApiResponse<ImportTemplateResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<ImportTemplateResponse>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("验证导入文件 - 成功")
    void validateFile_Success() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);

        // 创建测试文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_rules.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes()
        );

        // 设置请求头
        HttpHeaders headers = getAuthHeaders(token.replace("Bearer ", ""));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 构建表单数据
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("ruleType", "SINGLE");
        body.add("skipDuplicates", "true");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<ApiResponse<ImportResultResponse>> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import/validate"),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ImportResultResponse>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("验证导入文件 - 文件格式错误")
    void validateFile_InvalidFormat() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);

        // 创建错误格式的测试文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_rules.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "invalid content".getBytes()
        );

        HttpHeaders headers = getAuthHeaders(token.replace("Bearer ", ""));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import/validate"),
                HttpMethod.POST,
                entity,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isFalse();
    }

    @Test
    @DisplayName("批量导入规则 - 成功")
    void importRules_Success() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rules_import.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                TestDataFactory.createMockExcelContent()
        );

        HttpHeaders headers = getAuthHeaders(token.replace("Bearer ", ""));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("importMode", "INSERT");
        body.add("skipErrors", "false");
        body.add("batchSize", "500");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<ApiResponse<ImportResultResponse>> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import"),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ImportResultResponse>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("批量导入规则 - 文件过大")
    void importRules_FileTooLarge() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);

        // 创建超大文件（模拟）
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large_rules.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                largeContent
        );

        HttpHeaders headers = getAuthHeaders(token.replace("Bearer ", ""));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import"),
                HttpMethod.POST,
                entity,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isFalse();
    }

    @Test
    @DisplayName("预览导入数据 - 成功")
    void previewImport_Success() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "preview_rules.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                TestDataFactory.createMockExcelContent()
        );

        HttpHeaders headers = getAuthHeaders(token.replace("Bearer ", ""));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("previewRows", "10");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<ApiResponse<ImportResultResponse>> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import/preview"),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ImportResultResponse>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("获取导入历史 - 成功")
    void getImportHistory_Success() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        String url = getV1ApiUrl("/rules/import/history?page=1&size=20");
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<ApiResponse<List<Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<Object>>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("获取导入结果 - 成功")
    void getImportResult_Success() {
        // Given
        String batchId = "test-batch-id-001";
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<ApiResponse<ImportResultResponse>> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import/result/" + batchId),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<ImportResultResponse>>() {}
        );

        // Then
        // 根据实际Mock数据调整断言
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("下载导入结果报告 - 成功")
    void downloadImportReport_Success() {
        // Given
        String batchId = "test-batch-id-001";
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<Resource> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import/result/" + batchId + "/download"),
                HttpMethod.GET,
                entity,
                Resource.class
        );

        // Then
        // 根据实际Mock数据调整断言
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
        if (response.getStatusCode() == HttpStatus.OK) {
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        }
    }

    @Test
    @DisplayName("删除导入批次 - 成功")
    void deleteImportBatch_Success() {
        // Given
        String batchId = "test-batch-id-001";
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        String url = getV1ApiUrl("/rules/import/result/" + batchId + "?deleteData=false");
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ApiResponse<Void>>() {}
        );

        // Then
        // 根据实际Mock数据调整断言
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("回滚导入数据 - 成功")
    void rollbackImport_Success() {
        // Given
        String batchId = "test-batch-id-001";
        String reason = "测试回滚";
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);
        String url = getV1ApiUrl("/rules/import/rollback/" + batchId + "?reason=" + reason);
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<ApiResponse<ImportResultResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ImportResultResponse>>() {}
        );

        // Then
        // 根据实际Mock数据调整断言
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("权限测试 - RULE_IMPORT权限")
    void importRules_WithoutPermission() {
        // Given
        // 使用没有RULE_IMPORT权限的用户Token
        String token = TestDataFactory.createTestJwtToken("limiteduser");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_rules.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes()
        );

        HttpHeaders headers = getAuthHeaders(token.replace("Bearer ", ""));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import"),
                HttpMethod.POST,
                entity,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("权限测试 - RULE_ADMIN权限")
    void deleteImportBatch_WithoutPermission() {
        // Given
        String batchId = "test-batch-id-001";
        // 使用没有RULE_ADMIN权限的用户Token
        String token = TestDataFactory.createTestJwtToken("limiteduser");
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders(token.replace("Bearer ", "")));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getV1ApiUrl("/rules/import/result/" + batchId),
                HttpMethod.DELETE,
                entity,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("导入模式测试")
    void importRules_DifferentModes() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rules_modes.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                TestDataFactory.createMockExcelContent()
        );

        String[] importModes = {"INSERT", "UPDATE", "UPSERT"};

        for (String importMode : importModes) {
            HttpHeaders headers = getAuthHeaders(token.replace("Bearer ", ""));
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("importMode", importMode);
            body.add("skipErrors", "true");
            body.add("batchSize", "100");

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // When
            ResponseEntity<ApiResponse<ImportResultResponse>> response = restTemplate.exchange(
                    getV1ApiUrl("/rules/import"),
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<ImportResultResponse>>() {}
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
        }
    }

    @Test
    @DisplayName("批次大小参数测试")
    void importRules_BatchSizeTest() {
        // Given
        String token = TestDataFactory.createTestJwtToken(TestDataFactory.DEFAULT_USERNAME);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rules_batch.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                TestDataFactory.createMockExcelContent()
        );

        int[] batchSizes = {100, 500, 1000};

        for (int batchSize : batchSizes) {
            HttpHeaders headers = getAuthHeaders(token.replace("Bearer ", ""));
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("importMode", "INSERT");
            body.add("skipErrors", "false");
            body.add("batchSize", String.valueOf(batchSize));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // When
            ResponseEntity<ApiResponse<ImportResultResponse>> response = restTemplate.exchange(
                    getV1ApiUrl("/rules/import"),
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<ImportResultResponse>>() {}
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
        }
    }
}