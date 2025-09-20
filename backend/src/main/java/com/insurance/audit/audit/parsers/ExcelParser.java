package com.insurance.audit.audit.parsers;

import com.insurance.audit.audit.service.DocumentProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel文档解析器
 * 支持解析XLS和XLSX格式文档，提取文本、格式和结构信息
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class ExcelParser {

    /**
     * 解析Excel文档
     *
     * @param content  文档二进制内容
     * @param metadata 文档元数据
     * @return 解析后的文档
     */
    public DocumentProvider.ParsedDocument parse(byte[] content, DocumentProvider.DocumentMetadata metadata) {
        log.info("开始解析Excel文档: documentId={}, size={}", metadata.getId(), content.length);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {

            Workbook workbook = createWorkbook(content, metadata.getType());

            StringBuilder plainTextBuilder = new StringBuilder();
            List<DocumentProvider.DocumentSection> sections = new ArrayList<>();
            Map<String, Object> documentStyleInfo = new HashMap<>();

            int currentPos = 0;

            // 遍历所有工作表
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                DocumentProvider.DocumentSection sheetSection = parseSheet(sheet, sheetIndex, currentPos);
                sections.add(sheetSection);

                String sheetText = sheetSection.getText();
                plainTextBuilder.append(sheetText).append("\n");
                currentPos += sheetText.length() + 1;
            }

            // 提取文档级别的样式信息
            extractDocumentStyles(workbook, documentStyleInfo);

            DocumentProvider.ParsedDocument result = DocumentProvider.ParsedDocument.builder()
                    .plainText(plainTextBuilder.toString())
                    .encoding("UTF-8")
                    .sections(sections)
                    .styleInfo(documentStyleInfo)
                    .build();

            log.info("Excel文档解析完成: documentId={}, sheetCount={}, textLength={}",
                    metadata.getId(), sections.size(), plainTextBuilder.length());

            workbook.close();
            return result;

        } catch (IOException e) {
            log.error("解析Excel文档失败: documentId={}, error={}", metadata.getId(), e.getMessage(), e);
            throw new RuntimeException("Excel文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建Workbook实例
     */
    private Workbook createWorkbook(byte[] content, String fileType) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        if ("XLSX".equalsIgnoreCase(fileType)) {
            return new XSSFWorkbook(inputStream);
        } else if ("XLS".equalsIgnoreCase(fileType)) {
            return new HSSFWorkbook(inputStream);
        } else {
            // 尝试自动检测
            try {
                return new XSSFWorkbook(new ByteArrayInputStream(content));
            } catch (Exception e) {
                return new HSSFWorkbook(new ByteArrayInputStream(content));
            }
        }
    }

    /**
     * 解析工作表
     */
    private DocumentProvider.DocumentSection parseSheet(Sheet sheet, int sheetIndex, int startPos) {
        String sheetName = sheet.getSheetName();
        StringBuilder sheetText = new StringBuilder();
        Map<String, Object> formatting = new HashMap<>();

        // 获取实际使用的行范围
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();

        // 解析表头（如果存在）
        if (firstRowNum >= 0) {
            Row headerRow = sheet.getRow(firstRowNum);
            if (headerRow != null) {
                List<String> headers = new ArrayList<>();
                for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                    Cell cell = headerRow.getCell(cellIndex);
                    String cellValue = getCellValueAsString(cell);
                    headers.add(cellValue);
                    sheetText.append(cellValue);
                    if (cellIndex < headerRow.getLastCellNum() - 1) {
                        sheetText.append("\t");
                    }
                }
                sheetText.append("\n");
                formatting.put("headers", headers);
            }
        }

        // 解析数据行
        List<List<String>> rows = new ArrayList<>();
        for (int rowIndex = firstRowNum + 1; rowIndex <= lastRowNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                List<String> rowData = new ArrayList<>();
                boolean hasData = false;

                for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    String cellValue = getCellValueAsString(cell);
                    rowData.add(cellValue);
                    sheetText.append(cellValue);

                    if (!cellValue.trim().isEmpty()) {
                        hasData = true;
                    }

                    if (cellIndex < row.getLastCellNum() - 1) {
                        sheetText.append("\t");
                    }
                }

                if (hasData) {
                    rows.add(rowData);
                    sheetText.append("\n");
                }
            }
        }

        // 提取格式信息
        formatting.put("sheetName", sheetName);
        formatting.put("rowCount", rows.size());
        formatting.put("columnCount", rows.isEmpty() ? 0 : rows.get(0).size());
        formatting.put("hasHeaders", firstRowNum >= 0);

        // 检查合并单元格
        int mergedRegions = sheet.getNumMergedRegions();
        if (mergedRegions > 0) {
            formatting.put("mergedCells", mergedRegions);
        }

        return DocumentProvider.DocumentSection.builder()
                .id("sheet-" + sheetIndex)
                .text(sheetText.toString())
                .type("spreadsheet")
                .level(null)
                .formatting(formatting)
                .startPos(startPos)
                .endPos(startPos + sheetText.length())
                .build();
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，不显示小数点
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    // 尝试获取公式计算结果
                    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);
                    switch (cellValue.getCellType()) {
                        case STRING:
                            return cellValue.getStringValue();
                        case NUMERIC:
                            return String.valueOf(cellValue.getNumberValue());
                        case BOOLEAN:
                            return String.valueOf(cellValue.getBooleanValue());
                        default:
                            return cell.getCellFormula();
                    }
                } catch (Exception e) {
                    // 如果计算失败，返回公式本身
                    return cell.getCellFormula();
                }
            case BLANK:
            default:
                return "";
        }
    }

    /**
     * 提取文档级别样式信息
     */
    private void extractDocumentStyles(Workbook workbook, Map<String, Object> styleInfo) {
        styleInfo.put("documentType", "excel");
        styleInfo.put("sheetCount", workbook.getNumberOfSheets());

        // 工作表名称列表
        List<String> sheetNames = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        styleInfo.put("sheetNames", sheetNames);

        // 检查是否有图表或图片
        boolean hasCharts = false;
        boolean hasImages = false;

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);

            // 检查绘图对象（图表等）
            if (sheet.getDrawingPatriarch() != null) {
                hasCharts = true;
                break;
            }
        }

        styleInfo.put("hasCharts", hasCharts);
        styleInfo.put("hasImages", hasImages);

        // 自定义样式数量
        styleInfo.put("customStyleCount", workbook.getNumCellStyles());
    }
}