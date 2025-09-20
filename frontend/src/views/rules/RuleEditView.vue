<template>
  <div class="rule-edit">
    <div class="page-header">
      <div class="header-left">
        <a-button type="link" class="back-button" @click="goBack">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回
        </a-button>
        <h1 class="page-title">编辑规则</h1>
      </div>
      <div class="header-right">
        <a-space>
          <a-button @click="goDetail">查看详情</a-button>
          <a-button type="primary" :loading="saving" @click="save">保存</a-button>
        </a-space>
      </div>
    </div>

    <a-card :loading="loading" title="基础配置">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="规则名称">
              <a-input v-model:value="form.name" placeholder="请输入" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="规则类型">
              <a-select v-model:value="form.type" :options="typeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="描述">
              <a-textarea v-model:value="form.description" rows="4" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()

const ruleId = route.params.id as string
const loading = ref(false)
const saving = ref(false)
const form = ref({
  name: '',
  type: 'single',
  description: ''
})

const typeOptions = [
  { label: '单句规则', value: 'single' },
  { label: '双句规则', value: 'double' },
  { label: '格式规则', value: 'format' },
  { label: '高级规则', value: 'advanced' }
]

onMounted(async () => {
  loading.value = true
  try {
    // TODO: 接入真实接口：rulesApi.getDetail(ruleId)
    form.value = {
      name: `示例规则 ${ruleId}`,
      type: 'single',
      description: '这是占位的编辑页面，用于打通路由与视图。'
    }
  } finally {
    loading.value = false
  }
})

const goBack = () => {
  router.push('/rules')
}

const goDetail = () => {
  router.push(`/rules/detail/${ruleId}`)
}

const save = async () => {
  saving.value = true
  try {
    // TODO: 接入真实保存接口：rulesApi.update(ruleId, form.value)
    await new Promise(resolve => setTimeout(resolve, 500))
    message.success('保存成功')
    goDetail()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.rule-edit {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.back-button {
  padding: 0;
}
</style>


