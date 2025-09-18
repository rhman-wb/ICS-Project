package com.insurance.audit.rules.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则审核状态枚举
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Getter
@AllArgsConstructor
public enum RuleAuditStatus implements IEnum<String> {

    /**
     * 草稿状态
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 待审核状态
     */
    PENDING("PENDING", "待审核"),

    /**
     * 已通过状态
     */
    APPROVED("APPROVED", "已通过"),

    /**
     * 已驳回状态
     */
    REJECTED("REJECTED", "已驳回"),

    /**
     * 生效状态
     */
    EFFECTIVE("EFFECTIVE", "生效"),

    /**
     * 已取消状态
     */
    CANCELLED("CANCELLED", "已取消"),

    /**
     * 待技术评估
     */
    PENDING_TECH_EVALUATION("PENDING_TECH_EVALUATION", "待技术评估"),

    /**
     * 待提交OA审核
     */
    PENDING_OA_SUBMISSION("PENDING_OA_SUBMISSION", "待提交OA审核"),

    /**
     * 已提交OA审核
     */
    SUBMITTED_TO_OA("SUBMITTED_TO_OA", "已提交OA审核"),

    /**
     * OA审核通过
     */
    OA_APPROVED("OA_APPROVED", "OA审核通过"),

    /**
     * OA审核驳回
     */
    OA_REJECTED("OA_REJECTED", "OA审核驳回"),

    /**
     * 技术评估通过
     */
    TECH_APPROVED("TECH_APPROVED", "技术评估通过"),

    /**
     * 技术评估驳回
     */
    TECH_REJECTED("TECH_REJECTED", "技术评估驳回");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    @Override
    public String getValue() {
        return this.code;
    }

    public static RuleAuditStatus fromCode(String code) {
        for (RuleAuditStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown rule audit status code: " + code);
    }
}