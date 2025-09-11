package com.insurance.audit.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.insurance.audit.user.domain.model.Role;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色数据访问层
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据角色名称查询角色
     */
    default Role findByName(String name) {
        return selectOne(Wrappers.<Role>lambdaQuery()
                .eq(Role::getName, name)
                .eq(Role::getDeleted, false));
    }
    
    /**
     * 检查角色名称是否存在
     */
    default boolean existsByName(String name) {
        return selectCount(Wrappers.<Role>lambdaQuery()
                .eq(Role::getName, name)
                .eq(Role::getDeleted, false)) > 0;
    }
    
    /**
     * 根据用户ID查询角色列表
     */
    @Select("""
        SELECT r.*
        FROM roles r
        INNER JOIN user_roles ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId} AND r.is_deleted = 0
        ORDER BY r.created_at DESC
        """)
    List<Role> findByUserId(@Param("userId") String userId);
    
    /**
     * 查询角色及其权限信息
     */
    @Select("""
        SELECT r.*, p.id as permission_id, p.permission_name, p.description as permission_description, p.resource_path
        FROM roles r
        LEFT JOIN role_permissions rp ON r.id = rp.role_id
        LEFT JOIN permissions p ON rp.permission_id = p.id
        WHERE r.id = #{roleId} AND r.is_deleted = 0
        """)
    Role findRoleWithPermissionsById(@Param("roleId") String roleId);
    
    /**
     * 查询所有可用角色
     */
    default List<Role> findAllAvailable() {
        return selectList(Wrappers.<Role>lambdaQuery()
                .eq(Role::getDeleted, false)
                .orderByAsc(Role::getName));
    }
    
    // ==================== 角色权限关联操作 ====================
    
    /**
     * 为角色分配权限
     */
    @Insert("""
        INSERT INTO role_permissions (role_id, permission_id, created_at)
        VALUES (#{roleId}, #{permissionId}, NOW())
        """)
    int insertRolePermission(@Param("roleId") String roleId, @Param("permissionId") String permissionId);
    
    /**
     * 批量为角色分配权限
     */
    default int batchInsertRolePermissions(String roleId, List<String> permissionIds) {
        int count = 0;
        for (String permissionId : permissionIds) {
            count += insertRolePermission(roleId, permissionId);
        }
        return count;
    }
    
    /**
     * 移除角色权限
     */
    @Delete("""
        DELETE FROM role_permissions 
        WHERE role_id = #{roleId} AND permission_id = #{permissionId}
        """)
    int deleteRolePermission(@Param("roleId") String roleId, @Param("permissionId") String permissionId);
    
    /**
     * 批量移除角色权限
     */
    default int batchDeleteRolePermissions(String roleId, List<String> permissionIds) {
        int count = 0;
        for (String permissionId : permissionIds) {
            count += deleteRolePermission(roleId, permissionId);
        }
        return count;
    }
    
    /**
     * 清除角色所有权限
     */
    @Delete("""
        DELETE FROM role_permissions WHERE role_id = #{roleId}
        """)
    int deleteAllRolePermissions(@Param("roleId") String roleId);
    
    /**
     * 统计角色下的用户数量
     */
    @Select("""
        SELECT COUNT(DISTINCT ur.user_id)
        FROM user_roles ur
        INNER JOIN users u ON ur.user_id = u.id
        WHERE ur.role_id = #{roleId} AND u.is_deleted = 0
        """)
    long countUsersByRoleId(@Param("roleId") String roleId);
}