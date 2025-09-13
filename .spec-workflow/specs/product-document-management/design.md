# 产品文档管理模块 - 详细设计文档

## 概述

本文档描述产品文档管理模块的详细设计，包括系统架构、组件设计、API设计和数据流。

## 系统架构

### 整体架构
```
Frontend (Vue 3 + TypeScript)
├── Views (页面层)
├── Components (组件层)
├── Services (服务层)
├── Store (状态管理)
└── Types (类型定义)

Backend (Spring Boot)
├── Controller (控制器层)
├── Service (业务层)
├── Repository (数据层)
└── Entity (实体层)
```

### 技术栈
- **前端**: Vue 3 + TypeScript + Ant Design Vue + Pinia
- **后端**: Spring Boot + MyBatis Plus + MySQL + Redis
- **文件存储**: 本地文件系统/OSS
- **认证**: JWT Token

## 核心组件设计

### 1. ProductListComponent (产品列表组件)

**功能职责**: 显示产品列表，支持筛选、搜索、分页和批量操作

**Props接口**:
```typescript
interface ProductListProps {
  filters?: ProductFilters;
  pagination?: PaginationConfig;
}
```

**State管理**:
```typescript
interface ProductListState {
  products: Product[];
  loading: boolean;
  selectedItems: string[];
  totalCount: number;
  currentPage: number;
}
```

**主要方法**:
- `loadProducts()`: 加载产品列表
- `handleFilter()`: 处理筛选条件
- `handleSearch()`: 处理搜索
- `handleSelection()`: 处理多选
- `handleBatchOperation()`: 批量操作

### 2. ProductFormComponent (产品表单组件)

**功能职责**: 新增和编辑产品信息

**Props接口**:
```typescript
interface ProductFormProps {
  mode: 'create' | 'edit';
  productId?: string;
  productType: 'agricultural' | 'filing';
}
```

**表单字段**:
- 基础信息: 产品名称、报送类型、年度等
- 产品属性: 产品类别、开发方式、主附险等
- 文档信息: 条款、可行性报告、精算报告、费率表等

### 3. DocumentUploadComponent (文档上传组件)

**功能职责**: 处理单个和批量文件上传

**Props接口**:
```typescript
interface DocumentUploadProps {
  multiple?: boolean;
  accept?: string;
  maxSize?: number;
  documentType?: string;
}
```

**支持的文件类型**:
- Word文档 (.doc, .docx)
- PDF文件 (.pdf)
- Excel文件 (.xls, .xlsx)

### 4. DocumentViewerComponent (文档查看组件)

**功能职责**: 文档预览和基础信息展示

**Props接口**:
```typescript
interface DocumentViewerProps {
  documentId: string;
  documentType: string;
}
```

**显示内容**:
- 产品基础信息
- 文档预览窗口
- 章节导航
- 操作按钮

### 5. DocumentAuditViewComponent (智能审核结果显示组件)

**功能职责**: 显示智能审核结果，包括命中规则和修改建议

**Props接口**:
```typescript
interface DocumentAuditViewProps {
  documentId: string;
  auditResults: AuditResult[];
}

interface AuditResult {
  ruleId: string;
  ruleType: string;
  applicableChapter: string;
  ruleSource: string;
  originalContent: string;
  suggestion: string;
  severity: 'error' | 'warning' | 'info';
}
```

**组件结构**:
```vue
<template>
  <div class="audit-view-container">
    <!-- 左侧：文档预览区域 -->
    <div class="document-preview-panel">
      <div class="document-header">
        <h3>产品预览</h3>
      </div>
      <div class="chapter-navigation">
        <a-button 
          v-for="chapter in chapters" 
          :key="chapter.id"
          @click="jumpToChapter(chapter.id)"
        >
          {{ chapter.name }}
        </a-button>
      </div>
      <div class="document-content">
        <!-- 文档内容显示 -->
        <div v-html="documentContent"></div>
      </div>
    </div>
    
    <!-- 右侧：审核规则结果区域 -->
    <div class="audit-results-panel">
      <div class="audit-header">
        <h3>命中规则</h3>
      </div>
      <div class="audit-rules-list">
        <div 
          v-for="result in auditResults" 
          :key="result.ruleId"
          class="audit-rule-item"
          :class="severityClass(result.severity)"
        >
          <div class="rule-header">
            <span class="rule-id">{{ result.ruleId }}</span>
            <span class="applicable-chapter">{{ result.applicableChapter }}</span>
            <span class="rule-type">{{ result.ruleType }}</span>
            <a-button type="link" size="small" @click="jumpToRule(result.ruleId)">
              跳转
            </a-button>
          </div>
          <div class="rule-content">
            <div class="original-content">
              <strong>原文内容：</strong>
              <p>{{ result.originalContent }}</p>
            </div>
            <div class="modification-suggestion">
              <strong>修改建议：</strong>
              <p>{{ result.suggestion }}</p>
            </div>
          </div>
          <div class="rule-source">
            <small>规则来源：{{ result.ruleSource }}</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
```

