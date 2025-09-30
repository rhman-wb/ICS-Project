package com.insurance.audit.product.application.service;

import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.interfaces.dto.ValidationRule;

import java.util.List;
import java.util.Map;

/**
 * 字段验证服务接口
 * 提供基于模板规则的字段验证功能
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
public interface FieldValidationService {

    /**
     * 验证产品所有字段
     *
     * @param product 产品实体
     * @return 验证结果
     */
    FieldValidationResult validateProduct(Product product);

    /**
     * 根据模板类型验证产品字段
     *
     * @param product 产品实体
     * @param templateType 模板类型
     * @return 验证结果
     */
    FieldValidationResult validateProductByTemplate(Product product, String templateType);

    /**
     * 验证单个字段
     *
     * @param fieldName 字段名称
     * @param fieldValue 字段值
     * @param validationRules 验证规则列表
     * @return 验证结果
     */
    FieldValidationResult validateField(String fieldName, Object fieldValue, List<ValidationRule> validationRules);

    /**
     * 验证字段依赖关系
     *
     * @param product 产品实体
     * @param fieldName 字段名称
     * @return 验证结果
     */
    FieldValidationResult validateFieldDependencies(Product product, String fieldName);

    /**
     * 批量验证字段
     *
     * @param fieldValues 字段值映射
     * @param templateType 模板类型
     * @return 验证结果
     */
    FieldValidationResult validateFields(Map<String, Object> fieldValues, String templateType);

    /**
     * 字段验证结果类
     */
    class FieldValidationResult {
        private boolean valid;
        private List<FieldError> errors;
        private Map<String, Object> validatedFields;

        public FieldValidationResult() {
        }

        public FieldValidationResult(boolean valid, List<FieldError> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public List<FieldError> getErrors() {
            return errors;
        }

        public void setErrors(List<FieldError> errors) {
            this.errors = errors;
        }

        public Map<String, Object> getValidatedFields() {
            return validatedFields;
        }

        public void setValidatedFields(Map<String, Object> validatedFields) {
            this.validatedFields = validatedFields;
        }
    }

    /**
     * 字段错误类
     */
    class FieldError {
        private String fieldName;
        private String message;
        private String errorCode;
        private Object rejectedValue;

        public FieldError() {
        }

        public FieldError(String fieldName, String message) {
            this.fieldName = fieldName;
            this.message = message;
        }

        public FieldError(String fieldName, String message, String errorCode, Object rejectedValue) {
            this.fieldName = fieldName;
            this.message = message;
            this.errorCode = errorCode;
            this.rejectedValue = rejectedValue;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
    }
}