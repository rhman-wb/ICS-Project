package com.insurance.audit.product.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;

/**
 * 产品业务服务接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
public interface ProductService {

    /**
     * 分页查询产品列表
     *
     * @param queryRequest 查询请求
     * @return 分页产品列表
     */
    IPage<Product> getProductPage(ProductQueryRequest queryRequest);

    /**
     * 根据ID获取产品详情
     *
     * @param id 产品ID
     * @return 产品实体
     */
    Product getProductById(String id);

    /**
     * 创建产品
     *
     * @param product 产品实体
     * @return 创建后的产品实体
     */
    Product createProduct(Product product);

    /**
     * 更新产品
     *
     * @param product 产品实体
     * @return 更新后的产品实体
     */
    Product updateProduct(Product product);

    /**
     * 删除产品
     *
     * @param id 产品ID
     * @return 是否删除成功
     */
    boolean deleteProduct(String id);
}