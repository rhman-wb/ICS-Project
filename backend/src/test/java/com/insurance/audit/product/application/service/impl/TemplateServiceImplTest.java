package com.insurance.audit.product.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.product.application.service.FileStorageService;
import com.insurance.audit.product.domain.entity.ProductTemplate;
import com.insurance.audit.product.infrastructure.mapper.ProductTemplateMapper;
import com.insurance.audit.product.interfaces.dto.TemplateFieldConfig;
import com.insurance.audit.product.interfaces.dto.ValidationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 模板服务实现类测试
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("模板服务实现类测试")
class TemplateServiceImplTest {

    @Mock
    private ProductTemplateMapper templateMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private TemplateServiceImpl templateService;

    private ProductTemplate filingTemplate;
    private ProductTemplate agriculturalTemplate;

    @BeforeEach
    void setUp() {
        // 创建备案产品模板测试数据
        filingTemplate = ProductTemplate.builder()
                .id("filing-template-id")
                .templateType(ProductTemplate.TemplateType.FILING.name())
                .templateName("备案产品自主注册信息登记表")
                .templateFilePath("templates/filing-product-template.xlsx")
                .templateVersion("1.0.0")
                .description("备案产品模板")
                .enabled(true)
                .mimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .fileSize(102400L)
                .sortOrder(1)
                .fieldConfig(createFieldConfigs())
                .validationRules(createValidationRules())
                .build();

        // 创建农险产品模板测试数据
        agriculturalTemplate = ProductTemplate.builder()
                .id("agricultural-template-id")
                .templateType(ProductTemplate.TemplateType.AGRICULTURAL.name())
                .templateName("农险产品信息登记表")
                .templateFilePath("templates/agricultural-product-template.xls")
                .templateVersion("1.0.0")
                .description("农险产品模板")
                .enabled(true)
                .mimeType("application/vnd.ms-excel")
                .fileSize(81920L)
                .sortOrder(2)
                .fieldConfig(createFieldConfigs())
                .validationRules(createValidationRules())
                .build();
    }

    @Test
    @DisplayName("下载模板 - 备案产品模板成功")
    void downloadTemplate_FilingTemplate_Success() throws IOException {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();
        String fullPath = "/storage/templates/filing-product-template.xlsx";
        File mockFile = mock(File.class);

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(filingTemplate);
        when(templateMapper.selectById(filingTemplate.getId())).thenReturn(filingTemplate);
        when(fileStorageService.getFullPath(filingTemplate.getTemplateFilePath())).thenReturn(fullPath);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.isFile()).thenReturn(true);

