package com.insurance.audit.rules.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 枚举值验证注解
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidValidator.class)
@Documented
public @interface EnumValid {

    String message() default "Invalid enum value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 枚举类
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 是否允许空值
     */
    boolean allowNull() default true;
}