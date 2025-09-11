<template>
  <div class="dashboard-home">
    <!-- 顶部导航 -->
    <TopNav 
      :brand="homeData.topNav.brand"
      :menus="homeData.topNav.menus"
      :active="homeData.topNav.active"
      :username="homeData.topNav.username"
      @navigate="handleNavigate"
    />
    
    <!-- 主容器：1440px 固定宽度，左右两栏布局 -->
    <div class="dashboard-container">
      <!-- 左列：约 1100px -->
      <div class="left-column">
        <!-- 应用概要（左上） -->
        <div class="section app-overview">
          <h2 class="section-title">应用概要</h2>
          <AppOverview :overview="homeData.appOverview" />
        </div>

        <!-- 我的关注（左中大容器） -->
        <div class="section my-favorites">
          <h2 class="section-title">我的关注</h2>
          <MyFavorites 
            :items="homeData.myFavorites.items"
            :filters="homeData.myFavorites.filters"
            @select="handleSelectItem"
            @search="handleSearch"
          />
        </div>

        <!-- 汇总数据（底部） -->
        <div class="section summary-totals">
          <h2 class="section-title">汇总数据</h2>
          <SummaryTotals 
            :ranges="homeData.summaryTotals.ranges"
            :active-range="homeData.summaryTotals.activeRange"
            :metrics="homeData.summaryTotals.metrics"
            @changeRange="handleChangeRange"
          />
        </div>
      </div>

      <!-- 右列：约 552px -->
      <div class="right-column">
        <!-- 数量统计占比（右上） -->
        <div class="section ratio-chart">
          <h2 class="section-title">数量统计占比</h2>
          <RatioChart 
            :tabs="homeData.ratioChart.tabs"
            :active="homeData.ratioChart.active"
            :productTypes="homeData.ratioChart.productTypes"
            @changeTab="handleChangeTab"
          />
        </div>

        <!-- 快速开始（右中） -->
        <div class="section quick-start">
          <h2 class="section-title">快速开始 / 便捷导航</h2>
          <QuickStart 
            :items="homeData.quickStart.items"
            @click="handleQuickStartClick"
          />
        </div>

        <!-- 近期动态（右下） -->
        <div class="section recent-activities">
          <h2 class="section-title">近期动态</h2>
          <RecentActivities 
            :activities="homeData.recentActivities"
            @open="handleOpenLink"
            @viewMore="handleViewMore"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 导入数据和组件
import { useHomeMock } from '@/composables/useHomeMock'
import { useRouter } from 'vue-router'

// 导入组件
import TopNav from '@/components/dashboard/TopNav.vue'
import AppOverview from '@/components/dashboard/AppOverview.vue'
import MyFavorites from '@/components/dashboard/MyFavorites.vue'
import RatioChart from '@/components/dashboard/RatioChart.vue'
import QuickStart from '@/components/dashboard/QuickStart.vue'
import RecentActivities from '@/components/dashboard/RecentActivities.vue'
import SummaryTotals from '@/components/dashboard/SummaryTotals.vue'

// 导入样式令牌 - 已在style标签中引入

const homeData = useHomeMock()
const router = useRouter()

// 事件处理函数
const handleNavigate = (menu: string) => {
  console.log('Navigate to:', menu)
  // 这里可以添加路由导航逻辑
}

const handleSelectItem = (item: any) => {
  console.log('Select item:', item)
}

const handleSearch = (keyword: string) => {
  console.log('Search:', keyword)
}

const handleChangeRange = (range: string) => {
  console.log('Change range:', range)
}

const handleChangeTab = (tab: string) => {
  console.log('Change tab:', tab)
}

const handleQuickStartClick = (item: any) => {
  console.log('Quick start click:', item)
  if (item.route) {
    router.push(item.route)
  }
}

const handleOpenLink = (link: string) => {
  console.log('Open link:', link)
  if (link) {
    router.push(link)
  }
}

const handleViewMore = () => {
  console.log('View more activities')
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/dashboard/_tokens.scss' as tokens;

.dashboard-home {
  min-height: 100vh;
  background-color: tokens.$bg-page;
  padding: tokens.$spacing-lg;
  font-family: tokens.$font-family;
}

.dashboard-container {
  max-width: tokens.$page-width;
  margin: 0 auto;
  display: flex;
  gap: tokens.$content-gap;
  min-height: calc(100vh - #{tokens.$nav-height + tokens.$spacing-lg * 2});
}

// 左列布局 (约 1100px)
.left-column {
  flex: 0 0 tokens.$left-column-width;
  display: flex;
  flex-direction: column;
  gap: tokens.$section-gap;
}

// 右列布局 (约 552px)  
.right-column {
  flex: 0 0 tokens.$right-column-width;
  display: flex;
  flex-direction: column;
  gap: tokens.$section-gap;
}

// 通用区块样式
.section {
  @include tokens.card-base();
  padding: tokens.$card-padding;
}

// 区块标题样式
.section-title {
  @include tokens.section-title();
}

// 特殊区域高度调整
.app-overview {
  min-height: 280px;
}

.my-favorites {
  min-height: 400px;
  flex: 1;
}

.ratio-chart {
  min-height: 400px;
}

.quick-start {
  min-height: 200px;
}

.recent-activities {
  min-height: 300px;
  flex: 1;
}

.summary-totals {
  min-height: 160px;
}

// 响应式调整（针对较小屏幕）
@media (max-width: 1500px) {
  .dashboard-container {
    max-width: 1200px;
  }
  
  .left-column {
    flex: 0 0 calc(700px - #{tokens.$spacing-lg});
  }
  
  .right-column {
    flex: 0 0 calc(480px - #{tokens.$spacing-lg});
  }
}

@media (max-width: 1200px) {
  .dashboard-container {
    flex-direction: column;
  }
  
  .left-column,
  .right-column {
    flex: 1;
    width: 100%;
  }
}
</style>