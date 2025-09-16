package com.insurance.audit.rules.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 枚举值验证器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
public class EnumValidValidator implements ConstraintValidator<EnumValid, Object> {

    private Class<? extends Enum<?>> enumClass;
    private boolean allowNull;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }

        if (value instanceof Enum<?>) {
            return true;
        }

        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.trim().isEmpty()) {
                return allowNull;
            }

            // 检查字符串值是否匹配枚举
            try {
                for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                    if (enumConstant.name().equalsIgnoreCase(stringValue)) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }
}