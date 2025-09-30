package com.insurance.audit.product.application.service;

import com.insurance.audit.product.domain.entity.ProductTemplate;
import com.insurance.audit.product.interfaces.dto.TemplateFieldConfig;
import com.insurance.audit.product.interfaces.dto.ValidationRule;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * 模板管理服务接口
 * 提供模板下载、配置管理、元数据查询等功能
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
public interface TemplateService {

    /**
     * 根据模板类型下载模板文件
     *
     * @param templateType 模板类型 (FILING/AGRICULTURAL)
     * @return 模板文件资源
     * @throws IOException 文件读取异常
     */
    Resource downloadTemplate(String templateType) throws IOException;

    /**
     * 根据模板ID下载模板文件
     *
     * @param templateId 模板ID
     * @return 模板文件资源
     * @throws IOException 文件读取异常
     */
    Resource downloadTemplateById(String templateId) throws IOException;

    /**
     * 获取模板配置信息
     *
     * @param templateType 模板类型
     * @return 模板配置对象
     */
    ProductTemplate getTemplateConfig(String templateType);

    /**
     * 根据ID获取模板配置信息
     *
     * @param templateId 模板ID
     * @return 模板配置对象
     */
    ProductTemplate getTemplateConfigById(String templateId);

    /**
     * 获取模板字段配置列表
     *
     * @param templateType 模板类型
     * @return 字段配置列表
     */
    List<TemplateFieldConfig> getTemplateFields(String templateType);

    /**
     * 获取模板验证规则列表
     *
     * @param templateType 模板类型
     * @return 验证规则列表
     */
    List<ValidationRule> getValidationRules(String templateType);

    /**
     * 获取所有启用的模板列表
     *
     * @return 启用的模板列表
     */
    List<ProductTemplate> getEnabledTemplates();

    /**
     * 获取所有模板列表
     *
     * @param includeDisabled 是否包含禁用的模板
     * @return 模板列表
     */
    List<ProductTemplate> getAllTemplates(boolean includeDisabled);

    /**
     * 创建新模板配置
     *
     * @param template 模板配置
     * @return 创建的模板
     */
    ProductTemplate createTemplate(ProductTemplate template);

    /**
     * 更新模板配置
     *
     * @param template 模板配置
     * @return 更新后的模板
     */
    ProductTemplate updateTemplate(ProductTemplate template);

    /**
     * 启用模板
     *
     * @param templateId 模板ID
     * @return 是否成功
     */
    boolean enableTemplate(String templateId);

    /**
     * 禁用模板
     *
     * @param templateId 模板ID
     * @return 是否成功
     */
    boolean disableTemplate(String templateId);

    /**
     * 删除模板
     *
     * @param templateId 模板ID
     * @return 是否成功
     */
    boolean deleteTemplate(String templateId);

    /**
     * 验证模板类型是否有效
     *
     * @param templateType 模板类型
     * @return 是否有效
     */
    boolean isValidTemplateType(String templateType);

    /**
     * 获取模板元数据（名称、版本、描述等）
     *
     * @param templateType 模板类型
     * @return 模板元数据
     */
    ProductTemplate getTemplateMetadata(String templateType);
}