**样式设计**:
```scss
.audit-view-container {
  display: flex;
  height: 100vh;
  gap: 20px;
  
  .document-preview-panel {
    flex: 1;
    background: #fff;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    
    .chapter-navigation {
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      
      .ant-btn {
        margin-right: 8px;
        margin-bottom: 8px;
      }
    }
    
    .document-content {
      padding: 16px;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
    }
  }
  
  .audit-results-panel {
    width: 400px;
    background: #fff;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    
    .audit-rules-list {
      max-height: calc(100vh - 120px);
      overflow-y: auto;
      padding: 16px;
    }
    
    .audit-rule-item {
      border: 1px solid #f0f0f0;
      border-radius: 4px;
      margin-bottom: 16px;
      padding: 12px;
      
      &.error {
        border-color: #ff4d4f;
        background-color: #fff2f0;
      }
      
      &.warning {
        border-color: #faad14;
        background-color: #fffbf0;
      }
      
      &.info {
        border-color: #1890ff;
        background-color: #f6ffed;
      }
      
      .rule-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        
        .rule-id {
          font-weight: bold;
          color: #1890ff;
        }
      }
      
      .rule-content {
        .original-content,
        .modification-suggestion {
          margin-bottom: 8px;
          
          strong {
            color: #262626;
          }
          
          p {
            margin: 4px 0;
            padding: 8px;
            background-color: #fafafa;
            border-radius: 4px;
          }
        }
      }
      
      .rule-source {
        color: #8c8c8c;
        border-top: 1px solid #f0f0f0;
        padding-top: 8px;
      }
    }
  }
}
```

**主要方法**:
- `jumpToChapter(chapterId)`: 章节跳转功能
- `jumpToRule(ruleId)`: 规则跳转功能
- `severityClass(severity)`: 根据严重级别返回样式类
- `loadAuditResults()`: 加载审核结果
- `exportAuditReport()`: 导出审核报告

### 6. DocumentUploadDialogComponent (文档上传对话框组件)

**功能职责**: 模态对话框形式的文档上传功能，支持拖拽上传

**Props接口**:
```typescript
interface DocumentUploadDialogProps {
  visible: boolean;
  documentType?: string;
  maxFiles?: number;
  onClose: () => void;
  onSuccess: (files: UploadedFile[]) => void;
}

interface UploadedFile {
  id: string;
  name: string;
  size: number;
  type: string;
  url: string;
  uploadTime: string;
}
```

