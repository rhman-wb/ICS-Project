<template>
  <div class="quick-start-section">
    <div class="section-header">
      <h3 class="section-title">快速开始 / 便捷导航</h3>
    </div>
    
    <div class="quick-start-content">
      <transition-group name="list-item" tag="div">
        <a-row :gutter="[16, 16]" key="quick-actions">
          <!-- 第一行 -->
          <a-col 
            :xs="12" :sm="12" :md="6" :lg="6" :xl="6"
            :style="{ animationDelay: '0ms' }"
          >
            <div class="quick-action-card" v-permission="'product:import'">
              <a-badge :count="pendingImports" :offset="[10, 10]">
                <a-button 
                  type="default" 
                  size="large" 
                  block
                  class="quick-action-btn btn-hover-effect"
                  @click="handleImportProduct"
                  :loading="loadingStates.import"
                >
                  <template #icon>
                    <UploadOutlined />
                  </template>
                  导入产品
                </a-button>
              </a-badge>
            </div>
          </a-col>
        
          <a-col 
            :xs="12" :sm="12" :md="6" :lg="6" :xl="6"
            :style="{ animationDelay: '100ms' }"
          >
            <div class="quick-action-card" v-permission="'rule:create'">
              <a-badge :count="pendingRules" :offset="[10, 10]">
                <a-button 
                  type="default" 
                  size="large" 
                  block
                  class="quick-action-btn btn-hover-effect"
                  @click="handleCreateRule"
                  :loading="loadingStates.create"
                >
                  <template #icon>
                    <PlusOutlined />
                  </template>
                  创建规则
                </a-button>
              </a-badge>
            </div>
          </a-col>
          
          <a-col 
            :xs="12" :sm="12" :md="6" :lg="6" :xl="6"
            :style="{ animationDelay: '200ms' }"
          >
            <div class="quick-action-card" v-permission="'product:batch_import'">
              <a-badge :count="batchTasks" :offset="[10, 10]">
                <a-button 
                  type="default" 
                  size="large" 
                  block
                  class="quick-action-btn btn-hover-effect"
                  @click="handleBatchImport"
                  :loading="loadingStates.batch"
                >
                  <template #icon>
                    <InboxOutlined />
                  </template>
                  批量导入
                </a-button>
              </a-badge>
            </div>
          </a-col>
          
          <a-col 
            :xs="12" :sm="12" :md="6" :lg="6" :xl="6"
            :style="{ animationDelay: '300ms' }"
          >
            <div class="quick-action-card">
              <a-button 
                type="default" 
                size="large" 
                block
                class="quick-action-btn btn-hover-effect"
                @click="handleDownloadTemplate"
                :loading="loadingStates.download"
              >
                <template #icon>
                  <DownloadOutlined />
                </template>
                下载模板
              </a-button>
            </div>
          </a-col>
          
          <!-- 第二行 - 预留扩展按钮 -->
          <a-col 
            :xs="12" :sm="12" :md="6" :lg="6" :xl="6"
            :style="{ animationDelay: '400ms' }"
          >
            <div class="quick-action-card" v-permission:role="'admin'">
              <a-badge :count="otherTasks" :offset="[10, 10]">
                <a-button 
                  type="default" 
                  size="large" 
                  block
                  class="quick-action-btn btn-hover-effect"
                  @click="handleOperation4"
                  :loading="loadingStates.operation4"
                >
                  <template #icon>
                    <SettingOutlined />
                  </template>
                  操作四
                </a-button>
              </a-badge>
            </div>
          </a-col>
          
          <a-col 
            :xs="12" :sm="12" :md="6" :lg="6" :xl="6"
            :style="{ animationDelay: '500ms' }"
          >
            <div class="quick-action-card" v-permission:role="['admin', 'business_user']">
              <a-button 
                type="default" 
                size="large" 
                block
                class="quick-action-btn btn-hover-effect"
                @click="handleOperation5"
                :loading="loadingStates.operation5"
              >
                <template #icon>
                  <AppstoreOutlined />
                </template>
                操作五
              </a-button>
            </div>
          </a-col>
          
          <!-- 占位列保持布局平衡 -->
          <a-col 
            :xs="12" :sm="12" :md="6" :lg="6" :xl="6"
            :style="{ animationDelay: '600ms' }"
          >
            <div class="quick-action-card placeholder">
              <!-- 预留位置 -->
            </div>
          </a-col>
          
          <a-col 
            :xs="12" :sm="12" :md="6" :lg="6" :xl="6"
            :style="{ animationDelay: '700ms' }"
          >
            <div class="quick-action-card placeholder">
              <!-- 预留位置 -->
            </div>
          </a-col>
        </a-row>
      </transition-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UploadOutlined,
  PlusOutlined,
  InboxOutlined,
  DownloadOutlined,
  SettingOutlined,
  AppstoreOutlined
} from '@ant-design/icons-vue'

// 组件名称定义
defineOptions({
  name: 'QuickStartSection'
})

// Router
const router = useRouter()

// 徽章数字 - 模拟待处理数量
const pendingImports = ref(24)
const pendingRules = ref(8)
const batchTasks = ref(12)
const otherTasks = ref(5)

// 加载状态管理
const loadingStates = ref({
  import: false,
  create: false,
  batch: false,
  download: false,
  operation4: false,
  operation5: false
})

