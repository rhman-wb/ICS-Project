package com.insurance.audit.product.application.service.impl;

import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.product.application.service.DocumentValidationService;
import com.insurance.audit.product.domain.entity.Document;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.infrastructure.mapper.DocumentMapper;
import com.insurance.audit.product.infrastructure.mapper.ProductMapper;
import com.insurance.audit.product.interfaces.dto.response.DocumentValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文档校验服务实现
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentValidationServiceImpl implements DocumentValidationService {

    private final DocumentMapper documentMapper;
    private final ProductMapper productMapper;

    /**
     * 必需的要件类型列表
     */
    private static final List<Document.DocumentType> REQUIRED_DOCUMENT_TYPES = Arrays.asList(
        Document.DocumentType.REGISTRATION,    // 产品信息登记表
        Document.DocumentType.TERMS,           // 条款
        Document.DocumentType.FEASIBILITY_REPORT, // 可行性报告
        Document.DocumentType.ACTUARIAL_REPORT,   // 精算报告
        Document.DocumentType.RATE_TABLE       // 费率表
    );

    /**
     * 支持的文件类型
     */
    private static final Set<String> SUPPORTED_FILE_TYPES = Set.of("DOCX", "PDF", "XLS", "XLSX");

    /**
     * 最大文件大小 (20MB)
     */
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    @Override
    public DocumentValidationResult validateProductDocuments(String productId) {
        log.info("开始校验产品文档, productId: {}", productId);

        // 检查产品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "产品不存在: " + productId);
        }

        // 获取产品的所有文档
        List<Document> documents = documentMapper.selectByProductId(productId);

        List<DocumentValidationResult.ValidationError> errors = new ArrayList<>();
        List<DocumentValidationResult.ValidationWarning> warnings = new ArrayList<>();

        // 1. 校验文档完整性
        DocumentValidationResult completenessResult = validateDocumentCompleteness(productId);
        if (completenessResult.getErrors() != null) {
            errors.addAll(completenessResult.getErrors());
        }
        if (completenessResult.getWarnings() != null) {
            warnings.addAll(completenessResult.getWarnings());
        }

        // 2. 校验文档名称一致性
        DocumentValidationResult nameConsistencyResult = validateDocumentNameConsistency(productId);
        if (nameConsistencyResult.getErrors() != null) {
            errors.addAll(nameConsistencyResult.getErrors());
        }
        if (nameConsistencyResult.getWarnings() != null) {
            warnings.addAll(nameConsistencyResult.getWarnings());
        }

        // 3. 校验文档格式
        DocumentValidationResult formatResult = validateDocumentFormat(productId);
        if (formatResult.getErrors() != null) {
            errors.addAll(formatResult.getErrors());
        }
        if (formatResult.getWarnings() != null) {
            warnings.addAll(formatResult.getWarnings());
        }

        // 构建校验摘要
        DocumentValidationResult.ValidationSummary summary = buildValidationSummary(documents, errors, warnings);

        boolean isValid = errors.isEmpty();

        log.info("产品文档校验完成, productId: {}, isValid: {}, errors: {}, warnings: {}",
                productId, isValid, errors.size(), warnings.size());

        return DocumentValidationResult.builder()
            .isValid(isValid)
            .productId(productId)
            .errors(errors)
            .warnings(warnings)
            .summary(summary)
            .build();
    }

    @Override
    public DocumentValidationResult validateSingleDocument(String documentId) {
        log.info("开始校验单个文档, documentId: {}", documentId);

        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND, "文档不存在: " + documentId);
        }

        List<DocumentValidationResult.ValidationError> errors = new ArrayList<>();
        List<DocumentValidationResult.ValidationWarning> warnings = new ArrayList<>();

        // 校验文档格式
        validateSingleDocumentFormat(document, errors, warnings);

        boolean isValid = errors.isEmpty();

        return DocumentValidationResult.builder()
            .isValid(isValid)
            .productId(document.getProductId())
            .errors(errors)
            .warnings(warnings)
            .summary(DocumentValidationResult.ValidationSummary.builder()
                .totalDocuments(1)
                .validDocuments(isValid ? 1 : 0)
                .totalErrors(errors.size())
                .totalWarnings(warnings.size())
                .build())
            .build();
    }

    @Override
    public DocumentValidationResult validateDocumentCompleteness(String productId) {
        List<Document> documents = documentMapper.selectByProductId(productId);
        List<DocumentValidationResult.ValidationError> errors = new ArrayList<>();

        // 获取已上传的要件类型
        Set<Document.DocumentType> uploadedTypes = documents.stream()
            .filter(doc -> doc.getUploadStatus() == Document.UploadStatus.UPLOADED
                        || doc.getUploadStatus() == Document.UploadStatus.PROCESSED)
            .map(Document::getDocumentType)
            .collect(Collectors.toSet());

        // 检查缺失的必需要件
        for (Document.DocumentType requiredType : REQUIRED_DOCUMENT_TYPES) {
            if (!uploadedTypes.contains(requiredType)) {
                errors.add(DocumentValidationResult.ValidationError.builder()
                    .errorType("MISSING_DOCUMENT")
                    .errorCode("DOC_001")
                    .message("缺少必需的要件文档")
                    .documentType(requiredType.name())
                    .severity("HIGH")
                    .suggestion("请上传" + requiredType.getDescription() + "文档")
                    .build());
            }
        }

        boolean isValid = errors.isEmpty();

        return DocumentValidationResult.builder()
            .isValid(isValid)
            .productId(productId)
            .errors(errors)
            .warnings(new ArrayList<>())
            .build();
    }

    @Override
    public DocumentValidationResult validateDocumentNameConsistency(String productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "产品不存在: " + productId);
        }

        List<Document> documents = documentMapper.selectByProductId(productId);
        List<DocumentValidationResult.ValidationWarning> warnings = new ArrayList<>();

        String productName = product.getProductName();
        if (productName == null || productName.trim().isEmpty()) {
            return DocumentValidationResult.builder()
                .isValid(true)
                .productId(productId)
                .errors(new ArrayList<>())
                .warnings(warnings)
                .build();
        }

        // 检查文档名称与产品名称的一致性
        for (Document document : documents) {
            if (document.getFileName() != null &&
                !document.getFileName().toLowerCase().contains(productName.toLowerCase())) {

                warnings.add(DocumentValidationResult.ValidationWarning.builder()
                    .warningType("NAME_INCONSISTENCY")
                    .warningCode("WARN_001")
                    .message("文档名称与产品名称不一致")
                    .documentType(document.getDocumentType().name())
                    .documentId(document.getId())
                    .recommendation("建议检查并统一产品名称，确保文档名称包含产品名称")
                    .build());
            }
        }

        return DocumentValidationResult.builder()
            .isValid(true)
            .productId(productId)
            .errors(new ArrayList<>())
            .warnings(warnings)
            .build();
    }

    @Override
    public DocumentValidationResult validateDocumentFormat(String productId) {
        List<Document> documents = documentMapper.selectByProductId(productId);
        List<DocumentValidationResult.ValidationError> errors = new ArrayList<>();
        List<DocumentValidationResult.ValidationWarning> warnings = new ArrayList<>();

        for (Document document : documents) {
            validateSingleDocumentFormat(document, errors, warnings);
        }

        boolean isValid = errors.isEmpty();

        return DocumentValidationResult.builder()
            .isValid(isValid)
            .productId(productId)
            .errors(errors)
            .warnings(warnings)
            .build();
    }

    /**
     * 校验单个文档格式
     */
    private void validateSingleDocumentFormat(Document document,
                                            List<DocumentValidationResult.ValidationError> errors,
                                            List<DocumentValidationResult.ValidationWarning> warnings) {

        // 检查文件类型
        if (document.getFileType() == null || !SUPPORTED_FILE_TYPES.contains(document.getFileType().toUpperCase())) {
            errors.add(DocumentValidationResult.ValidationError.builder()
                .errorType("UNSUPPORTED_FILE_TYPE")
                .errorCode("DOC_002")
                .message("不支持的文件类型")
                .documentType(document.getDocumentType().name())
                .documentId(document.getId())
                .severity("MEDIUM")
                .suggestion("请上传支持的文件格式：" + String.join(", ", SUPPORTED_FILE_TYPES))
                .build());
        }

        // 检查文件大小
        if (document.getFileSize() != null && document.getFileSize() > MAX_FILE_SIZE) {
            errors.add(DocumentValidationResult.ValidationError.builder()
                .errorType("FILE_TOO_LARGE")
                .errorCode("DOC_003")
                .message("文件过大")
                .documentType(document.getDocumentType().name())
                .documentId(document.getId())
                .severity("MEDIUM")
                .suggestion("文件大小不能超过20MB，请压缩文件或分割后上传")
                .build());
        }

        // 检查文件名
        if (document.getFileName() == null || document.getFileName().trim().isEmpty()) {
            warnings.add(DocumentValidationResult.ValidationWarning.builder()
                .warningType("EMPTY_FILENAME")
                .warningCode("WARN_002")
                .message("文件名为空")
                .documentType(document.getDocumentType().name())
                .documentId(document.getId())
                .recommendation("建议设置有意义的文件名")
                .build());
        }

        // 检查上传状态
        if (document.getUploadStatus() == Document.UploadStatus.FAILED) {
            errors.add(DocumentValidationResult.ValidationError.builder()
                .errorType("UPLOAD_FAILED")
                .errorCode("DOC_004")
                .message("文档上传失败")
                .documentType(document.getDocumentType().name())
                .documentId(document.getId())
                .severity("HIGH")
                .suggestion("请重新上传文档")
                .build());
        }
    }

    /**
     * 构建校验摘要
     */
    private DocumentValidationResult.ValidationSummary buildValidationSummary(List<Document> documents,
                                                                             List<DocumentValidationResult.ValidationError> errors,
                                                                             List<DocumentValidationResult.ValidationWarning> warnings) {

        int totalDocuments = documents.size();
        int requiredDocuments = REQUIRED_DOCUMENT_TYPES.size();

        long uploadedDocuments = documents.stream()
            .filter(doc -> doc.getUploadStatus() == Document.UploadStatus.UPLOADED
                        || doc.getUploadStatus() == Document.UploadStatus.PROCESSED)
            .count();

        // 计算通过校验的文档数（无错误的文档）
        Set<String> errorDocumentIds = errors.stream()
            .map(DocumentValidationResult.ValidationError::getDocumentId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());

        long validDocuments = documents.stream()
            .filter(doc -> !errorDocumentIds.contains(doc.getId()))
            .count();

        double completenessPercentage = requiredDocuments > 0
            ? (double) uploadedDocuments / requiredDocuments * 100.0
            : 100.0;

        return DocumentValidationResult.ValidationSummary.builder()
            .totalDocuments(totalDocuments)
            .requiredDocuments(requiredDocuments)
            .uploadedDocuments((int) uploadedDocuments)
            .validDocuments((int) validDocuments)
            .totalErrors(errors.size())
            .totalWarnings(warnings.size())
            .completenessPercentage(Math.round(completenessPercentage * 100.0) / 100.0)
            .build();
    }
}