package com.insurance.audit.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    // 通用错误码
    SUCCESS(200, "操作成功"),
    INVALID_PARAMETER(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    RESOURCE_NOT_FOUND(404, "资源不存在"),
    OPERATION_FAILED(500, "操作失败"),
    INTERNAL_ERROR(500, "系统内部错误"),
    
    // 用户认证相关错误码 (1000-1999)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    INVALID_CREDENTIALS(1003, "用户名或密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    USER_LOCKED(1005, "用户账户已被锁定"),
    PASSWORD_EXPIRED(1006, "密码已过期"),
    
    // JWT相关错误码 (1100-1199)
    TOKEN_INVALID(1101, "令牌无效"),
    TOKEN_EXPIRED(1102, "令牌已过期"),
    TOKEN_MALFORMED(1103, "令牌格式错误"),
    TOKEN_SIGNATURE_INVALID(1104, "令牌签名无效"),
    TOKEN_BLACKLISTED(1105, "令牌已被列入黑名单"),
    
    // 权限相关错误码 (1200-1299)
    PERMISSION_DENIED(1201, "权限不足"),
    ROLE_NOT_FOUND(1202, "角色不存在"),
    PERMISSION_NOT_FOUND(1203, "权限不存在"),
    
    // 产品管理相关错误码 (2000-2999)
    PRODUCT_NOT_FOUND(2001, "产品不存在"),
    PRODUCT_ALREADY_EXISTS(2002, "产品已存在"),
    PRODUCT_CREATE_FAILED(2003, "创建产品失败"),
    PRODUCT_UPDATE_FAILED(2004, "更新产品失败"),
    PRODUCT_DELETE_FAILED(2005, "删除产品失败"),
    PRODUCT_QUERY_FAILED(2006, "查询产品失败"),
    
    // 文档管理相关错误码 (2100-2199)
    DOCUMENT_NOT_FOUND(2101, "文档不存在"),
    DOCUMENT_UPLOAD_FAILED(2102, "文档上传失败"),
    DOCUMENT_PARSE_FAILED(2103, "文档解析失败"),
    DOCUMENT_VALIDATION_FAILED(2104, "文档校验失败"),
    FILE_NOT_FOUND(2105, "文件不存在"),

    // 模板管理相关错误码 (2200-2299)
    TEMPLATE_NOT_FOUND(2201, "模板不存在"),
    TEMPLATE_DISABLED(2202, "模板已禁用"),
    TEMPLATE_CREATE_FAILED(2203, "创建模板失败"),
    TEMPLATE_UPDATE_FAILED(2204, "更新模板失败"),
    TEMPLATE_DELETE_FAILED(2205, "删除模板失败");

    private final Integer code;
    private final String message;
}