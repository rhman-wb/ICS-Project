package com.insurance.audit.rules.application.service;

import com.insurance.audit.rules.interfaces.dto.request.ImportValidationRequest;
import com.insurance.audit.rules.interfaces.dto.response.ImportResultResponse;
import com.insurance.audit.rules.interfaces.dto.response.ImportTemplateResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 规则导入服务接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
public interface RuleImportService {

    /**
     * 生成导入模板
     *
     * @param ruleType 规则类型
     * @return 模板文件资源
     */
    Resource generateTemplate(String ruleType);

    /**
     * 获取模板信息
     *
     * @param ruleType 规则类型
     * @return 模板信息
     */
    ImportTemplateResponse getTemplateInfo(String ruleType);

    /**
     * 验证导入文件
     *
     * @param file 上传文件
     * @param request 验证请求
     * @return 验证结果
     */
    ImportResultResponse validateImportFile(MultipartFile file, ImportValidationRequest request);

    /**
     * 批量导入规则
     *
     * @param file 上传文件
     * @param importMode 导入模式
     * @param skipErrors 是否跳过错误
     * @param batchSize 批次大小
     * @return 导入结果
     */
    ImportResultResponse importRules(MultipartFile file, String importMode, boolean skipErrors, int batchSize);

    /**
     * 预览导入数据
     *
     * @param file 上传文件
     * @param previewRows 预览行数
     * @return 预览结果
     */
    ImportResultResponse previewImport(MultipartFile file, int previewRows);

    /**
     * 获取导入历史
     *
     * @param page 页码
     * @param size 页面大小
     * @return 导入历史列表
     */
    List<Object> getImportHistory(int page, int size);

    /**
     * 获取导入结果
     *
     * @param batchId 批次ID
     * @return 导入结果
     */
    ImportResultResponse getImportResult(String batchId);

    /**
     * 生成导入报告
     *
     * @param batchId 批次ID
     * @return 报告文件资源
     */
    Resource generateImportReport(String batchId);

    /**
     * 删除导入批次
     *
     * @param batchId 批次ID
     * @param deleteData 是否删除数据
     * @return 是否成功
     */
    boolean deleteImportBatch(String batchId, boolean deleteData);

    /**
     * 回滚导入数据
     *
     * @param batchId 批次ID
     * @param reason 回滚原因
     * @return 回滚结果
     */
    ImportResultResponse rollbackImport(String batchId, String reason);

    /**
     * 解析Excel文件
     *
     * @param file 上传文件
     * @param sheetName 工作表名称
     * @param startRow 起始行
     * @return 解析结果
     */
    List<Object> parseExcelFile(MultipartFile file, String sheetName, int startRow);

    /**
     * 验证规则数据
     *
     * @param data 规则数据
     * @param ruleType 规则类型
     * @return 验证结果
     */
    List<ImportResultResponse.ImportError> validateRuleData(Object data, String ruleType);

    /**
     * 转换导入数据为规则实体
     *
     * @param data 导入数据
     * @param ruleType 规则类型
     * @return 规则实体
     */
    Object convertToRuleEntity(Object data, String ruleType);

    /**
     * 批量保存规则
     *
     * @param rules 规则列表
     * @param batchSize 批次大小
     * @return 保存结果
     */
    ImportResultResponse batchSaveRules(List<Object> rules, int batchSize);
}