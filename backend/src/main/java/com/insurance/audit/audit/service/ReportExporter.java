package com.insurance.audit.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.insurance.audit.audit.dto.AuditJobResponse;
import com.insurance.audit.audit.dto.AuditResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 报告导出器
 * 支持导出Word/PDF/JSON/Excel格式的检核报告
 * 增强版：支持模板、安全检查、性能监控、多格式输出
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ReportExporter {

    @Autowired(required = false)
    private SecurityComplianceService securityComplianceService;

    @Value("${audit.report.max-size:50MB}")
    private String maxReportSize;

    @Value("${audit.report.template-path:#{null}}")
    private String templatePath;

    @Value("${audit.report.export-timeout:30000}")
    private long exportTimeoutMs;

    @Value("${audit.report.max-evidences:100}")
    private int maxEvidencesPerReport;

    @Value("${audit.report.enable-compression:true}")
    private boolean enableCompression;

    // 缓存导出结果以避免重复生成
    private final Map<String, ExportResult> exportCache = new ConcurrentHashMap<>();

    // Jackson ObjectMapper for JSON serialization
    private final ObjectMapper objectMapper;

    public ReportExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    /**
     * 导出检核报告（增强版：支持安全检查、性能监控、缓存）
     *
     * @param format 导出格式 (WORD, PDF, JSON, EXCEL)
     * @param job 作业信息
     * @param results 检核结果列表
     * @return 导出结果信息
     */
    public ExportResult exportReport(String format, AuditJobResponse job, List<AuditResultDto> results) {
        long startTime = System.currentTimeMillis();
        String cacheKey = generateCacheKey(format, job.getJobId(), results);

        log.info("开始导出报告: jobId={}, format={}, resultCount={}", job.getJobId(), format, results.size());

        try {
            // 安全检查：验证导出权限
            if (securityComplianceService != null) {
                boolean hasPermission = securityComplianceService.hasAuditPermission("EXPORT_REPORT");
                if (!hasPermission) {
                    throw new SecurityException("无权限导出检核报告");
                }

                boolean hasDataScope = securityComplianceService.hasDataScopePermission("report", job.getJobId());
                if (!hasDataScope) {
                    throw new SecurityException("无权限访问指定作业的报告数据");
                }

                log.debug("报告导出权限验证通过: jobId={}, format={}", job.getJobId(), format);
            }

            // 检查缓存
            if (exportCache.containsKey(cacheKey)) {
                log.debug("使用缓存的导出结果: cacheKey={}", cacheKey);
                return exportCache.get(cacheKey);
            }

            // 数据验证
            validateExportRequest(format, job, results);

            // 数据脱敏处理
            List<AuditResultDto> sanitizedResults = results;
            if (securityComplianceService != null) {
                sanitizedResults = securityComplianceService.redactExportData(results);
                log.debug("已执行导出数据脱敏处理: jobId={}", job.getJobId());
            }

            // 限制证据数量以控制报告大小
            List<AuditResultDto> limitedResults = limitEvidences(sanitizedResults);

            // 执行导出
            ExportResult result = performExport(format, job, limitedResults);

            // 添加元数据
            enrichExportResult(result, job, limitedResults, startTime);

            // 缓存结果（小于10MB的结果才缓存）
            if (result.getSize() < 10 * 1024 * 1024) {
                exportCache.put(cacheKey, result);
            }

            // 记录审计日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("REPORT_EXPORTED",
                    String.format("导出报告: jobId=%s, format=%s, size=%d bytes",
                                  job.getJobId(), format, result.getSize()),
                    job.getJobId(), "INFO");
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("报告导出完成: jobId={}, format={}, size={}KB, duration={}ms",
                    job.getJobId(), format, result.getSize() / 1024, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("导出报告失败: jobId={}, format={}, duration={}ms, error={}",
                     job.getJobId(), format, duration, e.getMessage(), e);

            // 记录失败审计日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("REPORT_EXPORT_FAILED",
                    String.format("导出报告失败: jobId=%s, format=%s, error=%s",
                                  job.getJobId(), format, e.getMessage()),
                    job.getJobId(), "ERROR");
            }

            throw new RuntimeException("报告导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行实际的导出操作
     */
    private ExportResult performExport(String format, AuditJobResponse job, List<AuditResultDto> results) throws IOException {
        switch (format.toUpperCase()) {
            case "WORD":
            case "DOCX":
                return exportWordReport(job, results);
            case "PDF":
                return exportPdfReport(job, results);
            case "JSON":
                return exportJsonReport(job, results);
            case "EXCEL":
            case "XLSX":
                return exportExcelReport(job, results);
            default:
                throw new UnsupportedOperationException("不支持的导出格式: " + format);
        }
    }

    /**
     * 导出Word报告
     */
    private ExportResult exportWordReport(AuditJobResponse job, List<AuditResultDto> results) throws IOException {
        log.debug("生成Word报告: jobId={}", job.getJobId());

        XWPFDocument document = new XWPFDocument();

        try {
            // 1. 添加标题
            addTitle(document, job);

            // 2. 添加作业概要
            addJobSummary(document, job);

            // 3. 添加检核结果摘要
            addResultSummary(document, results);

            // 4. 添加详细结果
            addDetailedResults(document, results);

            // 5. 添加附录
            addAppendix(document, job, results);

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            byte[] content = outputStream.toByteArray();

            String fileName = generateFileName(job.getJobId(), "docx");

            log.info("Word报告生成完成: jobId={}, fileName={}, size={}KB",
                    job.getJobId(), fileName, content.length / 1024);

            return ExportResult.builder()
                    .fileName(fileName)
                    .format("WORD")
                    .content(content)
                    .size(content.length)
                    .exportTime(LocalDateTime.now())
                    .build();

        } finally {
            document.close();
        }
    }

    /**
     * 添加标题
     */
    private void addTitle(XWPFDocument document, AuditJobResponse job) {
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("保险产品智能检核报告");
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setFontFamily("宋体");

        // 添加副标题
        XWPFParagraph subtitleParagraph = document.createParagraph();
        subtitleParagraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun subtitleRun = subtitleParagraph.createRun();
        subtitleRun.setText(job.getJobName());
        subtitleRun.setFontSize(14);
        subtitleRun.setFontFamily("宋体");

        // 添加空行
        document.createParagraph();
    }

    /**
     * 添加作业概要
     */
    private void addJobSummary(XWPFDocument document, AuditJobResponse job) {
        XWPFParagraph heading = document.createParagraph();
        XWPFRun headingRun = heading.createRun();
        headingRun.setText("一、作业概要");
        headingRun.setBold(true);
        headingRun.setFontSize(14);

        // 创建表格
        XWPFTable table = document.createTable(6, 2);
        table.setWidth("100%");

        // 设置表格内容
        setTableCell(table, 0, 0, "作业ID", true);
        setTableCell(table, 0, 1, job.getJobId(), false);

        setTableCell(table, 1, 0, "作业名称", true);
        setTableCell(table, 1, 1, job.getJobName(), false);

        setTableCell(table, 2, 0, "执行状态", true);
        setTableCell(table, 2, 1, job.getStatus(), false);

        setTableCell(table, 3, 0, "开始时间", true);
        setTableCell(table, 3, 1, formatDateTime(job.getStartTime()), false);

        setTableCell(table, 4, 0, "结束时间", true);
        setTableCell(table, 4, 1, formatDateTime(job.getEndTime()), false);

        setTableCell(table, 5, 0, "执行进度", true);
        setTableCell(table, 5, 1, job.getProgress() + "%", false);

        document.createParagraph(); // 空行
    }

    /**
     * 添加结果摘要
     */
    private void addResultSummary(XWPFDocument document, List<AuditResultDto> results) {
        XWPFParagraph heading = document.createParagraph();
        XWPFRun headingRun = heading.createRun();
        headingRun.setText("二、检核结果摘要");
        headingRun.setBold(true);
        headingRun.setFontSize(14);

        // 统计结果
        Map<String, Long> statusCounts = results.stream()
                .collect(Collectors.groupingBy(AuditResultDto::getStatus, Collectors.counting()));

        long totalRules = results.size();
        long passedRules = statusCounts.getOrDefault("PASSED", 0L);
        long failedRules = statusCounts.getOrDefault("FAILED", 0L);
        long warningRules = statusCounts.getOrDefault("WARNING", 0L);

        double passRate = totalRules > 0 ? (double) passedRules / totalRules * 100 : 0.0;

        // 创建摘要表格
        XWPFTable summaryTable = document.createTable(5, 2);
        summaryTable.setWidth("100%");

        setTableCell(summaryTable, 0, 0, "总规则数", true);
        setTableCell(summaryTable, 0, 1, String.valueOf(totalRules), false);

        setTableCell(summaryTable, 1, 0, "通过规则数", true);
        setTableCell(summaryTable, 1, 1, String.valueOf(passedRules), false);

        setTableCell(summaryTable, 2, 0, "失败规则数", true);
        setTableCell(summaryTable, 2, 1, String.valueOf(failedRules), false);

        setTableCell(summaryTable, 3, 0, "警告规则数", true);
        setTableCell(summaryTable, 3, 1, String.valueOf(warningRules), false);

        setTableCell(summaryTable, 4, 0, "通过率", true);
        setTableCell(summaryTable, 4, 1, String.format("%.2f%%", passRate), false);

        document.createParagraph(); // 空行
    }

    /**
     * 添加详细结果
     */
    private void addDetailedResults(XWPFDocument document, List<AuditResultDto> results) {
        XWPFParagraph heading = document.createParagraph();
        XWPFRun headingRun = heading.createRun();
        headingRun.setText("三、详细检核结果");
        headingRun.setBold(true);
        headingRun.setFontSize(14);

        int resultIndex = 1;
        for (AuditResultDto result : results) {
            // 子标题
            XWPFParagraph subHeading = document.createParagraph();
            XWPFRun subHeadingRun = subHeading.createRun();
            subHeadingRun.setText(String.format("%d. %s", resultIndex++, result.getRuleName()));
            subHeadingRun.setBold(true);
            subHeadingRun.setFontSize(12);

            // 结果详情表格
            XWPFTable resultTable = document.createTable(6, 2);
            resultTable.setWidth("100%");

            setTableCell(resultTable, 0, 0, "规则ID", true);
            setTableCell(resultTable, 0, 1, result.getRuleId(), false);

            setTableCell(resultTable, 1, 0, "检核状态", true);
            setTableCell(resultTable, 1, 1, getStatusText(result.getStatus()), false);

            setTableCell(resultTable, 2, 0, "置信度分数", true);
            setTableCell(resultTable, 2, 1, String.format("%.3f", result.getScore()), false);

            setTableCell(resultTable, 3, 0, "阈值", true);
            setTableCell(resultTable, 3, 1, String.format("%.3f", result.getThreshold()), false);

            setTableCell(resultTable, 4, 0, "证据数量", true);
            setTableCell(resultTable, 4, 1, String.valueOf(result.getEvidences().size()), false);

            setTableCell(resultTable, 5, 0, "建议", true);
            setTableCell(resultTable, 5, 1, result.getRecommendation(), false);

            // 如果有证据，添加证据详情
            if (!result.getEvidences().isEmpty()) {
                XWPFParagraph evidenceHeading = document.createParagraph();
                XWPFRun evidenceHeadingRun = evidenceHeading.createRun();
                evidenceHeadingRun.setText("证据详情：");
                evidenceHeadingRun.setBold(true);

                for (int i = 0; i < Math.min(result.getEvidences().size(), 5); i++) { // 最多显示5个证据
                    AuditResultDto.Evidence evidence = result.getEvidences().get(i);
                    XWPFParagraph evidencePara = document.createParagraph();
                    XWPFRun evidenceRun = evidencePara.createRun();
                    evidenceRun.setText(String.format("• %s (位置: %d-%d, 类型: %s)",
                            truncateText(evidence.getText(), 100),
                            evidence.getStartPos(),
                            evidence.getEndPos(),
                            evidence.getMatchType()));
                }
            }

            document.createParagraph(); // 空行
        }
    }

    /**
     * 添加附录
     */
    private void addAppendix(XWPFDocument document, AuditJobResponse job, List<AuditResultDto> results) {
        XWPFParagraph heading = document.createParagraph();
        XWPFRun headingRun = heading.createRun();
        headingRun.setText("四、附录");
        headingRun.setBold(true);
        headingRun.setFontSize(14);

        // 生成时间
        XWPFParagraph genTime = document.createParagraph();
        XWPFRun genTimeRun = genTime.createRun();
        genTimeRun.setText("报告生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 统计信息
        XWPFParagraph stats = document.createParagraph();
        XWPFRun statsRun = stats.createRun();
        int totalEvidences = results.stream().mapToInt(r -> r.getEvidences().size()).sum();
        statsRun.setText(String.format("统计信息: 共检核 %d 条规则，发现 %d 处证据", results.size(), totalEvidences));

        // 版本信息
        XWPFParagraph version = document.createParagraph();
        XWPFRun versionRun = version.createRun();
        versionRun.setText("系统版本: 智能检核引擎 v1.0.0");
    }

    /**
     * 导出JSON报告
     */
    private ExportResult exportJsonReport(AuditJobResponse job, List<AuditResultDto> results) {
        log.debug("生成JSON报告: jobId={}", job.getJobId());

        // 构建JSON结构
        Map<String, Object> report = Map.of(
            "job", job,
            "results", results,
            "summary", createSummary(results),
            "exportTime", LocalDateTime.now(),
            "version", "1.0.0"
        );

        // 转换为JSON字符串
        String jsonContent = convertToJson(report);
        byte[] content = jsonContent.getBytes();

        String fileName = generateFileName(job.getJobId(), "json");

        log.info("JSON报告生成完成: jobId={}, fileName={}, size={}KB",
                job.getJobId(), fileName, content.length / 1024);

        return ExportResult.builder()
                .fileName(fileName)
                .format("JSON")
                .content(content)
                .size(content.length)
                .exportTime(LocalDateTime.now())
                .build();
    }

    /**
     * 导出PDF报告（简化实现，使用HTML转PDF方式）
     */
    private ExportResult exportPdfReport(AuditJobResponse job, List<AuditResultDto> results) throws IOException {
        log.debug("生成PDF报告: jobId={}", job.getJobId());

        try {
            // 生成HTML内容
            String htmlContent = generateHtmlReport(job, results);

            // 简化的PDF实现：将HTML转换为PDF格式的文本文件
            // 实际项目中应使用专业的PDF库如iText或wkhtmltopdf
            String pdfContent = convertHtmlToPdfText(htmlContent);
            byte[] content = pdfContent.getBytes("UTF-8");

            String fileName = generateFileName(job.getJobId(), "pdf");

            log.info("PDF报告生成完成: jobId={}, fileName={}, size={}KB",
                    job.getJobId(), fileName, content.length / 1024);

            return ExportResult.builder()
                    .fileName(fileName)
                    .format("PDF")
                    .content(content)
                    .size(content.length)
                    .exportTime(LocalDateTime.now())
                    .metadata(Map.of(
                        "note", "简化的PDF实现，建议使用专业PDF库",
                        "format", "text-based-pdf"
                    ))
                    .build();

        } catch (Exception e) {
            log.error("PDF报告生成失败: jobId={}, error={}", job.getJobId(), e.getMessage());
            throw new IOException("PDF导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成HTML报告内容
     */
    private String generateHtmlReport(AuditJobResponse job, List<AuditResultDto> results) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html><head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>保险产品智能检核报告</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: SimSun, serif; margin: 20px; }\n");
        html.append("h1 { text-align: center; color: #333; }\n");
        html.append("h2 { color: #666; border-bottom: 2px solid #eee; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin: 10px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #f5f5f5; font-weight: bold; }\n");
        html.append(".status-passed { color: green; }\n");
        html.append(".status-failed { color: red; }\n");
        html.append(".status-warning { color: orange; }\n");
        html.append("</style>\n");
        html.append("</head><body>\n");

        // 标题
        html.append("<h1>保险产品智能检核报告</h1>\n");
        html.append("<h2>").append(job.getJobName()).append("</h2>\n");

        // 作业概要
        html.append("<h2>一、作业概要</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>作业ID</th><td>").append(job.getJobId()).append("</td></tr>\n");
        html.append("<tr><th>作业名称</th><td>").append(job.getJobName()).append("</td></tr>\n");
        html.append("<tr><th>执行状态</th><td>").append(job.getStatus()).append("</td></tr>\n");
        html.append("<tr><th>开始时间</th><td>").append(formatDateTime(job.getStartTime())).append("</td></tr>\n");
        html.append("<tr><th>结束时间</th><td>").append(formatDateTime(job.getEndTime())).append("</td></tr>\n");
        html.append("<tr><th>执行进度</th><td>").append(job.getProgress()).append("%</td></tr>\n");
        html.append("</table>\n");

        // 结果摘要
        Map<String, Long> statusCounts = results.stream()
                .collect(Collectors.groupingBy(AuditResultDto::getStatus, Collectors.counting()));

        long totalRules = results.size();
        long passedRules = statusCounts.getOrDefault("PASSED", 0L);
        double passRate = totalRules > 0 ? (double) passedRules / totalRules * 100 : 0.0;

        html.append("<h2>二、检核结果摘要</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>总规则数</th><td>").append(totalRules).append("</td></tr>\n");
        html.append("<tr><th>通过规则数</th><td>").append(passedRules).append("</td></tr>\n");
        html.append("<tr><th>失败规则数</th><td>").append(statusCounts.getOrDefault("FAILED", 0L)).append("</td></tr>\n");
        html.append("<tr><th>警告规则数</th><td>").append(statusCounts.getOrDefault("WARNING", 0L)).append("</td></tr>\n");
        html.append("<tr><th>通过率</th><td>").append(String.format("%.2f%%", passRate)).append("</td></tr>\n");
        html.append("</table>\n");

        // 详细结果
        html.append("<h2>三、详细检核结果</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>规则ID</th><th>规则名称</th><th>状态</th><th>置信度</th><th>证据数</th><th>建议</th></tr>\n");

        for (AuditResultDto result : results) {
            String statusClass = "status-" + result.getStatus().toLowerCase();
            html.append("<tr>\n");
            html.append("<td>").append(result.getRuleId()).append("</td>\n");
            html.append("<td>").append(result.getRuleName()).append("</td>\n");
            html.append("<td class='").append(statusClass).append("'>").append(getStatusText(result.getStatus())).append("</td>\n");
            html.append("<td>").append(String.format("%.3f", result.getScore())).append("</td>\n");
            html.append("<td>").append(result.getEvidences().size()).append("</td>\n");
            html.append("<td>").append(result.getRecommendation()).append("</td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");

        // 附录
        html.append("<h2>四、附录</h2>\n");
        html.append("<p>报告生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");
        html.append("<p>系统版本: 智能检核引擎 v1.0.0</p>\n");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * 将HTML转换为PDF格式的文本内容（简化实现）
     */
    private String convertHtmlToPdfText(String htmlContent) {
        // 简化的HTML到文本转换
        // 实际项目中应使用专业的HTML转PDF库
        String textContent = htmlContent
                .replaceAll("<[^>]+>", "")  // 移除HTML标签
                .replaceAll("&nbsp;", " ")  // 转换HTML实体
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("\\s+", " ")   // 压缩空白字符
                .trim();

        // 添加PDF标识头部
        StringBuilder pdfText = new StringBuilder();
        pdfText.append("%PDF-1.4 (简化文本格式)\n");
        pdfText.append("% 注意：这是简化的PDF实现，建议使用专业PDF库如iText\n");
        pdfText.append("% 生成时间: ").append(LocalDateTime.now()).append("\n\n");
        pdfText.append(textContent);

        return pdfText.toString();
    }

    /**
     * 数据验证
     */
    private void validateExportRequest(String format, AuditJobResponse job, List<AuditResultDto> results) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("导出格式不能为空");
        }

        if (job == null) {
            throw new IllegalArgumentException("作业信息不能为空");
        }

        if (results == null) {
            throw new IllegalArgumentException("检核结果不能为空");
        }

        // 验证导出格式
        String upperFormat = format.toUpperCase();
        if (!List.of("WORD", "DOCX", "PDF", "JSON", "EXCEL", "XLSX").contains(upperFormat)) {
            throw new IllegalArgumentException("不支持的导出格式: " + format);
        }

        // 验证结果数量
        if (results.size() > 1000) {
            log.warn("导出结果数量较大: count={}, 建议分批导出", results.size());
        }

        log.debug("导出请求验证通过: format={}, jobId={}, resultCount={}",
                format, job.getJobId(), results.size());
    }

    /**
     * 限制证据数量
     */
    private List<AuditResultDto> limitEvidences(List<AuditResultDto> results) {
        return results.stream()
                .map(result -> {
                    List<AuditResultDto.Evidence> limitedEvidences = result.getEvidences().stream()
                            .limit(maxEvidencesPerReport)
                            .collect(Collectors.toList());

                    return AuditResultDto.builder()
                            .ruleId(result.getRuleId())
                            .ruleName(result.getRuleName())
                            .status(result.getStatus())
                            .score(result.getScore())
                            .threshold(result.getThreshold())
                            .recommendation(result.getRecommendation())
                            .evidences(limitedEvidences)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 丰富导出结果
     */
    private void enrichExportResult(ExportResult result, AuditJobResponse job,
                                  List<AuditResultDto> results, long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("jobId", job.getJobId());
        metadata.put("jobName", job.getJobName());
        metadata.put("resultCount", results.size());
        metadata.put("exportDuration", duration);
        metadata.put("generator", "智能检核引擎");
        metadata.put("version", "1.0.0");

        // 添加统计信息
        Map<String, Long> statusCounts = results.stream()
                .collect(Collectors.groupingBy(AuditResultDto::getStatus, Collectors.counting()));
        metadata.put("statistics", statusCounts);

        result.setMetadata(metadata);
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String format, String jobId, List<AuditResultDto> results) {
        // 基于格式、作业ID和结果哈希生成缓存键
        int resultHash = results.stream()
                .mapToInt(r -> r.hashCode())
                .sum();

        return String.format("%s_%s_%d", format.toUpperCase(), jobId, resultHash);
    }

    /**
     * 导出Excel报告（完整实现）
     */
    private ExportResult exportExcelReport(AuditJobResponse job, List<AuditResultDto> results) throws IOException {
        log.debug("生成Excel报告: jobId={}", job.getJobId());

        XSSFWorkbook workbook = new XSSFWorkbook();

        try {
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 1. 概要工作表
            createSummarySheet(workbook, headerStyle, dataStyle, job, results);

            // 2. 详细结果工作表
            createDetailedResultsSheet(workbook, headerStyle, dataStyle, results);

            // 3. 证据明细工作表
            createEvidenceSheet(workbook, headerStyle, dataStyle, results);

            // 4. 统计图表工作表
            createStatisticsSheet(workbook, headerStyle, dataStyle, results);

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] content = outputStream.toByteArray();

            String fileName = generateFileName(job.getJobId(), "xlsx");

            log.info("Excel报告生成完成: jobId={}, fileName={}, size={}KB",
                    job.getJobId(), fileName, content.length / 1024);

            return ExportResult.builder()
                    .fileName(fileName)
                    .format("EXCEL")
                    .content(content)
                    .size(content.length)
                    .exportTime(LocalDateTime.now())
                    .build();

        } finally {
            workbook.close();
        }
    }

    /**
     * 创建概要工作表
     */
    private void createSummarySheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle,
                                  AuditJobResponse job, List<AuditResultDto> results) {
        Sheet sheet = workbook.createSheet("作业概要");

        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("保险产品智能检核报告");
        titleCell.setCellStyle(headerStyle);

        rowNum++; // 空行

        // 作业信息
        String[][] jobInfo = {
            {"作业ID", job.getJobId()},
            {"作业名称", job.getJobName()},
            {"执行状态", job.getStatus()},
            {"开始时间", formatDateTime(job.getStartTime())},
            {"结束时间", formatDateTime(job.getEndTime())},
            {"执行进度", job.getProgress() + "%"}
        };

        for (String[] info : jobInfo) {
            Row row = sheet.createRow(rowNum++);
            Cell keyCell = row.createCell(0);
            keyCell.setCellValue(info[0]);
            keyCell.setCellStyle(headerStyle);

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(info[1]);
            valueCell.setCellStyle(dataStyle);
        }

        rowNum++; // 空行

        // 结果统计
        Map<String, Long> statusCounts = results.stream()
                .collect(Collectors.groupingBy(AuditResultDto::getStatus, Collectors.counting()));

        long totalRules = results.size();
        long passedRules = statusCounts.getOrDefault("PASSED", 0L);
        double passRate = totalRules > 0 ? (double) passedRules / totalRules * 100 : 0.0;

        String[][] stats = {
            {"总规则数", String.valueOf(totalRules)},
            {"通过规则数", String.valueOf(passedRules)},
            {"失败规则数", String.valueOf(statusCounts.getOrDefault("FAILED", 0L))},
            {"警告规则数", String.valueOf(statusCounts.getOrDefault("WARNING", 0L))},
            {"通过率", String.format("%.2f%%", passRate)}
        };

        for (String[] stat : stats) {
            Row row = sheet.createRow(rowNum++);
            Cell keyCell = row.createCell(0);
            keyCell.setCellValue(stat[0]);
            keyCell.setCellStyle(headerStyle);

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(stat[1]);
            valueCell.setCellStyle(dataStyle);
        }

        // 自动调整列宽
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建详细结果工作表
     */
    private void createDetailedResultsSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle,
                                          List<AuditResultDto> results) {
        Sheet sheet = workbook.createSheet("检核结果");

        int rowNum = 0;

        // 表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"规则ID", "规则名称", "检核状态", "置信度分数", "阈值", "证据数量", "建议"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据行
        for (AuditResultDto result : results) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(result.getRuleId());
            row.createCell(1).setCellValue(result.getRuleName());
            row.createCell(2).setCellValue(getStatusText(result.getStatus()));
            row.createCell(3).setCellValue(result.getScore());
            row.createCell(4).setCellValue(result.getThreshold());
            row.createCell(5).setCellValue(result.getEvidences().size());
            row.createCell(6).setCellValue(result.getRecommendation());

            // 应用样式
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建证据明细工作表
     */
    private void createEvidenceSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle,
                                   List<AuditResultDto> results) {
        Sheet sheet = workbook.createSheet("证据明细");

        int rowNum = 0;

        // 表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"规则ID", "规则名称", "证据文本", "开始位置", "结束位置", "匹配类型", "匹配分数"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据行
        for (AuditResultDto result : results) {
            for (AuditResultDto.Evidence evidence : result.getEvidences()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(result.getRuleId());
                row.createCell(1).setCellValue(result.getRuleName());
                row.createCell(2).setCellValue(truncateText(evidence.getText(), 200));
                row.createCell(3).setCellValue(evidence.getStartPos());
                row.createCell(4).setCellValue(evidence.getEndPos());
                row.createCell(5).setCellValue(evidence.getMatchType());
                row.createCell(6).setCellValue(0.0); // Evidence类没有score字段，使用默认值

                // 应用样式
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建统计图表工作表
     */
    private void createStatisticsSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle,
                                     List<AuditResultDto> results) {
        Sheet sheet = workbook.createSheet("统计分析");

        int rowNum = 0;

        // 状态分布统计
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("检核状态分布");
        titleCell.setCellStyle(headerStyle);

        rowNum++; // 空行

        Map<String, Long> statusCounts = results.stream()
                .collect(Collectors.groupingBy(AuditResultDto::getStatus, Collectors.counting()));

        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("状态");
        headerRow.createCell(1).setCellValue("数量");
        headerRow.createCell(2).setCellValue("百分比");

        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }

        long total = results.size();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(getStatusText(entry.getKey()));
            row.createCell(1).setCellValue(entry.getValue());
            row.createCell(2).setCellValue(String.format("%.2f%%",
                (double) entry.getValue() / total * 100));

            for (Cell cell : row) {
                cell.setCellStyle(dataStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 设置表格单元格
     */
    private void setTableCell(XWPFTable table, int row, int col, String text, boolean bold) {
        XWPFTableRow tableRow = table.getRow(row);
        XWPFTableCell cell = tableRow.getCell(col);
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontFamily("宋体");
        run.setFontSize(10);
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "PASSED": return "通过";
            case "FAILED": return "失败";
            case "WARNING": return "警告";
            case "ERROR": return "错误";
            default: return status;
        }
    }

    /**
     * 截断文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String jobId, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("audit_report_%s_%s.%s", jobId, timestamp, extension);
    }

    /**
     * 创建摘要
     */
    private Map<String, Object> createSummary(List<AuditResultDto> results) {
        Map<String, Long> statusCounts = results.stream()
                .collect(Collectors.groupingBy(AuditResultDto::getStatus, Collectors.counting()));

        long totalRules = results.size();
        long passedRules = statusCounts.getOrDefault("PASSED", 0L);
        double passRate = totalRules > 0 ? (double) passedRules / totalRules * 100 : 0.0;

        return Map.of(
            "totalRules", totalRules,
            "passedRules", passedRules,
            "failedRules", statusCounts.getOrDefault("FAILED", 0L),
            "warningRules", statusCounts.getOrDefault("WARNING", 0L),
            "passRate", passRate
        );
    }

    /**
     * 转换为JSON字符串
     */
    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("JSON序列化失败: error={}", e.getMessage());
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    /**
     * 导出结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExportResult {
        private String fileName;
        private String format;
        private byte[] content;
        private Integer size;
        private LocalDateTime exportTime;
        private String downloadUrl;
        private Map<String, Object> metadata;
    }
}