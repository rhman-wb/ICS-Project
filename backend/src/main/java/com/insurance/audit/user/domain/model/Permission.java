package com.insurance.audit.user.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.insurance.audit.common.base.BaseEntity;
import com.insurance.audit.user.domain.enums.PermissionStatus;
import com.insurance.audit.user.domain.enums.PermissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 权限实体
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
@TableName("permissions")
@Schema(description = "权限实体")
public class Permission extends BaseEntity {
    
    /**
     * 权限名称
     */
    @TableField("name")
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100字符")
    @Schema(description = "权限名称", required = true, example = "用户管理")
    private String name;
    
    /**
     * 权限编码
     */
    @TableField("code")
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100字符")
    @Schema(description = "权限编码", required = true, example = "user:view")
    private String code;
    
    /**
     * 权限类型
     */
    @TableField("type")
    @NotNull(message = "权限类型不能为空")
    @Schema(description = "权限类型", required = true)
    @lombok.Builder.Default
    private PermissionType type = PermissionType.MENU;
    
    /**
     * 父权限ID
     */
    @TableField("parent_id")
    @Size(max = 32, message = "父权限ID长度不能超过32字符")
    @Schema(description = "父权限ID")
    private String parentId;
    
    /**
     * 路径
     */
    @TableField("path")
    @Size(max = 200, message = "路径长度不能超过200字符")
    @Schema(description = "路径", example = "/user/list")
    private String path;
    
    /**
     * 组件
     */
    @TableField("component")
    @Size(max = 200, message = "组件长度不能超过200字符")
    @Schema(description = "组件", example = "UserList")
    private String component;
    
    /**
     * 图标
     */
    @TableField("icon")
    @Size(max = 100, message = "图标长度不能超过100字符")
    @Schema(description = "图标", example = "user")
    private String icon;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    @NotNull(message = "排序不能为空")
    @Schema(description = "排序", required = true, example = "1")
    @lombok.Builder.Default
    private Integer sortOrder = 0;
    
    /**
     * 权限状态
     */
    @TableField("status")
    @NotNull(message = "权限状态不能为空")
    @Schema(description = "权限状态", required = true)
    @lombok.Builder.Default
    private PermissionStatus status = PermissionStatus.ACTIVE;
    
    /**
     * 获取资源路径（兼容性方法）
     */
    public String getResourcePath() {
        return this.path;
    }
    
    /**
     * 设置资源路径（兼容性方法）
     */
    public void setResourcePath(String resourcePath) {
        this.path = resourcePath;
    }
}
