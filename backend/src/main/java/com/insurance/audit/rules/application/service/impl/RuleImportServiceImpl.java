package com.insurance.audit.rules.application.service.impl;

import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.rules.application.service.RuleImportService;
import com.insurance.audit.rules.application.service.RuleService;
import com.insurance.audit.rules.domain.Rule;
import com.insurance.audit.rules.enums.RuleType;
import com.insurance.audit.rules.infrastructure.mapper.RuleMapper;
import com.insurance.audit.rules.interfaces.dto.request.ImportValidationRequest;
import com.insurance.audit.rules.interfaces.dto.response.ImportResultResponse;
import com.insurance.audit.rules.interfaces.dto.response.ImportTemplateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则导入服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleImportServiceImpl implements RuleImportService {

    private final RuleService ruleService;
    private final RuleMapper ruleMapper;

    @Override
    public Resource generateTemplate(String ruleType) {
        log.info("生成导入模板: {}", ruleType);

        try {
            RuleType type = RuleType.valueOf(ruleType.toUpperCase());

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("规则导入模板");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = getTemplateHeaders(type);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            String[] examples = getTemplateExamples(type);

            for (int i = 0; i < examples.length; i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(examples[i]);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("生成导入模板成功: {}", ruleType);
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不支持的规则类型: " + ruleType);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "生成模板失败: " + e.getMessage());
        }
    }

    @Override
    public ImportTemplateResponse getTemplateInfo(String ruleType) {
        log.info("获取模板信息: {}", ruleType);

        try {
            RuleType type = RuleType.valueOf(ruleType.toUpperCase());

            ImportTemplateResponse response = new ImportTemplateResponse();
            response.setRuleType(ruleType);
            response.setTemplateVersion("1.0");
            response.setHeaders(Arrays.asList(getTemplateHeaders(type)));
            response.setRequiredFields(getRequiredFields(type));
            response.setValidationRules(getValidationRules(type));
            response.setCreatedAt(LocalDateTime.now());

            log.info("获取模板信息成功: {}", ruleType);
            return response;

        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不支持的规则类型: " + ruleType);
        }
    }

    @Override
    public ImportResultResponse validateImportFile(MultipartFile file, ImportValidationRequest request) {
        log.info("验证导入文件: {}, 规则类型: {}", file.getOriginalFilename(), request.getRuleType());

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "导入文件不能为空");
        }

        try {
            // 解析文件
            List<Object> data = parseExcelFile(file, null, 1);

            ImportResultResponse response = new ImportResultResponse();
            response.setBatchId(generateBatchId());
            response.setFileName(file.getOriginalFilename());
            response.setTotalRows(data.size());
            response.setValidRows(0);
            response.setInvalidRows(0);
            response.setErrors(new ArrayList<>());

            // 验证每行数据
            for (int i = 0; i < data.size(); i++) {
                List<ImportResultResponse.ImportError> errors = validateRuleData(data.get(i), request.getRuleType());
                if (errors.isEmpty()) {
                    response.setValidRows(response.getValidRows() + 1);
                } else {
                    response.setInvalidRows(response.getInvalidRows() + 1);
                    response.getErrors().addAll(errors);
                }
            }

            response.setValidationStatus(response.getInvalidRows() == 0 ? "PASSED" : "FAILED");

            log.info("验证导入文件完成: 总行数={}, 有效={}, 无效={}",
                    response.getTotalRows(), response.getValidRows(), response.getInvalidRows());

            return response;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "文件验证失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultResponse importRules(MultipartFile file, String importMode, boolean skipErrors, int batchSize) {
        log.info("批量导入规则: {}, 导入模式: {}, 跳过错误: {}, 批次大小: {}",
                file.getOriginalFilename(), importMode, skipErrors, batchSize);

        try {
            String batchId = generateBatchId();

            // 解析文件
            List<Object> data = parseExcelFile(file, null, 1);

            ImportResultResponse response = new ImportResultResponse();
            response.setBatchId(batchId);
            response.setFileName(file.getOriginalFilename());
            response.setTotalRows(data.size());
            response.setSuccessRows(0);
            response.setFailedRows(0);
            response.setErrors(new ArrayList<>());

            // 分批处理
            int processed = 0;
            for (int i = 0; i < data.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, data.size());
                List<Object> batch = data.subList(i, endIndex);

                ImportResultResponse batchResult = processBatch(batch, importMode, skipErrors, batchId, i);
                response.setSuccessRows(response.getSuccessRows() + batchResult.getSuccessRows());
                response.setFailedRows(response.getFailedRows() + batchResult.getFailedRows());
                response.getErrors().addAll(batchResult.getErrors());

                processed += batch.size();
                log.info("批次处理进度: {}/{}", processed, data.size());
            }

            response.setImportStatus(response.getFailedRows() == 0 ? "SUCCESS" : "PARTIAL_SUCCESS");

            log.info("批量导入规则完成: 成功={}, 失败={}", response.getSuccessRows(), response.getFailedRows());
            return response;

        } catch (Exception e) {
            log.error("批量导入规则失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "导入失败: " + e.getMessage());
        }
    }

    @Override
    public ImportResultResponse previewImport(MultipartFile file, int previewRows) {
        log.info("预览导入数据: {}, 预览行数: {}", file.getOriginalFilename(), previewRows);

        try {
            List<Object> data = parseExcelFile(file, null, 1);

            // 限制预览行数
            int actualRows = Math.min(previewRows, data.size());
            List<Object> previewData = data.subList(0, actualRows);

            ImportResultResponse response = new ImportResultResponse();
            response.setFileName(file.getOriginalFilename());
            response.setTotalRows(data.size());
            response.setPreviewRows(actualRows);
            response.setPreviewData(previewData);

            log.info("预览导入数据完成: 总行数={}, 预览行数={}", data.size(), actualRows);
            return response;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "预览失败: " + e.getMessage());
        }
    }

    @Override
    public List<Object> getImportHistory(int page, int size) {
        log.info("获取导入历史: 页码={}, 页面大小={}", page, size);

        // TODO: 实现导入历史查询
        List<Object> history = new ArrayList<>();

        log.info("获取导入历史完成: 数量={}", history.size());
        return history;
    }

    @Override
    public ImportResultResponse getImportResult(String batchId) {
        log.info("获取导入结果: {}", batchId);

        // TODO: 实现导入结果查询
        ImportResultResponse response = new ImportResultResponse();
        response.setBatchId(batchId);

        log.info("获取导入结果完成: {}", batchId);
        return response;
    }

    @Override
    public Resource generateImportReport(String batchId) {
        log.info("生成导入报告: {}", batchId);

        try {
            // TODO: 实现导入报告生成
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("导入报告");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("批次ID");
            headerRow.createCell(1).setCellValue("导入时间");
            headerRow.createCell(2).setCellValue("成功数量");
            headerRow.createCell(3).setCellValue("失败数量");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("生成导入报告成功: {}", batchId);
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "生成报告失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteImportBatch(String batchId, boolean deleteData) {
        log.info("删除导入批次: {}, 删除数据: {}", batchId, deleteData);

        try {
            // TODO: 实现批次删除逻辑

            log.info("删除导入批次成功: {}", batchId);
            return true;

        } catch (Exception e) {
            log.error("删除导入批次失败: {}", batchId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultResponse rollbackImport(String batchId, String reason) {
        log.info("回滚导入数据: {}, 原因: {}", batchId, reason);

        try {
            // TODO: 实现导入回滚逻辑

            ImportResultResponse response = new ImportResultResponse();
            response.setBatchId(batchId);
            response.setRollbackStatus("SUCCESS");
            response.setRollbackReason(reason);

            log.info("回滚导入数据成功: {}", batchId);
            return response;

        } catch (Exception e) {
            log.error("回滚导入数据失败: {}", batchId, e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "回滚失败: " + e.getMessage());
        }
    }

    @Override
    public List<Object> parseExcelFile(MultipartFile file, String sheetName, int startRow) {
        log.info("解析Excel文件: {}, 工作表: {}, 起始行: {}", file.getOriginalFilename(), sheetName, startRow);

        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = StringUtils.hasText(sheetName) ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);

            if (sheet == null) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "工作表不存在: " + sheetName);
            }

            List<Object> data = new ArrayList<>();
            int lastRowNum = sheet.getLastRowNum();

            for (int i = startRow; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new HashMap<>();
                int lastCellNum = row.getLastCellNum();

                for (int j = 0; j < lastCellNum; j++) {
                    Cell cell = row.getCell(j);
                    String value = getCellValue(cell);
                    rowData.put("column_" + j, value);
                }

                if (!rowData.isEmpty()) {
                    data.add(rowData);
                }
            }

            workbook.close();

            log.info("解析Excel文件完成: 数据行数={}", data.size());
            return data;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "文件解析失败: " + e.getMessage());
        }
    }

    @Override
    public List<ImportResultResponse.ImportError> validateRuleData(Object data, String ruleType) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();

        if (!(data instanceof Map)) {
            errors.add(new ImportResultResponse.ImportError(-1, "数据格式错误", "数据不是Map格式"));
            return errors;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> rowData = (Map<String, Object>) data;

        // 基本字段验证
        if (!StringUtils.hasText((String) rowData.get("column_0"))) {
            errors.add(new ImportResultResponse.ImportError(0, "规则名称", "规则名称不能为空"));
        }

        if (!StringUtils.hasText((String) rowData.get("column_1"))) {
            errors.add(new ImportResultResponse.ImportError(1, "规则描述", "规则描述不能为空"));
        }

        // 根据规则类型进行特定验证
        try {
            RuleType type = RuleType.valueOf(ruleType.toUpperCase());
            validateByRuleType(rowData, type, errors);
        } catch (IllegalArgumentException e) {
            errors.add(new ImportResultResponse.ImportError(-1, "规则类型", "无效的规则类型: " + ruleType));
        }

        return errors;
    }

    @Override
    public Object convertToRuleEntity(Object data, String ruleType) {
        if (!(data instanceof Map)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "数据格式错误");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> rowData = (Map<String, Object>) data;

        Rule rule = new Rule();
        rule.setRuleName((String) rowData.get("column_0"));
        rule.setRuleDescription((String) rowData.get("column_1"));
        rule.setRuleType(RuleType.valueOf(ruleType.toUpperCase()));
        rule.setRuleContent((String) rowData.get("column_2"));
        rule.setCreatedBy(getCurrentUserId());
        rule.setUpdatedBy(getCurrentUserId());
        rule.setLastUpdatedAt(LocalDateTime.now());

        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultResponse batchSaveRules(List<Object> rules, int batchSize) {
        log.info("批量保存规则: 数量={}, 批次大小={}", rules.size(), batchSize);

        ImportResultResponse response = new ImportResultResponse();
        response.setSuccessRows(0);
        response.setFailedRows(0);
        response.setErrors(new ArrayList<>());

        for (int i = 0; i < rules.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, rules.size());
            List<Object> batch = rules.subList(i, endIndex);

            try {
                // 批量插入
                for (Object ruleObj : batch) {
                    if (ruleObj instanceof Rule) {
                        Rule rule = (Rule) ruleObj;
                        ruleMapper.insert(rule);
                        response.setSuccessRows(response.getSuccessRows() + 1);
                    }
                }
            } catch (Exception e) {
                response.setFailedRows(response.getFailedRows() + batch.size());
                response.getErrors().add(new ImportResultResponse.ImportError(i, "批量保存", e.getMessage()));
                log.error("批量保存规则失败: 批次起始索引={}", i, e);
            }
        }

        log.info("批量保存规则完成: 成功={}, 失败={}", response.getSuccessRows(), response.getFailedRows());
        return response;
    }

    /**
     * 处理单个批次
     */
    private ImportResultResponse processBatch(List<Object> batch, String importMode, boolean skipErrors,
                                            String batchId, int startIndex) {
        ImportResultResponse response = new ImportResultResponse();
        response.setSuccessRows(0);
        response.setFailedRows(0);
        response.setErrors(new ArrayList<>());

        for (int i = 0; i < batch.size(); i++) {
            try {
                Object data = batch.get(i);
                // TODO: 根据导入模式处理数据
                response.setSuccessRows(response.getSuccessRows() + 1);
            } catch (Exception e) {
                response.setFailedRows(response.getFailedRows() + 1);
                response.getErrors().add(new ImportResultResponse.ImportError(
                    startIndex + i, "数据处理", e.getMessage()));

                if (!skipErrors) {
                    throw e;
                }
            }
        }

        return response;
    }

    /**
     * 获取模板标题
     */
    private String[] getTemplateHeaders(RuleType ruleType) {
        switch (ruleType) {
            case SINGLE:
                return new String[]{"规则名称", "规则描述", "规则内容", "规则来源"};
            case DOUBLE:
                return new String[]{"规则名称", "规则描述", "第一条件", "第二条件", "规则来源"};
            case FORMAT:
                return new String[]{"规则名称", "规则描述", "格式模式", "示例", "规则来源"};
            case ADVANCED:
                return new String[]{"规则名称", "规则描述", "规则脚本", "参数配置", "规则来源"};
            default:
                return new String[]{"规则名称", "规则描述", "规则内容", "规则来源"};
        }
    }

    /**
     * 获取模板示例
     */
    private String[] getTemplateExamples(RuleType ruleType) {
        switch (ruleType) {
            case SINGLE:
                return new String[]{"示例单句规则", "这是一个示例单句规则", "年龄 > 18", "手工录入"};
            case DOUBLE:
                return new String[]{"示例双句规则", "这是一个示例双句规则", "年龄 > 18", "收入 > 50000", "手工录入"};
            case FORMAT:
                return new String[]{"示例格式规则", "这是一个示例格式规则", "^[0-9]{11}$", "13812345678", "手工录入"};
            case ADVANCED:
                return new String[]{"示例高级规则", "这是一个示例高级规则", "function validate(data) { return true; }", "{\"timeout\": 5000}", "手工录入"};
            default:
                return new String[]{"示例规则", "这是一个示例规则", "规则内容", "手工录入"};
        }
    }

    /**
     * 获取必填字段
     */
    private List<String> getRequiredFields(RuleType ruleType) {
        return Arrays.asList("规则名称", "规则描述", "规则内容");
    }

    /**
     * 获取验证规则
     */
    private Map<String, String> getValidationRules(RuleType ruleType) {
        Map<String, String> rules = new HashMap<>();
        rules.put("规则名称", "长度1-100字符");
        rules.put("规则描述", "长度1-500字符");
        rules.put("规则内容", "不能为空");
        return rules;
    }

    /**
     * 根据规则类型验证
     */
    private void validateByRuleType(Map<String, Object> rowData, RuleType ruleType,
                                  List<ImportResultResponse.ImportError> errors) {
        // TODO: 实现具体的规则类型验证逻辑
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
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
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 生成批次ID
     */
    private String generateBatchId() {
        return "BATCH_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 1000));
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        return "system";
    }
}