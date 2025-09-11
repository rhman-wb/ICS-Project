# Dashboard Components

This directory contains components for the dashboard/workbench page.

## SummaryDataSection.vue

### Overview
The SummaryDataSection component displays key metrics and statistics for the insurance product audit system. It provides a responsive card layout with time-based filtering capabilities.

### Features
- **Responsive Layout**: Uses Ant Design's grid system (xl:6, lg:6, md:12, sm:24)
- **Time Filtering**: Supports filtering by Total, Today, This Week, This Month
- **Animated Data Updates**: Smooth number animations when switching time filters
- **Loading States**: Shows loading indicators during data fetching
- **Mock Data Integration**: Uses centralized mock data service

### Props
This component doesn't accept any props.

### Data Structure
```typescript
interface SummaryDataItem {
  key: string      // Unique identifier (products, documents, rules, errors)
  label: string    // Display label in Chinese
  value: number    // Numeric value to display
}
```

### Time Filters
- `total`: 总计 - All time data
- `today`: 本日 - Today's data
- `week`: 本周 - This week's data  
- `month`: 本月 - This month's data

### Styling
- **Card Values**: 28px font size, bold weight, color-coded by type
- **Card Labels**: 14px font size, secondary text color
- **Animations**: Fade-in animation for cards, smooth number transitions
- **Responsive**: Adapts to different screen sizes with appropriate font scaling

### Usage
```vue
<template>
  <div>
    <SummaryDataSection />
  </div>
</template>

<script setup>
import SummaryDataSection from '@/components/dashboard/SummaryDataSection.vue'
</script>
```

### Mock Data
The component uses `DashboardMockService.getSummaryData()` to fetch data. The mock service provides realistic data for different time periods:

- **Products**: Number of insurance products
- **Documents**: Number of requirement documents  
- **Rules**: Number of audit rules
- **Errors**: Number of detected audit errors

### Testing
The component includes comprehensive unit tests covering:
- Component rendering
- Initial state validation
- Data structure verification
- Number formatting
- Time filter functionality

Run tests with:
```bash
npm run test:run -- src/test/components/SummaryDataSection.test.ts
```

### Requirements Fulfilled
This component fulfills requirements 9.1-9.7 from the specification:
- ✅ 9.1: Responsive card layout with proper grid system
- ✅ 9.2: Four key metrics display (products, documents, rules, errors)
- ✅ 9.3: Large font (28px) for values, small font (14px) for labels
- ✅ 9.4: Time filtering with tabs (Total, Today, Week, Month)
- ✅ 9.5: Mock data service for different time dimensions
- ✅ 9.6: Data switching animations and loading states
- ✅ 9.7: Professional styling following UI design standards
## MyFoc
usSection.vue

### Overview
The MyFocusSection component displays a list of tasks that the user is following or focusing on. It provides an interactive task list with detailed information and status tracking.

### Features
- **Interactive Task List**: Clickable task items with hover effects
- **Task Information Display**: Shows task name, submitter, submission time, and status
- **Icon Support**: Different icons for different task types
- **Status Tags**: Color-coded status indicators
- **Virtual Scrolling**: Supports virtual scrolling for large lists (>20 items)
- **Load More**: Pagination support with "Load More" functionality
- **Empty State**: Friendly empty state when no tasks are available
- **Responsive Design**: Adapts to different screen sizes

### Props
This component doesn't accept any props.

### Data Structure
```typescript
interface FocusTask {
  id: string          // Unique task identifier
  name: string        // Task name/title
  submitter: string   // Person who submitted the task
  submitTime: string  // Submission time in YYYY/MM/DD format
  type: string        // Task type (产品检核, 规则配置, etc.)
  status: string      // Task status (进行中, 待审核, etc.)
  icon: string        // Icon identifier
}
```

### Task Types
- `产品检核`: Product audit tasks
- `规则配置`: Rule configuration tasks
- `合规检查`: Compliance check tasks
- `风险评估`: Risk assessment tasks
- `条款审核`: Terms review tasks
- `费率核算`: Rate calculation tasks

### Task Status
- `进行中`: In progress (processing color)
- `待审核`: Pending review (warning color)
- `已完成`: Completed (success color)
- `已拒绝`: Rejected (error color)
- `已暂停`: Paused (default color)

### Icon Mapping
- `file-text`: FileTextOutlined
- `setting`: SettingOutlined
- `check-circle`: CheckCircleOutlined
- `clock-circle`: ClockCircleOutlined
- `exclamation-circle`: ExclamationCircleOutlined

