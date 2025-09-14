package com.insurance.audit.product.application.service;

import com.insurance.audit.product.interfaces.dto.response.DocumentParseResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文档解析服务接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-14
 */
public interface DocumentParsingService {

    /**
     * 解析Excel格式的产品信息登记表
     *
     * @param file Excel文件
     * @return 解析结果
     * @throws IOException 文件读取异常
     */
    DocumentParseResult parseProductRegistrationForm(MultipartFile file) throws IOException;

    /**
     * 验证文件格式是否支持
     *
     * @param file 文件
     * @return 是否支持
     */
    boolean isSupportedFormat(MultipartFile file);

    /**
     * 获取支持的文件格式列表
     *
     * @return 支持的格式列表
     */
    String[] getSupportedFormats();
}