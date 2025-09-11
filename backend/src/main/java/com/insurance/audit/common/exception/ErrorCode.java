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
    PERMISSION_NOT_FOUND(1203, "权限不存在");
    
    private final Integer code;
    private final String message;
}