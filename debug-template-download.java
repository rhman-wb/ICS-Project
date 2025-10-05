package com.insurance.audit.product.application.service.impl;

// 在 TemplateServiceImpl 的 downloadTemplateById 方法中添加详细日志

@Override
public Resource downloadTemplateById(String templateId) throws IOException {
    log.info("=== 开始下载模板 ===");
    log.info("模板ID: {}", templateId);

    ProductTemplate template = templateMapper.selectById(templateId);
    if (template == null) {
        log.error("未找到模板, ID: {}", templateId);
        throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "未找到模板: " + templateId);
    }

    log.info("模板名称: {}", template.getTemplateName());
    log.info("模板文件路径(数据库): {}", template.getTemplateFilePath());
    log.info("uploadDir配置: {}", uploadDir);  // 添加这行

    // 获取模板文件完整路径
    String fullPath = fileStorageService.getFullPath(template.getTemplateFilePath());
    log.info("计算后的完整路径: {}", fullPath);  // 添加这行

    File file = new File(fullPath);
    log.info("文件是否存在: {}", file.exists());  // 添加这行
    log.info("是否是文件: {}", file.isFile());     // 添加这行
    log.info("文件绝对路径: {}", file.getAbsolutePath());  // 添加这行

    if (!file.exists() || !file.isFile()) {
        log.error("模板文件不存在: {}", fullPath);
        throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "模板文件不存在: " + template.getTemplateName());
    }

    log.info("模板下载成功, 文件: {}", template.getTemplateName());
    return new FileSystemResource(file);
}
