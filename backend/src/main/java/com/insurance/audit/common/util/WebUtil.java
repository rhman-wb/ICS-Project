package com.insurance.audit.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Web工具类
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
public class WebUtil {
    
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    
    /**
     * 获取当前请求
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    /**
     * 获取客户端IP地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? getClientIp(request) : UNKNOWN;
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String ip = null;
        
        // X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ipAddresses) && !UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            ip = ipAddresses.split(",")[0];
        }
        
        // Proxy-Client-IP：apache 服务代理
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        
        // WL-Proxy-Client-IP：weblogic 服务代理
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        // HTTP_CLIENT_IP：有些代理服务器
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        
        // HTTP_X_FORWARDED_FOR：有些代理服务器
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        // X-Real-IP：nginx服务代理
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        // 如果都没有则使用 getRemoteAddr() 获取
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理本地地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }
        
        return StringUtils.hasText(ip) ? ip.trim() : UNKNOWN;
    }
    
    /**
     * 获取用户代理
     */
    public static String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? getUserAgent(request) : UNKNOWN;
    }
    
    /**
     * 获取用户代理
     * 
     * @param request HTTP请求
     * @return 用户代理字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : UNKNOWN;
    }
    
    /**
     * 获取请求URL
     */
    public static String getRequestUrl() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? getRequestUrl(request) : UNKNOWN;
    }
    
    /**
     * 获取请求URL
     * 
     * @param request HTTP请求
     * @return 请求URL
     */
    public static String getRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        return request.getRequestURI();
    }
    
    /**
     * 获取请求方法
     */
    public static String getRequestMethod() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getMethod() : UNKNOWN;
    }
    
    /**
     * 获取请求方法
     * 
     * @param request HTTP请求
     * @return 请求方法
     */
    public static String getRequestMethod(HttpServletRequest request) {
        return request != null ? request.getMethod() : UNKNOWN;
    }
}