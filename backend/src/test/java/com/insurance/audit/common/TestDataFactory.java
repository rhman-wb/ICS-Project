package com.insurance.audit.common;

import com.insurance.audit.user.domain.enums.PermissionStatus;
import com.insurance.audit.user.domain.enums.PermissionType;
import com.insurance.audit.user.domain.enums.RoleStatus;
import com.insurance.audit.user.domain.enums.UserStatus;
import com.insurance.audit.user.domain.model.Permission;
import com.insurance.audit.user.domain.model.Role;
import com.insurance.audit.user.domain.model.User;
import com.insurance.audit.user.infrastructure.security.CustomUserDetails;
import com.insurance.audit.user.interfaces.dto.request.ChangePasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.CreateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.LoginRequest;
import com.insurance.audit.user.interfaces.dto.request.RefreshTokenRequest;
import com.insurance.audit.user.interfaces.dto.request.ResetPasswordRequest;
import com.insurance.audit.user.interfaces.dto.request.UpdateUserRequest;
import com.insurance.audit.user.interfaces.dto.request.UserProfileRequest;
import com.insurance.audit.user.interfaces.dto.request.UserQueryRequest;
import com.insurance.audit.user.interfaces.dto.response.LoginResponse;
import com.insurance.audit.user.interfaces.dto.response.UserProfileResponse;
import com.insurance.audit.user.interfaces.dto.response.UserResponse;
import com.insurance.audit.product.domain.entity.Product;
import com.insurance.audit.product.domain.entity.Document;
import com.insurance.audit.product.interfaces.dto.request.CreateProductRequest;
import com.insurance.audit.product.interfaces.dto.request.UpdateProductRequest;
import com.insurance.audit.product.interfaces.dto.request.ProductQueryRequest;
import com.insurance.audit.product.interfaces.dto.response.ProductResponse;
import com.insurance.audit.product.interfaces.dto.response.DocumentValidationResult;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 测试数据工厂
 * 提供统一的测试数据创建方法
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
public class TestDataFactory {
    
    // 常量定义
    public static final String DEFAULT_USER_ID = "test-user-id-001";
    public static final String DEFAULT_USERNAME = "testuser";
    public static final String DEFAULT_PASSWORD = "password123";
    public static final String DEFAULT_ENCODED_PASSWORD = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjxSOpE6.jVgKlW";
    public static final String DEFAULT_REAL_NAME = "测试用户";
    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_PHONE = "13800138000";
    public static final String DEFAULT_AVATAR = "https://example.com/avatar.jpg";
    public static final String DEFAULT_CLIENT_IP = "192.168.1.100";
    
    public static final String DEFAULT_ROLE_ID = "test-role-id-001";
    public static final String DEFAULT_ROLE_NAME = "测试角色";
    public static final String DEFAULT_ROLE_CODE = "TEST_ROLE";
    
    public static final String DEFAULT_PERMISSION_ID = "test-permission-id-001";
    public static final String DEFAULT_PERMISSION_NAME = "测试权限";
    public static final String DEFAULT_PERMISSION_CODE = "test:view";
    
    public static final String DEFAULT_JWT_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwNDY3MjAwMCwiZXhwIjoxNzA0NzU4NDAwfQ.test";
    public static final String DEFAULT_REFRESH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInR5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNzA0NjcyMDAwLCJleHAiOjE3MDUyNzY4MDB9.test";

    // 产品相关常量
    public static final String DEFAULT_PRODUCT_ID = "test-product-id-001";
    public static final String DEFAULT_PRODUCT_NAME = "中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险";
    public static final String DEFAULT_REPORT_TYPE = "修订产品";
    public static final String DEFAULT_PRODUCT_NATURE = "政策性农险";
    public static final Integer DEFAULT_YEAR = 2024;
    public static final String DEFAULT_REVISION_TYPE = "条款修订";
    public static final String DEFAULT_OPERATING_REGION = "西藏自治区";
    public static final String DEFAULT_PRODUCT_CATEGORY = "养殖险";

