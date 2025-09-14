package com.insurance.audit.product.application.service.impl;

import com.insurance.audit.product.application.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-14
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public String storeFile(MultipartFile file, String subPath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 创建日期子目录
        String dateStr = LocalDateTime.now().format(DATE_FORMAT);
        String finalSubPath = StringUtils.isNotBlank(subPath) ? subPath + "/" + dateStr : dateStr;

        // 创建目录
        Path uploadPath = Paths.get(uploadDir, finalSubPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path targetPath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 返回相对路径
        String relativePath = finalSubPath + "/" + fileName;
        log.info("文件保存成功: {}", relativePath);

        return relativePath;
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return false;
        }

        try {
            Path path = Paths.get(uploadDir, filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("文件删除成功: {}", filePath);
            }
            return deleted;
        } catch (Exception e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public String getFullPath(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        return Paths.get(uploadDir, filePath).toString();
    }
}