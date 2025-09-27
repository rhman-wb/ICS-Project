<template>
  <div class="user-profile">
    <div class="profile-container">
      <!-- 页面标题 -->
      <a-page-header
        class="profile-header"
        title="个人设置"
        sub-title="查看和管理您的个人信息"
        @back="() => $router.push('/dashboard/home')"
      />

      <!-- 个人信息卡片 -->
      <a-card title="个人信息" class="profile-card">
        <template #extra>
          <a-button type="primary" @click="editMode = !editMode">
            {{ editMode ? '取消编辑' : '编辑资料' }}
          </a-button>
        </template>

        <a-form
          :model="profileForm"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 12 }"
          class="profile-form"
        >
          <a-form-item label="用户ID">
            <a-input v-model:value="userInfo?.id" disabled />
          </a-form-item>

          <a-form-item label="用户名">
            <a-input v-model:value="userInfo?.username" disabled />
          </a-form-item>

          <a-form-item label="真实姓名">
            <a-input
              v-model:value="profileForm.realName"
              :disabled="!editMode"
              placeholder="请输入真实姓名"
            />
          </a-form-item>

          <a-form-item label="邮箱地址">
            <a-input
              v-model:value="profileForm.email"
              :disabled="!editMode"
              placeholder="请输入邮箱地址"
            />
          </a-form-item>

          <a-form-item label="手机号码">
            <a-input
              v-model:value="profileForm.phone"
              :disabled="!editMode"
              placeholder="请输入手机号码"
            />
          </a-form-item>

          <a-form-item label="用户角色">
            <a-tag
              v-for="role in userInfo?.roles"
              :key="role"
              color="blue"
              class="role-tag"
            >
              {{ getRoleDisplayName(role) }}
            </a-tag>
          </a-form-item>

          <a-form-item v-if="editMode" :wrapper-col="{ offset: 4, span: 12 }">
            <a-space>
              <a-button type="primary" @click="handleSave" :loading="saving">
                保存修改
              </a-button>
              <a-button @click="handleCancel">
                取消
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 安全设置卡片 -->
      <a-card title="安全设置" class="security-card">
        <a-list :data-source="securityActions">
          <template #renderItem="{ item }">
            <a-list-item>
              <template #actions>
                <a-button type="link" @click="item.action">
                  {{ item.buttonText }}
                </a-button>
              </template>
              <a-list-item-meta
                :title="item.title"
                :description="item.description"
              >
                <template #avatar>
                  <component :is="item.icon" class="security-icon" />
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  LockOutlined,
  LogoutOutlined,
  KeyOutlined
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/modules/auth'
import type { User } from '@/stores/modules/auth'

defineOptions({
  name: 'UserProfile'
})

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const editMode = ref(false)
const saving = ref(false)
const userInfo = computed(() => authStore.userInfo)

// 表单数据
const profileForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

// 初始化表单数据
const initFormData = () => {
  if (userInfo.value) {
    profileForm.realName = userInfo.value.realName || ''
    profileForm.email = userInfo.value.email || ''
    profileForm.phone = userInfo.value.phone || ''
  }
}

// 监听用户信息变化
watch(userInfo, () => {
  initFormData()
}, { immediate: true })

// 角色显示名映射
const getRoleDisplayName = (role: string) => {
  const roleMap: Record<string, string> = {
    'admin': '系统管理员',
    'business_user': '业务用户',
    'audit_user': '审核用户'
  }
  return roleMap[role] || role
}

// 安全操作列表
const securityActions = computed(() => [
  {
    title: '修改密码',
    description: '定期修改密码，保护账户安全',
    icon: LockOutlined,
    buttonText: '修改',
    action: () => message.info('修改密码功能正在开发中')
  },
  {
    title: '登录记录',
    description: '查看最近的登录活动记录',
    icon: KeyOutlined,
    buttonText: '查看',
    action: () => message.info('登录记录功能正在开发中')
  },
  {
    title: '退出登录',
    description: '安全退出当前账户',
    icon: LogoutOutlined,
    buttonText: '退出',
    action: handleLogout
  }
])

// 处理保存
const handleSave = async () => {
  await authStore.logout()\n  message.success('退出登录成功')\n  router.push('/login')
}
</script>

<style lang="scss" scoped>
.user-profile {
  background: #f5f5f5;
  min-height: 100vh;
  padding: 24px;

  .profile-container {
    max-width: 1200px;
    margin: 0 auto;
  }

  .profile-header {
    background: #fff;
    margin-bottom: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .profile-card,
  .security-card {
    margin-bottom: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f0f0;
    }
  }

  .profile-form {
    .role-tag {
      margin-right: 8px;
      margin-bottom: 8px;
    }
  }

  .security-card {
    .security-icon {
      font-size: 24px;
      color: #1890ff;
    }

    :deep(.ant-list-item) {
      padding: 16px 24px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }

    :deep(.ant-list-item-meta-title) {
      font-weight: 500;
      font-size: 16px;
    }

    :deep(.ant-list-item-meta-description) {
      color: #666;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .user-profile {
    padding: 16px;

    .profile-form {
      :deep(.ant-form-item) {
        .ant-form-item-label {
          text-align: left;
        }
      }
    }
  }
}
</style>
