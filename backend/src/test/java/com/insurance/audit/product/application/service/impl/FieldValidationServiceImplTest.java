package com.insurance.audit.product.application.service.impl;

import com.insurance.audit.product.application.service.FieldValidationService.FieldError;
import com.insurance.audit.product.application.service.FieldValidationService.FieldValidationResult;
import com.insurance.audit.product.application.service.TemplateService;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.interfaces.dto.ValidationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 字段验证服务实现类测试
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("字段验证服务实现类测试")
class FieldValidationServiceImplTest {

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private FieldValidationServiceImpl fieldValidationService;

    private Product testProduct;
    private List<ValidationRule> validationRules;

    @BeforeEach
    void setUp() {
        // 创建测试产品
        testProduct = Product.builder()
                .id("test-product-id")
                .productName("测试产品")
                .productCode("TEST001")
                .productType(Product.ProductType.FILING)
                .templateType("FILING")
                .status(Product.ProductStatus.DRAFT)
                .build();

        // 创建验证规则
        validationRules = createValidationRules();
    }

    @Test
    @DisplayName("验证产品 - 产品有模板类型，验证成功")
    void validateProduct_WithTemplateType_Success() {
        // Given
        when(templateService.getValidationRules(testProduct.getTemplateType()))
                .thenReturn(validationRules);

        // When
        FieldValidationResult result = fieldValidationService.validateProduct(testProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        verify(templateService).getValidationRules(testProduct.getTemplateType());
    }

    @Test
    @DisplayName("验证产品 - 产品无模板类型，使用默认验证")
    void validateProduct_WithoutTemplateType_UsesBasicValidation() {
        // Given
        testProduct.setTemplateType(null);

        // When
        FieldValidationResult result = fieldValidationService.validateProduct(testProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();

        verify(templateService, never()).getValidationRules(anyString());
    }

    @Test
    @DisplayName("验证产品 - 基本字段验证失败")
    void validateProduct_BasicValidation_Failed() {
        // Given
        testProduct.setTemplateType(null);
        testProduct.setProductName(null);
        testProduct.setProductType(null);

        // When
        FieldValidationResult result = fieldValidationService.validateProduct(testProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors())
                .extracting(FieldError::getFieldName)
                .containsExactlyInAnyOrder("productName", "productType");
    }

    @Test
    @DisplayName("根据模板类型验证产品 - 成功")
    void validateProductByTemplate_Success() {
        // Given
        String templateType = "FILING";

        when(templateService.getValidationRules(templateType)).thenReturn(validationRules);

        // When
        FieldValidationResult result = fieldValidationService.validateProductByTemplate(testProduct, templateType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        verify(templateService).getValidationRules(templateType);
    }

    @Test
    @DisplayName("根据模板类型验证产品 - 未找到验证规则")
    void validateProductByTemplate_NoValidationRules() {
        // Given
        String templateType = "FILING";

        when(templateService.getValidationRules(templateType)).thenReturn(Collections.emptyList());

        // When
        FieldValidationResult result = fieldValidationService.validateProductByTemplate(testProduct, templateType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();

        verify(templateService).getValidationRules(templateType);
    }

    @Test
    @DisplayName("根据模板类型验证产品 - 必填字段为空")
    void validateProductByTemplate_RequiredFieldEmpty() {
        // Given
        String templateType = "FILING";
        testProduct.setProductName("");

        ValidationRule requiredRule = new ValidationRule();
        requiredRule.setValidator("productName");
        requiredRule.setType("required");
        requiredRule.setMessage("产品名称不能为空");

        when(templateService.getValidationRules(templateType))
                .thenReturn(Collections.singletonList(requiredRule));

        // When
        FieldValidationResult result = fieldValidationService.validateProductByTemplate(testProduct, templateType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getFieldName()).isEqualTo("productName");
        assertThat(result.getErrors().get(0).getMessage()).contains("不能为空");

        verify(templateService).getValidationRules(templateType);
    }

    @Test
    @DisplayName("验证单个字段 - 成功")
    void validateField_Success() {
        // Given
        String fieldName = "productName";
        Object fieldValue = "测试产品";

        // When
        FieldValidationResult result = fieldValidationService.validateField(fieldName, fieldValue, validationRules);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("验证单个字段 - 必填字段为空")
    void validateField_RequiredFieldEmpty() {
        // Given
        String fieldName = "productName";
        Object fieldValue = null;

        ValidationRule requiredRule = new ValidationRule();
        requiredRule.setValidator("productName");
        requiredRule.setType("required");
        requiredRule.setMessage("产品名称不能为空");

        // When
        FieldValidationResult result = fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(requiredRule));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("不能为空");
    }

    @Test
    @DisplayName("验证单个字段 - 最小长度验证失败")
    void validateField_MinLengthValidationFailed() {
        // Given
        String fieldName = "productCode";
        Object fieldValue = "ABC";

        ValidationRule minLengthRule = new ValidationRule();
        minLengthRule.setValidator("productCode");
        minLengthRule.setType("minlength");
        minLengthRule.setValue(6);
        minLengthRule.setMessage("产品代码长度不能少于6个字符");

        // When
        FieldValidationResult result = fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(minLengthRule));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("6个字符");
    }

    @Test
    @DisplayName("验证单个字段 - 最大长度验证失败")
    void validateField_MaxLengthValidationFailed() {
        // Given
        String fieldName = "productCode";
        Object fieldValue = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        ValidationRule maxLengthRule = new ValidationRule();
        maxLengthRule.setValidator("productCode");
        maxLengthRule.setType("maxlength");
        maxLengthRule.setValue(20);
        maxLengthRule.setMessage("产品代码长度不能超过20个字符");

        // When
        FieldValidationResult result = fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(maxLengthRule));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("20个字符");
    }

    @Test
    @DisplayName("验证单个字段 - 正则表达式验证失败")
    void validateField_PatternValidationFailed() {
        // Given
        String fieldName = "productCode";
        Object fieldValue = "abc123";

        ValidationRule patternRule = new ValidationRule();
        patternRule.setValidator("productCode");
        patternRule.setType("pattern");
        patternRule.setValue("^[A-Z0-9]{6,20}$");
        patternRule.setMessage("产品代码格式不正确");

        // When
        FieldValidationResult result = fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(patternRule));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("格式不正确");
    }

    @Test
    @DisplayName("验证单个字段 - 数值范围验证成功")
    void validateField_RangeValidationSuccess() {
        // Given
        String fieldName = "premium";
        Object fieldValue = new BigDecimal("5000");

        ValidationRule rangeRule = new ValidationRule();
        rangeRule.setValidator("premium");
        rangeRule.setType("range");
        rangeRule.setValue("1000,10000");
        rangeRule.setMessage("保费必须在1000-10000之间");

        // When
        FieldValidationResult result = fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(rangeRule));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("验证单个字段 - 数值范围验证失败（小于最小值）")
    void validateField_RangeValidationFailed_BelowMin() {
        // Given
        String fieldName = "premium";
        Object fieldValue = new BigDecimal("500");

        ValidationRule rangeRule = new ValidationRule();
        rangeRule.setValidator("premium");
        rangeRule.setType("range");
        rangeRule.setValue("1000,10000");
        rangeRule.setMessage("保费必须在1000-10000之间");

        // When
        FieldValidationResult result = fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(rangeRule));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("不能小于");
    }

