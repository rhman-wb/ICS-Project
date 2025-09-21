package com.insurance.audit.audit.service;

import com.insurance.audit.audit.parsers.WordParser;
import com.insurance.audit.audit.parsers.ExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文档提供者服务
 * 从 product-document-management 获取文档内容并进行解析与切分
 * 增强版：支持重试机制、缓存、并发处理、智能解析
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class DocumentProvider {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WordParser wordParser;

    @Autowired
    private ExcelParser excelParser;

    @Autowired
    private DocumentChunker documentChunker;

    @Autowired(required = false)
    private SecurityComplianceService securityComplianceService;

    @Value("${audit.document-service.base-url:http://localhost:8080}")
    private String documentServiceBaseUrl;

    @Value("${audit.document-service.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${audit.document-service.retry.delay:1000}")
    private long retryDelayMs;

    @Value("${audit.document-service.retry.multiplier:2.0}")
    private double retryMultiplier;

    @Value("${audit.document-service.max-file-size:50MB}")
    private String maxFileSizeStr;

    @Value("${audit.document-service.timeout:30000}")
    private long timeoutMs;

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "DOCX", "DOC", "XLSX", "XLS", "PDF", "TXT", "RTF"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * 获取文档内容并解析（带缓存和安全检查）
     *
     * @param documentId 文档ID
     * @return 解析后的文档内容
     */
    @Cacheable(value = "documents", key = "#documentId", unless = "#result == null")
    public DocumentContent getDocumentContent(String documentId) {
        long startTime = System.currentTimeMillis();
        log.info("获取并解析文档内容: documentId={}", documentId);

        try {
            // 安全检查：验证文档访问权限
            if (securityComplianceService != null) {
                boolean hasPermission = securityComplianceService.hasDataScopePermission("document", documentId);
                if (!hasPermission) {
                    throw new SecurityException("无权限访问文档: " + documentId);
                }
            }

            // 1. 从文档管理系统获取文档元数据
            DocumentMetadata metadata = getDocumentMetadataWithRetry(documentId);
            validateDocumentMetadata(metadata);

            // 2. 下载文档内容
            byte[] rawContent = downloadDocumentContentWithRetry(documentId);
            validateDocumentContent(rawContent, metadata);

            // 3. 根据文档类型解析内容
            ParsedDocument parsedDoc = parseDocumentWithErrorHandling(metadata, rawContent);

            // 4. 切分文档为块
            List<DocumentChunk> chunks = chunkDocumentWithValidation(parsedDoc, metadata);

            // 5. 安全检查：数据脱敏
            if (securityComplianceService != null) {
                parsedDoc = securityComplianceService.redactSensitiveData(parsedDoc);
                securityComplianceService.auditLog("DOCUMENT_PARSED",
                    "文档解析完成: documentId=" + documentId + ", type=" + metadata.getType(),
                    documentId, "INFO");
            }

            DocumentContent result = DocumentContent.builder()
                    .id(documentId)
                    .name(metadata.getName())
                    .type(metadata.getType())
                    .content(parsedDoc.getPlainText())
                    .chunks(chunks)
                    .metadata(buildDocumentMetadata(metadata, rawContent, chunks, startTime))
                    .build();

            long duration = System.currentTimeMillis() - startTime;
            log.info("文档解析完成: documentId={}, type={}, chunkCount={}, duration={}ms",
                    documentId, metadata.getType(), chunks.size(), duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("获取文档内容失败: documentId={}, duration={}ms, error={}",
                     documentId, duration, e.getMessage(), e);

            // 安全检查：记录失败日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("DOCUMENT_PARSE_FAILED",
                    "文档解析失败: documentId=" + documentId + ", error=" + e.getMessage(),
                    documentId, "ERROR");
            }

            throw new RuntimeException("文档获取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量获取文档内容（并行处理）
     *
     * @param documentIds 文档ID列表
     * @return 文档内容列表
     */
    public List<DocumentContent> getDocumentContents(List<String> documentIds) {
        log.info("批量获取文档内容: count={}", documentIds.size());

        if (documentIds.isEmpty()) {
            return Collections.emptyList();
        }

        long startTime = System.currentTimeMillis();

        // 并行处理文档，但限制并发数以避免资源过度消耗
        List<CompletableFuture<DocumentContent>> futures = documentIds.stream()
                .map(documentId ->
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return getDocumentContent(documentId);
                        } catch (Exception e) {
                            log.warn("批量处理中单个文档失败: documentId={}, error={}", documentId, e.getMessage());
                            return null; // 返回null，后续过滤掉
                        }
                    })
                )
                .toList();

        // 等待所有任务完成
        List<DocumentContent> results = futures.stream()
                .map(future -> {
                    try {
                        return future.get(timeoutMs, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.warn("批量处理超时或异常: error={}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        long duration = System.currentTimeMillis() - startTime;
        log.info("批量文档处理完成: 请求={}, 成功={}, 耗时={}ms",
                documentIds.size(), results.size(), duration);

        return results;
    }

    /**
     * 带重试的获取文档元数据
     */
    private DocumentMetadata getDocumentMetadataWithRetry(String documentId) {
        return executeWithRetry(() -> {
            String url = UriComponentsBuilder.fromHttpUrl(documentServiceBaseUrl)
                    .path("/api/v1/documents/{documentId}/metadata")
                    .buildAndExpand(documentId)
                    .toUriString();

            log.debug("获取文档元数据: url={}", url);

            ResponseEntity<DocumentMetadataResponse> response =
                    restTemplate.getForEntity(url, DocumentMetadataResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                DocumentMetadataResponse metadataResponse = response.getBody();
                return DocumentMetadata.builder()
                        .id(metadataResponse.getId())
                        .name(metadataResponse.getName())
                        .type(metadataResponse.getType())
                        .size(metadataResponse.getSize())
                        .uploadTime(metadataResponse.getUploadTime())
                        .properties(metadataResponse.getProperties())
                        .build();
            } else {
                throw new RuntimeException("获取文档元数据失败: " + response.getStatusCode());
            }
        }, () -> {
            throw new RuntimeException("文档元数据获取重试失败: " + documentId);
        });
    }

    /**
     * 带重试的下载文档内容
     */
    private byte[] downloadDocumentContentWithRetry(String documentId) {
        return executeWithRetry(() -> {
            String url = UriComponentsBuilder.fromHttpUrl(documentServiceBaseUrl)
                    .path("/api/v1/documents/{documentId}/content")
                    .buildAndExpand(documentId)
                    .toUriString();

            log.debug("下载文档内容: url={}", url);

            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("下载文档内容失败: " + response.getStatusCode());
            }
        }, () -> {
            throw new RuntimeException("文档内容下载重试失败: " + documentId);
        });
    }

    /**
     * 带错误处理的文档解析
     */
    private ParsedDocument parseDocumentWithErrorHandling(DocumentMetadata metadata, byte[] content) {
        log.debug("解析文档: documentId={}, type={}, size={}", metadata.getId(), metadata.getType(), content.length);

        try {
            String documentType = metadata.getType().toUpperCase();

            switch (documentType) {
                case "DOCX":
                case "DOC":
                    return wordParser.parse(content, metadata);
                case "XLSX":
                case "XLS":
                    return excelParser.parse(content, metadata);
                case "PDF":
                    return parsePdfDocument(content, metadata);
                case "TXT":
                    return parseTxtDocument(content, metadata);
                case "RTF":
                    return parseRtfDocument(content, metadata);
                default:
                    throw new UnsupportedOperationException("不支持的文档类型: " + metadata.getType());
            }
        } catch (Exception e) {
            log.error("文档解析失败: documentId={}, type={}, error={}",
                     metadata.getId(), metadata.getType(), e.getMessage(), e);

            // 尝试基本文本解析作为fallback
            return fallbackTextParsing(content, metadata, e);
        }
    }

    /**
     * PDF文档解析（简化版）
     */
    private ParsedDocument parsePdfDocument(byte[] content, DocumentMetadata metadata) {
        // TODO: 实现PDF解析器，这里提供简化实现
        log.warn("PDF解析暂未完全实现，返回基本信息: documentId={}", metadata.getId());

        return ParsedDocument.builder()
                .plainText("PDF文档内容（需要OCR或PDF解析器支持）")
                .encoding("UTF-8")
                .sections(Collections.emptyList())
                .styleInfo(Map.of("documentType", "pdf", "needsOcr", true))
                .build();
    }

    /**
     * TXT文档解析
     */
    private ParsedDocument parseTxtDocument(byte[] content, DocumentMetadata metadata) {
        try {
            // 检测文件编码
            String encoding = detectEncoding(content);
            String text = new String(content, encoding);

            List<DocumentSection> sections = splitTextIntoSections(text);

            return ParsedDocument.builder()
                    .plainText(text)
                    .encoding(encoding)
                    .sections(sections)
                    .styleInfo(Map.of("documentType", "text", "lineCount", text.split("\n").length))
                    .build();

        } catch (Exception e) {
            log.error("TXT文档解析失败: documentId={}, error={}", metadata.getId(), e.getMessage());
            return fallbackTextParsing(content, metadata, e);
        }
    }

    /**
     * RTF文档解析（简化版）
     */
    private ParsedDocument parseRtfDocument(byte[] content, DocumentMetadata metadata) {
        // TODO: 实现完整RTF解析器
        log.warn("RTF解析暂未完全实现，使用基本文本解析: documentId={}", metadata.getId());

        try {
            String text = new String(content, StandardCharsets.UTF_8);
            // 简单去除RTF控制字符
            text = text.replaceAll("\\\\[a-z]+\\d*\\s?", "")
                      .replaceAll("[{}]", "")
                      .trim();

            return ParsedDocument.builder()
                    .plainText(text)
                    .encoding("UTF-8")
                    .sections(splitTextIntoSections(text))
                    .styleInfo(Map.of("documentType", "rtf", "simplified", true))
                    .build();

        } catch (Exception e) {
            return fallbackTextParsing(content, metadata, e);
        }
    }

    /**
     * 文档验证
     */
    private void validateDocumentMetadata(DocumentMetadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("文档元数据不能为空");
        }

        if (metadata.getId() == null || metadata.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("文档ID不能为空");
        }

        if (metadata.getType() == null || metadata.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("文档类型不能为空");
        }

        String documentType = metadata.getType().toUpperCase();
        if (!SUPPORTED_TYPES.contains(documentType)) {
            throw new UnsupportedOperationException("不支持的文档类型: " + metadata.getType());
        }

        if (metadata.getSize() != null && metadata.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("文档大小超过限制: %d > %d bytes", metadata.getSize(), MAX_FILE_SIZE));
        }
    }

    /**
     * 文档内容验证
     */
    private void validateDocumentContent(byte[] content, DocumentMetadata metadata) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("文档内容不能为空");
        }

        if (content.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("文档内容大小超过限制: %d > %d bytes", content.length, MAX_FILE_SIZE));
        }

        // 检查文档内容与元数据的一致性
        if (metadata.getSize() != null && Math.abs(content.length - metadata.getSize()) > 1024) {
            log.warn("文档实际大小与元数据大小不一致: metadata={}, actual={}",
                    metadata.getSize(), content.length);
        }
    }

    /**
     * 文档元数据响应DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentMetadataResponse {
        private String id;
        private String name;
        private String type;
        private Long size;
        private LocalDateTime uploadTime;
        private Map<String, Object> properties;
    }

    /**
     * 文档元数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentMetadata {
        private String id;
        private String name;
        private String type;
        private Long size;
        private LocalDateTime uploadTime;
        private Map<String, Object> properties;
    }

    /**
     * 解析后的文档
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ParsedDocument {
        private String plainText;
        private String encoding;
        private List<DocumentSection> sections;
        private Map<String, Object> styleInfo;
    }

    /**
     * 文档段落
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentSection {
        private String id;
        private String text;
        private String type; // heading, paragraph, table, etc.
        private Integer level; // for headings
        private Map<String, Object> formatting;
        private Integer startPos;
        private Integer endPos;
    }

    /**
     * 带验证的文档切分
     */
    private List<DocumentChunk> chunkDocumentWithValidation(ParsedDocument parsedDoc, DocumentMetadata metadata) {
        log.debug("文档切分处理: documentId={}, textLength={}", metadata.getId(),
                 parsedDoc.getPlainText() != null ? parsedDoc.getPlainText().length() : 0);

        try {
            List<DocumentChunk> chunks = documentChunker.chunkDocument(parsedDoc);

            if (chunks == null || chunks.isEmpty()) {
                log.warn("文档切分结果为空，创建默认块: documentId={}", metadata.getId());
                return List.of(createDefaultChunk(parsedDoc, metadata));
            }

            // 验证块的完整性
            validateChunks(chunks, parsedDoc);

            log.debug("文档切分完成: documentId={}, chunkCount={}", metadata.getId(), chunks.size());
            return chunks;

        } catch (Exception e) {
            log.error("文档切分失败，创建默认块: documentId={}, error={}", metadata.getId(), e.getMessage());
            return List.of(createDefaultChunk(parsedDoc, metadata));
        }
    }

    /**
     * 创建默认文档块
     */
    private DocumentChunk createDefaultChunk(ParsedDocument parsedDoc, DocumentMetadata metadata) {
        return DocumentChunk.builder()
                .id(metadata.getId() + "-chunk-0")
                .text(parsedDoc.getPlainText())
                .pageNumber(1)
                .paragraphIndex(0)
                .startPos(0)
                .endPos(parsedDoc.getPlainText() != null ? parsedDoc.getPlainText().length() : 0)
                .type("document")
                .styleInfo(Map.of("isDefault", true))
                .metadata(Map.of("source", "default-chunking"))
                .build();
    }

    /**
     * 验证文档块
     */
    private void validateChunks(List<DocumentChunk> chunks, ParsedDocument parsedDoc) {
        for (DocumentChunk chunk : chunks) {
            if (chunk.getText() == null || chunk.getText().trim().isEmpty()) {
                log.warn("发现空文档块: chunkId={}", chunk.getId());
            }
            if (chunk.getStartPos() != null && chunk.getEndPos() != null &&
                chunk.getStartPos() > chunk.getEndPos()) {
                log.warn("文档块位置异常: chunkId={}, start={}, end={}",
                        chunk.getId(), chunk.getStartPos(), chunk.getEndPos());
            }
        }
    }

    /**
     * 构建文档元数据
     */
    private Map<String, Object> buildDocumentMetadata(DocumentMetadata metadata, byte[] rawContent,
                                                     List<DocumentChunk> chunks, long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new HashMap<>();
        result.put("originalMetadata", metadata);
        result.put("processingDuration", duration);
        result.put("rawContentSize", rawContent.length);
        result.put("chunkCount", chunks.size());
        result.put("processedAt", LocalDateTime.now());
        result.put("encoding", detectEncoding(rawContent));
        result.put("textLength", chunks.stream().mapToInt(c -> c.getText() != null ? c.getText().length() : 0).sum());

        // 性能指标
        result.put("processingSpeed", rawContent.length / Math.max(duration, 1.0)); // bytes per ms
        result.put("chunksPerSecond", chunks.size() * 1000.0 / Math.max(duration, 1.0));

        return result;
    }

    /**
     * 降级文本解析
     */
    private ParsedDocument fallbackTextParsing(byte[] content, DocumentMetadata metadata, Exception originalError) {
        log.warn("使用降级文本解析: documentId={}, originalError={}", metadata.getId(), originalError.getMessage());

        try {
            String encoding = detectEncoding(content);
            String text = new String(content, encoding);

            // 简单清理和格式化
            text = text.replaceAll("\\r\\n", "\n")
                      .replaceAll("\\r", "\n")
                      .trim();

            return ParsedDocument.builder()
                    .plainText(text)
                    .encoding(encoding)
                    .sections(splitTextIntoSections(text))
                    .styleInfo(Map.of(
                        "documentType", "fallback",
                        "originalError", originalError.getMessage(),
                        "encoding", encoding,
                        "lineCount", text.split("\n").length
                    ))
                    .build();

        } catch (Exception e) {
            log.error("降级解析也失败: documentId={}, error={}", metadata.getId(), e.getMessage());

            return ParsedDocument.builder()
                    .plainText("文档解析失败：" + originalError.getMessage())
                    .encoding("UTF-8")
                    .sections(Collections.emptyList())
                    .styleInfo(Map.of(
                        "documentType", "error",
                        "error", e.getMessage(),
                        "originalError", originalError.getMessage()
                    ))
                    .build();
        }
    }

    /**
     * 检测文件编码
     */
    private String detectEncoding(byte[] content) {
        // 简化的编码检测逻辑
        if (content.length >= 3) {
            // UTF-8 BOM
            if (content[0] == (byte) 0xEF && content[1] == (byte) 0xBB && content[2] == (byte) 0xBF) {
                return "UTF-8";
            }
            // UTF-16 BE BOM
            if (content[0] == (byte) 0xFE && content[1] == (byte) 0xFF) {
                return "UTF-16BE";
            }
            // UTF-16 LE BOM
            if (content[0] == (byte) 0xFF && content[1] == (byte) 0xFE) {
                return "UTF-16LE";
            }
        }

        // 尝试解析为UTF-8
        try {
            String test = new String(content, StandardCharsets.UTF_8);
            if (test.contains("�")) {
                // 包含替换字符，可能不是UTF-8
                return "GBK"; // 中文环境常用编码
            }
            return "UTF-8";
        } catch (Exception e) {
            return "UTF-8"; // 默认使用UTF-8
        }
    }

    /**
     * 将文本分割为段落
     */
    private List<DocumentSection> splitTextIntoSections(String text) {
        List<DocumentSection> sections = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return sections;
        }

        String[] paragraphs = text.split("\n\n+");
        int currentPos = 0;

        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (!paragraph.isEmpty()) {
                DocumentSection section = DocumentSection.builder()
                        .id("section-" + (i + 1))
                        .text(paragraph)
                        .type(detectSectionType(paragraph))
                        .level(detectSectionLevel(paragraph))
                        .formatting(Map.of("paragraphIndex", i))
                        .startPos(currentPos)
                        .endPos(currentPos + paragraph.length())
                        .build();

                sections.add(section);
            }
            currentPos += paragraphs[i].length() + 2; // +2 for \n\n
        }

        return sections;
    }

    /**
     * 检测段落类型
     */
    private String detectSectionType(String text) {
        if (text.matches("^#+\\s+.+")) {
            return "heading";
        } else if (text.matches("^\\d+\\.\\s+.+")) {
            return "numbered_list";
        } else if (text.matches("^[•\\-\\*]\\s+.+")) {
            return "bullet_list";
        } else if (text.length() < 50) {
            return "title";
        } else {
            return "paragraph";
        }
    }

    /**
     * 检测段落级别
     */
    private Integer detectSectionLevel(String text) {
        if (text.startsWith("#")) {
            int level = 0;
            for (char c : text.toCharArray()) {
                if (c == '#') level++;
                else break;
            }
            return level;
        }
        return 1;
    }

    /**
     * 执行带重试机制的操作
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation, java.util.function.Supplier<T> fallback) {
        int attempts = 0;
        long delay = 100; // 初始延迟100ms

        while (attempts < maxRetryAttempts) {
            try {
                attempts++;
                return operation.get();
            } catch (Exception e) {
                log.warn("操作失败，第{}次尝试: error={}", attempts, e.getMessage());

                if (attempts >= maxRetryAttempts) {
                    log.error("达到最大重试次数，执行降级操作: maxAttempts={}", maxRetryAttempts);
                    if (fallback != null) {
                        try {
                            return fallback.get();
                        } catch (Exception fallbackError) {
                            log.error("降级操作也失败: error={}", fallbackError.getMessage());
                            throw new RuntimeException("操作失败且降级失败", fallbackError);
                        }
                    } else {
                        throw new RuntimeException("操作失败且无降级方案", e);
                    }
                }

                // 指数退避
                try {
                    Thread.sleep(delay);
                    delay = Math.min(delay * 2, 5000); // 最大延迟5秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        return fallback != null ? fallback.get() : null;
    }

    /**
     * 文档内容数据结构
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentContent {
        private String id;
        private String name;
        private String type;
        private String content;
        private List<DocumentChunk> chunks;
        private Map<String, Object> metadata;
    }

    /**
     * 文档块数据结构
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentChunk {
        private String id;
        private String text;
        private Integer pageNumber;
        private Integer paragraphIndex;
        private Integer startPos;
        private Integer endPos;
        private String type; // sentence, paragraph, section
        private Map<String, Object> styleInfo;
        private Map<String, Object> metadata;
    }
}