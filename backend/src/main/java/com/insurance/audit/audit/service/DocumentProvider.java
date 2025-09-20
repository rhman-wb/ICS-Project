package com.insurance.audit.audit.service;

import com.insurance.audit.audit.parsers.WordParser;
import com.insurance.audit.audit.parsers.ExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文档提供者服务
 * 从 product-document-management 获取文档内容并进行解析与切分
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

    @Value("${audit.document-service.base-url:http://localhost:8080}")
    private String documentServiceBaseUrl;

    /**
     * 获取文档内容并解析
     *
     * @param documentId 文档ID
     * @return 解析后的文档内容
     */
    public DocumentContent getDocumentContent(String documentId) {
        log.info("获取并解析文档内容: documentId={}", documentId);

        try {
            // 1. 从文档管理系统获取文档元数据
            DocumentMetadata metadata = getDocumentMetadata(documentId);

            // 2. 下载文档内容
            byte[] rawContent = downloadDocumentContent(documentId);

            // 3. 根据文档类型解析内容
            ParsedDocument parsedDoc = parseDocument(metadata, rawContent);

            // 4. 切分文档为块
            List<DocumentChunk> chunks = documentChunker.chunkDocument(parsedDoc);

            DocumentContent result = DocumentContent.builder()
                    .id(documentId)
                    .name(metadata.getName())
                    .type(metadata.getType())
                    .content(parsedDoc.getPlainText())
                    .chunks(chunks)
                    .metadata(Map.of(
                        "originalSize", rawContent.length,
                        "chunkCount", chunks.size(),
                        "parseTime", LocalDateTime.now(),
                        "encoding", parsedDoc.getEncoding()
                    ))
                    .build();

            log.info("文档解析完成: documentId={}, type={}, chunkCount={}",
                    documentId, metadata.getType(), chunks.size());

            return result;

        } catch (Exception e) {
            log.error("获取文档内容失败: documentId={}, error={}", documentId, e.getMessage(), e);
            throw new RuntimeException("文档获取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量获取文档内容
     *
     * @param documentIds 文档ID列表
     * @return 文档内容列表
     */
    public List<DocumentContent> getDocumentContents(List<String> documentIds) {
        log.info("批量获取文档内容: count={}", documentIds.size());

        return documentIds.parallelStream()
                .map(this::getDocumentContent)
                .toList();
    }

    /**
     * 获取文档元数据
     */
    private DocumentMetadata getDocumentMetadata(String documentId) {
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
    }

    /**
     * 下载文档内容
     */
    private byte[] downloadDocumentContent(String documentId) {
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
    }

    /**
     * 解析文档
     */
    private ParsedDocument parseDocument(DocumentMetadata metadata, byte[] content) {
        log.debug("解析文档: documentId={}, type={}", metadata.getId(), metadata.getType());

        switch (metadata.getType().toUpperCase()) {
            case "DOCX":
            case "DOC":
                return wordParser.parse(content, metadata);
            case "XLSX":
            case "XLS":
                return excelParser.parse(content, metadata);
            case "PDF":
                // TODO: 实现PDF解析器
                throw new UnsupportedOperationException("PDF解析暂未实现");
            case "TXT":
                return ParsedDocument.builder()
                        .plainText(new String(content))
                        .encoding("UTF-8")
                        .sections(List.of())
                        .build();
            default:
                throw new UnsupportedOperationException("不支持的文档类型: " + metadata.getType());
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