// 事件处理函数
const handleImportProduct = async () => {
  loadingStates.value.import = true
  try {
    // 模拟异步操作
    await new Promise(resolve => setTimeout(resolve, 1000))
    message.info('跳转到产品导入页面')
    // 跳转到产品导入页面
    router.push('/product/import')
  } finally {
    loadingStates.value.import = false
  }
}

const handleCreateRule = async () => {
  loadingStates.value.create = true
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    message.info('跳转到规则创建页面')
    // TODO: 实际项目中跳转到规则创建页面
    // router.push('/rule/create')
  } finally {
    loadingStates.value.create = false
  }
}

const handleBatchImport = async () => {
  loadingStates.value.batch = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1200))
    message.info('跳转到批量导入页面')
    // TODO: 实际项目中跳转到批量导入页面
    // router.push('/batch/import')
  } finally {
    loadingStates.value.batch = false
  }
}

const handleDownloadTemplate = async () => {
  loadingStates.value.download = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    message.success('模板下载已开始')
    // TODO: 实际项目中触发模板文件下载
    // downloadTemplate()
  } finally {
    loadingStates.value.download = false
  }
}

const handleOperation4 = async () => {
  loadingStates.value.operation4 = true
  try {
    await new Promise(resolve => setTimeout(resolve, 600))
    message.info('执行操作四')
    // TODO: 实际项目中实现具体功能
  } finally {
    loadingStates.value.operation4 = false
  }
}

const handleOperation5 = async () => {
  loadingStates.value.operation5 = true
  try {
    await new Promise(resolve => setTimeout(resolve, 900))
    message.info('执行操作五')
    // TODO: 实际项目中实现具体功能
  } finally {
    loadingStates.value.operation5 = false
  }
}
</script>

<style lang="scss" scoped>
.quick-start-section {
  background: #ffffff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .section-header {
    margin-bottom: 20px;
    
    .section-title {
      font-size: 20px;
      font-weight: 500;
      color: #262626;
      margin: 0;
    }
  }
  
  .quick-start-content {
    .quick-action-card {
      position: relative;
      height: 80px;
      
      &.placeholder {
        // 预留位置样式
        opacity: 0;
        pointer-events: none;
      }
      
      .quick-action-btn {
        height: 100%;
        border: 1px solid #f0f0f0;
        border-radius: var(--border-radius-lg);
        background: #ffffff;
        color: #262626;
        font-size: 14px;
        font-weight: 400;
        transition: all var(--animation-duration-base) var(--animation-timing-ease-out);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 8px;
        position: relative;
        overflow: hidden;
        
        // 图标样式
        .anticon {
          font-size: 20px;
          color: #1890ff;
          transition: all var(--animation-duration-base) var(--animation-timing-ease-out);
        }
        
        // 悬停效果增强
        &:hover:not(:disabled):not(.ant-btn-loading) {
          border-color: #40a9ff;
          background: linear-gradient(135deg, #f6ffed 0%, #f0f9ff 100%);
          transform: translateY(-3px);
          box-shadow: 0 6px 16px rgba(24, 144, 255, 0.2);
          
          .anticon {
            color: #096dd9;
            transform: scale(1.15) rotate(5deg);
          }
        }
        
        // 点击效果
        &:active:not(:disabled):not(.ant-btn-loading) {
          transform: translateY(-1px);
          box-shadow: 0 3px 10px rgba(24, 144, 255, 0.25);
          transition-duration: var(--animation-duration-fast);
        }
        
        // 焦点状态
        &:focus-visible {
          border-color: #40a9ff;
          box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
          outline: none;
        }
        
        // 加载状态
        &.ant-btn-loading {
          .anticon {
            animation: spin 1s linear infinite;
          }
        }
        
        // 禁用状态
        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
          transform: none;
          
          &:hover {
            transform: none;
            box-shadow: none;
            background: #ffffff;
            border-color: #f0f0f0;
          }
        }
      }
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .quick-start-section {
    padding: 16px;
    
    .section-header {
      margin-bottom: 16px;
      
      .section-title {
        font-size: 18px;
      }
    }
    
    .quick-start-content {
      .quick-action-card {
        height: 70px;
        
        .quick-action-btn {
          font-size: 13px;
          gap: 6px;
          
          .anticon {
            font-size: 18px;
          }
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .quick-start-section {
    padding: 12px;
    
    .quick-start-content {
      .quick-action-card {
        height: 60px;
        
        .quick-action-btn {
          font-size: 12px;
          gap: 4px;
          
          .anticon {
            font-size: 16px;
          }
        }
      }
    }
  }
}

// 徽章样式优化
:deep(.ant-badge) {
  width: 100%;
  
  .ant-badge-count {
    background: #f5222d;
    color: #ffffff;
    font-size: 10px;
    font-weight: 500;
    min-width: 18px;
    height: 18px;
    line-height: 18px;
    border-radius: 9px;
    box-shadow: 0 0 0 1px #ffffff;
  }
}

// 列表项动画
.list-item-enter-active {
  transition: all var(--animation-duration-base) var(--animation-timing-ease-out);
}

.list-item-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

// 按钮点击动画
@keyframes buttonClick {
  0% {
    transform: translateY(-3px) scale(1);
  }
  50% {
    transform: translateY(-1px) scale(0.98);
  }
  100% {
    transform: translateY(-3px) scale(1);
  }
}

.quick-action-btn:active:not(:disabled):not(.ant-btn-loading) {
  animation: buttonClick var(--animation-duration-fast) ease;
}

// 旋转动画
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>