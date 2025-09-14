package com.insurance.audit.product.application.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.common.TestDataFactory;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.product.application.service.DocumentValidationService;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.infrastructure.mapper.ProductMapper;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;
import com.insurance.audit.product.interfaces.dto.response.DocumentValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 产品服务实现类测试
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("产品服务实现类测试")
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private DocumentValidationService documentValidationService;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        // 测试前置准备
    }

    @Test
    @DisplayName("分页查询产品 - 成功")
    void getProductPage_Success() {
        // Given
        ProductQueryRequest request = TestDataFactory.createDefaultProductQueryRequest();
        List<Product> products = TestDataFactory.createProductList(5);
        Page<Product> page = new Page<>(request.getPage(), request.getSize());
        page.setRecords(products);
        page.setTotal(5);

        when(productMapper.selectPageWithQuery(any(Page.class), eq(request))).thenReturn(page);

        // When
        IPage<Product> result = productService.getProductPage(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(5);
        assertThat(result.getTotal()).isEqualTo(5);
        assertThat(result.getCurrent()).isEqualTo((long) request.getPage());
        assertThat(result.getSize()).isEqualTo(request.getSize());

        verify(productMapper).selectPageWithQuery(any(Page.class), eq(request));
    }

    @Test
    @DisplayName("根据ID获取产品 - 成功")
    void getProductById_Success() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();

        when(productMapper.selectById(productId)).thenReturn(product);

        // When
        Product result = productService.getProductById(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getProductName()).isEqualTo(product.getProductName());

        verify(productMapper).selectById(productId);
    }

    @Test
    @DisplayName("根据ID获取产品 - 产品不存在")
    void getProductById_ProductNotFound() {
        // Given
        String productId = "non-existent-id";

        when(productMapper.selectById(productId)).thenReturn(null);

        // When
        Product result = productService.getProductById(productId);

        // Then
        assertThat(result).isNull();
        verify(productMapper).selectById(productId);
    }

    @Test
    @DisplayName("创建产品 - 成功")
    void createProduct_Success() {
        // Given
        Product product = TestDataFactory.createDefaultProduct();
        product.setId(null); // 新创建的产品ID为空

        when(productMapper.insert(any(Product.class))).thenReturn(1);

        // When
        Product result = productService.createProduct(product);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Product.ProductStatus.DRAFT);

        verify(productMapper).insert(any(Product.class));
    }

    @Test
    @DisplayName("创建产品 - 数据库插入失败")
    void createProduct_InsertFailed() {
        // Given
        Product product = TestDataFactory.createDefaultProduct();
        product.setId(null);

        when(productMapper.insert(any(Product.class))).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(product))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("创建产品失败");

        verify(productMapper).insert(any(Product.class));
    }

    @Test
    @DisplayName("更新产品 - 成功")
    void updateProduct_Success() {
        // Given
        Product product = TestDataFactory.createDefaultProduct();

        when(productMapper.updateById(product)).thenReturn(1);

        // When
        Product result = productService.updateProduct(product);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(product.getId());

        verify(productMapper).updateById(product);
    }

    @Test
    @DisplayName("更新产品 - 数据库更新失败")
    void updateProduct_UpdateFailed() {
        // Given
        Product product = TestDataFactory.createDefaultProduct();

        when(productMapper.updateById(product)).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(product))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("更新产品失败");

        verify(productMapper).updateById(product);
    }

    @Test
    @DisplayName("删除产品 - 成功")
    void deleteProduct_Success() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;

        when(productMapper.deleteById(productId)).thenReturn(1);

        // When
        boolean result = productService.deleteProduct(productId);

        // Then
        assertThat(result).isTrue();
        verify(productMapper).deleteById(productId);
    }

    @Test
    @DisplayName("删除产品 - 数据库删除失败")
    void deleteProduct_DeleteFailed() {
        // Given
        String productId = "non-existent-id";

        when(productMapper.deleteById(productId)).thenReturn(0);

        // When
        boolean result = productService.deleteProduct(productId);

        // Then
        assertThat(result).isFalse();
        verify(productMapper).deleteById(productId);
    }

    @Test
    @DisplayName("提交产品 - 成功")
    void submitProduct_Success() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        product.setStatus(Product.ProductStatus.DRAFT);
        DocumentValidationResult validationResult = TestDataFactory.createSuccessfulValidationResult(productId);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentValidationService.validateProductDocuments(productId)).thenReturn(validationResult);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When
        Product result = productService.submitProduct(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Product.ProductStatus.SUBMITTED);

        verify(productMapper).selectById(productId);
        verify(documentValidationService).validateProductDocuments(productId);
        verify(productMapper).updateById(any(Product.class));
    }

    @Test
    @DisplayName("提交产品 - 产品不存在")
    void submitProduct_ProductNotFound() {
        // Given
        String productId = "non-existent-id";

        when(productMapper.selectById(productId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> productService.submitProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("产品不存在");

        verify(productMapper).selectById(productId);
        verify(documentValidationService, never()).validateProductDocuments(anyString());
    }

    @Test
    @DisplayName("提交产品 - 产品已提交")
    void submitProduct_AlreadySubmitted() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        product.setStatus(Product.ProductStatus.SUBMITTED);

        when(productMapper.selectById(productId)).thenReturn(product);

        // When & Then
        assertThatThrownBy(() -> productService.submitProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("产品已提交，无需重复提交");

        verify(productMapper).selectById(productId);
        verify(documentValidationService, never()).validateProductDocuments(anyString());
    }

    @Test
    @DisplayName("提交产品 - 产品已审批")
    void submitProduct_AlreadyApproved() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        product.setStatus(Product.ProductStatus.APPROVED);

        when(productMapper.selectById(productId)).thenReturn(product);

        // When & Then
        assertThatThrownBy(() -> productService.submitProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("产品已审批通过，无法重新提交");

        verify(productMapper).selectById(productId);
        verify(documentValidationService, never()).validateProductDocuments(anyString());
    }

    @Test
    @DisplayName("提交产品 - 文档校验失败")
    void submitProduct_DocumentValidationFailed() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        product.setStatus(Product.ProductStatus.DRAFT);
        DocumentValidationResult validationResult = TestDataFactory.createFailedValidationResult(productId);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentValidationService.validateProductDocuments(productId)).thenReturn(validationResult);

        // When & Then
        assertThatThrownBy(() -> productService.submitProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文档校验未通过");

        verify(productMapper).selectById(productId);
        verify(documentValidationService).validateProductDocuments(productId);
        verify(productMapper, never()).updateById(any(Product.class));
    }

    @Test
    @DisplayName("提交产品 - 文档不完整")
    void submitProduct_DocumentIncomplete() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        product.setStatus(Product.ProductStatus.DRAFT);

        // 创建文档完整性不足的校验结果
        DocumentValidationResult.ValidationSummary summary = DocumentValidationResult.ValidationSummary.builder()
                .completenessPercentage(80.0)
                .build();
        DocumentValidationResult validationResult = DocumentValidationResult.builder()
                .isValid(true)
                .productId(productId)
                .summary(summary)
                .build();

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentValidationService.validateProductDocuments(productId)).thenReturn(validationResult);

        // When & Then
        assertThatThrownBy(() -> productService.submitProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文档不完整");

        verify(productMapper).selectById(productId);
        verify(documentValidationService).validateProductDocuments(productId);
        verify(productMapper, never()).updateById(any(Product.class));
    }

    @Test
    @DisplayName("提交产品 - 文档校验异常")
    void submitProduct_DocumentValidationException() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        product.setStatus(Product.ProductStatus.DRAFT);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentValidationService.validateProductDocuments(productId))
                .thenThrow(new RuntimeException("校验服务异常"));

        // When & Then
        assertThatThrownBy(() -> productService.submitProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文档校验异常");

        verify(productMapper).selectById(productId);
        verify(documentValidationService).validateProductDocuments(productId);
        verify(productMapper, never()).updateById(any(Product.class));
    }

    @Test
    @DisplayName("提交产品 - 更新状态失败")
    void submitProduct_UpdateStatusFailed() {
        // Given
        String productId = TestDataFactory.DEFAULT_PRODUCT_ID;
        Product product = TestDataFactory.createDefaultProduct();
        product.setStatus(Product.ProductStatus.DRAFT);
        DocumentValidationResult validationResult = TestDataFactory.createSuccessfulValidationResult(productId);

        when(productMapper.selectById(productId)).thenReturn(product);
        when(documentValidationService.validateProductDocuments(productId)).thenReturn(validationResult);
        when(productMapper.updateById(any(Product.class))).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> productService.submitProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("更新产品状态失败");

        verify(productMapper).selectById(productId);
        verify(documentValidationService).validateProductDocuments(productId);
        verify(productMapper).updateById(any(Product.class));
    }
}