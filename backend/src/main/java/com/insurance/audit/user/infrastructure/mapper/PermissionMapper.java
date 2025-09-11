package com.insurance.audit.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.insurance.audit.user.domain.model.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 权限数据访问层
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface PermissionMapper extends BaseMapper<Permission> {
    
    /**
     * 根据权限名称查询权限
     */
    default Permission findByName(String name) {
        return selectOne(Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getName, name)
                .eq(Permission::getDeleted, false));
    }
    
    /**
     * 检查权限名称是否存在
     */
    default boolean existsByName(String name) {
        return selectCount(Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getName, name)
                .eq(Permission::getDeleted, false)) > 0;
    }
    
    /**
     * 根据角色ID查询权限列表
     */
    @Select("""
        SELECT p.*
        FROM permissions p
        INNER JOIN role_permissions rp ON p.id = rp.permission_id
        WHERE rp.role_id = #{roleId} AND p.is_deleted = 0
        ORDER BY p.permission_name
        """)
    List<Permission> findByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据用户ID查询权限列表
     */
    @Select("""
        SELECT DISTINCT p.*
        FROM permissions p
        INNER JOIN role_permissions rp ON p.id = rp.permission_id
        INNER JOIN user_roles ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND p.is_deleted = 0
        ORDER BY p.permission_name
        """)
    List<Permission> findByUserId(@Param("userId") String userId);
    
    /**
     * 查询所有可用权限
     */
    default List<Permission> findAllAvailable() {
        return selectList(Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getDeleted, false)
                .orderByAsc(Permission::getName));
    }
    
    /**
     * 根据资源路径查询权限
     */
    default List<Permission> findByResourcePath(String resourcePath) {
        return selectList(Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getResourcePath, resourcePath)
                .eq(Permission::getDeleted, false));
    }
    
    /**
     * 根据权限名称列表查询权限
     */
    default List<Permission> findByNames(List<String> names) {
        return selectList(Wrappers.<Permission>lambdaQuery()
                .in(Permission::getName, names)
                .eq(Permission::getDeleted, false)
                .orderByAsc(Permission::getName));
    }
    
    /**
     * 统计权限被多少个角色使用
     */
    @Select("""
        SELECT COUNT(DISTINCT rp.role_id)
        FROM role_permissions rp
        INNER JOIN roles r ON rp.role_id = r.id
        WHERE rp.permission_id = #{permissionId} AND r.is_deleted = 0
        """)
    long countRolesByPermissionId(@Param("permissionId") String permissionId);
}