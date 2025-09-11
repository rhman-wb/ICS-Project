package com.insurance.audit.user.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.insurance.audit.common.base.BaseEntity;
import com.insurance.audit.user.domain.enums.RoleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 角色实体
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("roles")
@Schema(description = "角色实体")
public class Role extends BaseEntity {
    
    /**
     * 角色名称
     */
    @TableField("name")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50字符")
    @Schema(description = "角色名称", required = true, example = "管理员")
    private String name;
    
    /**
     * 角色编码
     */
    @TableField("code")
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50字符")
    @Schema(description = "角色编码", required = true, example = "ADMIN")
    private String code;
    
    /**
     * 角色描述
     */
    @TableField("description")
    @Size(max = 200, message = "角色描述长度不能超过200字符")
    @Schema(description = "角色描述", example = "系统管理员")
    private String description;
    
    /**
     * 角色状态
     */
    @TableField("status")
    @NotNull(message = "角色状态不能为空")
    @Schema(description = "角色状态", required = true)
    @lombok.Builder.Default
    private RoleStatus status = RoleStatus.ACTIVE;
    
    /**
     * 权限列表（不存储在数据库中）
     */
    @TableField(exist = false)
    @Schema(description = "权限列表")
    private List<Permission> permissions;
}