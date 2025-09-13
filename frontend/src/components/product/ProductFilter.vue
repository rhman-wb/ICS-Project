<template>
  <div class="product-filter">
    <a-card title="筛选查询" class="filter-card">
      <a-form
        :model="formData"
        layout="horizontal"
        class="filter-form"
        @finish="handleSubmit"
      >
        <a-row :gutter="16">
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="文件名" name="fileName">
              <a-input
                v-model:value="formData.fileName"
                placeholder="支持产品名称关键字搜索"
                allow-clear
                @press-enter="handleSubmit"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="报送类型" name="reportType">
              <a-select
                v-model:value="formData.reportType"
                placeholder="请选择报送类型"
                allow-clear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="新产品">新产品</a-select-option>
                <a-select-option value="修订产品">修订产品</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="开发类型" name="developmentType">
              <a-select
                v-model:value="formData.developmentType"
                placeholder="请选择开发类型"
                allow-clear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="自主开发">自主开发</a-select-option>
                <a-select-option value="合作开发">合作开发</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="产品类别" name="productCategory">
              <a-select
                v-model:value="formData.productCategory"
                placeholder="请选择产品类别"
                allow-clear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="种植险">种植险</a-select-option>
                <a-select-option value="养殖险">养殖险</a-select-option>
                <a-select-option value="不区分">不区分</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="主附险" name="primaryAdditional">
              <a-select
                v-model:value="formData.primaryAdditional"
                placeholder="请选择主附险"
                allow-clear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="主险">主险</a-select-option>
                <a-select-option value="附险">附险</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="修订类型" name="revisionType">
              <a-select
                v-model:value="formData.revisionType"
                placeholder="请选择修订类型"
                allow-clear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="条款修订">条款修订</a-select-option>
                <a-select-option value="费率修订">费率修订</a-select-option>
                <a-select-option value="条款费率修订">条款费率修订</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="经营区域" name="operatingRegion">
              <a-input
                v-model:value="formData.operatingRegion"
                placeholder="支持地区名称输入"
                allow-clear
                @press-enter="handleSubmit"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="年度" name="year">
              <a-select
                v-model:value="formData.year"
                placeholder="请选择年度"
                allow-clear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="2024">2024</a-select-option>
                <a-select-option value="2023">2023</a-select-option>
                <a-select-option value="2022">2022</a-select-option>
                <a-select-option value="2021">2021</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="24" style="text-align: right">
            <a-space>
              <a-button @click="handleReset">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
              <a-button type="primary" html-type="submit">
                <template #icon>
                  <SearchOutlined />
                </template>
                查询
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'

interface FilterData {
  fileName?: string
  reportType?: string
  developmentType?: string
  productCategory?: string
  primaryAdditional?: string
  revisionType?: string
  operatingRegion?: string
  year?: string
}

interface Props {
  modelValue: FilterData
}

interface Emits {
  (e: 'update:modelValue', value: FilterData): void
  (e: 'search', filters: FilterData): void
  (e: 'reset'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formData = reactive<FilterData>({ ...props.modelValue })

watch(
  () => props.modelValue,
  (newValue) => {
    Object.assign(formData, newValue)
  },
  { deep: true }
)

watch(
  formData,
  (newValue) => {
    emit('update:modelValue', { ...newValue })
  },
  { deep: true }
)

const handleSubmit = () => {
  emit('search', { ...formData })
}

const handleReset = () => {
  Object.keys(formData).forEach(key => {
    formData[key as keyof FilterData] = ''
  })
  emit('reset')
}
</script>

<style scoped>
.product-filter {
  margin-bottom: 16px;
}

.filter-card {
  border-radius: 8px;
}

.filter-form {
  margin-bottom: 0;
}

.filter-form .ant-form-item {
  margin-bottom: 16px;
}

.filter-form .ant-form-item-label {
  width: 80px;
  flex: 0 0 80px;
}

@media (max-width: 768px) {
  .filter-form .ant-form-item-label {
    width: 60px;
    flex: 0 0 60px;
  }
}
</style>