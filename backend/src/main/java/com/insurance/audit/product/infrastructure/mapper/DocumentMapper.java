package com.insurance.audit.product.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insurance.audit.product.domain.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 根据产品ID查询文档列表
     *
     * @param productId 产品ID
     * @return 文档列表
     */
    List<Document> selectByProductId(@Param("productId") String productId);

    /**
     * 根据产品ID和要件类型查询文档
     *
     * @param productId 产品ID
     * @param documentType 要件类型
     * @return 文档
     */
    Document selectByProductIdAndType(@Param("productId") String productId,
                                     @Param("documentType") Document.DocumentType documentType);

    /**
     * 统计产品的文档数量
     *
     * @param productId 产品ID
     * @return 文档数量
     */
    Integer countByProductId(@Param("productId") String productId);

    /**
     * 根据产品ID和上传状态查询文档列表
     *
     * @param productId 产品ID
     * @param uploadStatus 上传状态
     * @return 文档列表
     */
    List<Document> selectByProductIdAndStatus(@Param("productId") String productId,
                                            @Param("uploadStatus") Document.UploadStatus uploadStatus);
}