**组件结构**:
```vue
<template>
  <a-modal
    :visible="visible"
    title="上传文档"
    width="600px"
    :footer="null"
    @cancel="handleClose"
  >
    <div class="upload-dialog-content">
      <!-- 拖拽上传区域 -->
      <div 
        class="upload-drag-area"
        :class="{ 'drag-over': isDragOver }"
        @drop="handleDrop"
        @dragover="handleDragOver"
        @dragleave="handleDragLeave"
      >
        <div class="upload-hint">
          <a-icon type="cloud-upload" class="upload-icon" />
          <p class="upload-text">点击或将文件拖拽到此区域上传</p>
          <p class="upload-desc">
            支持扩展名：.doc .docx .pdf .xls .xlsx
          </p>
        </div>
        <input
          ref="fileInput"
          type="file"
          multiple
          :accept="acceptTypes"
          @change="handleFileSelect"
          style="display: none"
        />
      </div>
      
      <!-- 文件列表 -->
      <div class="upload-file-list" v-if="fileList.length > 0">
        <div class="file-list-header">
          <span>已选择文件 ({{ fileList.length }})</span>
          <a-button type="link" size="small" @click="clearFiles">
            清空
          </a-button>
        </div>
        <div class="file-list-items">
          <div 
            v-for="(file, index) in fileList" 
            :key="index"
            class="file-item"
          >
            <div class="file-info">
              <a-icon :type="getFileIcon(file.type)" />
              <span class="file-name">{{ file.name }}</span>
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
            </div>
            <div class="file-actions">
              <a-button 
                type="link" 
                size="small" 
                @click="removeFile(index)"
                danger
              >
                删除
              </a-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 上传进度 -->
      <div class="upload-progress" v-if="uploading">
        <a-progress 
          :percent="uploadProgress" 
          :status="uploadStatus"
          :showInfo="true"
        />
        <p class="progress-text">{{ uploadProgressText }}</p>
      </div>
      
      <!-- 操作按钮 -->
      <div class="upload-actions">
        <a-button @click="handleClose">取消</a-button>
        <a-button 
          type="primary" 
          @click="handleUpload"
          :loading="uploading"
          :disabled="fileList.length === 0"
        >
          上传 ({{ fileList.length }})
        </a-button>
      </div>
    </div>
  </a-modal>
</template>
```

**样式设计**:
```scss
.upload-dialog-content {
  .upload-drag-area {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    padding: 40px 20px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover, &.drag-over {
      border-color: #1890ff;
      background-color: #fafafa;
    }
    
    .upload-hint {
      .upload-icon {
        font-size: 48px;
        color: #1890ff;
        margin-bottom: 16px;
      }
      
      .upload-text {
        font-size: 16px;
        color: #262626;
        margin-bottom: 8px;
      }
      
      .upload-desc {
        font-size: 14px;
        color: #8c8c8c;
      }
    }
  }
  
  .upload-file-list {
    margin-top: 20px;
    
    .file-list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid #f0f0f0;
    }
    
    .file-list-items {
      max-height: 200px;
      overflow-y: auto;
    }
    
    .file-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      border: 1px solid #f0f0f0;
      border-radius: 4px;
      margin-bottom: 8px;
      
      .file-info {
        display: flex;
        align-items: center;
        flex: 1;
        
        .anticon {
          margin-right: 8px;
          color: #1890ff;
        }
        
        .file-name {
          flex: 1;
          margin-right: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .file-size {
          color: #8c8c8c;
          font-size: 12px;
        }
      }
    }
  }
  
  .upload-progress {
    margin: 20px 0;
    
    .progress-text {
      text-align: center;
      margin-top: 8px;
      color: #8c8c8c;
    }
  }
  
  .upload-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}
```

**主要方法**:
- `handleDrop(event)`: 处理拖拽文件放置
- `handleDragOver(event)`: 处理拖拽悬停
- `handleFileSelect(event)`: 处理文件选择
- `validateFile(file)`: 文件验证（类型、大小）
- `handleUpload()`: 执行文件上传
- `getFileIcon(type)`: 根据文件类型返回图标
- `formatFileSize(size)`: 格式化文件大小显示
- `removeFile(index)`: 移除指定文件
- `clearFiles()`: 清空文件列表

## 页面路由设计

### 路由配置
```typescript
const routes = [
  {
    path: '/product-management',
    name: 'ProductManagement',
    component: () => import('@/views/ProductManagementView.vue'),
    children: [
      {
        path: '',
        name: 'ProductList',
        component: () => import('@/views/ProductListView.vue')
      },
      {
        path: 'create/:type',
        name: 'ProductCreate',
        component: () => import('@/views/ProductFormView.vue'),
        props: true
      },
      {
        path: 'edit/:id',
        name: 'ProductEdit',
        component: () => import('@/views/ProductFormView.vue'),
        props: true
      },
      {
        path: 'view/:id',
        name: 'ProductView',
        component: () => import('@/views/ProductDetailView.vue'),
        props: true
      }
    ]
  }
];
```

## API设计

### RESTful API端点

