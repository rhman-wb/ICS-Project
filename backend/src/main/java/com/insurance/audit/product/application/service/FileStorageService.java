package com.insurance.audit.product.application.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储服务接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-14
 */
public interface FileStorageService {

    /**
     * 存储文件
     *
     * @param file 文件
     * @param subPath 子路径
     * @return 文件存储路径
     * @throws IOException 存储异常
     */
    String storeFile(MultipartFile file, String subPath) throws IOException;

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);

    /**
     * 获取文件完整路径
     *
     * @param filePath 相对路径
     * @return 完整路径
     */
    String getFullPath(String filePath);
}