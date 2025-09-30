package com.insurance.audit.product.application.service.impl;

import com.insurance.audit.product.application.service.DocumentParsingService;
import com.insurance.audit.product.application.service.TemplateService;
import com.insurance.audit.product.domain.entity.ProductTemplate;
import com.insurance.audit.product.interfaces.dto.TemplateFieldConfig;
import com.insurance.audit.product.interfaces.dto.response.DocumentParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 文档解析服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParsingServiceImpl implements DocumentParsingService {

    private final TemplateService templateService;

    // 支持的文件格式
    private static final String[] SUPPORTED_FORMATS = {
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-excel", // .xls
            ".xlsx", ".xls"
    };

    // 产品信息字段映射（中文关键词到英文字段名）
    private static final Map<String, String> FIELD_MAPPING = new HashMap<>();

    static {
        FIELD_MAPPING.put("产品名称", "productName");
        FIELD_MAPPING.put("产品全称", "productName");
        FIELD_MAPPING.put("报送类型", "reportType");
        FIELD_MAPPING.put("产品性质", "productNature");
        FIELD_MAPPING.put("年度", "year");
        FIELD_MAPPING.put("产品类别", "productCategory");
        FIELD_MAPPING.put("险种类别", "productCategory");
        FIELD_MAPPING.put("经营区域", "operatingRegion");
        FIELD_MAPPING.put("开发方式", "developmentMethod");
        FIELD_MAPPING.put("开发类型", "developmentMethod");
        FIELD_MAPPING.put("主附险", "primaryAdditional");
        FIELD_MAPPING.put("修订类型", "revisionType");
        FIELD_MAPPING.put("原产品名称", "originalProductName");
        FIELD_MAPPING.put("原产品名称和编号", "originalProductName");
        FIELD_MAPPING.put("示范条款名称", "demonstrationClause");
        FIELD_MAPPING.put("经营范围", "businessScope");
        FIELD_MAPPING.put("销售推广名称", "salesPromotionName");
        FIELD_MAPPING.put("经营地域1", "businessArea1");
        FIELD_MAPPING.put("经营地域2", "businessArea2");
        FIELD_MAPPING.put("产品属性", "productProperty");
        FIELD_MAPPING.put("基准费率", "basicRate");
        FIELD_MAPPING.put("保险期限", "insurancePeriod");
        FIELD_MAPPING.put("保险责任", "insuranceResponsibility");
        FIELD_MAPPING.put("销售区域", "salesArea");
        FIELD_MAPPING.put("销售渠道", "salesChannel");
        FIELD_MAPPING.put("保险标的", "insuranceTarget");
        FIELD_MAPPING.put("是否经营", "isOperated");
        FIELD_MAPPING.put("经营日期", "operationDate");
        FIELD_MAPPING.put("费率浮动范围", "rateFloatingRange");
        FIELD_MAPPING.put("费率浮动系数", "rateFloatingCoefficient");
        FIELD_MAPPING.put("绝对免赔", "absoluteDeductible");
        FIELD_MAPPING.put("相对免赔", "relativeDeductible");
        FIELD_MAPPING.put("备注", "remarks");
    }

    @Override
    public DocumentParseResult parseProductRegistrationForm(MultipartFile file) throws IOException {
        log.info("开始解析产品信息登记表文件: {}", file.getOriginalFilename());

        try {
            // 验证文件格式
            if (!isSupportedFormat(file)) {
                return DocumentParseResult.builder()
                        .success(false)
                        .errorMessage("不支持的文件格式，请上传Excel文件(.xlsx或.xls)")
                        .parseTime(LocalDateTime.now())
                        .build();
            }

            // 尝试从文件名判断模板类型
            String templateType = detectTemplateType(file.getOriginalFilename());

            // 如果能识别模板类型，使用模板配置解析
            if (templateType != null) {
                return parseWithTemplate(file, templateType);
            }

            // 否则使用通用解析
            return parseGeneric(file);

        } catch (Exception e) {
            log.error("解析产品信息登记表失败: {}", e.getMessage(), e);
            return DocumentParseResult.builder()
                    .success(false)
                    .errorMessage("文件解析失败: " + e.getMessage())
                    .parseTime(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 使用模板配置解析文件
     */
    private DocumentParseResult parseWithTemplate(MultipartFile file, String templateType) throws IOException {
        log.info("使用模板配置解析文件, 模板类型: {}", templateType);

        Workbook workbook = createWorkbook(file);
        DocumentParseResult.FileInfo fileInfo = extractFileInfo(file, workbook);

        // 获取模板字段配置
        List<TemplateFieldConfig> fieldConfigs = templateService.getTemplateFields(templateType);

        // 使用模板配置解析
        Map<String, Object> rawData = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        DocumentParseResult.ProductBasicInfo productInfo = parseWithFieldConfigs(
                workbook, fieldConfigs, rawData, warnings);

        workbook.close();

        return DocumentParseResult.builder()
                .success(true)
                .parseTime(LocalDateTime.now())
                .fileInfo(fileInfo)
                .productInfo(productInfo)
                .rawData(rawData)
                .warnings(warnings)
                .build();
    }

    /**
     * 通用解析（原有逻辑）
     */
    private DocumentParseResult parseGeneric(MultipartFile file) throws IOException {
        log.info("使用通用方式解析文件");

        // 解析Excel文件
        Workbook workbook = createWorkbook(file);
        DocumentParseResult.FileInfo fileInfo = extractFileInfo(file, workbook);

        // 解析产品信息
        Map<String, Object> rawData = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        DocumentParseResult.ProductBasicInfo productInfo = parseProductInfo(workbook, rawData, warnings);

        workbook.close();

        return DocumentParseResult.builder()
                .success(true)
                .parseTime(LocalDateTime.now())
                .fileInfo(fileInfo)
                .productInfo(productInfo)
                .rawData(rawData)
                .warnings(warnings)
                .build();
    }

    /**
     * 从文件名检测模板类型
     */
    private String detectTemplateType(String fileName) {
        if (fileName == null) {
            return null;
        }

        String lowerFileName = fileName.toLowerCase();

        if (lowerFileName.contains("备案") || lowerFileName.contains("自主注册")) {
            return ProductTemplate.TemplateType.FILING.name();
        } else if (lowerFileName.contains("农险") || lowerFileName.contains("农业保险")) {
            return ProductTemplate.TemplateType.AGRICULTURAL.name();
        }

        return null;
    }

    /**
     * 使用字段配置解析
     */
    private DocumentParseResult.ProductBasicInfo parseWithFieldConfigs(
            Workbook workbook,
            List<TemplateFieldConfig> fieldConfigs,
            Map<String, Object> rawData,
            List<String> warnings) {

        DocumentParseResult.ProductBasicInfo.ProductBasicInfoBuilder builder =
                DocumentParseResult.ProductBasicInfo.builder();

        // 创建字段名到配置的映射
        Map<String, TemplateFieldConfig> configMap = new HashMap<>();
        if (fieldConfigs != null) {
            for (TemplateFieldConfig config : fieldConfigs) {
                configMap.put(config.getFieldName(), config);
                if (config.getFieldLabel() != null) {
                    configMap.put(config.getFieldLabel(), config);
                }
            }
        }

        // 遍历所有工作表
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            log.debug("解析工作表: {}", sheet.getSheetName());

            // 解析工作表
            parseSheetWithConfig(sheet, configMap, builder, rawData, warnings);
        }

        return builder.build();
    }

    /**
     * 使用配置解析工作表
     */
    private void parseSheetWithConfig(
            Sheet sheet,
            Map<String, TemplateFieldConfig> configMap,
            DocumentParseResult.ProductBasicInfo.ProductBasicInfoBuilder builder,
            Map<String, Object> rawData,
            List<String> warnings) {

        for (Row row : sheet) {
            if (row == null) {
                continue;
            }

            // 遍历行中的每个单元格
            for (Cell cell : row) {
                if (cell == null) {
                    continue;
                }

                // 尝试识别字段并提取值
                parseCell(cell, row, configMap, builder, rawData, warnings);
            }
        }
    }

    /**
     * 解析单元格
     */
    private void parseCell(
            Cell cell,
            Row row,
            Map<String, TemplateFieldConfig> configMap,
            DocumentParseResult.ProductBasicInfo.ProductBasicInfoBuilder builder,
            Map<String, Object> rawData,
            List<String> warnings) {

        String cellValue = getCellValueAsString(cell);
        if (StringUtils.isBlank(cellValue)) {
            return;
        }

        // 检查是否为字段标签
        TemplateFieldConfig config = configMap.get(cellValue.trim());
        if (config == null) {
            // 尝试从FIELD_MAPPING查找
            String fieldName = FIELD_MAPPING.get(cellValue.trim());
            if (fieldName != null) {
                config = configMap.get(fieldName);
            }
        }

        if (config != null) {
            // 提取字段值（通常在标签的下一个单元格或下一行）
            String fieldValue = extractFieldValue(cell, row);
            if (fieldValue != null) {
                rawData.put(config.getFieldName(), fieldValue);
                setFieldValue(builder, config.getFieldName(), fieldValue);
                log.debug("解析字段: {} = {}", config.getFieldName(), fieldValue);
            }
        }
    }

    /**
     * 提取字段值
     */
    private String extractFieldValue(Cell labelCell, Row row) {
        // 尝试从右侧单元格获取值
        int nextColumnIndex = labelCell.getColumnIndex() + 1;
        if (nextColumnIndex < row.getLastCellNum()) {
            Cell valueCell = row.getCell(nextColumnIndex);
            if (valueCell != null) {
                String value = getCellValueAsString(valueCell);
                if (StringUtils.isNotBlank(value)) {
                    return value.trim();
                }
            }
        }

        // 尝试从下一行同列获取值
        Sheet sheet = row.getSheet();
        int nextRowIndex = row.getRowNum() + 1;
        if (nextRowIndex <= sheet.getLastRowNum()) {
            Row nextRow = sheet.getRow(nextRowIndex);
            if (nextRow != null) {
                Cell valueCell = nextRow.getCell(labelCell.getColumnIndex());
                if (valueCell != null) {
                    String value = getCellValueAsString(valueCell);
                    if (StringUtils.isNotBlank(value)) {
                        return value.trim();
                    }
                }
            }
        }

        return null;
    }

    /**
     * 设置字段值到builder
     */
    private void setFieldValue(DocumentParseResult.ProductBasicInfo.ProductBasicInfoBuilder builder,
                              String fieldName,
                              String fieldValue) {
        // 这里简化处理，实际应该根据字段类型进行转换
        switch (fieldName) {
            case "productName":
                builder.productName(fieldValue);
                break;
            case "reportType":
                builder.reportType(fieldValue);
                break;
            case "productNature":
                builder.productNature(fieldValue);
                break;
            case "year":
                try {
                    builder.year(Integer.parseInt(fieldValue));
                } catch (NumberFormatException e) {
                    log.warn("年度字段解析失败: {}", fieldValue);
                }
                break;
            case "productCategory":
                builder.productCategory(fieldValue);
                break;
            case "operatingRegion":
                builder.operatingRegion(fieldValue);
                break;
            default:
                log.debug("未映射的字段: {}", fieldName);
        }
    }

    @Override
    public boolean isSupportedFormat(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        // 检查Content-Type
        if (contentType != null) {
            for (String format : SUPPORTED_FORMATS) {
                if (format.equals(contentType)) {
                    return true;
                }
            }
        }

        // 检查文件扩展名
        if (fileName != null) {
            String lowerFileName = fileName.toLowerCase();
            return lowerFileName.endsWith(".xlsx") || lowerFileName.endsWith(".xls");
        }

        return false;
    }

    @Override
    public String[] getSupportedFormats() {
        return SUPPORTED_FORMATS.clone();
    }

    /**
     * 创建Workbook对象
     */
    private Workbook createWorkbook(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
                return new XSSFWorkbook(inputStream);
            } else {
                return new HSSFWorkbook(inputStream);
            }
        }
    }

    /**
     * 提取文件信息
     */
    private DocumentParseResult.FileInfo extractFileInfo(MultipartFile file, Workbook workbook) {
        int sheetCount = workbook.getNumberOfSheets();
        int dataRowCount = 0;

        // 计算总数据行数
        for (int i = 0; i < sheetCount; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            dataRowCount += sheet.getLastRowNum() + 1;
        }

        return DocumentParseResult.FileInfo.builder()
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .sheetCount(sheetCount)
                .dataRowCount(dataRowCount)
                .build();
    }

    /**
     * 解析产品信息
     */
    private DocumentParseResult.ProductBasicInfo parseProductInfo(Workbook workbook,
                                                                Map<String, Object> rawData,
                                                                List<String> warnings) {
        DocumentParseResult.ProductBasicInfo.ProductBasicInfoBuilder builder =
                DocumentParseResult.ProductBasicInfo.builder();

        // 遍历所有工作表
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            log.debug("解析工作表: {}", sheet.getSheetName());

            // 解析当前工作表
            parseSheet(sheet, builder, rawData, warnings);
        }

        return builder.build();
    }

    /**
     * 解析单个工作表
     */
    private void parseSheet(Sheet sheet, DocumentParseResult.ProductBasicInfo.ProductBasicInfoBuilder builder,
                           Map<String, Object> rawData, List<String> warnings) {

        for (Row row : sheet) {
            if (row == null) continue;

            // 尝试解析键值对模式（如：产品名称: XXX）
            parseKeyValueRow(row, builder, rawData, warnings);
        }
    }

    /**
     * 解析键值对行
     */
    private void parseKeyValueRow(Row row, DocumentParseResult.ProductBasicInfo.ProductBasicInfoBuilder builder,
                                 Map<String, Object> rawData, List<String> warnings) {

        if (row.getLastCellNum() < 2) return;

        Cell keyCell = row.getCell(0);
        Cell valueCell = row.getCell(1);

        if (keyCell == null || valueCell == null) return;

        String key = getCellValueAsString(keyCell).trim();
        String value = getCellValueAsString(valueCell).trim();

        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) return;

        // 清理key，去除冒号和空格
        key = key.replaceAll("[：:]\\s*$", "").trim();

        // 存储原始数据
        rawData.put(key, value);

        // 根据字段映射设置产品信息
        String fieldName = findMatchingField(key);
        if (fieldName != null) {
            setProductField(builder, fieldName, value, warnings);
            log.debug("解析字段: {} -> {} = {}", key, fieldName, value);
        }
    }

    /**
     * 查找匹配的字段名
     */
    private String findMatchingField(String key) {
        // 直接匹配
        if (FIELD_MAPPING.containsKey(key)) {
            return FIELD_MAPPING.get(key);
        }

        // 模糊匹配
        for (Map.Entry<String, String> entry : FIELD_MAPPING.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 设置产品字段值
     */
    private void setProductField(DocumentParseResult.ProductBasicInfo.ProductBasicInfoBuilder builder,
                                String fieldName, String value, List<String> warnings) {
        try {
            switch (fieldName) {
                case "productName":
                    builder.productName(value);
                    break;
                case "reportType":
                    builder.reportType(value);
                    break;
                case "productNature":
                    builder.productNature(value);
                    break;
                case "year":
                    // 尝试解析年度
                    Integer year = parseYear(value);
                    if (year != null) {
                        builder.year(year);
                    } else {
                        warnings.add("年度格式无法识别: " + value);
                    }
                    break;
                case "productCategory":
                    builder.productCategory(value);
                    break;
                case "operatingRegion":
                    builder.operatingRegion(value);
                    break;
                case "developmentMethod":
                    builder.developmentMethod(value);
                    break;
                case "primaryAdditional":
                    builder.primaryAdditional(value);
                    break;
                case "revisionType":
                    builder.revisionType(value);
                    break;
                case "originalProductName":
                    builder.originalProductName(value);
                    break;
                case "demonstrationClauseName":
                    builder.demonstrationClauseName(value);
                    break;
                case "operatingScope":
                    builder.operatingScope(value);
                    break;
                case "salesPromotionName":
                    builder.salesPromotionName(value);
                    break;
            }
        } catch (Exception e) {
            warnings.add("设置字段 " + fieldName + " 失败: " + e.getMessage());
        }
    }

    /**
     * 解析年度
     */
    private Integer parseYear(String value) {
        if (StringUtils.isBlank(value)) return null;

        // 提取数字
        Pattern yearPattern = Pattern.compile("(\\d{4})");
        java.util.regex.Matcher matcher = yearPattern.matcher(value);

        if (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                // 验证年度合理性
                int currentYear = LocalDateTime.now().getYear();
                if (year >= 2000 && year <= currentYear + 5) {
                    return year;
                }
            } catch (NumberFormatException e) {
                log.debug("年度解析失败: {}", value);
            }
        }

        return null;
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字，避免科学记数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
}