### Styling
- **Task Items**: Hover effects with background color change
- **Task Names**: Clickable links with primary color
- **Meta Information**: Secondary text color for submitter and time
- **Status Tags**: Color-coded based on status type
- **Responsive**: Single column layout on mobile devices

### Usage
```vue
<template>
  <div>
    <MyFocusSection />
  </div>
</template>

<script setup>
import MyFocusSection from '@/components/dashboard/MyFocusSection.vue'
</script>
```

### Mock Data
The component uses `DashboardMockService.getFocusTasks()` to fetch data. The mock service provides:
- 10 different insurance product audit tasks
- Various task types and statuses
- Realistic submitter names and dates
- Support for search and filtering (extensible)

### Interactions
- **Task Click**: Currently shows a message with task name (placeholder for future navigation)
- **Load More**: Loads additional tasks when available
- **Virtual Scrolling**: Automatically enabled for lists with >20 items

### Testing
The component includes comprehensive unit tests covering:
- Component rendering and structure
- Task data loading and display
- Click interactions and event handling
- Empty state display
- Load more functionality
- Status color mapping
- Icon mapping and fallbacks

Run tests with:
```bash
npm run test:run -- src/test/components/MyFocusSection.test.ts
```

### Requirements Fulfilled
This component fulfills requirements 10.1-10.7 from the specification:
- ✅ 10.1: Task list display with proper structure
- ✅ 10.2: Clickable task names with link styling
- ✅ 10.3: Submitter and submission time display (YYYY/MM/DD format)
- ✅ 10.4: Icon identifiers for different task types
- ✅ 10.5: Task click navigation (placeholder implementation)
- ✅ 10.6: Mock data with various insurance product audit tasks
- ✅ 10.7: Scrolling support for long task lists with load more functionality

### Future Enhancements
- Integration with real task management API
- Task detail page navigation
- Search and filtering functionality
- Task priority indicators
- Real-time task status updates

## QuickStartSection.vue

### Overview
The QuickStartSection component provides quick access to commonly used functions in the insurance product audit system. It displays action buttons in a responsive grid layout with badge indicators for pending tasks.

### Features
- **Responsive Grid Layout**: 2x4 grid layout using Ant Design's grid system
- **Action Buttons**: Six main action buttons with icons and labels
- **Badge Indicators**: Numeric badges showing pending task counts
- **Hover Effects**: Smooth hover animations with visual feedback
- **Click Animations**: Button press animations for better user experience
- **Placeholder Support**: Reserved positions for future functionality expansion
- **Mobile Responsive**: Adapts to different screen sizes with appropriate scaling

### Props
This component doesn't accept any props.

### Action Buttons
1. **导入产品** (Import Product)
   - Icon: UploadOutlined
   - Badge: Shows pending imports count (24)
   - Action: Navigate to product import page

2. **创建规则** (Create Rule)
   - Icon: PlusOutlined
   - Badge: Shows pending rules count (8)
   - Action: Navigate to rule creation page

3. **批量导入** (Batch Import)
   - Icon: InboxOutlined
   - Badge: Shows batch tasks count (12)
   - Action: Navigate to batch import page

4. **下载模板** (Download Template)
   - Icon: DownloadOutlined
   - Badge: None
   - Action: Trigger template file download

5. **操作四** (Operation 4)
   - Icon: SettingOutlined
   - Badge: Shows other tasks count (5)
   - Action: Placeholder for future functionality

6. **操作五** (Operation 5)
   - Icon: AppstoreOutlined
   - Badge: None
   - Action: Placeholder for future functionality

### Badge Configuration
```typescript
const pendingImports = ref(24)  // Import tasks pending
const pendingRules = ref(8)     // Rules pending creation
const batchTasks = ref(12)      // Batch import tasks
const otherTasks = ref(5)       // Other miscellaneous tasks
```

### Styling
- **Button Size**: 80px height for desktop, scales down for mobile
- **Grid Layout**: 2x4 responsive grid with 16px gaps
- **Hover Effects**: 
  - Button lifts up (-2px transform)
  - Border color changes to primary blue
  - Background changes to light green
  - Icon scales up (1.1x) and color deepens
  - Box shadow appears
