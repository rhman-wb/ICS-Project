/**
 * Ant Design Vue 主题配置
 * 基于保险行业专业性和可信度的深蓝色主题
 */

import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'

// 主题色彩配置
export const themeConfig: ThemeConfig = {
  token: {
    // 主色调 - 深蓝色主题
    colorPrimary: '#1890ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#f5222d',
    colorInfo: '#1890ff',
    
    // 字体配置
    fontFamily: '"PingFang SC", "Microsoft YaHei", "微软雅黑", "Helvetica Neue", Helvetica, Arial, sans-serif',
    fontSize: 14,
    fontSizeLG: 16,
    fontSizeSM: 12,
    fontSizeXL: 20,
    
    // 圆角配置
    borderRadius: 6,
    borderRadiusLG: 8,
    borderRadiusSM: 4,
    borderRadiusXS: 2,
    
    // 间距配置
    padding: 16,
    paddingLG: 24,
    paddingSM: 12,
    paddingXS: 8,
    paddingXXS: 4,
    
    margin: 16,
    marginLG: 24,
    marginSM: 12,
    marginXS: 8,
    marginXXS: 4,
    
    // 阴影配置
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.06)',
    boxShadowSecondary: '0 4px 12px rgba(0, 0, 0, 0.15)',
    
    // 动画配置
    motionDurationFast: '0.1s',
    motionDurationMid: '0.2s',
    motionDurationSlow: '0.3s',
    
    // 线条配置
    lineWidth: 1,
    lineType: 'solid',
    
    // 控件高度
    controlHeight: 32,
    controlHeightLG: 40,
    controlHeightSM: 24,
    
    // Z-index 配置
    zIndexBase: 0,
    zIndexPopupBase: 1000,
    
    // 屏幕尺寸
    screenXS: 480,
    screenSM: 576,
    screenMD: 768,
    screenLG: 992,
    screenXL: 1200,
    screenXXL: 1600,
  },
  
  components: {
    // 按钮组件定制
    Button: {
      borderRadius: 6,
    },
    
    // 输入框组件定制
    Input: {
      borderRadius: 6,
    },
    
    // 选择器组件定制
    Select: {
      borderRadius: 6,
    },
    
    // 表格组件定制
    Table: {
      borderRadius: 8,
    },
    
    // 卡片组件定制
    Card: {
      borderRadius: 8,
    },
    
    // 模态框组件定制
    Modal: {
      borderRadius: 8,
    },
    
    // 消息组件定制
    Message: {
      borderRadius: 8,
    },
    
    // 通知组件定制
    Notification: {
      borderRadius: 8,
    },
    
    // 菜单组件定制
    Menu: {
      borderRadius: 6,
    },
    
    // 标签页组件定制
    Tabs: {
      borderRadius: 6,
    },
    
    // 步骤条组件定制
    Steps: {
      borderRadius: 6,
    },
    
    // 分页组件定制
    Pagination: {
      borderRadius: 4,
    },
    
    // 标签组件定制
    Tag: {
      borderRadius: 4,
    },
    
    // 徽章组件定制
    Badge: {
      borderRadius: 10,
    },
    
    // 头像组件定制
    Avatar: {
      borderRadius: 6,
    },
    
    // 工具提示组件定制
    Tooltip: {
      borderRadius: 6,
    },
    
    // 气泡确认框组件定制
    Popconfirm: {
      borderRadius: 8,
    },
    
    // 下拉菜单组件定制
    Dropdown: {
      borderRadius: 8,
    },
    
    // 日期选择器组件定制
    DatePicker: {
      borderRadius: 6,
    },
    

    
    // 上传组件定制
    Upload: {
      borderRadius: 6,
    },
    
    // 树形控件组件定制
    Tree: {
      borderRadius: 4,
    },
    
    // 穿梭框组件定制
    Transfer: {
      borderRadius: 6,
    },
    
    // 级联选择器组件定制
    Cascader: {
      borderRadius: 6,
    },
    
    // 自动完成组件定制
    AutoComplete: {
      borderRadius: 6,
    },
    
    // 颜色选择器组件定制
    ColorPicker: {
      borderRadius: 6,
    },
    
    // 浮动按钮组件定制
    FloatButton: {
      borderRadius: 40,
    },
    
    // 锚点组件定制
    Anchor: {
      borderRadius: 4,
    },
    
    // 回到顶部组件定制
    BackTop: {
      borderRadius: 20,
    },
  },
  
  // 算法配置（可选）
  algorithm: undefined, // 可以设置为 theme.darkAlgorithm 或 theme.compactAlgorithm
}

// 深色主题配置（预留）
export const darkThemeConfig: ThemeConfig = {
  ...themeConfig,
  token: {
    ...themeConfig.token,
    colorBgBase: '#141414',
    colorTextBase: '#ffffff',
    colorBorder: '#434343',
    colorBorderSecondary: '#303030',
    colorBgContainer: '#1f1f1f',
    colorBgElevated: '#262626',
    colorBgLayout: '#000000',
  },
}

// 紧凑主题配置（预留）
export const compactThemeConfig: ThemeConfig = {
  ...themeConfig,
  token: {
    ...themeConfig.token,
    controlHeight: 28,
    controlHeightLG: 36,
    controlHeightSM: 20,
    padding: 12,
    paddingLG: 16,
    paddingSM: 8,
    margin: 12,
    marginLG: 16,
    marginSM: 8,
  },
}

export default themeConfig