package com.insurance.audit.common.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 头工具类
 *
 * 统一生成 Content-Disposition，兼容中文文件名：
 * - filename 提供 ASCII 回退名，避免 Tomcat 校验移除响应头
 * - filename* 提供 UTF-8 百分号编码名（RFC 5987/6266），浏览器优先使用
 */
public final class HttpHeaderUtils {

    private HttpHeaderUtils() {}

    /**
     * 生成带回退和 UTF-8 编码的 Content-Disposition（attachment）。
     * @param originalFileName 原始文件名（可含中文/空格）
     * @return 仅包含 ASCII 的 header 值，例如：
     *         attachment; filename="fallback.xlsx"; filename*=UTF-8''%E9%99%84%E4%BB%B65.xlsx
     */
    public static String contentDispositionAttachment(String originalFileName) {
        String safe = sanitizeAsciiFilename(extractFileName(originalFileName));
        String encoded = rfc5987Encode(extractFileName(originalFileName));
        return "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;
    }

    // 仅抽取文件名部分，去掉路径分隔符
    private static String extractFileName(String name) {
        if (name == null || name.isEmpty()) {
            return "download";
        }
        String n = name.replace('\\', '/');
        int idx = n.lastIndexOf('/');
        return idx >= 0 ? n.substring(idx + 1) : n;
    }

    // 生成 ASCII 回退名：保留扩展名，非 ASCII 或不安全字符替换为下划线
    private static String sanitizeAsciiFilename(String fileName) {
        String base = fileName;
        int dot = base.lastIndexOf('.');
        String name = dot >= 0 ? base.substring(0, dot) : base;
        String ext = dot >= 0 ? base.substring(dot) : "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            // 仅允许可见 ASCII 中的一部分安全字符，其余替换为下划线
            if (c >= 0x20 && c <= 0x7E && c != '"' && c != '\\' && c != ';' && c != '/') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        if (sb.length() == 0) {
            sb.append('f').append('i').append('l').append('e');
        }
        // 扩展名若包含非 ASCII，同样替换
        StringBuilder safeExt = new StringBuilder();
        for (int i = 0; i < ext.length(); i++) {
            char c = ext.charAt(i);
            if (c <= 0x7F && c != '"' && c != '\\' && c != ';' && c != '/') {
                safeExt.append(c);
            } else {
                safeExt.append('_');
            }
        }
        return sb.toString() + safeExt.toString();
    }

    // RFC 5987 百分号编码（UTF-8），并将 + 替换为空格的 %20
    private static String rfc5987Encode(String value) {
        try {
            String enc = URLEncoder.encode(value, StandardCharsets.UTF_8.name());
            // URLEncoder 将空格编码为 +，需替换为 %20；保持其他编码不变
            return enc.replace("+", "%20");
        } catch (Exception e) {
            // 理论不会发生，退回原值以避免抛错
            return value;
        }
    }
}