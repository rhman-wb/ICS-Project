# 样式系统文档

本目录包含了整个前端应用的样式系统，基于保险行业专业性和可信度设计的深蓝色主题。

## 📁 目录结构

```
styles/
├── index.scss          # 样式入口文件，统一导入所有样式
├── variables.scss      # 全局CSS变量和SCSS变量定义
├── mixins.scss         # 常用的混入函数和工具
├── reset.scss          # 浏览器样式重置
├── common.scss         # 通用样式类和组件样式
├── animations.scss     # 动画定义和动画工具类
├── antd-theme.scss     # Ant Design Vue 组件样式定制
├── responsive.scss     # 响应式设计样式
└── README.md          # 本文档
```

## 🎨 设计系统

### 色彩系统

#### 主色调（深蓝色主题）
- **主色**: `#1890FF` - 体现保险行业专业性和可信度
- **主色悬停**: `#40A9FF`
- **主色激活**: `#096DD9`

#### 功能色
- **成功色**: `#52C41A` - 表示成功、通过状态
- **警告色**: `#FAAD14` - 表示警告、需注意状态
- **错误色**: `#F5222D` - 表示错误、失败状态
- **信息色**: `#1890FF` - 表示一般信息提示

#### 中性色
- **主要文本**: `#262626`
- **次要文本**: `#595959`
- **辅助文本**: `#8C8C8C`
- **禁用文本**: `#BFBFBF`
- **边框色**: `#D9D9D9`
- **背景色**: `#FAFAFA`

### 字体系统

#### 字体族
- **中文字体**: "PingFang SC", "Microsoft YaHei", "微软雅黑"
- **英文字体**: "Segoe UI", Roboto, "Helvetica Neue", Arial
- **等宽字体**: "SF Mono", Monaco, Inconsolata, "Roboto Mono", Consolas

#### 字体大小
- **小字**: `10px` - 版权信息等
- **辅助文字**: `12px` - 说明文字、时间戳
- **正文**: `14px` - 主要内容
- **小节标题**: `16px` - 卡片标题、表单标题
- **区块标题**: `20px` - 工作台各区域标题
- **页面主标题**: `24px` - 登录页面系统标题
- **数据展示**: `28px` - 汇总数据大号字体

#### 字重
- **常规**: `400` - 正文内容
- **中等**: `500` - 次要标题、导航菜单
- **半粗**: `600` - 重要标题、强调文字

### 间距系统

基于 `16px` 的基础间距单位：

- **微间距**: `4px` - 紧密相关元素间
- **小间距**: `8px` - 相关元素间
- **中间距**: `16px` - 组件间
- **大间距**: `24px` - 页面区块间
- **超大间距**: `32px` - 页面区块间

### 圆角系统

- **小圆角**: `4px` - 标签、徽章
- **基础圆角**: `6px` - 按钮、输入框
- **大圆角**: `8px` - 卡片、模态框
- **超大圆角**: `12px` - 特殊组件

### 阴影系统

- **基础阴影**: `0 2px 8px rgba(0, 0, 0, 0.06)` - 卡片
- **大阴影**: `0 4px 12px rgba(0, 0, 0, 0.15)` - 悬停效果
- **超大阴影**: `0 6px 16px rgba(0, 0, 0, 0.12)` - 模态框

## 📱 响应式设计

### 断点定义

- **xs**: `< 480px` - 手机
- **sm**: `≥ 576px` - 大手机
- **md**: `≥ 768px` - 平板
- **lg**: `≥ 992px` - 桌面
- **xl**: `≥ 1200px` - 大桌面
- **xxl**: `≥ 1600px` - 超大桌面

### 使用方式

```scss
// 使用混入
@include respond-to(md) {
  // 平板及以上样式
}

@include mobile-first {
  // 移动端优先样式
}

// 使用工具类
.hide-md-down  // 在平板以下隐藏
.show-lg-up    // 在桌面及以上显示
```

## 🛠 使用指南

### 1. 变量使用

```scss
// CSS 变量（推荐）
.my-component {
  color: var(--color-text-primary);
  padding: var(--spacing-md);
  border-radius: var(--border-radius-base);
}

// SCSS 变量
.my-component {
  color: $text-color-primary;
  padding: $spacing-md;
  border-radius: $border-radius-base;
}
```

### 2. 混入使用

```scss
.my-component {
  @include flex-center;
  @include text-ellipsis;
  @include card-base;
  
  &:hover {
    @include card-hover;
  }
}
```

### 3. 工具类使用

```html
<!-- 布局工具类 -->
<div class="flex-center p-lg m-md">
  <span class="text-primary font-medium">内容</span>
</div>

<!-- 响应式工具类 -->
<div class="d-none d-md-block text-center text-md-left">
  响应式内容
</div>
```

### 4. 动画使用

```scss
.my-component {
  @include fade-in;
  
  &.loading {
    @extend .animate-pulse;
  }
}
```

```html
<!-- 动画工具类 -->
<div class="animate-fade-in-up hover-lift">
  带动画的内容
</div>
```

## 🎯 Ant Design Vue 主题定制

主题配置文件位于 `src/config/theme.ts`，包含：

- **Token 配置**: 全局设计令牌
- **组件配置**: 各组件的专门定制
- **算法配置**: 主题算法（深色、紧凑等）

### 使用方式

```vue
<template>
  <a-config-provider :theme="themeConfig">
    <!-- 应用内容 -->
  </a-config-provider>
</template>

<script setup lang="ts">
import themeConfig from '@/config/theme'
</script>
```

## 📋 最佳实践

### 1. 样式组织

- 使用 BEM 命名规范
- 组件样式使用 `scoped`
- 全局样式放在对应的样式文件中
- 避免内联样式

### 2. 响应式设计

- 移动端优先设计
- 使用相对单位（rem、em、%）
- 合理使用断点
- 测试不同设备的显示效果

### 3. 性能优化

- 避免深层嵌套选择器
- 使用 CSS 变量减少重复
- 合理使用动画和过渡
- 压缩和优化样式文件

### 4. 可维护性

- 保持样式的一致性
- 使用语义化的类名
- 及时清理无用样式
- 编写样式文档

## 🔧 开发工具

### VS Code 插件推荐

- **SCSS IntelliSense**: SCSS 智能提示
- **CSS Peek**: CSS 定义跳转
- **Prettier**: 代码格式化
- **stylelint**: CSS 代码检查

### 配置文件

```json
// .vscode/settings.json
{
  "scss.completion.completePropertyWithSemicolon": true,
  "scss.completion.triggerPropertyValueCompletion": true,
  "css.validate": false,
  "scss.validate": false,
  "stylelint.enable": true
}
```

## 📚 参考资源

- [Ant Design Vue 官方文档](https://antdv.com/)
- [CSS 变量 MDN 文档](https://developer.mozilla.org/zh-CN/docs/Web/CSS/Using_CSS_custom_properties)
- [SCSS 官方文档](https://sass-lang.com/documentation)
- [BEM 命名规范](https://getbem.com/)
- [响应式设计指南](https://web.dev/responsive-web-design-basics/)

## 🚀 更新日志

### v1.0.0 (2024-01-01)
- 初始化样式系统
- 建立深蓝色主题
- 完成响应式设计
- 集成 Ant Design Vue 主题定制
- 添加动画系统
- 完善工具类库