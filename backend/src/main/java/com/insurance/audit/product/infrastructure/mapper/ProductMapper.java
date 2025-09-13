package com.insurance.audit.product.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 产品数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分页查询产品列表（带条件筛选）
     *
     * @param page 分页参数
     * @param queryRequest 查询条件
     * @return 分页产品列表
     */
    IPage<Product> selectPageWithQuery(@Param("page") Page<Product> page,
                                      @Param("query") ProductQueryRequest queryRequest);
}