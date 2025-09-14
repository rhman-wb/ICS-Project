package com.insurance.audit.product.application.converter;

import com.insurance.audit.product.domain.entity.Document;
import com.insurance.audit.product.interfaces.dto.response.DocumentResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文档转换器
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Component
public class DocumentConverter {

    /**
     * 将DocumentResponse转换为Document实体用于持久化
     *
     * @param response DocumentResponse对象
     * @param productId 产品ID
     * @param documentType 文档类型
     * @return Document实体
     */
    public Document toEntity(DocumentResponse response, String productId, String documentType) {
        if (response == null) {
            return null;
        }

        Document document = new Document();
        document.setId(UUID.randomUUID().toString());
        document.setFileName(response.getFileName());
        document.setFilePath(response.getFilePath());
        document.setFileSize(response.getFileSize());
        document.setFileType(response.getFileType());

        // 转换文档类型
        if (documentType != null) {
            try {
                document.setDocumentType(Document.DocumentType.valueOf(documentType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // 如果转换失败，设为默认值
                document.setDocumentType(Document.DocumentType.REGISTRATION);
            }
        }

        document.setProductId(productId);

        // 转换上传状态
        if (response.getUploadStatus() != null) {
            try {
                document.setUploadStatus(Document.UploadStatus.valueOf(response.getUploadStatus()));
            } catch (IllegalArgumentException e) {
                document.setUploadStatus(Document.UploadStatus.UPLOADED);
            }
        } else {
            document.setUploadStatus(Document.UploadStatus.UPLOADED);
        }

        // 转换审核状态
        if (response.getAuditStatus() != null) {
            try {
                document.setAuditStatus(Document.AuditStatus.valueOf(response.getAuditStatus()));
            } catch (IllegalArgumentException e) {
                document.setAuditStatus(Document.AuditStatus.PENDING);
            }
        } else {
            document.setAuditStatus(Document.AuditStatus.PENDING);
        }

        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        document.setCreatedTime(now);
        document.setUpdatedTime(now);

        // 设置默认值
        document.setDeleted(false);
        document.setVersion(1);

        return document;
    }

    /**
     * 将实体转换为响应DTO
     * 
     * @param entity 文档实体
     * @return 文档响应DTO
     */
    public DocumentResponse toResponse(Document entity) {
        if (entity == null) {
            return null;
        }

        DocumentResponse.DocumentResponseBuilder builder = DocumentResponse.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .fileType(entity.getFileType())
                .productId(entity.getProductId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // 格式化文件大小
        if (entity.getFileSize() != null) {
            builder.formattedFileSize(formatFileSize(entity.getFileSize()));
        }

        // 设置要件类型
        if (entity.getDocumentType() != null) {
            builder.documentType(entity.getDocumentType().name())
                    .documentTypeDescription(entity.getDocumentType().getDescription());
        }

        // 设置上传状态
        if (entity.getUploadStatus() != null) {
            builder.uploadStatus(entity.getUploadStatus().name())
                    .uploadStatusDescription(entity.getUploadStatus().getDescription());
        }

        // 设置审核状态
        if (entity.getAuditStatus() != null) {
            builder.auditStatus(entity.getAuditStatus().name())
                    .auditStatusDescription(entity.getAuditStatus().getDescription());
        }

        // 设置审核结果统计
        if (entity.getAuditResults() != null) {
            builder.auditResultCount(entity.getAuditResults().size());
            
            int errorCount = 0;
            int warningCount = 0;
            int infoCount = 0;
            
            for (var auditResult : entity.getAuditResults()) {
                switch (auditResult.getSeverity()) {
                    case ERROR:
                        errorCount++;
                        break;
                    case WARNING:
                        warningCount++;
                        break;
                    case INFO:
                        infoCount++;
                        break;
                    default:
                        break;
                }
            }
            
            builder.errorCount(errorCount)
                    .warningCount(warningCount)
                    .infoCount(infoCount);
        } else {
            builder.auditResultCount(0)
                    .errorCount(0)
                    .warningCount(0)
                    .infoCount(0);
        }

        return builder.build();
    }

    /**
     * 将实体列表转换为响应DTO列表
     * 
     * @param entities 文档实体列表
     * @return 文档响应DTO列表
     */
    public List<DocumentResponse> toResponseList(List<Document> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 格式化文件大小
     * 
     * @param sizeInBytes 文件大小（字节）
     * @return 格式化后的文件大小
     */
    private String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null || sizeInBytes <= 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = sizeInBytes.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }
}