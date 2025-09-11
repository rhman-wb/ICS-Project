package com.insurance.audit.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.insurance.audit.common.base.BaseEntity;
import com.insurance.audit.common.enums.OperationStatus;
import com.insurance.audit.common.enums.OperationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 操作日志实体类
 * 记录用户操作行为，包括登录、业务操作等
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("operation_logs")
@Schema(description = "操作日志实体")
public class OperationLog extends BaseEntity {
    
    /**
     * 操作用户ID
     */
    @TableField("user_id")
    @Schema(description = "操作用户ID", example = "123e4567e89b12d3a456426614174000")
    private String userId;
    
    /**
     * 操作用户名
     */
    @TableField("username")
    @Size(max = 50, message = "用户名长度不能超过50字符")
    @Schema(description = "操作用户名", example = "admin")
    private String username;
    
    /**
     * 操作类型
     */
    @TableField("operation_type")
    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型", required = true)
    private OperationType operationType;
    
    /**
     * 操作名称（与表列 operation_name 对齐，非空）
     */
    @TableField("operation_name")
    @NotBlank(message = "操作名称不能为空")
    @Size(max = 100, message = "操作名称长度不能超过100字符")
    @Schema(description = "操作名称", example = "用户登录")
    private String operationName;
    
    /**
     * 操作描述
     */
    @TableField("operation_desc")
    @NotBlank(message = "操作描述不能为空")
    @Size(max = 500, message = "操作描述长度不能超过500字符")
    @Schema(description = "操作描述", required = true, example = "用户登录系统")
    private String operationDesc;
    
    /**
     * 操作状态
     */
    @TableField("operation_status")
    @NotNull(message = "操作状态不能为空")
    @Builder.Default
    @Schema(description = "操作状态", required = true)
    private OperationStatus operationStatus = OperationStatus.SUCCESS;
    
    /**
     * 请求方法
     */
    @TableField("request_method")
    @Size(max = 10, message = "请求方法长度不能超过10字符")
    @Schema(description = "请求方法", example = "POST")
    private String requestMethod;
    
    /**
     * 请求方法（兼容旧表列名 method，非空）
     */
    @TableField("method")
    @Size(max = 10, message = "请求方法长度不能超过10字符")
    @Schema(description = "请求方法(兼容老字段)", example = "POST")
    private String method;
    
    /**
     * 请求URL
     */
    @TableField("request_url")
    @Size(max = 500, message = "请求URL长度不能超过500字符")
    @Schema(description = "请求URL", example = "/api/v1/auth/login")
    private String requestUrl;
    
    /**
     * 请求参数
     */
    @TableField("request_params")
    @Schema(description = "请求参数")
    private String requestParams;
    
    /**
     * 响应结果
     */
    @TableField("response_result")
    @Schema(description = "响应结果")
    private String responseResult;
    
    /**
     * 客户端IP地址
     */
    @TableField("client_ip")
    @Size(max = 45, message = "IP地址长度不能超过45字符")
    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;
    
    /**
     * 兼容旧表列名 ip_address
     */
    @TableField("ip_address")
    @Size(max = 50, message = "IP地址长度不能超过50字符")
    @Schema(description = "客户端IP地址(兼容老字段)", example = "192.168.1.100")
    private String ipAddress;
    
    /**
     * 用户代理
     */
    @TableField("user_agent")
    @Size(max = 1000, message = "用户代理长度不能超过1000字符")
    @Schema(description = "用户代理")
    private String userAgent;
    
    /**
     * 操作耗时（毫秒）
     */
    @TableField("execution_time")
    @Schema(description = "操作耗时（毫秒）", example = "150")
    private Long executionTime;
    
    /**
     * 错误信息
     */
    @TableField("error_message")
    @Size(max = 2000, message = "错误信息长度不能超过2000字符")
    @Schema(description = "错误信息")
    private String errorMessage;
    
    /**
     * 操作模块
     */
    @TableField("module")
    @Size(max = 50, message = "操作模块长度不能超过50字符")
    @Schema(description = "操作模块", example = "用户管理")
    private String module;
    
    /**
     * 业务ID（关联的业务对象ID）
     */
    @TableField("business_id")
    @Schema(description = "业务ID", example = "123e4567e89b12d3a456426614174000")
    private String businessId;
    
    /**
     * 业务类型
     */
    @TableField("business_type")
    @Size(max = 50, message = "业务类型长度不能超过50字符")
    @Schema(description = "业务类型", example = "USER")
    private String businessType;
}