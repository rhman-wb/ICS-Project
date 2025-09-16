<template>
  <div class="rule-success">
    <div class="success-content">
      <!-- 成功状态 -->
      <div class="success-header">
        <CheckCircleOutlined class="success-icon" />
        <h1 class="success-title">提交成功</h1>
        <p class="success-subtitle">您的规则已成功提交到系统进行处理</p>
      </div>

      <!-- 基础项信息 -->
      <div class="rule-info-section">
        <h3>规则信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">规则编号：</span>
            <span class="value">{{ ruleInfo.ruleNumber || 'R-' + Date.now() }}</span>
          </div>
          <div class="info-item">
            <span class="label">规则描述：</span>
            <span class="value">{{ ruleInfo.ruleDescription || '新建规则' }}</span>
          </div>
          <div class="info-item">
            <span class="label">规则类型：</span>
            <span class="value">{{ getRuleTypeName(ruleInfo.ruleType) }}</span>
          </div>
          <div class="info-item">
            <span class="label">提交时间：</span>
            <span class="value">{{ formatDateTime(new Date()) }}</span>
          </div>
          <div class="info-item">
            <span class="label">当前状态：</span>
            <span class="value">
              <a-tag color="blue">待技术评估</a-tag>
            </span>
          </div>
        </div>
      </div>

      <!-- 流程进度 -->
      <div class="process-section">
        <h3>审核流程</h3>
        <a-steps :current="0" size="small">
          <a-step title="待技术评估" description="技术团队将对规则进行评估" />
          <a-step title="待提交OA审核" description="评估通过后将提交OA审核" />
          <a-step title="已提交OA审核" description="等待OA审核结果" />
          <a-step title="OA审核通过" description="规则审核完成，准备上线" />
        </a-steps>
        <div class="process-note">
          <InfoCircleOutlined />
          <span>预计审核时长：3-5个工作日，您可以在规则列表中查看审核进度</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-section">
        <a-space size="large">
          <a-button type="primary" size="large" @click="handleGoToList">
            <UnorderedListOutlined />
            返回列表
          </a-button>
          <a-button size="large" @click="handleViewProject">
            <EyeOutlined />
            查看项目
          </a-button>
          <a-button size="large" @click="handlePrint">
            <PrinterOutlined />
            打印
          </a-button>
        </a-space>
      </div>

      <!-- 下一步建议 -->
      <div class="next-steps">
        <h4>接下来您可以：</h4>
        <ul>
          <li>在<a @click="handleGoToList">规则列表</a>中跟踪审核进度</li>
          <li>继续<a @click="handleCreateNew">创建其他类型的规则</a></li>
          <li>查看<a @click="handleViewGuide">规则配置指南</a>了解更多信息</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  CheckCircleOutlined,
  InfoCircleOutlined,
  UnorderedListOutlined,
  EyeOutlined,
  PrinterOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

interface RuleInfo {
  ruleNumber?: string
  ruleDescription?: string
  ruleType?: string
  [key: string]: any
}

const router = useRouter()
const route = useRoute()

// 规则信息
const ruleInfo = ref<RuleInfo>({})

// 获取规则类型名称
const getRuleTypeName = (type?: string) => {
  const typeMap: Record<string, string> = {
    'single': '单句规则',
    'double': '双句规则',
    'format': '格式规则',
    'advanced': '高级规则'
  }
  return typeMap[type || 'single'] || '单句规则'
}

// 格式化日期时间
const formatDateTime = (date: Date) => {
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 返回规则列表
const handleGoToList = () => {
  router.push('/rules')
}

// 查看项目
const handleViewProject = () => {
  // 如果有规则ID，跳转到详情页
  if (ruleInfo.value.id) {
    router.push(`/rules/${ruleInfo.value.id}`)
  } else {
    message.info('规则正在处理中，请稍后在列表中查看')
    handleGoToList()
  }
}

// 打印
const handlePrint = () => {
  // 打印当前页面
  window.print()
}

// 继续创建新规则
const handleCreateNew = () => {
  router.push('/rules/new')
}

// 查看指南
const handleViewGuide = () => {
  // 跳转到帮助页面或打开指南
  message.info('规则配置指南功能待完善')
}

// 组件挂载时获取规则信息
onMounted(() => {
  // 从路由参数或状态中获取规则信息
  if (route.params.ruleData) {
    ruleInfo.value = route.params.ruleData as RuleInfo
  } else if (route.query.type) {
    ruleInfo.value.ruleType = route.query.type as string
  }
})
</script>

<style scoped lang="scss">
.rule-success {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .success-content {
    max-width: 800px;
    margin: 0 auto;
    background: white;
    border-radius: 8px;
    padding: 40px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .success-header {
      text-align: center;
      margin-bottom: 40px;

      .success-icon {
        font-size: 64px;
        color: #52c41a;
        margin-bottom: 16px;
      }

      .success-title {
        font-size: 32px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 8px;
      }

      .success-subtitle {
        font-size: 16px;
        color: #8c8c8c;
        margin: 0;
      }
    }

    .rule-info-section {
      margin-bottom: 32px;

      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 16px;
        border-bottom: 2px solid #1890ff;
        padding-bottom: 8px;
      }

      .info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 16px;

        .info-item {
          display: flex;
          align-items: center;

          .label {
            font-weight: 500;
            color: #595959;
            min-width: 100px;
          }

          .value {
            color: #262626;
            font-weight: 500;
          }
        }
      }
    }

    .process-section {
      margin-bottom: 32px;

      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 16px;
        border-bottom: 2px solid #1890ff;
        padding-bottom: 8px;
      }

      .process-note {
        margin-top: 16px;
        padding: 12px;
        background: #f6f8fa;
        border-radius: 6px;
        display: flex;
        align-items: center;
        gap: 8px;
        color: #595959;
        font-size: 14px;
      }
    }

    .action-section {
      text-align: center;
      margin-bottom: 32px;
      padding: 24px 0;
      border-top: 1px solid #f0f0f0;
      border-bottom: 1px solid #f0f0f0;
    }

    .next-steps {
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 12px;
      }

      ul {
        margin: 0;
        padding-left: 20px;
        color: #595959;

        li {
          margin-bottom: 8px;

          a {
            color: #1890ff;
            cursor: pointer;
            text-decoration: none;

            &:hover {
              text-decoration: underline;
            }
          }
        }
      }
    }
  }
}

// 打印样式
@media print {
  .rule-success {
    padding: 0;
    background: white;

    .success-content {
      box-shadow: none;
      padding: 20px;

      .action-section {
        display: none;
      }

      .next-steps {
        display: none;
      }
    }
  }
}
</style>