- **Click Animation**: Brief scale animation (0.98x) on press
- **Badge Style**: Red background (#f5222d), white text, positioned top-right

### Responsive Breakpoints
- **Desktop (≥768px)**: Full 80px height buttons, 20px icons, 14px text
- **Tablet (≥576px)**: 70px height buttons, 18px icons, 13px text
- **Mobile (<576px)**: 60px height buttons, 16px icons, 12px text

### Usage
```vue
<template>
  <div>
    <QuickStartSection />
  </div>
</template>

<script setup>
import QuickStartSection from '@/components/dashboard/QuickStartSection.vue'
</script>
```

### Event Handlers
All button clicks currently show message notifications as placeholders:

```typescript
const handleImportProduct = () => {
  message.info('跳转到产品导入页面')
  // TODO: router.push('/product/import')
}

const handleDownloadTemplate = () => {
  message.success('模板下载已开始')
  // TODO: downloadTemplate()
}
```

### Testing
The component includes comprehensive unit tests covering:
- Component rendering and structure
- Button text and icon display
- Badge count display
- Click event handling for all buttons
- Responsive layout structure
- Component naming and configuration

Run tests with:
```bash
npm run test:run -- src/test/components/QuickStartSection.test.ts
```

### Requirements Fulfilled
This component fulfills requirements 12.1-12.8 from the specification:
- ✅ 12.1: Quick start section display with proper title
- ✅ 12.2: 2x4 responsive grid layout using a-row and a-col
- ✅ 12.3: Four main action buttons (import product, create rule, batch import, download template)
- ✅ 12.4: Appropriate icons (UploadOutlined, PlusOutlined, InboxOutlined, DownloadOutlined)
- ✅ 12.5: Click navigation functionality (placeholder implementation with message notifications)
- ✅ 12.6: Numeric badges in top-right corner showing pending counts
- ✅ 12.7: Reserved expansion positions (操作四, 操作五) for future functionality
- ✅ 12.8: Hover effects and click feedback animations

### Future Enhancements
- Integration with real routing system for navigation
- Dynamic badge count updates from API
- Actual file download functionality
- Permission-based button visibility
- Customizable action button configuration
- Real-time task count updates

## ApplicationOverviewSection.vue

### Overview
The ApplicationOverviewSection component displays comprehensive application statistics in a 2x4 layout, showing both cumulative and recent statistics for the insurance product audit system. It provides detailed insights into system usage and performance metrics.

### Features
- **2x4 Grid Layout**: Two main sections (cumulative and recent) each with 4 statistical cards
- **Cumulative Statistics**: Long-term system metrics and performance indicators
- **Recent Statistics**: Short-term activity metrics with trend indicators
- **Trend Analysis**: Visual trend indicators with colored arrows and percentages
- **Time Format Display**: Specialized formatting for time-based metrics (HH:MM:SS)
- **Animated Data Updates**: Smooth number animations when data loads
- **Responsive Design**: Adapts to different screen sizes with appropriate scaling
- **Loading States**: Shows loading indicators during data fetching

### Props
This component doesn't accept any props.

### Data Structure
```typescript
interface OverviewData {
  cumulative: {
    totalProducts: number        // Total uploaded products
    totalRules: number          // Total imported rules
    errorDetectionRate: string  // Error detection rate percentage
    avgAuditTime: string        // Average audit time in HH:MM:SS format
  }
  recent: {
    products7Days: number       // Products uploaded in last 7 days
    products30Days: number      // Products uploaded in last 30 days
    rules7Days: number          // Rules imported in last 7 days
    rules30Days: number         // Rules imported in last 30 days
    productGrowthRate: string   // Product growth rate percentage
    ruleGrowthRate: string      // Rule growth rate percentage
  }
}
```

### Cumulative Statistics (Left Section)
1. **累计上传产品数量** (Total Uploaded Products)
   - Shows total number of products uploaded to the system
   - Large font display (20px, weight 600)

2. **累计导入规则数量** (Total Imported Rules)
   - Shows total number of audit rules imported
   - Large font display (20px, weight 600)

3. **错误检出率** (Error Detection Rate)
   - Shows system's error detection effectiveness as percentage
   - Format: "85.6%" style display

4. **平均检核时间** (Average Audit Time)
   - Shows average time taken for audit processes
   - Format: "HH:MM:SS" (e.g., "00:02:27")

### Recent Statistics (Right Section)
1. **近7日上传产品数量** (7-Day Product Uploads)
   - Shows products uploaded in the last 7 days
   - Includes trend indicator with growth percentage

2. **近30日上传产品数量** (30-Day Product Uploads)
   - Shows products uploaded in the last 30 days
   - Includes trend indicator with growth percentage

3. **近7日导入规则数量** (7-Day Rule Imports)
   - Shows rules imported in the last 7 days
   - Includes trend indicator with growth percentage

4. **近30日导入规则数量** (30-Day Rule Imports)
   - Shows rules imported in the last 30 days
   - Includes trend indicator with growth percentage

### Trend Indicators
- **Upward Trend**: Red arrow (ArrowUpOutlined) with red text (#f5222d)
- **Downward Trend**: Green arrow (ArrowDownOutlined) with green text (#52c41a)
- **Format**: Arrow icon + percentage (e.g., "↑ +12.5%", "↓ -3.2%")

### Styling
- **Card Values**: 20px font size, weight 600, primary text color
- **Card Labels**: 12px font size, secondary text color
- **Group Titles**: 16px font size, weight 500, with bottom border
- **Card Height**: 120px for desktop, scales down for mobile
- **Hover Effects**: Card lift animation (-2px transform) with enhanced shadow
- **Responsive**: Adapts card sizes and font sizes for different screen sizes

### Responsive Breakpoints
- **Desktop (≥1200px)**: Full 120px height cards, 20px values, 12px labels
- **Large Tablet (≥768px)**: 110px height cards, 18px values, 11px labels
- **Small Tablet (≥576px)**: 100px height cards, 16px values, 10px labels
- **Mobile (<576px)**: 90px height cards, 14px values, 9px labels

### Usage
```vue
<template>
  <div>
    <ApplicationOverviewSection />
  </div>
</template>

<script setup>
import ApplicationOverviewSection from '@/components/dashboard/ApplicationOverviewSection.vue'
</script>
```

### Mock Data
The component uses `DashboardMockService.getOverviewData()` to fetch data. The mock service provides:
- Realistic cumulative statistics for system usage
- Recent activity data with growth trends
- Properly formatted time strings (HH:MM:SS)
- Percentage-based growth indicators

### Animation Effects
- **Data Loading**: Smooth number animation using easeOutQuart easing function
- **Duration**: 800ms animation duration for value changes
- **String Data**: Non-numeric data (percentages, time) updates after animation completes

### Testing
The component includes comprehensive unit tests covering:
- Component rendering and structure
- Cumulative and recent statistics sections
- Trend indicator display and colors
- Data fetching and loading states
- Number formatting functionality
- Time format validation (HH:MM:SS pattern)
- Responsive design classes
- Label text accuracy

Run tests with:
```bash
npm run test:run -- src/test/components/ApplicationOverviewSection.test.ts
```

### Requirements Fulfilled
This component fulfills requirements 14.1-14.8 from the specification:
- ✅ 14.1: Application overview section with proper title
- ✅ 14.2: 2x4 layout using a-row and a-col for cumulative and recent statistics
- ✅ 14.3: Cumulative statistics (total products, total rules, error detection rate, avg audit time)
- ✅ 14.4: Recent statistics (7-day/30-day products and rules)
- ✅ 14.5: Large font (20px) for key numbers with weight 600
- ✅ 14.6: Trend indicators with percentage changes and arrows
- ✅ 14.7: Red arrows for upward trends, green arrows for downward trends
- ✅ 14.8: Average audit time in HH:MM:SS format (e.g., "00:02:27")

### Future Enhancements
- Integration with real analytics API
- Historical trend charts
- Drill-down functionality for detailed metrics
- Customizable time periods
- Export functionality for reports
- Real-time data updates
## Statist
icsChartSection.vue

### Overview
The StatisticsChartSection component displays statistical data in an interactive pie chart format with detailed type listings. It provides three different views through tab switching: product management departments, product types, and rule types. The component uses ECharts for professional chart visualization.

### Features
- **Interactive Pie Chart**: Professional donut-style pie chart using ECharts
- **Tab Navigation**: Three tabs for different data categories
- **Detailed Type List**: Color-coded list showing percentages and counts
- **Responsive Design**: Adapts to different screen sizes with mobile-friendly layout
- **Real-time Data Updates**: Smooth transitions when switching between tabs
- **Color Coordination**: Consistent color scheme between chart and legend
- **Total Summary**: Shows total count for each category

### Props
This component doesn't accept any props.

### Tab Categories
1. **产品管理部门** (Product Management Departments)
   - 产品部 (Product Department)
   - 核保部 (Underwriting Department)
   - 理赔部 (Claims Department)
   - 其他部门 (Other Departments)

2. **产品类型** (Product Types)
   - 种植险 (Crop Insurance)
   - 意健险 (Accident & Health Insurance)
   - 车险 (Auto Insurance)
   - 家财险 (Home Insurance)
   - 养殖险 (Livestock Insurance)
   - 其他 (Others)

3. **规则类型** (Rule Types)
   - 基础规则 (Basic Rules)
   - 业务规则 (Business Rules)
   - 合规规则 (Compliance Rules)
   - 自定义规则 (Custom Rules)

### Data Structure
```typescript
interface ChartDataItem {
  name: string        // Type name
  value: number       // Numeric value
  percentage: string  // Formatted percentage (e.g., "28.8%")
  color: string       // Hex color code for visualization
}

interface StatisticsData {
  chartData: ChartDataItem[]
  total: number       // Total count across all types
}
```

### Chart Configuration
- **Chart Type**: Donut pie chart (inner radius: 40%, outer radius: 70%)
- **Colors**: Predefined color palette for consistent visualization
- **Interactions**: Hover effects with emphasis and shadow
- **Responsive**: Automatically resizes based on container size
- **Animation**: Smooth transitions when data changes

### Color Palette
- Primary Blue: `#1890ff`
- Success Green: `#52c41a`
- Warning Orange: `#faad14`
- Error Red: `#f5222d`
- Purple: `#722ed1`
- Cyan: `#13c2c2`

### Styling
- **Chart Size**: 300x300px for desktop, scales down for mobile
- **Tab Style**: Ant Design tabs with custom styling
- **Type List**: Clean list layout with color dots and aligned statistics
- **Card Layout**: White background with shadow and rounded corners
- **Responsive**: Mobile-first design with appropriate breakpoints

### Responsive Breakpoints
- **Desktop (≥768px)**: Full 300x300px chart, side-by-side layout
- **Tablet (<768px)**: 280x280px chart, stacked layout
- **Mobile (<480px)**: 240x240px chart, compact layout

### Usage
```vue
<template>
  <div>
    <StatisticsChartSection />
  </div>
</template>

<script setup>
import StatisticsChartSection from '@/components/dashboard/StatisticsChartSection.vue'
</script>
```

### Dependencies
- **ECharts**: `echarts` package for chart visualization
- **Ant Design Vue**: `a-tabs` component for tab navigation
- **Mock Data**: `DashboardMockService.getStatisticsData()` for data fetching

### Mock Data
The component uses `DashboardMockService.getStatisticsData()` with different type parameters:
- `productType`: Product type distribution data
- `department`: Department distribution data  
- `ruleType`: Rule type distribution data

Each dataset includes:
- Realistic percentage distributions that sum to 100%
- Appropriate color assignments for visual distinction
- Meaningful category names in Chinese
- Proper total counts for each category

### Event Handling
- **Tab Change**: Automatically loads new data when switching tabs
- **Chart Resize**: Responds to window resize events
- **Hover Effects**: Interactive chart elements with tooltips
- **Loading States**: Shows loading indicators during data fetching

### Testing
The component includes comprehensive unit tests covering:
- Component rendering and structure
- Tab navigation functionality
- Chart container and type list display
- Data loading and error handling
- Color dot and percentage display
- Responsive design behavior
- ECharts integration (mocked)

Run tests with:
```bash
npm run test:run -- src/test/components/StatisticsChartSection.test.ts
```

### Requirements Fulfilled
This component fulfills requirements 15.1-15.8 from the specification:
- ✅ 15.1: Statistics chart section with proper title "数量统计占比"
- ✅ 15.2: Three tabs (产品管理部门, 产品类型, 规则类型) using a-tabs
- ✅ 15.3: ECharts integration for professional pie chart visualization
- ✅ 15.4: Product types including 种植险, 意健险, 车险, 家财险, 养殖险, 其他
- ✅ 15.5: Type list with color dots, type names, percentages, and counts
- ✅ 15.6: Percentage totals sum to 100% with distinct colors
- ✅ 15.7: Dynamic chart and list updates when switching tabs
- ✅ 15.8: Mock data service providing various statistical distributions

### Chart Features
- **Tooltip**: Shows detailed information on hover
- **Legend**: Custom legend implementation using type list
- **Animation**: Smooth transitions between data sets
- **Accessibility**: Proper ARIA labels and keyboard navigation support
- **Performance**: Efficient rendering with proper cleanup

### Future Enhancements
- Integration with real analytics API
- Export functionality (PNG, SVG, PDF)
- Drill-down capabilities for detailed analysis
- Custom color theme support
- Additional chart types (bar, line charts)
- Data filtering and search functionality
- Historical data comparison
- Real-time data updates