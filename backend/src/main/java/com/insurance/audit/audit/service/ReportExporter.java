package com.insurance.audit.audit.service;

import com.insurance.audit.audit.dto.AuditJobResponse;
import com.insurance.audit.audit.dto.AuditResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告导出器
 * 支持导出Word/PDF/JSON格式的检核报告
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ReportExporter {

    @Value("${audit.report.max-size:50MB}")
    private String maxReportSize;

    @Value("${audit.report.template-path:#{null}}")
    private String templatePath;

    /**
     * 导出检核报告
     *
     * @param format 导出格式 (WORD, PDF, JSON, EXCEL)
     * @param job 作业信息
     * @param results 检核结果列表
     * @return 导出结果信息
     */
    public ExportResult exportReport(String format, AuditJobResponse job, List<AuditResultDto> results) {
        log.info("开始导出报告: jobId={}, format={}, resultCount={}", job.getJobId(), format, results.size());

        try {
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
        } catch (Exception e) {
            log.error("导出报告失败: jobId={}, format={}, error={}", job.getJobId(), format, e.getMessage(), e);
            throw new RuntimeException("报告导出失败: " + e.getMessage(), e);
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
     * 导出PDF报告
     */
    private ExportResult exportPdfReport(AuditJobResponse job, List<AuditResultDto> results) {
        // TODO: 实现PDF导出
        throw new UnsupportedOperationException("PDF导出功能暂未实现");
    }

    /**
     * 导出Excel报告
     */
    private ExportResult exportExcelReport(AuditJobResponse job, List<AuditResultDto> results) {
        // TODO: 实现Excel导出
        throw new UnsupportedOperationException("Excel导出功能暂未实现");
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
        // 简化的JSON转换，实际项目中应使用Jackson或Gson
        return object.toString(); // TODO: 实现proper JSON serialization
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