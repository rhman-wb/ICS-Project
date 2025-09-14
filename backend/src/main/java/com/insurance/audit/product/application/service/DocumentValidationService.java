package com.insurance.audit.product.application.service;

import com.insurance.audit.product.interfaces.dto.response.DocumentValidationResult;

/**
 * 文档校验服务接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
public interface DocumentValidationService {

    /**
     * 校验产品的所有文档
     *
     * @param productId 产品ID
     * @return 校验结果
     */
    DocumentValidationResult validateProductDocuments(String productId);

    /**
     * 校验单个文档
     *
     * @param documentId 文档ID
     * @return 校验结果
     */
    DocumentValidationResult validateSingleDocument(String documentId);

    /**
     * 校验文档完整性
     * 检查是否上传了所有必需的要件文档
     *
     * @param productId 产品ID
     * @return 校验结果
     */
    DocumentValidationResult validateDocumentCompleteness(String productId);

    /**
     * 校验文档名称一致性
     * 检查文档名称与产品名称是否一致
     *
     * @param productId 产品ID
     * @return 校验结果
     */
    DocumentValidationResult validateDocumentNameConsistency(String productId);

    /**
     * 校验文档格式和类型
     * 检查文档格式是否符合要求
     *
     * @param productId 产品ID
     * @return 校验结果
     */
    DocumentValidationResult validateDocumentFormat(String productId);
}