        // When & Then
        assertThatCode(() -> templateService.downloadTemplate(templateType))
                .doesNotThrowAnyException();

        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(templateMapper).selectById(filingTemplate.getId());
        verify(fileStorageService).getFullPath(filingTemplate.getTemplateFilePath());
    }

    @Test
    @DisplayName("下载模板 - 无效的模板类型")
    void downloadTemplate_InvalidTemplateType() {
        // Given
        String invalidTemplateType = "INVALID_TYPE";

        // When & Then
        assertThatThrownBy(() -> templateService.downloadTemplate(invalidTemplateType))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无效的模板类型");

        verify(templateMapper, never()).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("下载模板 - 模板配置不存在")
    void downloadTemplate_TemplateNotFound() {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> templateService.downloadTemplate(templateType))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未找到模板配置");

        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID下载模板 - 成功")
    void downloadTemplateById_Success() throws IOException {
        // Given
        String templateId = filingTemplate.getId();
        String fullPath = "/storage/templates/filing-product-template.xlsx";
        File mockFile = mock(File.class);

        when(templateMapper.selectById(templateId)).thenReturn(filingTemplate);
        when(fileStorageService.getFullPath(filingTemplate.getTemplateFilePath())).thenReturn(fullPath);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.isFile()).thenReturn(true);

        // When & Then
        assertThatCode(() -> templateService.downloadTemplateById(templateId))
                .doesNotThrowAnyException();

        verify(templateMapper).selectById(templateId);
        verify(fileStorageService).getFullPath(filingTemplate.getTemplateFilePath());
    }

    @Test
    @DisplayName("根据ID下载模板 - 模板不存在")
    void downloadTemplateById_TemplateNotFound() {
        // Given
        String templateId = "non-existent-id";

        when(templateMapper.selectById(templateId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> templateService.downloadTemplateById(templateId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未找到模板");

        verify(templateMapper).selectById(templateId);
    }

    @Test
    @DisplayName("根据ID下载模板 - 模板已禁用")
    void downloadTemplateById_TemplateDisabled() {
        // Given
        filingTemplate.setEnabled(false);

        when(templateMapper.selectById(filingTemplate.getId())).thenReturn(filingTemplate);

        // When & Then
        assertThatThrownBy(() -> templateService.downloadTemplateById(filingTemplate.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("模板已禁用");

        verify(templateMapper).selectById(filingTemplate.getId());
    }

    @Test
    @DisplayName("获取模板配置 - 成功")
    void getTemplateConfig_Success() {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(filingTemplate);

        // When
        ProductTemplate result = templateService.getTemplateConfig(templateType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTemplateType()).isEqualTo(templateType);
        assertThat(result.getTemplateName()).isEqualTo("备案产品自主注册信息登记表");
        assertThat(result.getEnabled()).isTrue();

        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取模板配置 - 未找到配置")
    void getTemplateConfig_NotFound() {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        ProductTemplate result = templateService.getTemplateConfig(templateType);

        // Then
        assertThat(result).isNull();
        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID获取模板配置 - 成功")
    void getTemplateConfigById_Success() {
        // Given
        String templateId = filingTemplate.getId();

        when(templateMapper.selectById(templateId)).thenReturn(filingTemplate);

        // When
        ProductTemplate result = templateService.getTemplateConfigById(templateId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(templateId);
        assertThat(result.getTemplateName()).isEqualTo("备案产品自主注册信息登记表");

        verify(templateMapper).selectById(templateId);
    }

    @Test
    @DisplayName("获取模板字段配置 - 成功")
    void getTemplateFields_Success() {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(filingTemplate);

        // When
        List<TemplateFieldConfig> result = templateService.getTemplateFields(templateType);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThan(0);

        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取模板字段配置 - 模板不存在返回空列表")
    void getTemplateFields_TemplateNotFound() {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        List<TemplateFieldConfig> result = templateService.getTemplateFields(templateType);

        // Then
        assertThat(result).isEmpty();
        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取验证规则 - 成功")
    void getValidationRules_Success() {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(filingTemplate);

        // When
        List<ValidationRule> result = templateService.getValidationRules(templateType);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThan(0);

        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取所有启用的模板 - 成功")
    void getEnabledTemplates_Success() {
        // Given
        List<ProductTemplate> templates = Arrays.asList(filingTemplate, agriculturalTemplate);

        when(templateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(templates);

        // When
        List<ProductTemplate> result = templateService.getEnabledTemplates();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(filingTemplate, agriculturalTemplate);

        verify(templateMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取所有模板 - 包含禁用的模板")
    void getAllTemplates_IncludeDisabled() {
        // Given
        List<ProductTemplate> templates = Arrays.asList(filingTemplate, agriculturalTemplate);

        when(templateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(templates);

        // When
        List<ProductTemplate> result = templateService.getAllTemplates(true);

        // Then
        assertThat(result).hasSize(2);
        verify(templateMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("创建模板 - 成功")
    void createTemplate_Success() {
        // Given
        ProductTemplate newTemplate = ProductTemplate.builder()
                .templateType(ProductTemplate.TemplateType.FILING.name())
                .templateName("新模板")
                .templateFilePath("templates/new-template.xlsx")
                .templateVersion("1.0.0")
                .build();

        when(templateMapper.insert(any(ProductTemplate.class))).thenReturn(1);

        // When
        ProductTemplate result = templateService.createTemplate(newTemplate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getSortOrder()).isEqualTo(0);

        verify(templateMapper).insert(any(ProductTemplate.class));
    }

    @Test
    @DisplayName("创建模板 - 无效的模板类型")
    void createTemplate_InvalidTemplateType() {
        // Given
        ProductTemplate newTemplate = ProductTemplate.builder()
                .templateType("INVALID_TYPE")
                .templateName("新模板")
                .build();

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(newTemplate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无效的模板类型");

        verify(templateMapper, never()).insert(any(ProductTemplate.class));
    }

    @Test
    @DisplayName("创建模板 - 插入失败")
    void createTemplate_InsertFailed() {
        // Given
        ProductTemplate newTemplate = ProductTemplate.builder()
                .templateType(ProductTemplate.TemplateType.FILING.name())
                .templateName("新模板")
                .build();

        when(templateMapper.insert(any(ProductTemplate.class))).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(newTemplate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("创建模板配置失败");

        verify(templateMapper).insert(any(ProductTemplate.class));
    }

    @Test
    @DisplayName("更新模板 - 成功")
    void updateTemplate_Success() {
        // Given
        filingTemplate.setDescription("更新后的描述");

        when(templateMapper.selectById(filingTemplate.getId())).thenReturn(filingTemplate);
        when(templateMapper.updateById(filingTemplate)).thenReturn(1);

        // When
        ProductTemplate result = templateService.updateTemplate(filingTemplate);

        // Then
        assertThat(result).isNotNull();
        verify(templateMapper).selectById(filingTemplate.getId());
        verify(templateMapper, times(2)).updateById(filingTemplate);
    }

    @Test
    @DisplayName("更新模板 - 模板不存在")
    void updateTemplate_TemplateNotFound() {
        // Given
        when(templateMapper.selectById(filingTemplate.getId())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> templateService.updateTemplate(filingTemplate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("模板不存在");

        verify(templateMapper).selectById(filingTemplate.getId());
        verify(templateMapper, never()).updateById(any(ProductTemplate.class));
    }

    @Test
    @DisplayName("启用模板 - 成功")
    void enableTemplate_Success() {
        // Given
        filingTemplate.setEnabled(false);

        when(templateMapper.selectById(filingTemplate.getId())).thenReturn(filingTemplate);
        when(templateMapper.updateById(any(ProductTemplate.class))).thenReturn(1);

        // When
        boolean result = templateService.enableTemplate(filingTemplate.getId());

        // Then
        assertThat(result).isTrue();
        verify(templateMapper).selectById(filingTemplate.getId());
        verify(templateMapper).updateById(any(ProductTemplate.class));
    }

    @Test
    @DisplayName("启用模板 - 模板不存在")
    void enableTemplate_TemplateNotFound() {
        // Given
        String templateId = "non-existent-id";

        when(templateMapper.selectById(templateId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> templateService.enableTemplate(templateId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("模板不存在");

        verify(templateMapper).selectById(templateId);
    }

    @Test
    @DisplayName("禁用模板 - 成功")
    void disableTemplate_Success() {
        // Given
        when(templateMapper.selectById(filingTemplate.getId())).thenReturn(filingTemplate);
        when(templateMapper.updateById(any(ProductTemplate.class))).thenReturn(1);

        // When
        boolean result = templateService.disableTemplate(filingTemplate.getId());

        // Then
        assertThat(result).isTrue();
        verify(templateMapper).selectById(filingTemplate.getId());
        verify(templateMapper).updateById(any(ProductTemplate.class));
    }

    @Test
    @DisplayName("删除模板 - 成功")
    void deleteTemplate_Success() {
        // Given
        when(templateMapper.selectById(filingTemplate.getId())).thenReturn(filingTemplate);
        when(templateMapper.deleteById(filingTemplate.getId())).thenReturn(1);
        doNothing().when(fileStorageService).deleteFile(anyString());

        // When
        boolean result = templateService.deleteTemplate(filingTemplate.getId());

        // Then
        assertThat(result).isTrue();
        verify(templateMapper).selectById(filingTemplate.getId());
        verify(templateMapper).deleteById(filingTemplate.getId());
        verify(fileStorageService).deleteFile(filingTemplate.getTemplateFilePath());
    }

    @Test
    @DisplayName("删除模板 - 模板不存在仍返回成功")
    void deleteTemplate_TemplateNotFound() {
        // Given
        String templateId = "non-existent-id";

        when(templateMapper.selectById(templateId)).thenReturn(null);

        // When
        boolean result = templateService.deleteTemplate(templateId);

        // Then
        assertThat(result).isTrue();
        verify(templateMapper).selectById(templateId);
        verify(templateMapper, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("验证模板类型 - 有效的模板类型")
    void isValidTemplateType_ValidTypes() {
        // Given & When & Then
        assertThat(templateService.isValidTemplateType("FILING")).isTrue();
        assertThat(templateService.isValidTemplateType("AGRICULTURAL")).isTrue();
        assertThat(templateService.isValidTemplateType("filing")).isTrue();
        assertThat(templateService.isValidTemplateType("agricultural")).isTrue();
    }

    @Test
    @DisplayName("验证模板类型 - 无效的模板类型")
    void isValidTemplateType_InvalidTypes() {
        // Given & When & Then
        assertThat(templateService.isValidTemplateType("INVALID")).isFalse();
        assertThat(templateService.isValidTemplateType(null)).isFalse();
        assertThat(templateService.isValidTemplateType("")).isFalse();
        assertThat(templateService.isValidTemplateType("   ")).isFalse();
    }

    @Test
    @DisplayName("获取模板元数据 - 成功")
    void getTemplateMetadata_Success() {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(filingTemplate);

        // When
        ProductTemplate result = templateService.getTemplateMetadata(templateType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(filingTemplate.getId());
        assertThat(result.getTemplateName()).isEqualTo(filingTemplate.getTemplateName());
        assertThat(result.getTemplateVersion()).isEqualTo(filingTemplate.getTemplateVersion());
        // 元数据不应包含大字段配置
        assertThat(result.getFieldConfig()).isNull();
        assertThat(result.getValidationRules()).isNull();

        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取模板元数据 - 模板不存在")
    void getTemplateMetadata_TemplateNotFound() {
        // Given
        String templateType = ProductTemplate.TemplateType.FILING.name();

        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        ProductTemplate result = templateService.getTemplateMetadata(templateType);

        // Then
        assertThat(result).isNull();
        verify(templateMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    // Helper methods for test data creation

    private List<TemplateFieldConfig> createFieldConfigs() {
        List<TemplateFieldConfig> configs = new ArrayList<>();

        TemplateFieldConfig config1 = new TemplateFieldConfig();
        config1.setFieldName("productName");
        config1.setFieldLabel("产品名称");
        config1.setFieldType("text");
        config1.setRequired(true);
        configs.add(config1);

        TemplateFieldConfig config2 = new TemplateFieldConfig();
        config2.setFieldName("productCode");
        config2.setFieldLabel("产品代码");
        config2.setFieldType("text");
        config2.setRequired(true);
        configs.add(config2);

        return configs;
    }

    private List<ValidationRule> createValidationRules() {
        List<ValidationRule> rules = new ArrayList<>();

        ValidationRule rule1 = new ValidationRule();
        rule1.setValidator("productName");
        rule1.setType("required");
        rule1.setMessage("产品名称不能为空");
        rules.add(rule1);

        ValidationRule rule2 = new ValidationRule();
        rule2.setValidator("productCode");
        rule2.setType("pattern");
        rule2.setValue("^[A-Z0-9]{6,20}$");
        rule2.setMessage("产品代码格式不正确");
        rules.add(rule2);

        return rules;
    }
}