    // 文档相关常量
    public static final String DEFAULT_DOCUMENT_ID = "test-document-id-001";
    public static final String DEFAULT_FILE_NAME = "中国人寿财产保险条款.docx";
    public static final String DEFAULT_FILE_PATH = "/uploads/2024/09/product_001/terms.docx";
    public static final Long DEFAULT_FILE_SIZE = 2048576L;
    public static final String DEFAULT_FILE_TYPE = "DOCX";
    
    /**
     * 创建默认用户
     */
    public static User createDefaultUser() {
        return User.builder()
                .id(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName(DEFAULT_REAL_NAME)
                .email(DEFAULT_EMAIL)
                .phone(DEFAULT_PHONE)
                .avatar(DEFAULT_AVATAR)
                .status(UserStatus.ACTIVE)
                .loginFailureCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建用户（指定用户名）
     */
    public static User createUser(String username) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("用户-" + username)
                .email(username + "@example.com")
                .phone("138" + String.format("%08d", username.hashCode() % 100000000))
                .status(UserStatus.ACTIVE)
                .loginFailureCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建锁定的用户
     */
    public static User createLockedUser() {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username("lockeduser")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("锁定用户")
                .email("locked@example.com")
                .phone("13800138001")
                .status(UserStatus.LOCKED)
                .loginFailureCount(5)
                .lockedUntil(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建停用的用户
     */
    public static User createInactiveUser() {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username("inactiveuser")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("停用用户")
                .email("inactive@example.com")
                .phone("13800138002")
                .status(UserStatus.INACTIVE)
                .loginFailureCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建默认角色
     */
    public static Role createDefaultRole() {
        return Role.builder()
                .id(DEFAULT_ROLE_ID)
                .name(DEFAULT_ROLE_NAME)
                .code(DEFAULT_ROLE_CODE)
                .description("测试角色描述")
                .status(RoleStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建管理员角色
     */
    public static Role createAdminRole() {
        return Role.builder()
                .id(UUID.randomUUID().toString())
                .name("管理员")
                .code("ADMIN")
                .description("系统管理员")
                .status(RoleStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建普通用户角色
     */
    public static Role createUserRole() {
        return Role.builder()
                .id(UUID.randomUUID().toString())
                .name("普通用户")
                .code("USER")
                .description("普通用户")
                .status(RoleStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建默认权限
     */
    public static Permission createDefaultPermission() {
        return Permission.builder()
                .id(DEFAULT_PERMISSION_ID)
                .name(DEFAULT_PERMISSION_NAME)
                .code(DEFAULT_PERMISSION_CODE)
                .type(PermissionType.MENU)
                .path("/test")
                .component("TestComponent")
                .icon("test")
                .sortOrder(1)
                .status(PermissionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建菜单权限
     */
    public static Permission createMenuPermission(String name, String code, String path) {
        return Permission.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .code(code)
                .type(PermissionType.MENU)
                .path(path)
                .component(name + "Component")
                .icon(code.toLowerCase())
                .sortOrder(1)
                .status(PermissionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建按钮权限
     */
    public static Permission createButtonPermission(String name, String code) {
        return Permission.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .code(code)
                .type(PermissionType.BUTTON)
                .sortOrder(1)
                .status(PermissionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建API权限
     */
    public static Permission createApiPermission(String name, String code, String path) {
        return Permission.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .code(code)
                .type(PermissionType.API)
                .path(path)
                .sortOrder(1)
                .status(PermissionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建自定义用户详情
     */
    public static CustomUserDetails createDefaultCustomUserDetails() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_TEST")
        );
        
        return CustomUserDetails.builder()
                .userId(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName(DEFAULT_REAL_NAME)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("test:view", "test:edit"))
                .build();
    }
    
    /**
     * 创建管理员用户详情
     */
    public static CustomUserDetails createAdminUserDetails() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        return CustomUserDetails.builder()
                .userId("admin-user-id")
                .username("admin")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("管理员")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("*:*"))
                .build();
    }
    
    /**
     * 创建登录请求
     */
    public static LoginRequest createDefaultLoginRequest() {
        return LoginRequest.builder()
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_PASSWORD)
                .rememberMe(false)
                .build();
    }
    
    /**
     * 创建登录请求（指定用户名和密码）
     */
    public static LoginRequest createLoginRequest(String username, String password) {
        return LoginRequest.builder()
                .username(username)
                .password(password)
                .rememberMe(false)
                .build();
    }
    
    /**
     * 创建登录响应
     */
    public static LoginResponse createDefaultLoginResponse() {
        return LoginResponse.builder()
                .accessToken(DEFAULT_JWT_TOKEN)
                .refreshToken(DEFAULT_REFRESH_TOKEN)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .realName(DEFAULT_REAL_NAME)
                .roles(Arrays.asList("USER", "TEST"))
                .permissions(Arrays.asList("test:view", "test:edit"))
                .build();
    }
    
    /**
     * 创建刷新令牌请求
     */
    public static RefreshTokenRequest createDefaultRefreshTokenRequest() {
        return RefreshTokenRequest.builder()
                .refreshToken(DEFAULT_REFRESH_TOKEN)
                .build();
    }
    
    /**
     * 创建用户创建请求
     */
    public static CreateUserRequest createDefaultCreateUserRequest() {
        return CreateUserRequest.builder()
                .username("newuser")
                .password("newpassword123")
                .realName("新用户")
                .email("newuser@example.com")
                .phone("13800138003")
                .roleIds(Arrays.asList(DEFAULT_ROLE_ID))
                .build();
    }
    
    /**
     * 创建用户更新请求
     */
    public static UpdateUserRequest createDefaultUpdateUserRequest() {
        return UpdateUserRequest.builder()
                .realName("更新后的用户")
                .email("updated@example.com")
                .phone("13800138004")
                .status(UserStatus.ACTIVE)
                .roleIds(Arrays.asList(DEFAULT_ROLE_ID))
                .build();
    }
    
    /**
     * 创建用户资料请求
     */
    public static UserProfileRequest createDefaultUserProfileRequest() {
        return UserProfileRequest.builder()
                .realName("更新资料")
                .email("profile@example.com")
                .phone("13800138005")
                .avatar("https://example.com/new-avatar.jpg")
                .build();
    }
    
    /**
     * 创建修改密码请求
     */
    public static ChangePasswordRequest createDefaultChangePasswordRequest() {
        return ChangePasswordRequest.builder()
                .currentPassword(DEFAULT_PASSWORD)
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();
    }
    
    /**
     * 创建重置密码请求
     */
    public static ResetPasswordRequest createDefaultResetPasswordRequest() {
        return ResetPasswordRequest.builder()
                .userId(DEFAULT_USER_ID)
                .newPassword("resetpassword123")
                .confirmPassword("resetpassword123")
                .build();
    }
    
    /**
     * 创建用户查询请求
     */
    public static UserQueryRequest createDefaultUserQueryRequest() {
        return UserQueryRequest.builder()
                .username("test")
                .realName("测试")
                .email("test")
                .status(UserStatus.ACTIVE)
                .page(1)
                .size(10)
                .build();
    }
    
    /**
     * 创建用户响应
     */
    public static UserResponse createDefaultUserResponse() {
        return UserResponse.builder()
                .id(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .realName(DEFAULT_REAL_NAME)
                .email(DEFAULT_EMAIL)
                .phone(DEFAULT_PHONE)
                .avatar(DEFAULT_AVATAR)
                .status(UserStatus.ACTIVE)
                .lastLoginTime(LocalDateTime.now().minusHours(1))
                .lastLoginIp(DEFAULT_CLIENT_IP)
                .roles(Arrays.asList(
                    UserResponse.RoleInfo.builder()
                        .id("role1")
                        .name("用户")
                        .code("USER")
                        .description("普通用户")
                        .build(),
                    UserResponse.RoleInfo.builder()
                        .id("role2")
                        .name("测试")
                        .code("TEST")
                        .description("测试角色")
                        .build()
                ))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建用户资料响应
     */
    public static UserProfileResponse createDefaultUserProfileResponse() {
        return UserProfileResponse.builder()
                .id(DEFAULT_USER_ID)
                .username(DEFAULT_USERNAME)
                .realName(DEFAULT_REAL_NAME)
                .email(DEFAULT_EMAIL)
                .phone(DEFAULT_PHONE)
                .avatar(DEFAULT_AVATAR)
                .status(UserStatus.ACTIVE)
                .lastLoginTime(LocalDateTime.now().minusHours(1))
                .lastLoginIp(DEFAULT_CLIENT_IP)
                .roles(Arrays.asList("USER", "TEST"))
                .permissions(Arrays.asList("test:view", "test:edit"))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建用户列表
     */
    public static List<User> createUserList(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(createUser("user" + i));
        }
        return users;
    }
    
    /**
     * 创建角色列表
     */
    public static List<Role> createRoleList() {
        return Arrays.asList(
                createAdminRole(),
                createUserRole(),
                createDefaultRole()
        );
    }
    
    /**
     * 创建权限列表
     */
    public static List<Permission> createPermissionList() {
        return Arrays.asList(
                createMenuPermission("用户管理", "user:view", "/user"),
                createButtonPermission("用户新增", "user:create"),
                createButtonPermission("用户编辑", "user:edit"),
                createButtonPermission("用户删除", "user:delete"),
                createApiPermission("用户API", "user:api", "/api/users/*")
        );
    }
    
    /**
     * 生成随机ID
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 生成随机用户名
     */
    public static String generateUsername() {
        return "user" + System.currentTimeMillis();
    }
    
    /**
     * 生成随机邮箱
     */
    public static String generateEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }
    
    /**
     * 生成随机手机号
     */
    public static String generatePhone() {
        return "138" + String.format("%08d", (int) (Math.random() * 100000000));
    }
    
    /**
     * 创建测试JWT令牌
     */
    public static String createTestJwtToken(String username) {
        // 返回一个模拟的JWT令牌，实际测试中可能需要真实的JWT工具类
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIiICsgdXNlcm5hbWUgKyAiIiwiaWF0IjoxNzA0NjcyMDAwLCJleHAiOjE3MDQ3NTg0MDB9.test-" + username;
    }

    // ========== 产品相关测试数据 ==========

    /**
     * 创建默认产品
     */
    public static Product createDefaultProduct() {
        return Product.builder()
                .id(DEFAULT_PRODUCT_ID)
                .productName(DEFAULT_PRODUCT_NAME)
                .reportType(DEFAULT_REPORT_TYPE)
                .productNature(DEFAULT_PRODUCT_NATURE)
                .year(DEFAULT_YEAR)
                .revisionType(DEFAULT_REVISION_TYPE)
                .operatingRegion(DEFAULT_OPERATING_REGION)
                .productCategory(DEFAULT_PRODUCT_CATEGORY)
                .developmentMethod("自主开发")
                .primaryAdditional("主险")
                .productType(Product.ProductType.AGRICULTURAL)
                .status(Product.ProductStatus.DRAFT)
                .documentCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建产品（指定状态）
     */
    public static Product createProduct(String productName, Product.ProductStatus status) {
        return Product.builder()
                .id(generateId())
                .productName(productName)
                .reportType(DEFAULT_REPORT_TYPE)
                .productNature(DEFAULT_PRODUCT_NATURE)
                .year(DEFAULT_YEAR)
                .operatingRegion(DEFAULT_OPERATING_REGION)
                .productCategory(DEFAULT_PRODUCT_CATEGORY)
                .developmentMethod("自主开发")
                .primaryAdditional("主险")
                .productType(Product.ProductType.AGRICULTURAL)
                .status(status)
                .documentCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建农险产品
     */
    public static Product createAgriculturalProduct() {
        return Product.builder()
                .id(generateId())
                .productName("测试农险产品")
                .reportType("新产品")
                .productNature("政策性农险")
                .year(2024)
                .operatingRegion("全国")
                .productCategory("种植险")
                .developmentMethod("自主开发")
                .primaryAdditional("主险")
                .productType(Product.ProductType.AGRICULTURAL)
                .status(Product.ProductStatus.DRAFT)
                .documentCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建备案产品
     */
    public static Product createFilingProduct() {
        return Product.builder()
                .id(generateId())
                .productName("测试备案产品")
                .reportType("新产品")
                .operatingScope("全国范围")
                .demonstrationClauseName("示范条款V1.0")
                .operatingRegion1("北京市")
                .operatingRegion2("上海市")
                .salesPromotionName("推广名称")
                .developmentMethod("合作开发")
                .primaryAdditional("附险")
                .productType(Product.ProductType.FILING)
                .status(Product.ProductStatus.DRAFT)
                .documentCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建默认文档
     */
    public static Document createDefaultDocument() {
        return Document.builder()
                .id(DEFAULT_DOCUMENT_ID)
                .fileName(DEFAULT_FILE_NAME)
                .filePath(DEFAULT_FILE_PATH)
                .fileSize(DEFAULT_FILE_SIZE)
                .fileType(DEFAULT_FILE_TYPE)
                .documentType(Document.DocumentType.TERMS)
                .productId(DEFAULT_PRODUCT_ID)
                .uploadStatus(Document.UploadStatus.UPLOADED)
                .auditStatus(Document.AuditStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建文档（指定类型和产品ID）
     */
    public static Document createDocument(Document.DocumentType documentType, String productId) {
        return Document.builder()
                .id(generateId())
                .fileName(documentType.getDescription() + ".docx")
                .filePath("/uploads/2024/09/" + productId + "/" + documentType.name().toLowerCase() + ".docx")
                .fileSize(DEFAULT_FILE_SIZE)
                .fileType(DEFAULT_FILE_TYPE)
                .documentType(documentType)
                .productId(productId)
                .uploadStatus(Document.UploadStatus.UPLOADED)
                .auditStatus(Document.AuditStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建文档（指定上传状态）
     */
    public static Document createDocument(Document.DocumentType documentType,
                                        String productId,
                                        Document.UploadStatus uploadStatus) {
        return Document.builder()
                .id(generateId())
                .fileName(documentType.getDescription() + ".docx")
                .filePath("/uploads/2024/09/" + productId + "/" + documentType.name().toLowerCase() + ".docx")
                .fileSize(DEFAULT_FILE_SIZE)
                .fileType(DEFAULT_FILE_TYPE)
                .documentType(documentType)
                .productId(productId)
                .uploadStatus(uploadStatus)
                .auditStatus(Document.AuditStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建完整的文档集合（所有必需类型）
     */
    public static List<Document> createCompleteDocumentSet(String productId) {
        List<Document> documents = new ArrayList<>();
        for (Document.DocumentType type : Arrays.asList(
                Document.DocumentType.REGISTRATION,
                Document.DocumentType.TERMS,
                Document.DocumentType.FEASIBILITY_REPORT,
                Document.DocumentType.ACTUARIAL_REPORT,
                Document.DocumentType.RATE_TABLE)) {
            documents.add(createDocument(type, productId));
        }
        return documents;
    }

    /**
     * 创建不完整的文档集合（缺少部分必需类型）
     */
    public static List<Document> createIncompleteDocumentSet(String productId) {
        List<Document> documents = new ArrayList<>();
        // 只创建部分必需文档
        documents.add(createDocument(Document.DocumentType.REGISTRATION, productId));
        documents.add(createDocument(Document.DocumentType.TERMS, productId));
        // 缺少 FEASIBILITY_REPORT, ACTUARIAL_REPORT, RATE_TABLE
        return documents;
    }

    /**
     * 创建产品创建请求
     */
    public static CreateProductRequest createDefaultCreateProductRequest() {
        return CreateProductRequest.builder()
                .productName("新测试产品")
                .reportType("新产品")
                .productNature("政策性农险")
                .year(2024)
                .operatingRegion("测试区域")
                .productCategory("种植险")
                .developmentMethod("自主开发")
                .primaryAdditional("主险")
                .productType("AGRICULTURAL")
                .build();
    }

    /**
     * 创建产品更新请求
     */
    public static UpdateProductRequest createDefaultUpdateProductRequest() {
        return UpdateProductRequest.builder()
                .productName("更新后的产品名称")
                .reportType("修订产品")
                .productNature("政策性农险")
                .year(2024)
                .revisionType("条款修订")
                .operatingRegion("更新区域")
                .productCategory("养殖险")
                .build();
    }

    /**
     * 创建产品查询请求
     */
    public static ProductQueryRequest createDefaultProductQueryRequest() {
        return ProductQueryRequest.builder()
                .fileName("测试")
                .reportType("新产品")
                .productCategory("种植险")
                .status("DRAFT")
                .year(2024)
                .page(1)
                .size(10)
                .build();
    }

    /**
     * 创建产品响应
     */
    public static ProductResponse createDefaultProductResponse() {
        return ProductResponse.builder()
                .id(DEFAULT_PRODUCT_ID)
                .productName(DEFAULT_PRODUCT_NAME)
                .reportType(DEFAULT_REPORT_TYPE)
                .productNature(DEFAULT_PRODUCT_NATURE)
                .year(DEFAULT_YEAR)
                .revisionType(DEFAULT_REVISION_TYPE)
                .operatingRegion(DEFAULT_OPERATING_REGION)
                .productCategory(DEFAULT_PRODUCT_CATEGORY)
                .developmentMethod("自主开发")
                .primaryAdditional("主险")
                .productType("AGRICULTURAL")
                .status("DRAFT")
                .documentCount(5)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建文档验证结果 - 成功
     */
    public static DocumentValidationResult createSuccessfulValidationResult(String productId) {
        DocumentValidationResult.ValidationSummary summary = DocumentValidationResult.ValidationSummary.builder()
                .totalDocuments(5)
                .requiredDocuments(5)
                .uploadedDocuments(5)
                .validDocuments(5)
                .totalErrors(0)
                .totalWarnings(0)
                .completenessPercentage(100.0)
                .build();

        return DocumentValidationResult.builder()
                .isValid(true)
                .productId(productId)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .summary(summary)
                .build();
    }

    /**
     * 创建文档验证结果 - 失败
     */
    public static DocumentValidationResult createFailedValidationResult(String productId) {
        List<DocumentValidationResult.ValidationError> errors = Arrays.asList(
                DocumentValidationResult.ValidationError.builder()
                        .errorType("MISSING_DOCUMENT")
                        .errorCode("DOC_001")
                        .message("缺少必需的要件文档")
                        .documentType("FEASIBILITY_REPORT")
                        .severity("HIGH")
                        .suggestion("请上传可行性报告文档")
                        .build(),
                DocumentValidationResult.ValidationError.builder()
                        .errorType("UNSUPPORTED_FILE_TYPE")
                        .errorCode("DOC_002")
                        .message("不支持的文件类型")
                        .documentType("TERMS")
                        .documentId("doc_001")
                        .severity("MEDIUM")
                        .suggestion("请上传支持的文件格式：DOCX, PDF, XLS, XLSX")
                        .build()
        );

        DocumentValidationResult.ValidationSummary summary = DocumentValidationResult.ValidationSummary.builder()
                .totalDocuments(3)
                .requiredDocuments(5)
                .uploadedDocuments(3)
                .validDocuments(1)
                .totalErrors(2)
                .totalWarnings(0)
                .completenessPercentage(60.0)
                .build();

        return DocumentValidationResult.builder()
                .isValid(false)
                .productId(productId)
                .errors(errors)
                .warnings(new ArrayList<>())
                .summary(summary)
                .build();
    }

    /**
     * 创建产品列表
     */
    public static List<Product> createProductList(int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            products.add(createProduct("测试产品" + i, Product.ProductStatus.DRAFT));
        }
        return products;
    }

    /**
     * 创建文档列表
     */
    public static List<Document> createDocumentList(String productId, int count) {
        List<Document> documents = new ArrayList<>();
        Document.DocumentType[] types = Document.DocumentType.values();
        for (int i = 0; i < count && i < types.length; i++) {
            documents.add(createDocument(types[i], productId));
        }
        return documents;
    }

    // ========== 规则相关测试数据 ==========

    // 规则相关常量
    public static final String DEFAULT_RULE_ID = "test-rule-id-001";
    public static final String DEFAULT_RULE_NAME = "测试规则";
    public static final String DEFAULT_RULE_DESCRIPTION = "这是一个测试规则的描述";

    /**
     * 创建默认创建规则请求
     */
    public static com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest createDefaultCreateRuleRequest() {
        return com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest.builder()
                .ruleName(DEFAULT_RULE_NAME)
                .ruleType("SINGLE")
                .description(DEFAULT_RULE_DESCRIPTION)
                .expression("测试表达式")
                .errorMessage("规则验证失败")
                .isActive(true)
                .build();
    }

    /**
     * 创建创建规则请求（指定类型）
     */
    public static com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest createCreateRuleRequest(String ruleType) {
        return com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest.builder()
                .ruleName("测试" + ruleType + "规则")
                .ruleType(ruleType)
                .description("测试" + ruleType + "规则描述")
                .expression("测试表达式")
                .errorMessage("规则验证失败")
                .isActive(true)
                .build();
    }

    /**
     * 创建默认更新规则请求
     */
    public static com.insurance.audit.rules.interfaces.dto.request.UpdateRuleRequest createDefaultUpdateRuleRequest() {
        return com.insurance.audit.rules.interfaces.dto.request.UpdateRuleRequest.builder()
                .ruleName("更新后的规则名称")
                .description("更新后的规则描述")
                .expression("更新后的表达式")
                .errorMessage("更新后的错误消息")
                .isActive(true)
                .build();
    }

    /**
     * 创建默认更新审核状态请求
     */
    public static com.insurance.audit.rules.interfaces.dto.request.UpdateAuditStatusRequest createDefaultUpdateAuditStatusRequest() {
        return com.insurance.audit.rules.interfaces.dto.request.UpdateAuditStatusRequest.builder()
                .auditStatus("APPROVED")
                .comments("审核通过")
                .build();
    }

    /**
     * 创建更新审核状态请求（指定状态）
     */
    public static com.insurance.audit.rules.interfaces.dto.request.UpdateAuditStatusRequest createUpdateAuditStatusRequest(String ruleId, String status) {
        return com.insurance.audit.rules.interfaces.dto.request.UpdateAuditStatusRequest.builder()
                .ruleId(ruleId)
                .auditStatus(status)
                .comments("更新状态为: " + status)
                .build();
    }

    /**
     * 创建默认更新有效状态请求
     */
    public static com.insurance.audit.rules.interfaces.dto.request.UpdateEffectiveStatusRequest createDefaultUpdateEffectiveStatusRequest() {
        return com.insurance.audit.rules.interfaces.dto.request.UpdateEffectiveStatusRequest.builder()
                .effectiveStatus("EFFECTIVE")
                .effectiveDate(LocalDateTime.now())
                .comments("设置为有效")
                .build();
    }

    /**
     * 创建更新有效状态请求（指定状态）
     */
    public static com.insurance.audit.rules.interfaces.dto.request.UpdateEffectiveStatusRequest createUpdateEffectiveStatusRequest(String ruleId, String status) {
        return com.insurance.audit.rules.interfaces.dto.request.UpdateEffectiveStatusRequest.builder()
                .ruleId(ruleId)
                .effectiveStatus(status)
                .effectiveDate(LocalDateTime.now())
                .comments("更新状态为: " + status)
                .build();
    }

    /**
     * 创建默认提交OA请求
     */
    public static com.insurance.audit.rules.interfaces.dto.request.SubmitOARequest createDefaultSubmitOARequest() {
        return com.insurance.audit.rules.interfaces.dto.request.SubmitOARequest.builder()
                .ruleIds(Arrays.asList("rule-id-001", "rule-id-002"))
                .submissionType("BATCH")
                .urgencyLevel("NORMAL")
                .comments("批量提交OA审核")
                .build();
    }

    /**
     * 创建提交OA请求（指定规则IDs）
     */
    public static com.insurance.audit.rules.interfaces.dto.request.SubmitOARequest createSubmitOARequest(List<String> ruleIds) {
        return com.insurance.audit.rules.interfaces.dto.request.SubmitOARequest.builder()
                .ruleIds(ruleIds)
                .submissionType("BATCH")
                .urgencyLevel("NORMAL")
                .comments("提交OA审核")
                .build();
    }

    /**
     * 创建模拟Excel内容
     */
    public static byte[] createMockExcelContent() {
        // 创建简单的Excel内容模拟
        String mockContent = """
                规则名称,规则类型,描述,表达式,错误消息
                测试规则1,SINGLE,测试描述1,表达式1,错误消息1
                测试规则2,DOUBLE,测试描述2,表达式2,错误消息2
                测试规则3,FORMAT,测试描述3,表达式3,错误消息3
                """;
        return mockContent.getBytes();
    }

    /**
     * 创建规则权限列表
     */
    public static List<Permission> createRulePermissionList() {
        return Arrays.asList(
                createApiPermission("规则查看", "RULE_VIEW", "/api/v1/rules/*"),
                createApiPermission("规则创建", "RULE_CREATE", "/api/v1/rules"),
                createApiPermission("规则编辑", "RULE_EDIT", "/api/v1/rules/*"),
                createApiPermission("规则删除", "RULE_DELETE", "/api/v1/rules/*"),
                createApiPermission("规则审核", "RULE_AUDIT", "/api/v1/rules/status/*"),
                createApiPermission("规则管理", "RULE_ADMIN", "/api/v1/rules/*"),
                createApiPermission("规则导入", "RULE_IMPORT", "/api/v1/rules/import/*"),
                createApiPermission("OA提交", "RULE_OA_SUBMIT", "/api/v1/rules/status/submit-oa"),
                createApiPermission("规则发布", "RULE_PUBLISH", "/api/v1/rules/status/publish"),
                createApiPermission("规则审批", "RULE_APPROVE", "/api/v1/rules/status/approve"),
                createApiPermission("规则统计", "RULE_STATISTICS", "/api/v1/rules/statistics/*")
        );
    }

    /**
     * 创建规则管理员用户详情
     */
    public static CustomUserDetails createRuleAdminUserDetails() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_RULE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return CustomUserDetails.builder()
                .userId("rule-admin-user-id")
                .username("ruleadmin")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("规则管理员")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList(
                        "RULE_VIEW", "RULE_CREATE", "RULE_EDIT", "RULE_DELETE",
                        "RULE_AUDIT", "RULE_ADMIN", "RULE_IMPORT", "RULE_OA_SUBMIT",
                        "RULE_PUBLISH", "RULE_APPROVE", "RULE_STATISTICS"
                ))
                .build();
    }

    /**
     * 创建规则审核员用户详情
     */
    public static CustomUserDetails createRuleAuditorUserDetails() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_RULE_AUDITOR"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return CustomUserDetails.builder()
                .userId("rule-auditor-user-id")
                .username("ruleauditor")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("规则审核员")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("RULE_VIEW", "RULE_AUDIT"))
                .build();
    }

    /**
     * 创建规则查看者用户详情
     */
    public static CustomUserDetails createRuleViewerUserDetails() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_RULE_VIEWER"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return CustomUserDetails.builder()
                .userId("rule-viewer-user-id")
                .username("ruleviewer")
                .password(DEFAULT_ENCODED_PASSWORD)
                .realName("规则查看者")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .permissions(Arrays.asList("RULE_VIEW"))
                .build();
    }
}