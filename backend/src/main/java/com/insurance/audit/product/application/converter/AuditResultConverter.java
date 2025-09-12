package com.insurance.audit.product.application.converter;

import com.insurance.audit.product.domain.entity.AuditResult;
import com.insurance.audit.product.interfaces.dto.response.AuditResultResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核结果转换器
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Component
public class AuditResultConverter {

    /**
     * 将实体转换为响应DTO
     * 
     * @param entity 审核结果实体
     * @return 审核结果响应DTO
     */
    public AuditResultResponse toResponse(AuditResult entity) {
        if (entity == null) {
            return null;
        }

        AuditResultResponse.AuditResultResponseBuilder builder = AuditResultResponse.builder()
                .id(entity.getId())
                .documentId(entity.getDocumentId())
                .ruleId(entity.getRuleId())
                .ruleType(entity.getRuleType())
                .applicableChapter(entity.getApplicableChapter())
                .ruleSource(entity.getRuleSource())
                .originalContent(entity.getOriginalContent())
                .suggestion(entity.getSuggestion())
                .positionStart(entity.getPositionStart())
                .positionEnd(entity.getPositionEnd())
                .chapterNumber(entity.getChapterNumber())
                .pageNumber(entity.getPageNumber())
                .lineNumber(entity.getLineNumber())
                .auditTime(entity.getAuditTime())
                .createdAt(entity.getCreatedAt());

        // 设置严重程度
        if (entity.getSeverity() != null) {
            builder.severity(entity.getSeverity().name())
                    .severityDescription(entity.getSeverity().getDescription());
        }

        return builder.build();
    }

    /**
     * 将实体列表转换为响应DTO列表
     * 
     * @param entities 审核结果实体列表
     * @return 审核结果响应DTO列表
     */
    public List<AuditResultResponse> toResponseList(List<AuditResult> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}