#### 产品管理API
```typescript
// 获取产品列表
GET /api/v1/products
Query Parameters:
- page: number
- size: number
- productType?: string
- reportType?: string
- year?: string
- keyword?: string

Response: {
  data: Product[];
  total: number;
  page: number;
  size: number;
}

// 创建产品
POST /api/v1/products
Body: {
  productInfo: ProductInfo;
  documents: DocumentInfo[];
}

// 获取产品详情
GET /api/v1/products/:id
Response: {
  product: Product;
  documents: Document[];
  auditResults?: AuditResult[];
}

// 更新产品
PUT /api/v1/products/:id
Body: {
  productInfo: ProductInfo;
  documents?: DocumentInfo[];
}

// 删除产品
DELETE /api/v1/products/:id

// 批量操作
POST /api/v1/products/batch
Body: {
  action: 'delete' | 'audit' | 'export';
  productIds: string[];
}
```

#### 文档管理API
```typescript
// 上传文档
POST /api/v1/documents/upload
Body: FormData (multipart/form-data)
- files: File[]
- documentType: string
- productId?: string

Response: {
  uploadedFiles: UploadedFile[];
}

// 获取文档内容
GET /api/v1/documents/:id/content
Response: {
  content: string;
  chapters: Chapter[];
}

// 智能审核
POST /api/v1/documents/:id/audit
Response: {
  auditResults: AuditResult[];
  status: 'completed' | 'processing' | 'failed';
}

// 导出审核报告
GET /api/v1/documents/:id/audit-report
Response: File (PDF/Excel)
```

## 数据模型设计

### 实体类设计

#### Product (产品实体)
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private String reportType;
    
    @Column
    private String productNature;
    
    @Column
    private Integer year;
    
    @Column
    private String revisionType;
    
    @Column
    private String developmentMethod;
    
    @Column
    private String primaryAdditional;
    
    @Column
    private String productCategory;
    
    @Column
    private String operatingRegion;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Document> documents;
    
    // 省略getter/setter
}
```

#### Document (文档实体)
```java
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column
    private Long fileSize;
    
    @Column
    private String fileType;
    
    @Column(nullable = false)
    private String documentType; // 条款、可行性报告、精算报告、费率表
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    private List<AuditResult> auditResults;
    
    // 省略getter/setter
}
```

#### AuditResult (审核结果实体)
```java
@Entity
@Table(name = "audit_results")
public class AuditResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String ruleId;
    
    @Column(nullable = false)
    private String ruleType;
    
    @Column
    private String applicableChapter;
    
    @Column
    private String ruleSource;
    
    @Column(columnDefinition = "TEXT")
    private String originalContent;
    
    @Column(columnDefinition = "TEXT")
    private String suggestion;
    
    @Enumerated(EnumType.STRING)
    private AuditSeverity severity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;
    
    // 省略getter/setter
}
```

### TypeScript类型定义

#### 前端类型定义
```typescript
// 产品相关类型
export interface Product {
  id: string;
  productName: string;
  reportType: string;
  productNature?: string;
  year?: number;
  revisionType?: string;
  developmentMethod?: string;
  primaryAdditional?: string;
  productCategory?: string;
  operatingRegion?: string;
  status: ProductStatus;
  documents: Document[];
  createdAt: string;
  updatedAt: string;
}

export type ProductStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED';

// 文档相关类型
export interface Document {
  id: string;
  fileName: string;
  filePath: string;
  fileSize: number;
  fileType: string;
  documentType: DocumentType;
  productId: string;
  auditResults?: AuditResult[];
  uploadedAt: string;
}

export type DocumentType = 'TERMS' | 'FEASIBILITY_REPORT' | 'ACTUARIAL_REPORT' | 'RATE_TABLE';

// 审核结果类型
export interface AuditResult {
  id: string;
  ruleId: string;
  ruleType: string;
  applicableChapter: string;
  ruleSource: string;
  originalContent: string;
  suggestion: string;
  severity: 'error' | 'warning' | 'info';
  documentId: string;
}

// 筛选条件类型
export interface ProductFilters {
  productType?: string;
  reportType?: string;
  year?: string;
  keyword?: string;
  developmentMethod?: string;
  auditStatus?: string;
  dateRange?: [string, string];
}

