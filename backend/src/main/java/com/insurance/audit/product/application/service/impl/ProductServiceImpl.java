package com.insurance.audit.product.application.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.product.application.service.DocumentValidationService;
import com.insurance.audit.product.application.service.ProductService;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.infrastructure.mapper.ProductMapper;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;
import com.insurance.audit.product.interfaces.dto.response.DocumentValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 产品业务服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final DocumentValidationService documentValidationService;

    @Override
    public IPage<Product> getProductPage(ProductQueryRequest queryRequest) {
        log.info("查询产品列表, 查询条件: {}", queryRequest);

        // 创建分页对象
        Page<Product> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());

        // 执行分页查询
        IPage<Product> resultPage = productMapper.selectPageWithQuery(page, queryRequest);

        log.info("查询产品列表完成, 总记录数: {}, 当前页: {}, 每页大小: {}",
                resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());

        return resultPage;
    }

    @Override
    public Product getProductById(String id) {
        log.info("根据ID查询产品详情: {}", id);

        Product product = productMapper.selectById(id);

        if (product == null) {
            log.warn("未找到ID为 {} 的产品", id);
        } else {
            log.info("查询产品详情成功: {}", product.getProductName());
        }

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product createProduct(Product product) {
        log.info("创建产品: {}", product.getProductName());

        // 设置初始状态
        product.setStatus(Product.ProductStatus.DRAFT);

        // 插入数据库
        int result = productMapper.insert(product);

        if (result > 0) {
            log.info("创建产品成功, ID: {}", product.getId());
        } else {
            log.error("创建产品失败: {}", product.getProductName());
            throw new BusinessException(ErrorCode.PRODUCT_CREATE_FAILED, "创建产品失败: " + product.getProductName());
        }

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product updateProduct(Product product) {
        log.info("更新产品: {}", product.getId());

        // 更新数据库
        int result = productMapper.updateById(product);

        if (result > 0) {
            log.info("更新产品成功: {}", product.getId());
        } else {
            log.error("更新产品失败: {}", product.getId());
            throw new BusinessException(ErrorCode.PRODUCT_UPDATE_FAILED, "更新产品失败: " + product.getId());
        }

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(String id) {
        log.info("删除产品: {}", id);

        // 删除数据库记录
        int result = productMapper.deleteById(id);

        if (result > 0) {
            log.info("删除产品成功: {}", id);
            return true;
        } else {
            log.error("删除产品失败: {}", id);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product submitProduct(String productId) {
        log.info("提交产品进行审核: {}", productId);

        // 1. 获取产品信息
        Product product = getProductById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "产品不存在: " + productId);
        }

        // 2. 检查产品状态
        if (product.getStatus() == Product.ProductStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "产品已提交，无需重复提交");
        }
        if (product.getStatus() == Product.ProductStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "产品已审批通过，无法重新提交");
        }

        // 3. 校验文档完整性
        try {
            DocumentValidationResult validationResult = documentValidationService.validateProductDocuments(productId);

            if (!validationResult.getIsValid()) {
                StringBuilder errorMsg = new StringBuilder("文档校验未通过：");
                validationResult.getErrors().forEach(error ->
                    errorMsg.append("\n").append(error.getMessage()));
                throw new BusinessException(ErrorCode.DOCUMENT_VALIDATION_FAILED, errorMsg.toString());
            }

            // 检查文档完整性
            if (validationResult.getSummary().getCompletenessPercentage() < 100.0) {
                throw new BusinessException(ErrorCode.DOCUMENT_VALIDATION_FAILED,
                    "文档不完整，完整性为：" + validationResult.getSummary().getCompletenessPercentage() + "%");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文档校验异常", e);
            throw new BusinessException(ErrorCode.DOCUMENT_VALIDATION_FAILED, "文档校验异常：" + e.getMessage());
        }

        // 4. 更新产品状态为已提交
        product.setStatus(Product.ProductStatus.SUBMITTED);

        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.PRODUCT_UPDATE_FAILED, "更新产品状态失败");
        }

        log.info("产品提交成功: {}", productId);
        return product;
    }
}