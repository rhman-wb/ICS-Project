package com.insurance.audit.audit.service;

import com.insurance.audit.audit.service.DocumentProvider.DocumentChunk;
import com.insurance.audit.audit.service.DocumentProvider.DocumentSection;
import com.insurance.audit.audit.service.DocumentProvider.ParsedDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 文档切分器
 * 将解析后的文档切分为更小的处理单元（句子、段落、章节）
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class DocumentChunker {

    // 中文句子分隔符
    private static final Pattern CHINESE_SENTENCE_PATTERN = Pattern.compile("[。！？；][\\s]*");

    // 英文句子分隔符
    private static final Pattern ENGLISH_SENTENCE_PATTERN = Pattern.compile("[.!?;][\\s]+");

    // 段落分隔符
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");

    /**
     * 切分文档
     *
     * @param document 解析后的文档
     * @return 文档块列表
     */
    public List<DocumentChunk> chunkDocument(ParsedDocument document) {
        log.info("开始切分文档: sectionCount={}", document.getSections().size());

        List<DocumentChunk> chunks = new ArrayList<>();
        int globalChunkIndex = 0;

        for (DocumentSection section : document.getSections()) {
            List<DocumentChunk> sectionChunks = chunkSection(section, globalChunkIndex);
            chunks.addAll(sectionChunks);
            globalChunkIndex += sectionChunks.size();
        }

        log.info("文档切分完成: totalChunks={}", chunks.size());
        return chunks;
    }

    /**
     * 切分章节
     */
    private List<DocumentChunk> chunkSection(DocumentSection section, int startIndex) {
        List<DocumentChunk> chunks = new ArrayList<>();

        switch (section.getType()) {
            case "heading":
                // 标题作为单独的块
                chunks.add(createChunk(section.getText(), "heading", section, startIndex, 0));
                break;

            case "paragraph":
                // 段落切分为句子
                chunks.addAll(chunkParagraph(section, startIndex));
                break;

            case "table":
            case "spreadsheet":
                // 表格按行切分
                chunks.addAll(chunkTable(section, startIndex));
                break;

            default:
                // 其他类型按段落切分
                chunks.addAll(chunkByParagraphs(section, startIndex));
                break;
        }

        return chunks;
    }

    /**
     * 切分段落
     */
    private List<DocumentChunk> chunkParagraph(DocumentSection section, int startIndex) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String text = section.getText();

        // 先尝试按句子切分
        List<String> sentences = splitIntoSentences(text);

        if (sentences.size() > 1) {
            // 多个句子，分别创建块
            int currentPos = section.getStartPos();

            for (int i = 0; i < sentences.size(); i++) {
                String sentence = sentences.get(i).trim();
                if (!sentence.isEmpty()) {
                    DocumentChunk chunk = createChunk(
                        sentence,
                        "sentence",
                        section,
                        startIndex + i,
                        currentPos - section.getStartPos()
                    );
                    chunks.add(chunk);
                    currentPos += sentence.length();
                }
            }
        } else {
            // 单个句子或短段落，作为整体
            chunks.add(createChunk(text, "paragraph", section, startIndex, 0));
        }

        return chunks;
    }

    /**
     * 切分表格
     */
    private List<DocumentChunk> chunkTable(DocumentSection section, int startIndex) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String text = section.getText();

        // 按行分割表格
        String[] lines = text.split("\n");
        int currentPos = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                String chunkType = (i == 0) ? "table_header" : "table_row";

                DocumentChunk chunk = createChunk(
                    line,
                    chunkType,
                    section,
                    startIndex + i,
                    currentPos
                );

                // 添加行索引到元数据
                Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                metadata.put("rowIndex", i);
                metadata.put("isHeader", i == 0);
                chunk.setMetadata(metadata);

                chunks.add(chunk);
                currentPos += line.length() + 1;
            }
        }

        return chunks;
    }

    /**
     * 按段落切分
     */
    private List<DocumentChunk> chunkByParagraphs(DocumentSection section, int startIndex) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String text = section.getText();

        String[] paragraphs = PARAGRAPH_PATTERN.split(text);
        int currentPos = section.getStartPos();

        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (!paragraph.isEmpty()) {
                DocumentChunk chunk = createChunk(
                    paragraph,
                    "paragraph",
                    section,
                    startIndex + i,
                    currentPos - section.getStartPos()
                );
                chunks.add(chunk);
                currentPos += paragraph.length();
            }
        }

        return chunks;
    }

    /**
     * 拆分为句子
     */
    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();

        // 首先按中文句子分隔符分割
        String[] chineseSentences = CHINESE_SENTENCE_PATTERN.split(text);

        for (String chineseSentence : chineseSentences) {
            if (chineseSentence.trim().isEmpty()) {
                continue;
            }

            // 再按英文句子分隔符分割
            String[] englishSentences = ENGLISH_SENTENCE_PATTERN.split(chineseSentence);

            for (String sentence : englishSentences) {
                String trimmed = sentence.trim();
                if (!trimmed.isEmpty()) {
                    sentences.add(trimmed);
                }
            }
        }

        // 如果没有找到句子分隔符，返回原文本
        if (sentences.isEmpty()) {
            sentences.add(text);
        }

        return sentences;
    }

    /**
     * 创建文档块
     */
    private DocumentChunk createChunk(String text, String type, DocumentSection section,
                                    int chunkIndex, int relativePos) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sectionId", section.getId());
        metadata.put("sectionType", section.getType());
        metadata.put("chunkIndex", chunkIndex);
        metadata.put("wordCount", countWords(text));
        metadata.put("charCount", text.length());

        // 继承章节的格式信息
        if (section.getFormatting() != null) {
            metadata.put("parentFormatting", section.getFormatting());
        }

        return DocumentChunk.builder()
                .id("chunk-" + chunkIndex)
                .text(text)
                .pageNumber(extractPageNumber(section))
                .paragraphIndex(chunkIndex)
                .startPos(section.getStartPos() + relativePos)
                .endPos(section.getStartPos() + relativePos + text.length())
                .type(type)
                .styleInfo(new HashMap<>())
                .metadata(metadata)
                .build();
    }

    /**
     * 提取页码（如果可用）
     */
    private Integer extractPageNumber(DocumentSection section) {
        // 这里可以根据具体需求实现页码提取逻辑
        // 目前返回null，表示页码信息不可用
        return null;
    }

    /**
     * 统计单词数
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        // 简单的单词统计：按空格分割
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
}