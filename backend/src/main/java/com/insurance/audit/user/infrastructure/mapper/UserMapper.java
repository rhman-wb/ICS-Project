package com.insurance.audit.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.domain.enums.UserStatus;
import com.insurance.audit.user.interfaces.dto.request.UserQueryRequest;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    default User findByUsername(String username) {
        return selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, false));
    }
    
    /**
     * 根据用户名查询用户（返回Optional）
     */
    default Optional<User> findOptionalByUsername(String username) {
        User user = findByUsername(username);
        return Optional.ofNullable(user);
    }
    
    /**
     * 根据邮箱查询用户
     */
    default Optional<User> findByEmail(String email) {
        User user = selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, email)
                .eq(User::getDeleted, false));
        return Optional.ofNullable(user);
    }
    
    /**
     * 检查用户名是否存在
     */
    default boolean existsByUsername(String username) {
        return selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, false)) > 0;
    }
    
    /**
     * 检查邮箱是否存在
     */
    default boolean existsByEmail(String email) {
        return selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, email)
                .eq(User::getDeleted, false)) > 0;
    }
    
    /**
     * 检查手机号是否存在
     */
    default boolean existsByPhone(String phone) {
        return selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getPhone, phone)
                .eq(User::getDeleted, false)) > 0;
    }
    
    /**
     * 检查邮箱是否存在（排除指定用户）
     */
    default boolean existsByEmailExcludeUser(String email, String excludeUserId) {
        return selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, email)
                .ne(User::getId, excludeUserId)
                .eq(User::getDeleted, false)) > 0;
    }
    
    /**
     * 检查邮箱是否存在（排除指定用户） - 兼容方法
     */
    default boolean existsByEmailExcludeId(String email, String excludeUserId) {
        return existsByEmailExcludeUser(email, excludeUserId);
    }
    
    /**
     * 检查手机号是否存在（排除指定用户）
     */
    default boolean existsByPhoneExcludeUser(String phone, String excludeUserId) {
        return selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getPhone, phone)
                .ne(User::getId, excludeUserId)
                .eq(User::getDeleted, false)) > 0;
    }
    
    /**
     * 检查手机号是否存在（排除指定用户） - 兼容方法
     */
    default boolean existsByPhoneExcludeId(String phone, String excludeUserId) {
        return existsByPhoneExcludeUser(phone, excludeUserId);
    }
    
    /**
     * 根据用户名查询用户 - 兼容方法
     */
    default User selectByUsername(String username) {
        return findByUsername(username);
    }
    
    /**
     * 批量为用户分配角色 - 兼容方法
     */
    default int insertUserRoles(String userId, List<String> roleIds) {
        return batchInsertUserRoles(userId, roleIds);
    }
    
    /**
     * 清除用户所有角色 - 兼容方法
     */
    default int deleteUserRoles(String userId) {
        return deleteAllUserRoles(userId);
    }
    
    /**
     * 查询用户角色名称列表（修正列名：roles.name）
     */
    @Select("""
        SELECT r.name
        FROM user_roles ur
        INNER JOIN roles r ON ur.role_id = r.id
        WHERE ur.user_id = #{userId} AND r.is_deleted = 0
        """)
    List<String> selectRoleNamesByUserId(@Param("userId") String userId);
    
    /**
     * 查询用户权限列表（保持不变）
     */
    @Select("""
        SELECT DISTINCT p.permission_name
        FROM user_roles ur
        INNER JOIN role_permissions rp ON ur.role_id = rp.role_id
        INNER JOIN permissions p ON rp.permission_id = p.id
        WHERE ur.user_id = #{userId} AND p.is_deleted = 0
        """)
    List<String> selectPermissionsByUserId(@Param("userId") String userId);
    
    /**
     * 查询用户及其角色信息（修正列名：roles.name/roles.code）
     */
    @Select("""
        SELECT u.*, r.id as role_id, r.name AS role_name, r.code AS role_type, r.description as role_description
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        WHERE u.username = #{username} AND u.is_deleted = 0
        """)
    User findUserWithRolesByUsername(@Param("username") String username);
    
    /**
     * 根据用户ID查询用户及其角色信息（修正列名：roles.name/roles.code）
     */
    @Select("""
        SELECT u.*, r.id as role_id, r.name AS role_name, r.code AS role_type, r.description as role_description
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        WHERE u.id = #{userId} AND u.is_deleted = 0
        """)
    User findUserWithRolesById(@Param("userId") String userId);
    
    /**
     * 更新用户最后登录时间
     */
    default int updateLastLoginTime(String userId, java.time.LocalDateTime lastLoginTime) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(lastLoginTime);
        return updateById(user);
    }
    
    /**
     * 分页查询用户列表（复杂查询）
     */
    Page<User> selectUserPage(Page<User> page, @Param("request") UserQueryRequest request);
    
    /**
     * 批量删除用户（软删除）
     */
    default int batchDeleteUsers(List<String> userIds) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .set(User::getDeleted, true)
                .in(User::getId, userIds));
    }
    
    // ==================== 用户角色关联操作 ====================
    
    /**
     * 为用户分配角色
     */
    @Insert("""
        INSERT INTO user_roles (user_id, role_id, created_at)
        VALUES (#{userId}, #{roleId}, NOW())
        """)
    int insertUserRole(@Param("userId") String userId, @Param("roleId") String roleId);
    
    /**
     * 批量为用户分配角色
     */
    default int batchInsertUserRoles(String userId, List<String> roleIds) {
        int count = 0;
        for (String roleId : roleIds) {
            count += insertUserRole(userId, roleId);
        }
        return count;
    }
    
    /**
     * 移除用户角色
     */
    @Delete("""
        DELETE FROM user_roles 
        WHERE user_id = #{userId} AND role_id = #{roleId}
        """)
    int deleteUserRole(@Param("userId") String userId, @Param("roleId") String roleId);
    
    /**
     * 批量移除用户角色
     */
    default int batchDeleteUserRoles(String userId, List<String> roleIds) {
        int count = 0;
        for (String roleId : roleIds) {
            count += deleteUserRole(userId, roleId);
        }
        return count;
    }
    
    /**
     * 清除用户所有角色
     */
    @Delete("""
        DELETE FROM user_roles WHERE user_id = #{userId}
        """)
    int deleteAllUserRoles(@Param("userId") String userId);
    
    /**
     * 检查用户是否具有指定角色（修正列名：roles.name）
     */
    @Select("""
        SELECT COUNT(*) > 0
        FROM user_roles ur
        INNER JOIN roles r ON ur.role_id = r.id
        WHERE ur.user_id = #{userId} AND r.name = #{roleName} AND r.is_deleted = 0
        """)
    boolean hasRole(@Param("userId") String userId, @Param("roleName") String roleName);
    
    /**
     * 检查用户是否具有指定权限
     */
    @Select("""
        SELECT COUNT(*) > 0
        FROM user_roles ur
        INNER JOIN role_permissions rp ON ur.role_id = rp.role_id
        INNER JOIN permissions p ON rp.permission_id = p.id
        WHERE ur.user_id = #{userId} AND p.permission_name = #{permissionName} AND p.is_deleted = 0
        """)
    boolean hasPermission(@Param("userId") String userId, @Param("permissionName") String permissionName);
    
    // ==================== 新增缺失方法 ====================
    
    /**
     * 锁定用户账户，设置锁定截止时间
     */
    default int lockAccount(String userId, java.time.LocalDateTime lockedUntil) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .set(User::getLockedUntil, lockedUntil)
                .eq(User::getId, userId));
    }
    
    /**
     * 更新用户密码（需要BCrypt加密）
     */
    default int updatePassword(String userId, String encryptedPassword) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .set(User::getPassword, encryptedPassword)
                .eq(User::getId, userId));
    }
    
    /**
     * 更新用户状态
     */
    default int updateStatus(String userId, UserStatus status) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .set(User::getStatus, status)
                .eq(User::getId, userId));
    }
    
    /**
     * 解锁用户，清空锁定时间并重置登录失败次数
     */
    default int unlockUser(String userId) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .set(User::getLockedUntil, null)
                .set(User::getLoginFailureCount, 0)
                .eq(User::getId, userId));
    }
    
    /**
     * 更新用户最后登录信息
     */
    default int updateLastLogin(String userId, java.time.LocalDateTime lastLoginTime, String lastLoginIp) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .set(User::getLastLoginTime, lastLoginTime)
                .set(User::getLastLoginIp, lastLoginIp)
                .eq(User::getId, userId));
    }
    
    /**
     * 重置登录失败次数为0
     */
    default int resetLoginAttempts(String userId) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .set(User::getLoginFailureCount, 0)
                .eq(User::getId, userId));
    }
    
    /**
     * 递增登录失败次数
     */
    default int incrementLoginAttempts(String userId) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .setSql("login_failure_count = login_failure_count + 1")
                .eq(User::getId, userId));
    }
    
    /**
     * 根据用户名软删除用户
     */
    default int deleteByUsername(String username) {
        return update(null, Wrappers.<User>lambdaUpdate()
                .set(User::getDeleted, true)
                .eq(User::getUsername, username));
    }
    
    /**
     * 查询所有未删除用户
     */
    default List<User> selectAll() {
        return selectList(Wrappers.<User>lambdaQuery()
                .eq(User::getDeleted, false)
                .orderByDesc(User::getCreatedAt));
    }
}