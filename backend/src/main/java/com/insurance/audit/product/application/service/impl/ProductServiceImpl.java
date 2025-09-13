package com.insurance.audit.product.application.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.product.application.service.ProductService;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.infrastructure.mapper.ProductMapper;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;
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
            throw new RuntimeException("创建产品失败");
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
            throw new RuntimeException("更新产品失败");
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
}