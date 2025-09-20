package com.insurance.audit.audit.parsers;

import com.insurance.audit.audit.service.DocumentProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Word文档解析器
 * 支持解析DOCX格式文档，提取文本、样式和结构信息
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class WordParser {

    /**
     * 解析Word文档
     *
     * @param content  文档二进制内容
     * @param metadata 文档元数据
     * @return 解析后的文档
     */
    public DocumentProvider.ParsedDocument parse(byte[] content, DocumentProvider.DocumentMetadata metadata) {
        log.info("开始解析Word文档: documentId={}, size={}", metadata.getId(), content.length);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            StringBuilder plainTextBuilder = new StringBuilder();
            List<DocumentProvider.DocumentSection> sections = new ArrayList<>();
            Map<String, Object> documentStyleInfo = new HashMap<>();

            int currentPos = 0;

            // 解析段落
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph paragraph = paragraphs.get(i);
                String paragraphText = paragraph.getText();

                if (paragraphText != null && !paragraphText.trim().isEmpty()) {
                    DocumentProvider.DocumentSection section = parseParagraph(paragraph, i, currentPos);
                    sections.add(section);

                    plainTextBuilder.append(paragraphText).append("\n");
                    currentPos += paragraphText.length() + 1;
                }
            }

            // 解析表格
            List<XWPFTable> tables = document.getTables();
            for (int i = 0; i < tables.size(); i++) {
                XWPFTable table = tables.get(i);
                DocumentProvider.DocumentSection tableSection = parseTable(table, i, currentPos);
                sections.add(tableSection);

                String tableText = tableSection.getText();
                plainTextBuilder.append(tableText).append("\n");
                currentPos += tableText.length() + 1;
            }

            // 提取文档级别的样式信息
            extractDocumentStyles(document, documentStyleInfo);

            DocumentProvider.ParsedDocument result = DocumentProvider.ParsedDocument.builder()
                    .plainText(plainTextBuilder.toString())
                    .encoding("UTF-8")
                    .sections(sections)
                    .styleInfo(documentStyleInfo)
                    .build();

            log.info("Word文档解析完成: documentId={}, sectionCount={}, textLength={}",
                    metadata.getId(), sections.size(), plainTextBuilder.length());

            return result;

        } catch (IOException e) {
            log.error("解析Word文档失败: documentId={}, error={}", metadata.getId(), e.getMessage(), e);
            throw new RuntimeException("Word文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析段落
     */
    private DocumentProvider.DocumentSection parseParagraph(XWPFParagraph paragraph, int index, int startPos) {
        String text = paragraph.getText();
        String type = determineParagraphType(paragraph);
        Integer level = extractHeadingLevel(paragraph);

        Map<String, Object> formatting = new HashMap<>();
        extractParagraphFormatting(paragraph, formatting);

        return DocumentProvider.DocumentSection.builder()
                .id("paragraph-" + index)
                .text(text)
                .type(type)
                .level(level)
                .formatting(formatting)
                .startPos(startPos)
                .endPos(startPos + text.length())
                .build();
    }

    /**
     * 解析表格
     */
    private DocumentProvider.DocumentSection parseTable(XWPFTable table, int index, int startPos) {
        StringBuilder tableText = new StringBuilder();
        Map<String, Object> formatting = new HashMap<>();

        List<XWPFTableRow> rows = table.getRows();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            XWPFTableRow row = rows.get(rowIndex);
            List<XWPFTableCell> cells = row.getTableCells();

            for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
                XWPFTableCell cell = cells.get(cellIndex);
                String cellText = cell.getText();
                tableText.append(cellText);

                if (cellIndex < cells.size() - 1) {
                    tableText.append("\t");
                }
            }
            tableText.append("\n");
        }

        formatting.put("rowCount", rows.size());
        formatting.put("columnCount", rows.isEmpty() ? 0 : rows.get(0).getTableCells().size());

        return DocumentProvider.DocumentSection.builder()
                .id("table-" + index)
                .text(tableText.toString())
                .type("table")
                .level(null)
                .formatting(formatting)
                .startPos(startPos)
                .endPos(startPos + tableText.length())
                .build();
    }

    /**
     * 确定段落类型
     */
    private String determineParagraphType(XWPFParagraph paragraph) {
        String styleName = paragraph.getStyle();

        if (styleName != null) {
            String lowerStyle = styleName.toLowerCase();
            if (lowerStyle.contains("heading") || lowerStyle.contains("标题")) {
                return "heading";
            }
            if (lowerStyle.contains("title")) {
                return "title";
            }
        }

        // 检查格式特征
        List<XWPFRun> runs = paragraph.getRuns();
        if (!runs.isEmpty()) {
            XWPFRun firstRun = runs.get(0);
            if (firstRun.isBold() && firstRun.getFontSize() > 12) {
                return "heading";
            }
        }

        return "paragraph";
    }

    /**
     * 提取标题级别
     */
    private Integer extractHeadingLevel(XWPFParagraph paragraph) {
        String styleName = paragraph.getStyle();

        if (styleName != null) {
            // 尝试从样式名称中提取级别
            if (styleName.matches(".*[Hh]eading\\s*(\\d+).*")) {
                String levelStr = styleName.replaceAll(".*[Hh]eading\\s*(\\d+).*", "$1");
                try {
                    return Integer.parseInt(levelStr);
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }

            // 中文标题样式
            if (styleName.matches(".*标题\\s*(\\d+).*")) {
                String levelStr = styleName.replaceAll(".*标题\\s*(\\d+).*", "$1");
                try {
                    return Integer.parseInt(levelStr);
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }

        return null;
    }

    /**
     * 提取段落格式信息
     */
    private void extractParagraphFormatting(XWPFParagraph paragraph, Map<String, Object> formatting) {
        // 对齐方式
        ParagraphAlignment alignment = paragraph.getAlignment();
        if (alignment != null) {
            formatting.put("alignment", alignment.toString());
        }

        // 缩进
        int indentationLeft = paragraph.getIndentationLeft();
        int indentationRight = paragraph.getIndentationRight();
        if (indentationLeft != -1) {
            formatting.put("indentLeft", indentationLeft);
        }
        if (indentationRight != -1) {
            formatting.put("indentRight", indentationRight);
        }

        // 行间距
        int spacingAfter = paragraph.getSpacingAfter();
        int spacingBefore = paragraph.getSpacingBefore();
        if (spacingAfter != -1) {
            formatting.put("spacingAfter", spacingAfter);
        }
        if (spacingBefore != -1) {
            formatting.put("spacingBefore", spacingBefore);
        }

        // 字体信息（从第一个run获取）
        List<XWPFRun> runs = paragraph.getRuns();
        if (!runs.isEmpty()) {
            XWPFRun firstRun = runs.get(0);
            Map<String, Object> fontInfo = new HashMap<>();

            if (firstRun.getFontFamily() != null) {
                fontInfo.put("family", firstRun.getFontFamily());
            }
            if (firstRun.getFontSize() != -1) {
                fontInfo.put("size", firstRun.getFontSize());
            }
            fontInfo.put("bold", firstRun.isBold());
            fontInfo.put("italic", firstRun.isItalic());
            fontInfo.put("underline", firstRun.getUnderline() != UnderlinePatterns.NONE);

            formatting.put("font", fontInfo);
        }
    }

    /**
     * 提取文档级别样式信息
     */
    private void extractDocumentStyles(XWPFDocument document, Map<String, Object> styleInfo) {
        // 文档主题
        XWPFStyles styles = document.getStyles();
        if (styles != null) {
            // 使用文档的样式信息，但避免使用可能不存在的方法
            styleInfo.put("hasCustomStyles", true);
            styleInfo.put("styleCount", 0); // 简化处理
        }

        // 页面设置
        // Note: POI对页面设置的支持有限，这里只是示例
        styleInfo.put("documentType", "word");
        styleInfo.put("hasImages", !document.getAllPictures().isEmpty());
        styleInfo.put("imageCount", document.getAllPictures().size());
    }
}