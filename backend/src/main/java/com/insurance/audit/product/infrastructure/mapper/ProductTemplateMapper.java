package com.insurance.audit.product.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insurance.audit.product.domain.entity.ProductTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 产品模板数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-29
 */
@Mapper
public interface ProductTemplateMapper extends BaseMapper<ProductTemplate> {

    /**
     * 根据模板类型查询启用的模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    @Select("SELECT * FROM product_templates WHERE template_type = #{templateType} AND enabled = 1 AND is_deleted = 0 ORDER BY sort_order ASC, created_at ASC")
    List<ProductTemplate> selectEnabledByType(@Param("templateType") String templateType);

    /**
     * 根据模板类型和版本查询模板
     *
     * @param templateType 模板类型
     * @param templateVersion 模板版本
     * @return 模板对象
     */
    @Select("SELECT * FROM product_templates WHERE template_type = #{templateType} AND template_version = #{templateVersion} AND is_deleted = 0")
    ProductTemplate selectByTypeAndVersion(@Param("templateType") String templateType,
                                          @Param("templateVersion") String templateVersion);

    /**
     * 查询所有启用的模板列表
     *
     * @return 模板列表
     */
    @Select("SELECT * FROM product_templates WHERE enabled = 1 AND is_deleted = 0 ORDER BY template_type ASC, sort_order ASC")
    List<ProductTemplate> selectAllEnabled();

    /**
     * 根据模板类型查询最新版本的模板
     *
     * @param templateType 模板类型
     * @return 模板对象
     */
    @Select("SELECT * FROM product_templates WHERE template_type = #{templateType} AND enabled = 1 AND is_deleted = 0 ORDER BY template_version DESC LIMIT 1")
    ProductTemplate selectLatestByType(@Param("templateType") String templateType);

    /**
     * 统计指定类型的模板数量
     *
     * @param templateType 模板类型
     * @return 模板数量
     */
    @Select("SELECT COUNT(*) FROM product_templates WHERE template_type = #{templateType} AND is_deleted = 0")
    Long countByType(@Param("templateType") String templateType);

    /**
     * 检查模板类型和版本是否已存在
     *
     * @param templateType 模板类型
     * @param templateVersion 模板版本
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 存在的数量
     */
    @Select("SELECT COUNT(*) FROM product_templates WHERE template_type = #{templateType} AND template_version = #{templateVersion} AND is_deleted = 0 AND id != #{excludeId}")
    Long countByTypeAndVersionExcludeId(@Param("templateType") String templateType,
                                       @Param("templateVersion") String templateVersion,
                                       @Param("excludeId") String excludeId);
}