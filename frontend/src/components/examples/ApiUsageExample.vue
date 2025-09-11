<template>
  <div class="api-usage-example">
    <h3>API使用示例</h3>
    
    <!-- 登录示例 -->
    <div class="example-section">
      <h4>登录API调用示例</h4>
      <a-form :model="loginForm" @finish="handleLogin">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="loginForm.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="密码" name="password">
          <a-input-password v-model:value="loginForm.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item>
          <a-button 
            type="primary" 
            html-type="submit" 
            :loading="loginLoading"
            :disabled="!loginForm.username || !loginForm.password"
          >
            {{ loginLoading ? '登录中...' : '登录' }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 用户信息示例 -->
    <div class="example-section">
      <h4>用户信息API调用示例</h4>
      <a-button @click="handleGetUserInfo" :loading="getUserInfoLoading">
        获取用户信息
      </a-button>
      
      <div v-if="userInfo" class="user-info">
        <p><strong>用户ID:</strong> {{ userInfo.id }}</p>
        <p><strong>用户名:</strong> {{ userInfo.username }}</p>
        <p><strong>真实姓名:</strong> {{ userInfo.realName }}</p>
        <p><strong>邮箱:</strong> {{ userInfo.email }}</p>
        <p><strong>角色:</strong> {{ userInfo.roles?.join(', ') }}</p>
      </div>
    </div>

    <!-- 权限信息示例 -->
    <div class="example-section">
      <h4>权限信息API调用示例</h4>
      <a-button @click="handleGetPermissions" :loading="getUserPermissionsLoading">
        获取用户权限
      </a-button>
      
      <div v-if="permissions.length > 0" class="permissions">
        <p><strong>用户权限:</strong></p>
        <a-tag v-for="permission in permissions" :key="permission" color="blue">
          {{ permission }}
        </a-tag>
      </div>
    </div>

    <!-- 修改密码示例 -->
    <div class="example-section">
      <h4>修改密码API调用示例</h4>
      <a-form :model="passwordForm" @finish="handleChangePassword">
        <a-form-item label="原密码" name="oldPassword">
          <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" />
        </a-form-item>
        <a-form-item label="新密码" name="newPassword">
          <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请确认新密码" />
        </a-form-item>
        <a-form-item>
          <a-button 
            type="primary" 
            html-type="submit" 
            :loading="changePasswordLoading"
            :disabled="!isPasswordFormValid"
          >
            {{ changePasswordLoading ? '修改中...' : '修改密码' }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 登出示例 -->
    <div class="example-section">
      <h4>登出API调用示例</h4>
      <a-button @click="handleLogout" :loading="logoutLoading" danger>
        {{ logoutLoading ? '登出中...' : '登出' }}
      </a-button>
    </div>

    <!-- 加载状态监控 -->
    <div class="example-section">
      <h4>API加载状态监控</h4>
      <div class="loading-states">
        <p>登录加载状态: <a-tag :color="loginLoading ? 'orange' : 'green'">{{ loginLoading ? '加载中' : '空闲' }}</a-tag></p>
        <p>用户信息加载状态: <a-tag :color="getUserInfoLoading ? 'orange' : 'green'">{{ getUserInfoLoading ? '加载中' : '空闲' }}</a-tag></p>
        <p>权限加载状态: <a-tag :color="getUserPermissionsLoading ? 'orange' : 'green'">{{ getUserPermissionsLoading ? '加载中' : '空闲' }}</a-tag></p>
        <p>修改密码加载状态: <a-tag :color="changePasswordLoading ? 'orange' : 'green'">{{ changePasswordLoading ? '加载中' : '空闲' }}</a-tag></p>
        <p>登出加载状态: <a-tag :color="logoutLoading ? 'orange' : 'green'">{{ logoutLoading ? '加载中' : '空闲' }}</a-tag></p>
        <p>任何认证API加载: <a-tag :color="anyAuthLoading ? 'red' : 'green'">{{ anyAuthLoading ? '有API正在加载' : '所有API空闲' }}</a-tag></p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { authApi } from '@/api/modules/auth'
import { useApiLoading } from '@/composables/useApiLoading'
import type { LoginRequest, ChangePasswordRequest, UserInfo } from '@/api/types/auth'

// 使用API加载状态
const {
  loginLoading,
  logoutLoading,
  getUserInfoLoading,
  getUserPermissionsLoading,
  changePasswordLoading,
  anyAuthLoading
} = useApiLoading()

// 表单数据
const loginForm = ref<LoginRequest>({
  username: '',
  password: ''
})

const passwordForm = ref<ChangePasswordRequest>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 响应数据
const userInfo = ref<UserInfo | null>(null)
const permissions = ref<string[]>([])

// 计算属性
const isPasswordFormValid = computed(() => {
  return passwordForm.value.oldPassword && 
         passwordForm.value.newPassword && 
         passwordForm.value.confirmPassword &&
         passwordForm.value.newPassword === passwordForm.value.confirmPassword
})

// 事件处理函数
const handleLogin = async () => {
  try {
    const response = await authApi.login(loginForm.value)
    
    if (response.success) {
      message.success('登录成功')
      console.log('Login response:', response.data)
      
      // 清空表单
      loginForm.value = { username: '', password: '' }
      
      // 自动获取用户信息
      await handleGetUserInfo()
    } else {
      message.error(response.message || '登录失败')
    }
  } catch (error: any) {
    message.error(error.message || '登录失败')
    console.error('Login error:', error)
  }
}

const handleGetUserInfo = async () => {
  try {
    const response = await authApi.getUserInfo()
    
    if (response.success) {
      userInfo.value = response.data
      message.success('用户信息获取成功')
    } else {
      message.error(response.message || '获取用户信息失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取用户信息失败')
    console.error('Get user info error:', error)
  }
}

const handleGetPermissions = async () => {
  try {
    const response = await authApi.getUserPermissions()
    
    if (response.success) {
      permissions.value = response.data
      message.success('权限信息获取成功')
    } else {
      message.error(response.message || '获取权限信息失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取权限信息失败')
    console.error('Get permissions error:', error)
  }
}

const handleChangePassword = async () => {
  try {
    const response = await authApi.changePassword(passwordForm.value)
    
    if (response.success) {
      message.success('密码修改成功')
      
      // 清空表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    } else {
      message.error(response.message || '密码修改失败')
    }
  } catch (error: any) {
    message.error(error.message || '密码修改失败')
    console.error('Change password error:', error)
  }
}

const handleLogout = async () => {
  try {
    const response = await authApi.logout()
    
    if (response.success) {
      message.success('登出成功')
      
      // 清空数据
      userInfo.value = null
      permissions.value = []
      loginForm.value = { username: '', password: '' }
    } else {
      message.error(response.message || '登出失败')
    }
  } catch (error: any) {
    message.error(error.message || '登出失败')
    console.error('Logout error:', error)
  }
}
</script>

<style scoped>
.api-usage-example {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.example-section {
  margin-bottom: 32px;
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

.example-section h4 {
  margin-bottom: 16px;
  color: #1890ff;
}

.user-info,
.permissions {
  margin-top: 16px;
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.user-info p {
  margin-bottom: 8px;
}

.permissions p {
  margin-bottom: 8px;
}

.loading-states {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.loading-states p {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>