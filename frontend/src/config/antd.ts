/**
 * Ant Design Vue 全局配置
 * 配置组件的默认行为和动画效果
 */

import { message, notification } from 'ant-design-vue'

// 配置全局消息组件
export const configureAntdMessage = () => {
  // 配置 message 组件
  message.config({
    duration: 3, // 显示时长（秒）
    maxCount: 5, // 最大显示数量
    // top: 由样式统一控制，避免遮挡导航
    rtl: false, // 是否从右到左
    prefixCls: 'ant-message', // 自定义类名前缀
  })

  // 配置 notification 组件
  notification.config({
    placement: 'topRight', // 弹出位置
    duration: 4.5, // 显示时长（秒）
    maxCount: 10, // 最大显示数量
    rtl: false, // 是否从右到左
    prefixCls: 'ant-notification', // 自定义类名前缀
  })
}

// 自定义消息方法，带有更好的动画效果
export const showMessage = {
  success: (content: string, duration = 3) => {
    return message.success({
      content,
      duration,
      style: {
        marginTop: '20px',
      },
    })
  },

  error: (content: string, duration = 4) => {
    return message.error({
      content,
      duration,
      style: {
        marginTop: '20px',
      },
    })
  },

  warning: (content: string, duration = 3) => {
    return message.warning({
      content,
      duration,
      style: {
        marginTop: '20px',
      },
    })
  },

  info: (content: string, duration = 3) => {
    return message.info({
      content,
      duration,
      style: {
        marginTop: '20px',
      },
    })
  },

  loading: (content: string, duration = 0) => {
    return message.loading({
      content,
      duration,
      style: {
        marginTop: '20px',
      },
    })
  },
}

// 自定义通知方法
export const showNotification = {
  success: (title: string, description?: string, duration = 4.5) => {
    return notification.success({
      message: title,
      description,
      duration,
      style: {
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
      },
    })
  },

  error: (title: string, description?: string, duration = 6) => {
    return notification.error({
      message: title,
      description,
      duration,
      style: {
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
      },
    })
  },

  warning: (title: string, description?: string, duration = 4.5) => {
    return notification.warning({
      message: title,
      description,
      duration,
      style: {
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
      },
    })
  },

  info: (title: string, description?: string, duration = 4.5) => {
    return notification.info({
      message: title,
      description,
      duration,
      style: {
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
      },
    })
  },
}
