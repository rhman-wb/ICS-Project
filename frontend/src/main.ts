import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import { useAuthStore } from './stores/modules/auth'
import { setupDirectives } from './directives'
import { configureAntdMessage } from './config/antd'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import '@/assets/styles/index.scss'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(Antd)

// 配置 Ant Design Vue 组件
configureAntdMessage()

// 注册全局指令
setupDirectives(app)

// 初始化认证状态
const authStore = useAuthStore()
authStore.initAuth().then(() => {
  app.mount('#app')
}).catch((error) => {
  console.error('Auth initialization failed:', error)
  app.mount('#app')
})
