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

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:${app.file.upload-path:uploads}}")
    private String uploadDir;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public String storeFile(MultipartFile file, String subPath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // Normalize and validate sub-path to prevent traversal
        String safeSubPath = StringUtils.defaultString(subPath, "").replace("\\", "/");
        // Allow only letters, digits, underscore, dash and slash
        safeSubPath = safeSubPath.replaceAll("[^A-Za-z0-9_\\-/]", "");
        if (safeSubPath.contains("../") || safeSubPath.contains("..\\") || "..".equals(safeSubPath)) {
            throw new IllegalArgumentException("非法的子路径");
        }

        // Date-based directory
        String dateStr = LocalDateTime.now().format(DATE_FORMAT);
        String finalSubPath = StringUtils.isNotBlank(safeSubPath) ? safeSubPath + "/" + dateStr : dateStr;

        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path uploadPath = base.resolve(finalSubPath).normalize();
        if (!uploadPath.startsWith(base)) {
            throw new SecurityException("目标路径越界");
        }

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        Path targetPath = uploadPath.resolve(fileName);
        try (java.io.InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

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
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path path = base.resolve(filePath).normalize();
            if (!path.startsWith(base)) {
                throw new SecurityException("文件路径越界");
            }
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
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path full = base.resolve(filePath).normalize();
        if (!full.startsWith(base)) {
            throw new SecurityException("文件路径越界");
        }
        return full.toString();
    }
}

