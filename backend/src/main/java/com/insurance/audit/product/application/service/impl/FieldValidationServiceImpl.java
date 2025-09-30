package com.insurance.audit.product.application.service.impl;

import com.insurance.audit.product.application.service.FieldValidationService;
import com.insurance.audit.product.application.service.TemplateService;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.interfaces.dto.ValidationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 字段验证服务实现类
 * 实现基于模板规则的字段验证逻辑
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldValidationServiceImpl implements FieldValidationService {

    private final TemplateService templateService;

    @Override
    public FieldValidationResult validateProduct(Product product) {
        log.info("验证产品字段: {}", product.getProductName());

        if (product.getTemplateType() == null) {
            log.warn("产品未指定模板类型，使用默认验证");
            return validateBasicFields(product);
        }

        return validateProductByTemplate(product, product.getTemplateType());
    }

    @Override
    public FieldValidationResult validateProductByTemplate(Product product, String templateType) {
        log.info("根据模板类型验证产品: {}, 模板类型: {}", product.getProductName(), templateType);

        List<FieldError> errors = new ArrayList<>();

        // 获取模板验证规则
        List<ValidationRule> validationRules = templateService.getValidationRules(templateType);

        if (validationRules == null || validationRules.isEmpty()) {
            log.warn("未找到模板验证规则, 模板类型: {}", templateType);
            return validateBasicFields(product);
        }

        // 验证产品字段
        for (ValidationRule rule : validationRules) {
            String fieldName = extractFieldName(rule);
            if (fieldName == null) {
                continue;
            }

            Object fieldValue = getFieldValue(product, fieldName);
            String errorMessage = validateByRule(fieldValue, rule);

            if (errorMessage != null) {
                errors.add(new FieldError(fieldName, errorMessage, rule.getType(), fieldValue));
            }
        }

        // 验证字段依赖关系
        FieldValidationResult dependencyResult = validateAllDependencies(product);
        if (!dependencyResult.isValid()) {
            errors.addAll(dependencyResult.getErrors());
        }

        boolean isValid = errors.isEmpty();
        log.info("产品字段验证完成, 是否通过: {}, 错误数量: {}", isValid, errors.size());

        return new FieldValidationResult(isValid, errors);
    }

    @Override
    public FieldValidationResult validateField(String fieldName, Object fieldValue, List<ValidationRule> validationRules) {
        List<FieldError> errors = new ArrayList<>();

        for (ValidationRule rule : validationRules) {
            String ruleFieldName = extractFieldName(rule);
            if (!fieldName.equals(ruleFieldName)) {
                continue;
            }

            String errorMessage = validateByRule(fieldValue, rule);
            if (errorMessage != null) {
                errors.add(new FieldError(fieldName, errorMessage, rule.getType(), fieldValue));
            }
        }

        return new FieldValidationResult(errors.isEmpty(), errors);
    }

    @Override
    public FieldValidationResult validateFieldDependencies(Product product, String fieldName) {
        List<FieldError> errors = new ArrayList<>();

        // 根据产品类型验证特定依赖关系
        if (product.getProductType() == Product.ProductType.FILING) {
            validateFilingDependencies(product, fieldName, errors);
        } else if (product.getProductType() == Product.ProductType.AGRICULTURAL) {
            validateAgriculturalDependencies(product, fieldName, errors);
        }

        return new FieldValidationResult(errors.isEmpty(), errors);
    }

    @Override
    public FieldValidationResult validateFields(Map<String, Object> fieldValues, String templateType) {
        log.info("批量验证字段, 模板类型: {}, 字段数量: {}", templateType, fieldValues.size());

        List<FieldError> errors = new ArrayList<>();
        List<ValidationRule> validationRules = templateService.getValidationRules(templateType);

        if (validationRules == null || validationRules.isEmpty()) {
            log.warn("未找到模板验证规则");
            return new FieldValidationResult(true, errors);
        }

        for (ValidationRule rule : validationRules) {
            String fieldName = extractFieldName(rule);
            if (fieldName == null) {
                continue;
            }

            Object fieldValue = fieldValues.get(fieldName);
            String errorMessage = validateByRule(fieldValue, rule);

            if (errorMessage != null) {
                errors.add(new FieldError(fieldName, errorMessage, rule.getType(), fieldValue));
            }
        }

        return new FieldValidationResult(errors.isEmpty(), errors);
    }

    /**
     * 从验证规则中提取字段名
     */
    private String extractFieldName(ValidationRule rule) {
        // 假设规则的validator字段包含字段名，或者需要从其他地方获取
        // 这里简化处理，实际应根据具体业务需求调整
        return rule.getValidator();
    }

    /**
     * 根据规则验证字段值
     */
    private String validateByRule(Object fieldValue, ValidationRule rule) {
        String ruleType = rule.getType();

        if (ruleType == null) {
            return null;
        }

        switch (ruleType.toLowerCase()) {
            case "required":
                return validateRequired(fieldValue, rule);
            case "minlength":
                return validateMinLength(fieldValue, rule);
            case "maxlength":
                return validateMaxLength(fieldValue, rule);
            case "pattern":
                return validatePattern(fieldValue, rule);
            case "range":
                return validateRange(fieldValue, rule);
            case "custom":
                return validateCustom(fieldValue, rule);
            default:
                log.warn("未知的验证规则类型: {}", ruleType);
                return null;
        }
    }

    /**
     * 验证必填
     */
    private String validateRequired(Object fieldValue, ValidationRule rule) {
        if (fieldValue == null) {
            return rule.getMessage() != null ? rule.getMessage() : "该字段不能为空";
        }

        if (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty()) {
            return rule.getMessage() != null ? rule.getMessage() : "该字段不能为空";
        }

        return null;
    }

    /**
     * 验证最小长度
     */
    private String validateMinLength(Object fieldValue, ValidationRule rule) {
        if (fieldValue == null || !(fieldValue instanceof String)) {
            return null;
        }

        String strValue = (String) fieldValue;
        Integer minLength = parseIntValue(rule.getValue());

        if (minLength != null && strValue.length() < minLength) {
            return rule.getMessage() != null ? rule.getMessage() :
                    "字段长度不能少于" + minLength + "个字符";
        }

        return null;
    }

    /**
     * 验证最大长度
     */
    private String validateMaxLength(Object fieldValue, ValidationRule rule) {
        if (fieldValue == null || !(fieldValue instanceof String)) {
            return null;
        }

        String strValue = (String) fieldValue;
        Integer maxLength = parseIntValue(rule.getValue());

        if (maxLength != null && strValue.length() > maxLength) {
            return rule.getMessage() != null ? rule.getMessage() :
                    "字段长度不能超过" + maxLength + "个字符";
        }

        return null;
    }

    /**
     * 验证正则表达式
     */
    private String validatePattern(Object fieldValue, ValidationRule rule) {
        if (fieldValue == null || !(fieldValue instanceof String)) {
            return null;
        }

        String strValue = (String) fieldValue;
        String pattern = rule.getValue() != null ? rule.getValue().toString() : null;

        if (pattern != null && !Pattern.matches(pattern, strValue)) {
            return rule.getMessage() != null ? rule.getMessage() : "字段格式不正确";
        }

        return null;
    }

    /**
     * 验证数值范围
     */
    private String validateRange(Object fieldValue, ValidationRule rule) {
        if (fieldValue == null) {
            return null;
        }

        BigDecimal value;
        if (fieldValue instanceof Number) {
            value = new BigDecimal(fieldValue.toString());
        } else {
            try {
                value = new BigDecimal(fieldValue.toString());
            } catch (NumberFormatException e) {
                return "该字段必须是数值类型";
            }
        }

        // 从rule.getValue()中解析范围，假设格式为 "min,max"
        String rangeStr = rule.getValue() != null ? rule.getValue().toString() : null;
        if (rangeStr != null && rangeStr.contains(",")) {
            String[] parts = rangeStr.split(",");
            try {
                BigDecimal minValue = new BigDecimal(parts[0].trim());
                BigDecimal maxValue = new BigDecimal(parts[1].trim());

                if (value.compareTo(minValue) < 0) {
                    return rule.getMessage() != null ? rule.getMessage() :
                            "该字段不能小于" + minValue;
                }

                if (value.compareTo(maxValue) > 0) {
                    return rule.getMessage() != null ? rule.getMessage() :
                            "该字段不能大于" + maxValue;
                }
            } catch (Exception e) {
                log.warn("解析范围值失败: {}", rangeStr);
            }
        }

        return null;
    }

    /**
     * 自定义验证
     */
    private String validateCustom(Object fieldValue, ValidationRule rule) {
        // 自定义验证逻辑可以根据rule.getValidator()来调用不同的验证器
        log.debug("执行自定义验证: {}", rule.getValidator());
        return null;
    }

    /**
     * 解析整数值
     */
    private Integer parseIntValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("解析整数值失败: {}", value);
            return null;
        }
    }

    /**
     * 验证基本字段
     */
    private FieldValidationResult validateBasicFields(Product product) {
        List<FieldError> errors = new ArrayList<>();

        // 基本必填字段验证
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            errors.add(new FieldError("productName", "产品名称不能为空"));
        }

        if (product.getProductType() == null) {
            errors.add(new FieldError("productType", "产品类型不能为空"));
        }

        return new FieldValidationResult(errors.isEmpty(), errors);
    }

    /**
     * 验证所有依赖关系
     */
    private FieldValidationResult validateAllDependencies(Product product) {
        List<FieldError> errors = new ArrayList<>();

        if (product.getProductType() == Product.ProductType.FILING) {
            validateFilingDependencies(product, null, errors);
        } else if (product.getProductType() == Product.ProductType.AGRICULTURAL) {
            validateAgriculturalDependencies(product, null, errors);
        }

        return new FieldValidationResult(errors.isEmpty(), errors);
    }

    /**
     * 验证备案产品依赖关系
     */
    private void validateFilingDependencies(Product product, String fieldName, List<FieldError> errors) {
        // 示范条款依赖验证
        if (Boolean.TRUE.equals(product.getUsesDemonstrationClause())) {
            if (product.getDemonstrationClause() == null || product.getDemonstrationClause().trim().isEmpty()) {
                errors.add(new FieldError("demonstrationClause", "使用示范条款时必须填写示范条款名称"));
            }
        }
    }

    /**
     * 验证农险产品依赖关系
     */
    private void validateAgriculturalDependencies(Product product, String fieldName, List<FieldError> errors) {
        // 经营状态依赖验证
        if (Boolean.TRUE.equals(product.getIsOperated())) {
            if (product.getOperationDate() == null) {
                errors.add(new FieldError("operationDate", "已经营的产品必须填写经营日期"));
            }
        }
    }

    /**
     * 获取字段值
     */
    private Object getFieldValue(Product product, String fieldName) {
        try {
            Field field = Product.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(product);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("获取字段值失败: {}, 错误: {}", fieldName, e.getMessage());
            return null;
        }
    }
}