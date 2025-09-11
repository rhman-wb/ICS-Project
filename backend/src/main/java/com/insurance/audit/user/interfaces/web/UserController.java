package com.insurance.audit.user.interfaces.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.user.application.service.UserService;
import com.insurance.audit.user.domain.model.Permission;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.interfaces.dto.request.ChangePasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.CreateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.ResetPasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.UpdateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.UserQueryRequest;
import com.insurance.audit.user.interfaces.dto.request.UserProfileRequest;
import com.insurance.audit.user.interfaces.dto.response.UserResponse;
import com.insurance.audit.user.interfaces.dto.response.UserProfileResponse;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 用户管理控制器
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户管理相关API")
public class UserController {
    
    private final UserService userService;
    
    // ==================== 用户CRUD操作 ====================
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建用户", description = "创建新用户")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    })
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user: {}", request.getUsername());
        UserResponse response = userService.createUser(request);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户详细信息")
    public ApiResponse<UserResponse> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId) {
        UserResponse response = userService.getUserById(userId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "根据用户名查询用户", description = "根据用户名查询用户详细信息")
    public ApiResponse<UserResponse> getUserByUsername(
            @Parameter(description = "用户名", required = true)
            @PathVariable @NotBlank String username) {
        UserResponse response = userService.getUserByUsername(username);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "更新用户信息", description = "更新用户基本信息")
    public ApiResponse<UserResponse> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user: {}", userId);
        UserResponse response = userService.updateUser(userId, request);
        return ApiResponse.success(response);
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户", description = "软删除用户")
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId) {
        log.info("Deleting user: {}", userId);
        userService.deleteUser(userId);
        return ApiResponse.success(null);
    }
    
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除用户", description = "批量软删除用户")
    public ApiResponse<Void> batchDeleteUsers(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody @NotEmpty List<String> userIds) {
        log.info("Batch deleting users: {}", userIds);
        userService.batchDeleteUsers(userIds);
        return ApiResponse.success(null);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户列表")
    public ApiResponse<Page<UserResponse>> getUserList(@Valid UserQueryRequest request) {
        Page<UserResponse> response = userService.getUserList(request);
        return ApiResponse.success(response);
    }
    
    // ==================== 密码管理功能 ====================
    
    @PutMapping("/{userId}/password")
    @PreAuthorize("#userId == authentication.principal.userId")
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    public ApiResponse<Void> changePassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);
        userService.changePassword(userId, request);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/password/reset")
    @Operation(summary = "重置密码", description = "通过验证码重置密码")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Resetting password for: {}", request.getUsernameOrEmail());
        userService.resetPassword(request);
        return ApiResponse.success(null);
    }
    
    @PutMapping("/{userId}/password/admin-reset")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "管理员重置密码", description = "管理员重置用户密码")
    public ApiResponse<Void> adminResetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Parameter(description = "新密码", required = true)
            @RequestParam @NotBlank String newPassword) {
        log.info("Admin resetting password for user: {}", userId);
        userService.adminResetPassword(userId, newPassword);
        return ApiResponse.success(null);
    }
    
    // ==================== 角色分配功能 ====================
    
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "为用户分配角色", description = "为指定用户分配角色")
    public ApiResponse<Void> assignRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @NotEmpty List<String> roleIds) {
        log.info("Assigning roles to user: {}, roles: {}", userId, roleIds);
        userService.assignRoles(userId, roleIds);
        return ApiResponse.success(null);
    }
    
    @DeleteMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "移除用户角色", description = "移除用户的指定角色")
    public ApiResponse<Void> removeRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @NotEmpty List<String> roleIds) {
        log.info("Removing roles from user: {}, roles: {}", userId, roleIds);
        userService.removeRoles(userId, roleIds);
        return ApiResponse.success(null);
    }
    
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "获取用户角色", description = "获取用户的角色列表")
    public ApiResponse<List<Role>> getUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId) {
        List<Role> roles = userService.getUserRoles(userId);
        return ApiResponse.success(roles);
    }
    
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "设置用户角色", description = "设置用户的角色（覆盖原有角色）")
    public ApiResponse<Void> setUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Parameter(description = "角色ID列表")
            @RequestBody List<String> roleIds) {
        log.info("Setting roles for user: {}, roles: {}", userId, roleIds);
        userService.setUserRoles(userId, roleIds);
        return ApiResponse.success(null);
    }
    
    // ==================== 权限验证功能 ====================
    
    @GetMapping("/{userId}/permissions")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "获取用户权限", description = "获取用户的权限列表")
    public ApiResponse<List<Permission>> getUserPermissions(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId) {
        List<Permission> permissions = userService.getUserPermissions(userId);
        return ApiResponse.success(permissions);
    }
    
    @GetMapping("/{userId}/permissions/{permissionName}/check")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "检查用户权限", description = "检查用户是否具有指定权限")
    public ApiResponse<Boolean> hasPermission(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Parameter(description = "权限名称", required = true)
            @PathVariable @NotBlank String permissionName) {
        boolean hasPermission = userService.hasPermission(userId, permissionName);
        return ApiResponse.success(hasPermission);
    }
    
    @GetMapping("/{userId}/roles/{roleName}/check")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "检查用户角色", description = "检查用户是否具有指定角色")
    public ApiResponse<Boolean> hasRole(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Parameter(description = "角色名称", required = true)
            @PathVariable @NotBlank String roleName) {
        boolean hasRole = userService.hasRole(userId, roleName);
        return ApiResponse.success(hasRole);
    }
    
    // ==================== 用户状态管理 ====================
    
    @PutMapping("/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "启用用户", description = "启用指定用户")
    public ApiResponse<Void> enableUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId) {
        log.info("Enabling user: {}", userId);
        userService.enableUser(userId);
        return ApiResponse.success(null);
    }
    
    @PutMapping("/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "禁用用户", description = "禁用指定用户")
    public ApiResponse<Void> disableUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId) {
        log.info("Disabling user: {}", userId);
        userService.disableUser(userId);
        return ApiResponse.success(null);
    }
    
    @PutMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "锁定用户", description = "锁定指定用户")
    public ApiResponse<Void> lockUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId,
            @Parameter(description = "锁定时长（分钟）", required = true)
            @RequestParam Integer lockMinutes) {
        log.info("Locking user: {} for {} minutes", userId, lockMinutes);
        userService.lockUser(userId, lockMinutes);
        return ApiResponse.success(null);
    }
    
    @PutMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "解锁用户", description = "解锁指定用户")
    public ApiResponse<Void> unlockUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotBlank String userId) {
        log.info("Unlocking user: {}", userId);
        userService.unlockUser(userId);
        return ApiResponse.success(null);
    }
    
    // ==================== 用户个人资料管理 ====================
    
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的个人资料信息")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        // 从Spring Security上下文获取当前用户ID
        String currentUserId = getCurrentUserId();
        UserProfileResponse response = userService.getCurrentUserProfile(currentUserId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人资料信息")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ApiResponse<UserProfileResponse> updateUserProfile(@Valid @RequestBody UserProfileRequest request) {
        // 从Spring Security上下文获取当前用户ID
        String currentUserId = getCurrentUserId();
        log.info("Updating user profile: {}", currentUserId);
        UserProfileResponse response = userService.updateUserProfile(currentUserId, request);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改密码", description = "当前用户修改自己的密码")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "修改成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误或原密码不正确"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录")
    })
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        // 从Spring Security上下文获取当前用户ID
        String currentUserId = getCurrentUserId();
        log.info("Changing password for user: {}", currentUserId);
        userService.changePassword(currentUserId, request);
        return ApiResponse.success(null);
    }
    
    // ==================== 用户验证功能 ====================
    
    @GetMapping("/check/username/{username}")
    @Operation(summary = "检查用户名是否存在", description = "检查指定用户名是否已存在")
    public ApiResponse<Boolean> existsByUsername(
            @Parameter(description = "用户名", required = true)
            @PathVariable @NotBlank String username) {
        boolean exists = userService.existsByUsername(username);
        return ApiResponse.success(exists);
    }
    
    @GetMapping("/check/email/{email}")
    @Operation(summary = "检查邮箱是否存在", description = "检查指定邮箱是否已存在")
    public ApiResponse<Boolean> existsByEmail(
            @Parameter(description = "邮箱", required = true)
            @PathVariable @NotBlank String email) {
        boolean exists = userService.existsByEmail(email);
        return ApiResponse.success(exists);
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 获取当前登录用户ID
     */
    private String getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            // 假设UserDetails实现类有getUserId方法，或者从用户名获取
            String username = authentication.getName();
            // 这里需要根据实际的UserDetails实现来获取用户ID
            // 暂时使用用户名查询用户ID
            UserResponse user = userService.getUserByUsername(username);
            return user.getId();
        }
        
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
    }
}