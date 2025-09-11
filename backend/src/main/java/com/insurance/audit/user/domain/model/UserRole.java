package com.insurance.audit.user.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_roles")
@Schema(description = "用户角色关联实体")
public class UserRole implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 关联ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "关联ID")
    private String id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    @NotBlank(message = "用户ID不能为空")
    @Size(max = 32, message = "用户ID长度不能超过32字符")
    @Schema(description = "用户ID", required = true)
    private String userId;
    
    /**
     * 角色ID
     */
    @TableField("role_id")
    @NotBlank(message = "角色ID不能为空")
    @Size(max = 32, message = "角色ID长度不能超过32字符")
    @Schema(description = "角色ID", required = true)
    private String roleId;
    
    /**
     * 创建人ID
     */
    @TableField("created_by")
    @Size(max = 32, message = "创建人ID长度不能超过32字符")
    @Schema(description = "创建人ID")
    private String createdBy;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}