// 分页配置类型
export interface PaginationConfig {
  current: number;
  pageSize: number;
  total: number;
  showSizeChanger?: boolean;
  showQuickJumper?: boolean;
}
```

## 状态管理设计

### Pinia Store设计

#### ProductStore
```typescript
import { defineStore } from 'pinia';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    currentProduct: null as Product | null,
    loading: false,
    filters: {} as ProductFilters,
    pagination: {
      current: 1,
      pageSize: 10,
      total: 0
    } as PaginationConfig,
    selectedProducts: [] as string[]
  }),
  
  getters: {
    filteredProducts: (state) => {
      return state.products.filter(product => {
        // 根据filters进行筛选
        return true;
      });
    }
  },
  
  actions: {
    async loadProducts() {
      this.loading = true;
      try {
        const response = await productApi.getProducts({
          page: this.pagination.current,
          size: this.pagination.pageSize,
          ...this.filters
        });
        this.products = response.data;
        this.pagination.total = response.total;
      } finally {
        this.loading = false;
      }
    },
    
    async createProduct(productData: CreateProductRequest) {
      const response = await productApi.createProduct(productData);
      this.products.unshift(response);
      return response;
    },
    
    async updateProduct(id: string, productData: UpdateProductRequest) {
      const response = await productApi.updateProduct(id, productData);
      const index = this.products.findIndex(p => p.id === id);
      if (index !== -1) {
        this.products[index] = response;
      }
      return response;
    },
    
    async deleteProduct(id: string) {
      await productApi.deleteProduct(id);
      this.products = this.products.filter(p => p.id !== id);
    },
    
    setFilters(filters: Partial<ProductFilters>) {
      this.filters = { ...this.filters, ...filters };
      this.pagination.current = 1; // 重置到第一页
    },
    
    setSelectedProducts(ids: string[]) {
      this.selectedProducts = ids;
    }
  }
});
```

## 错误处理和验证

### 表单验证规则
```typescript
export const productValidationRules = {
  productName: [
    { required: true, message: '请输入产品名称' },
    { min: 2, max: 200, message: '产品名称长度应在2-200个字符之间' }
  ],
  reportType: [
    { required: true, message: '请选择报送类型' }
  ],
  year: [
    { required: true, message: '请输入年度' },
    { pattern: /^\d{4}$/, message: '年度格式不正确' }
  ],
  documents: [
    { required: true, message: '请上传必要的文档' }
  ]
};
```

### 错误处理机制
```typescript
// API错误处理
export class ApiError extends Error {
  constructor(
    message: string,
    public code: string,
    public status: number
  ) {
    super(message);
  }
}

// 全局错误处理器
export function setupErrorHandler() {
  window.addEventListener('unhandledrejection', (event) => {
    if (event.reason instanceof ApiError) {
      // 显示用户友好的错误信息
      message.error(event.reason.message);
    }
  });
}
```

## 性能优化策略

### 前端优化
1. **组件懒加载**: 使用 `defineAsyncComponent` 实现路由级别的代码分割
2. **虚拟滚动**: 大数据列表使用虚拟滚动提升性能
3. **图片懒加载**: 文档预览图片懒加载
4. **缓存策略**: 使用 Pinia 持久化插件缓存数据

### 后端优化
1. **分页查询**: 所有列表查询支持分页
2. **索引优化**: 数据库关键字段添加索引
3. **缓存机制**: Redis 缓存热点数据
4. **异步处理**: 文档审核使用异步任务队列

## 安全考虑

### 文件上传安全
1. **文件类型验证**: 白名单方式验证文件类型
2. **文件大小限制**: 单文件最大 50MB
3. **病毒扫描**: 集成病毒扫描服务
4. **存储隔离**: 用户文件隔离存储

### 权限控制
1. **角色权限**: 基于角色的访问控制
2. **操作授权**: 每个操作都需要相应权限
3. **数据权限**: 用户只能访问自己的数据

## 测试策略

### 单元测试
- Vue 组件测试使用 Vue Test Utils
- 业务逻辑测试使用 Vitest
- 覆盖率要求 80% 以上

### 集成测试
- API 接口测试
- 数据库操作测试
- 文件上传流程测试

### E2E测试
- 关键用户流程测试
- 跨浏览器兼容性测试