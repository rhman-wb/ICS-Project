<template>
  <div class="rule-create">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <a-button
          type="link"
          class="back-button"
          @click="handleBack"
        >
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回
        </a-button>
        <h1 class="page-title">添加规则</h1>
      </div>
    </div>

    <!-- 规则类型选择 -->
    <div class="rule-type-tabs">
      <a-tabs v-model:activeKey="activeRuleType" @change="handleTabChange">
        <a-tab-pane key="single" tab="单句规则">
          <RuleCreateSingle
            v-if="activeRuleType === 'single'"
            @success="handleCreateSuccess"
            @cancel="handleCancel"
          />
        </a-tab-pane>
        <a-tab-pane key="double" tab="双句规则">
          <RuleCreateDouble
            v-if="activeRuleType === 'double'"
            @success="handleCreateSuccess"
            @cancel="handleCancel"
          />
        </a-tab-pane>
        <a-tab-pane key="format" tab="格式规则">
          <RuleCreateFormat
            v-if="activeRuleType === 'format'"
            @success="handleCreateSuccess"
            @cancel="handleCancel"
          />
        </a-tab-pane>
        <a-tab-pane key="advanced" tab="高级规则">
          <RuleCreateAdvanced
            v-if="activeRuleType === 'advanced'"
            @success="handleCreateSuccess"
            @cancel="handleCancel"
          />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import RuleCreateSingle from './RuleCreateSingle.vue'
import RuleCreateDouble from './RuleCreateDouble.vue'
import RuleCreateFormat from './RuleCreateFormat.vue'
import RuleCreateAdvanced from './RuleCreateAdvanced.vue'

const router = useRouter()
const route = useRoute()

// 当前活动的规则类型
const activeRuleType = ref('single')

// 初始化时根据路由参数设置默认tab
onMounted(() => {
  const type = route.query.type as string
  if (type && ['single', 'double', 'format', 'advanced'].includes(type)) {
    activeRuleType.value = type
  }
})

// 处理tab切换
const handleTabChange = (key: string) => {
  // 更新路由参数，但不刷新页面
  router.replace({
    path: route.path,
    query: { ...route.query, type: key }
  })
}

// 返回规则列表
const handleBack = () => {
  router.push('/rules')
}

// 处理创建成功
const handleCreateSuccess = (ruleData: any) => {
  router.push('/rules/success')
}

// 处理取消
const handleCancel = () => {
  router.push('/rules')
}
</script>

<style scoped lang="scss">
.rule-create {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .header-left {
      display: flex;
      align-items: center;

      .back-button {
        padding: 4px 8px;
        margin-right: 12px;
        color: #1890ff;

        &:hover {
          background: #f0f9ff;
        }
      }

      .page-title {
        font-size: 20px;
        font-weight: 600;
        color: #262626;
        margin: 0;
      }
    }
  }

  .rule-type-tabs {
    background: white;
    border-radius: 6px;
    padding: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    :deep(.ant-tabs-nav) {
      margin-bottom: 24px;
    }

    :deep(.ant-tabs-content-holder) {
      padding-top: 16px;
    }
  }
}
</style>