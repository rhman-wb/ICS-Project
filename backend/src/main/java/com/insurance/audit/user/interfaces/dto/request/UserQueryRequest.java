package com.insurance.audit.user.interfaces.dto.request;

import com.insurance.audit.common.dto.PageRequest;
import com.insurance.audit.user.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户查询请求
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询请求")
public class UserQueryRequest extends PageRequest {
    
    /**
     * 用户名关键字
     */
    @Size(max = 50, message = "用户名关键字长度不能超过50字符")
    @Schema(description = "用户名关键字", example = "admin")
    private String username;
    
    /**
     * 真实姓名关键字
     */
    @Size(max = 100, message = "真实姓名关键字长度不能超过100字符")
    @Schema(description = "真实姓名关键字", example = "张")
    private String realName;
    
    /**
     * 邮箱关键字
     */
    @Size(max = 100, message = "邮箱关键字长度不能超过100字符")
    @Schema(description = "邮箱关键字", example = "admin")
    private String email;
    
    /**
     * 手机号关键字
     */
    @Size(max = 20, message = "手机号关键字长度不能超过20字符")
    @Schema(description = "手机号关键字", example = "138")
    private String phone;
    
    /**
     * 用户状态
     */
    @Schema(description = "用户状态")
    private UserStatus status;
    
    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "role123")
    private String roleId;
    
    /**
     * 创建时间开始
     */
    @Schema(description = "创建时间开始", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAtStart;
    
    /**
     * 创建时间结束
     */
    @Schema(description = "创建时间结束", example = "2024-12-31T23:59:59")
    private LocalDateTime createdAtEnd;
    
    /**
     * 最后登录时间开始
     */
    @Schema(description = "最后登录时间开始", example = "2024-01-01T00:00:00")
    private LocalDateTime lastLoginTimeStart;
    
    /**
     * 最后登录时间结束
     */
    @Schema(description = "最后登录时间结束", example = "2024-12-31T23:59:59")
    private LocalDateTime lastLoginTimeEnd;
    
    /**
     * 是否被锁定
     */
    @Schema(description = "是否被锁定", example = "false")
    private Boolean locked;
    
    /**
     * 获取当前页码（兼容性方法）
     */
    public Integer getCurrent() {
        return getPage();
    }
    
    /**
     * 设置当前页码（兼容性方法）
     */
    public void setCurrent(Integer current) {
        setPage(current);
    }
}