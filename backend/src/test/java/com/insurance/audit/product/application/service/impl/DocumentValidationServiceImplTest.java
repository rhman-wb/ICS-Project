package com.insurance.audit.product.application.service.impl;

import com.insurance.audit.common.TestDataFactory;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.product.domain.entity.Document;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.infrastructure.mapper.DocumentMapper;
import com.insurance.audit.product.infrastructure.mapper.ProductMapper;
import com.insurance.audit.product.interfaces.dto.response.DocumentValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 文档验证服务实现类测试
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文档验证服务实现类测试")
class DocumentValidationServiceImplTest {

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private DocumentValidationServiceImpl documentValidationService;

    @BeforeEach
    void setUp() {
        // 测试前置准备
    }

    @Test
    @DisplayName("校验产品文档 - 成功（完整文档）")
    void validateProductDocuments_Success_CompleteDocuments() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        List<Document> completeDocuments = TestDataFactory.createCompleteDocumentSet(productId);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentMapper.selectByProductId(productId)).thenReturn(completeDocuments);

        // When
        DocumentValidationResult result = documentValidationService.validateProductDocuments(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getSummary()).isNotNull();
        assertThat(result.getSummary().getCompletenessPercentage()).isEqualTo(100.0);

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验产品文档 - 产品不存在")
    void validateProductDocuments_ProductNotFound() {
        // Given
        String productId = "non-existent-id";

        when(productMapper.selectById(productId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> documentValidationService.validateProductDocuments(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("产品不存在");

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper, never()).selectByProductId(anyString());
    }

    @Test
    @DisplayName("校验产品文档 - 文档不完整")
    void validateProductDocuments_IncompleteDocuments() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        List<Document> incompleteDocuments = TestDataFactory.createIncompleteDocumentSet(productId);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentMapper.selectByProductId(productId)).thenReturn(incompleteDocuments);

        // When
        DocumentValidationResult result = documentValidationService.validateProductDocuments(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors()).hasSize(3); // 缺少3个必需文档
        assertThat(result.getSummary().getCompletenessPercentage()).isEqualTo(40.0); // 2/5 * 100%

        // 验证错误信息
        List<String> errorMessages = result.getErrors().stream()
                .map(DocumentValidationResult.ValidationError::getMessage)
                .toList();
        assertThat(errorMessages).allMatch(msg -> msg.contains("缺少必需的要件文档"));

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验产品文档 - 文件类型不支持")
    void validateProductDocuments_UnsupportedFileType() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        List<Document> documents = TestDataFactory.createCompleteDocumentSet(productId);
        // 修改第一个文档的文件类型为不支持的类型
        documents.get(0).setFileType("TXT");

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentMapper.selectByProductId(productId)).thenReturn(documents);

        // When
        DocumentValidationResult result = documentValidationService.validateProductDocuments(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors()).anyMatch(error ->
                error.getErrorType().equals("UNSUPPORTED_FILE_TYPE"));

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验产品文档 - 文件过大")
    void validateProductDocuments_FileTooLarge() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        List<Document> documents = TestDataFactory.createCompleteDocumentSet(productId);
        // 修改第一个文档的文件大小为超大
        documents.get(0).setFileSize(60L * 1024 * 1024); // 60MB

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentMapper.selectByProductId(productId)).thenReturn(documents);

        // When
        DocumentValidationResult result = documentValidationService.validateProductDocuments(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors()).anyMatch(error ->
                error.getErrorType().equals("FILE_TOO_LARGE"));

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验产品文档 - 上传失败的文档")
    void validateProductDocuments_UploadFailedDocument() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        List<Document> documents = TestDataFactory.createCompleteDocumentSet(productId);
        // 修改第一个文档的上传状态为失败
        documents.get(0).setUploadStatus(Document.UploadStatus.FAILED);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentMapper.selectByProductId(productId)).thenReturn(documents);

        // When
        DocumentValidationResult result = documentValidationService.validateProductDocuments(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors()).anyMatch(error ->
                error.getErrorType().equals("UPLOAD_FAILED"));

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验单个文档 - 成功")
    void validateSingleDocument_Success() {
        // Given
        String documentId = TestDataFactory.DEFAULT_DOCUMENT_ID;
        Document document = TestDataFactory.createDefaultDocument();

        when(documentMapper.selectById(documentId)).thenReturn(document);

        // When
        DocumentValidationResult result = documentValidationService.validateSingleDocument(documentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getProductId()).isEqualTo(document.getProductId());
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getSummary().getTotalDocuments()).isEqualTo(1);
        assertThat(result.getSummary().getValidDocuments()).isEqualTo(1);

        verify(documentMapper).selectById(documentId);
    }

    @Test
    @DisplayName("校验单个文档 - 文档不存在")
    void validateSingleDocument_DocumentNotFound() {
        // Given
        String documentId = "non-existent-id";

        when(documentMapper.selectById(documentId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> documentValidationService.validateSingleDocument(documentId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文档不存在");

        verify(documentMapper).selectById(documentId);
    }

    @Test
    @DisplayName("校验单个文档 - 文件类型不支持")
    void validateSingleDocument_UnsupportedFileType() {
        // Given
        String documentId = TestDataFactory.DEFAULT_DOCUMENT_ID;
        Document document = TestDataFactory.createDefaultDocument();
        document.setFileType("TXT");

        when(documentMapper.selectById(documentId)).thenReturn(document);

        // When
        DocumentValidationResult result = documentValidationService.validateSingleDocument(documentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors()).anyMatch(error ->
                error.getErrorType().equals("UNSUPPORTED_FILE_TYPE"));

        verify(documentMapper).selectById(documentId);
    }

    @Test
    @DisplayName("校验文档完整性 - 完整")
    void validateDocumentCompleteness_Complete() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        List<Document> completeDocuments = TestDataFactory.createCompleteDocumentSet(productId);

        when(documentMapper.selectByProductId(productId)).thenReturn(completeDocuments);

        // When
        DocumentValidationResult result = documentValidationService.validateDocumentCompleteness(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验文档完整性 - 不完整")
    void validateDocumentCompleteness_Incomplete() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        List<Document> incompleteDocuments = TestDataFactory.createIncompleteDocumentSet(productId);

        when(documentMapper.selectByProductId(productId)).thenReturn(incompleteDocuments);

        // When
        DocumentValidationResult result = documentValidationService.validateDocumentCompleteness(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getErrors()).hasSize(3); // 缺少3个必需文档类型

        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验文档名称一致性 - 产品不存在")
    void validateDocumentNameConsistency_ProductNotFound() {
        // Given
        String productId = "non-existent-id";

        when(productMapper.selectById(productId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> documentValidationService.validateDocumentNameConsistency(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("产品不存在");

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper, never()).selectByProductId(anyString());
    }

    @Test
    @DisplayName("校验文档名称一致性 - 名称一致")
    void validateDocumentNameConsistency_Consistent() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        List<Document> documents = new ArrayList<>();
        Document document = TestDataFactory.createDefaultDocument();
        document.setFileName(product.getProductName() + "条款.docx"); // 包含产品名称
        documents.add(document);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentMapper.selectByProductId(productId)).thenReturn(documents);

        // When
        DocumentValidationResult result = documentValidationService.validateDocumentNameConsistency(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getWarnings()).isEmpty();

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验文档名称一致性 - 名称不一致")
    void validateDocumentNameConsistency_Inconsistent() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        List<Document> documents = new ArrayList<>();
        Document document = TestDataFactory.createDefaultDocument();
        document.setFileName("其他产品条款.docx"); // 不包含产品名称
        documents.add(document);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentMapper.selectByProductId(productId)).thenReturn(documents);

        // When
        DocumentValidationResult result = documentValidationService.validateDocumentNameConsistency(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getWarnings()).hasSize(1);
        assertThat(result.getWarnings().get(0).getWarningType()).isEqualTo("NAME_INCONSISTENCY");

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验文档名称一致性 - 产品名称为空")
    void validateDocumentNameConsistency_EmptyProductName() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        product.setProductName(null); // 产品名称为空

        when(productMapper.selectById(productId)).thenReturn(product);

        // When
        DocumentValidationResult result = documentValidationService.validateDocumentNameConsistency(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getWarnings()).isEmpty();

        verify(productMapper, times(2)).selectById(productId); // 调用2次：主方法1次，validateDocumentNameConsistency 1次
        verify(documentMapper, never()).selectByProductId(anyString());
    }

    @Test
    @DisplayName("校验文档格式 - 成功")
    void validateDocumentFormat_Success() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        List<Document> documents = TestDataFactory.createCompleteDocumentSet(productId);

        when(documentMapper.selectByProductId(productId)).thenReturn(documents);

        // When
        DocumentValidationResult result = documentValidationService.validateDocumentFormat(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        verify(documentMapper).selectByProductId(productId);
    }

    @Test
    @DisplayName("校验文档格式 - 格式错误")
    void validateDocumentFormat_FormatErrors() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        List<Document> documents = TestDataFactory.createCompleteDocumentSet(productId);
        // 修改文档属性制造格式错误
        documents.get(0).setFileType("TXT");      // 不支持的文件类型
        documents.get(1).setFileSize(60L * 1024 * 1024); // 文件过大
        documents.get(2).setFileName(null);       // 文件名为空
        documents.get(3).setUploadStatus(Document.UploadStatus.FAILED); // 上传失败

        when(documentMapper.selectByProductId(productId)).thenReturn(documents);

        // When
        DocumentValidationResult result = documentValidationService.validateDocumentFormat(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getErrors()).hasSize(3); // 3个错误（文件名为空算警告）
        assertThat(result.getWarnings()).hasSize(1); // 1个警告

        verify(documentMapper).selectByProductId(productId);
    }
}