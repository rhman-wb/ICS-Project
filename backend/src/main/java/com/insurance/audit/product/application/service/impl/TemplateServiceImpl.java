package com.insurance.audit.product.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.product.application.service.FileStorageService;
import com.insurance.audit.product.application.service.TemplateService;
import com.insurance.audit.product.domain.entity.ProductTemplate;
import com.insurance.audit.product.infrastructure.mapper.ProductTemplateMapper;
import com.insurance.audit.product.interfaces.dto.TemplateFieldConfig;
import com.insurance.audit.product.interfaces.dto.ValidationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 模板管理服务实现类
 * 提供模板下载、配置管理、元数据查询等功能
 * 集成文件存储服务和Redis缓存机制
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final ProductTemplateMapper templateMapper;
    private final FileStorageService fileStorageService;

    private static final String CACHE_NAME = "product_templates";
    private static final List<String> VALID_TEMPLATE_TYPES = Arrays.asList(
            ProductTemplate.TemplateType.FILING.name(),
            ProductTemplate.TemplateType.AGRICULTURAL.name()
    );

    @Override
    @Cacheable(value = CACHE_NAME, key = "'download_' + #templateType", unless = "#result == null")
    public Resource downloadTemplate(String templateType) throws IOException {
        log.info("下载模板, 模板类型: {}", templateType);

        // 验证模板类型
        if (!isValidTemplateType(templateType)) {
            log.error("无效的模板类型: {}", templateType);
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "无效的模板类型: " + templateType);
        }

        // 获取模板配置
        ProductTemplate template = getTemplateConfig(templateType);
        if (template == null) {
            log.error("未找到模板配置, 模板类型: {}", templateType);
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "未找到模板配置: " + templateType);
        }

        return downloadTemplateById(template.getId());
    }

    @Override
    public Resource downloadTemplateById(String templateId) throws IOException {
        log.info("根据ID下载模板, 模板ID: {}", templateId);

        ProductTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            log.error("未找到模板, ID: {}", templateId);
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "未找到模板: " + templateId);
        }

        if (!template.getEnabled()) {
            log.warn("模板已禁用, ID: {}", templateId);
            throw new BusinessException(ErrorCode.TEMPLATE_DISABLED, "模板已禁用: " + template.getTemplateName());
        }

        // 获取模板文件完整路径
        String fullPath = fileStorageService.getFullPath(template.getTemplateFilePath());
        File file = new File(fullPath);

        if (!file.exists() || !file.isFile()) {
            log.error("模板文件不存在: {}", fullPath);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "模板文件不存在: " + template.getTemplateName());
        }

        log.info("模板下载成功, 文件: {}", template.getTemplateName());
        return new FileSystemResource(file);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'config_' + #templateType", unless = "#result == null")
    public ProductTemplate getTemplateConfig(String templateType) {
        log.info("获取模板配置, 模板类型: {}", templateType);

        LambdaQueryWrapper<ProductTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductTemplate::getTemplateType, templateType)
                .eq(ProductTemplate::getEnabled, true)
                .orderByDesc(ProductTemplate::getTemplateVersion)
                .last("LIMIT 1");

        ProductTemplate template = templateMapper.selectOne(wrapper);

        if (template == null) {
            log.warn("未找到模板配置, 模板类型: {}", templateType);
        } else {
            log.info("获取模板配置成功: {}", template.getTemplateName());
        }

        return template;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'config_id_' + #templateId", unless = "#result == null")
    public ProductTemplate getTemplateConfigById(String templateId) {
        log.info("根据ID获取模板配置, 模板ID: {}", templateId);

        ProductTemplate template = templateMapper.selectById(templateId);

        if (template == null) {
            log.warn("未找到模板配置, ID: {}", templateId);
        } else {
            log.info("获取模板配置成功: {}", template.getTemplateName());
        }

        return template;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'fields_' + #templateType", unless = "#result == null || #result.isEmpty()")
    public List<TemplateFieldConfig> getTemplateFields(String templateType) {
        log.info("获取模板字段配置, 模板类型: {}", templateType);

        ProductTemplate template = getTemplateConfig(templateType);
        if (template == null) {
            log.warn("未找到模板配置, 无法获取字段配置, 模板类型: {}", templateType);
            return Collections.emptyList();
        }

        List<TemplateFieldConfig> fields = template.getFieldConfig();
        log.info("获取模板字段配置成功, 字段数量: {}", fields != null ? fields.size() : 0);

        return fields != null ? fields : Collections.emptyList();
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'rules_' + #templateType", unless = "#result == null || #result.isEmpty()")
    public List<ValidationRule> getValidationRules(String templateType) {
        log.info("获取模板验证规则, 模板类型: {}", templateType);

        ProductTemplate template = getTemplateConfig(templateType);
        if (template == null) {
            log.warn("未找到模板配置, 无法获取验证规则, 模板类型: {}", templateType);
            return Collections.emptyList();
        }

        List<ValidationRule> rules = template.getValidationRules();
        log.info("获取模板验证规则成功, 规则数量: {}", rules != null ? rules.size() : 0);

        return rules != null ? rules : Collections.emptyList();
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'enabled_templates'", unless = "#result == null || #result.isEmpty()")
    public List<ProductTemplate> getEnabledTemplates() {
        log.info("获取所有启用的模板");

        LambdaQueryWrapper<ProductTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductTemplate::getEnabled, true)
                .orderBy(true, true, ProductTemplate::getSortOrder);

        List<ProductTemplate> templates = templateMapper.selectList(wrapper);
        log.info("获取启用模板成功, 数量: {}", templates.size());

        return templates;
    }

    @Override
    public List<ProductTemplate> getAllTemplates(boolean includeDisabled) {
        log.info("获取所有模板, 包含禁用: {}", includeDisabled);

        LambdaQueryWrapper<ProductTemplate> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(ProductTemplate::getEnabled, true);
        }
        wrapper.orderBy(true, true, ProductTemplate::getSortOrder);

        List<ProductTemplate> templates = templateMapper.selectList(wrapper);
        log.info("获取模板列表成功, 数量: {}", templates.size());

        return templates;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public ProductTemplate createTemplate(ProductTemplate template) {
        log.info("创建模板配置: {}", template.getTemplateName());

        // 验证模板类型
        if (!isValidTemplateType(template.getTemplateType())) {
            log.error("无效的模板类型: {}", template.getTemplateType());
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "无效的模板类型");
        }

        // 检查模板类型是否已存在
        ProductTemplate existingTemplate = getTemplateConfig(template.getTemplateType());
        if (existingTemplate != null) {
            log.warn("模板类型已存在, 将创建新版本: {}", template.getTemplateType());
        }

        // 设置默认值
        if (template.getEnabled() == null) {
            template.setEnabled(true);
        }
        if (template.getSortOrder() == null) {
            template.setSortOrder(0);
        }

        int result = templateMapper.insert(template);
        if (result > 0) {
            log.info("创建模板配置成功, ID: {}", template.getId());
        } else {
            log.error("创建模板配置失败: {}", template.getTemplateName());
            throw new BusinessException(ErrorCode.TEMPLATE_CREATE_FAILED, "创建模板配置失败");
        }

        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public ProductTemplate updateTemplate(ProductTemplate template) {
        log.info("更新模板配置, ID: {}", template.getId());

        // 验证模板是否存在
        ProductTemplate existingTemplate = templateMapper.selectById(template.getId());
        if (existingTemplate == null) {
            log.error("模板不存在, ID: {}", template.getId());
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在");
        }

        // 验证模板类型
        if (template.getTemplateType() != null && !isValidTemplateType(template.getTemplateType())) {
            log.error("无效的模板类型: {}", template.getTemplateType());
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "无效的模板类型");
        }

        int result = templateMapper.updateById(template);
        if (result > 0) {
            log.info("更新模板配置成功, ID: {}", template.getId());
        } else {
            log.error("更新模板配置失败, ID: {}", template.getId());
            throw new BusinessException(ErrorCode.TEMPLATE_UPDATE_FAILED, "更新模板配置失败");
        }

        return templateMapper.selectById(template.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean enableTemplate(String templateId) {
        log.info("启用模板, ID: {}", templateId);

        ProductTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            log.error("模板不存在, ID: {}", templateId);
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在");
        }

        template.setEnabled(true);
        int result = templateMapper.updateById(template);

        boolean success = result > 0;
        if (success) {
            log.info("启用模板成功, ID: {}", templateId);
        } else {
            log.error("启用模板失败, ID: {}", templateId);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean disableTemplate(String templateId) {
        log.info("禁用模板, ID: {}", templateId);

        ProductTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            log.error("模板不存在, ID: {}", templateId);
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在");
        }

        template.setEnabled(false);
        int result = templateMapper.updateById(template);

        boolean success = result > 0;
        if (success) {
            log.info("禁用模板成功, ID: {}", templateId);
        } else {
            log.error("禁用模板失败, ID: {}", templateId);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean deleteTemplate(String templateId) {
        log.info("删除模板, ID: {}", templateId);

        ProductTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            log.warn("模板不存在, 无需删除, ID: {}", templateId);
            return true;
        }

        // 删除模板文件
        if (template.getTemplateFilePath() != null) {
            try {
                fileStorageService.deleteFile(template.getTemplateFilePath());
                log.info("删除模板文件成功: {}", template.getTemplateFilePath());
            } catch (Exception e) {
                log.warn("删除模板文件失败: {}, 错误: {}", template.getTemplateFilePath(), e.getMessage());
            }
        }

        int result = templateMapper.deleteById(templateId);

        boolean success = result > 0;
        if (success) {
            log.info("删除模板成功, ID: {}", templateId);
        } else {
            log.error("删除模板失败, ID: {}", templateId);
        }

        return success;
    }

    @Override
    public boolean isValidTemplateType(String templateType) {
        if (templateType == null || templateType.trim().isEmpty()) {
            return false;
        }
        return VALID_TEMPLATE_TYPES.contains(templateType.toUpperCase());
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'metadata_' + #templateType", unless = "#result == null")
    public ProductTemplate getTemplateMetadata(String templateType) {
        log.info("获取模板元数据, 模板类型: {}", templateType);

        ProductTemplate template = getTemplateConfig(templateType);
        if (template == null) {
            log.warn("未找到模板元数据, 模板类型: {}", templateType);
            return null;
        }

        // 创建仅包含元数据的对象（不包含大字段配置）
        ProductTemplate metadata = ProductTemplate.builder()
                .id(template.getId())
                .templateType(template.getTemplateType())
                .templateName(template.getTemplateName())
                .templateVersion(template.getTemplateVersion())
                .description(template.getDescription())
                .enabled(template.getEnabled())
                .fileSize(template.getFileSize())
                .mimeType(template.getMimeType())
                .sortOrder(template.getSortOrder())
                .templateFilePath(template.getTemplateFilePath())
                .build();

        log.info("获取模板元数据成功: {}", metadata.getTemplateName());
        return metadata;
    }
}