    @Test
    @DisplayName("验证单个字段 - 数值范围验证失败（大于最大值）")
    void validateField_RangeValidationFailed_AboveMax() {
        // Given
        String fieldName = "premium";
        Object fieldValue = new BigDecimal("15000");

        ValidationRule rangeRule = new ValidationRule();
        rangeRule.setValidator("premium");
        rangeRule.setType("range");
        rangeRule.setValue("1000,10000");
        rangeRule.setMessage("保费必须在1000-10000之间");

        // When
        FieldValidationResult result = fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(rangeRule));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("不能大于");
    }

    @Test
    @DisplayName("验证字段依赖关系 - 备案产品依赖验证成功")
    void validateFieldDependencies_FilingProduct_Success() {
        // Given
        testProduct.setProductType(Product.ProductType.FILING);
        testProduct.setUsesDemonstrationClause(true);
        testProduct.setDemonstrationClause("示范条款名称");

        // When
        FieldValidationResult result = fieldValidationService.validateFieldDependencies(testProduct, "demonstrationClause");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("验证字段依赖关系 - 备案产品依赖验证失败")
    void validateFieldDependencies_FilingProduct_Failed() {
        // Given
        testProduct.setProductType(Product.ProductType.FILING);
        testProduct.setUsesDemonstrationClause(true);
        testProduct.setDemonstrationClause(null);

        // When
        FieldValidationResult result = fieldValidationService.validateFieldDependencies(testProduct, "demonstrationClause");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getFieldName()).isEqualTo("demonstrationClause");
        assertThat(result.getErrors().get(0).getMessage()).contains("示范条款");
    }

    @Test
    @DisplayName("验证字段依赖关系 - 农险产品依赖验证成功")
    void validateFieldDependencies_AgriculturalProduct_Success() {
        // Given
        testProduct.setProductType(Product.ProductType.AGRICULTURAL);
        testProduct.setIsOperated(true);
        testProduct.setOperationDate(new Date());

        // When
        FieldValidationResult result = fieldValidationService.validateFieldDependencies(testProduct, "operationDate");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("验证字段依赖关系 - 农险产品依赖验证失败")
    void validateFieldDependencies_AgriculturalProduct_Failed() {
        // Given
        testProduct.setProductType(Product.ProductType.AGRICULTURAL);
        testProduct.setIsOperated(true);
        testProduct.setOperationDate(null);

        // When
        FieldValidationResult result = fieldValidationService.validateFieldDependencies(testProduct, "operationDate");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getFieldName()).isEqualTo("operationDate");
        assertThat(result.getErrors().get(0).getMessage()).contains("经营日期");
    }

    @Test
    @DisplayName("批量验证字段 - 成功")
    void validateFields_Success() {
        // Given
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("productName", "测试产品");
        fieldValues.put("productCode", "TEST001");
        String templateType = "FILING";

        when(templateService.getValidationRules(templateType)).thenReturn(validationRules);

        // When
        FieldValidationResult result = fieldValidationService.validateFields(fieldValues, templateType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        verify(templateService).getValidationRules(templateType);
    }

    @Test
    @DisplayName("批量验证字段 - 多个字段验证失败")
    void validateFields_MultipleFieldsFailed() {
        // Given
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("productName", "");
        fieldValues.put("productCode", "ABC");
        String templateType = "FILING";

        List<ValidationRule> rules = Arrays.asList(
                createRequiredRule("productName", "产品名称不能为空"),
                createMinLengthRule("productCode", 6, "产品代码长度不能少于6个字符")
        );

        when(templateService.getValidationRules(templateType)).thenReturn(rules);

        // When
        FieldValidationResult result = fieldValidationService.validateFields(fieldValues, templateType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors())
                .extracting(FieldError::getFieldName)
                .containsExactlyInAnyOrder("productName", "productCode");

        verify(templateService).getValidationRules(templateType);
    }

    @Test
    @DisplayName("批量验证字段 - 未找到验证规则")
    void validateFields_NoValidationRules() {
        // Given
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("productName", "测试产品");
        String templateType = "FILING";

        when(templateService.getValidationRules(templateType)).thenReturn(null);

        // When
        FieldValidationResult result = fieldValidationService.validateFields(fieldValues, templateType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        verify(templateService).getValidationRules(templateType);
    }

    @Test
    @DisplayName("自定义验证 - 不抛出异常")
    void validateField_CustomValidation_NoException() {
        // Given
        String fieldName = "customField";
        Object fieldValue = "customValue";

        ValidationRule customRule = new ValidationRule();
        customRule.setValidator("customField");
        customRule.setType("custom");
        customRule.setMessage("自定义验证失败");

        // When & Then
        assertThatCode(() -> fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(customRule)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("验证规则类型未知 - 不抛出异常")
    void validateField_UnknownRuleType_NoException() {
        // Given
        String fieldName = "testField";
        Object fieldValue = "testValue";

        ValidationRule unknownRule = new ValidationRule();
        unknownRule.setValidator("testField");
        unknownRule.setType("unknown_type");
        unknownRule.setMessage("未知类型验证");

        // When
        FieldValidationResult result = fieldValidationService.validateField(
                fieldName, fieldValue, Collections.singletonList(unknownRule));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    // Helper methods for creating validation rules

    private List<ValidationRule> createValidationRules() {
        List<ValidationRule> rules = new ArrayList<>();

        ValidationRule rule1 = createRequiredRule("productName", "产品名称不能为空");
        ValidationRule rule2 = createPatternRule("productCode", "^[A-Z0-9]{6,20}$", "产品代码格式不正确");

        rules.add(rule1);
        rules.add(rule2);

        return rules;
    }

    private ValidationRule createRequiredRule(String fieldName, String message) {
        ValidationRule rule = new ValidationRule();
        rule.setValidator(fieldName);
        rule.setType("required");
        rule.setMessage(message);
        return rule;
    }

    private ValidationRule createMinLengthRule(String fieldName, int minLength, String message) {
        ValidationRule rule = new ValidationRule();
        rule.setValidator(fieldName);
        rule.setType("minlength");
        rule.setValue(minLength);
        rule.setMessage(message);
        return rule;
    }

    private ValidationRule createMaxLengthRule(String fieldName, int maxLength, String message) {
        ValidationRule rule = new ValidationRule();
        rule.setValidator(fieldName);
        rule.setType("maxlength");
        rule.setValue(maxLength);
        rule.setMessage(message);
        return rule;
    }

    private ValidationRule createPatternRule(String fieldName, String pattern, String message) {
        ValidationRule rule = new ValidationRule();
        rule.setValidator(fieldName);
        rule.setType("pattern");
        rule.setValue(pattern);
        rule.setMessage(message);
        return rule;
    }

    private ValidationRule createRangeRule(String fieldName, String range, String message) {
        ValidationRule rule = new ValidationRule();
        rule.setValidator(fieldName);
        rule.setType("range");
        rule.setValue(range);
        rule.setMessage(message);
        return rule;
    }
}