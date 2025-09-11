/**
 * 工作台Mock数据服务
 * 提供测试和开发环境使用的模拟数据
 */

export interface SummaryData {
  productCount: number
  documentCount: number
  ruleCount: number
  errorCount: number
}

export interface FocusTask {
  id: string
  name: string
  submitter: string
  submitTime: string
  type: string
  status: string
  icon: string
}

export interface RecentActivity {
  id: string
  type: 'upload' | 'rule_update' | 'rule_approve' | 'rule_config'
  user: string
  content: string
  productName?: string
  productId?: string
  ruleCount?: number
  time: string
  relativeTime: string
}

export interface OverviewData {
  cumulative: {
    totalProducts: number
    totalRules: number
    errorDetectionRate: string
    avgAuditTime: string
  }
  recent: {
    products7Days: number
    products30Days: number
    rules7Days: number
    rules30Days: number
    productGrowthRate: string
    ruleGrowthRate: string
  }
}

export interface StatisticsData {
  chartData: Array<{
    name: string
    value: number
    percentage: string
    color: string
  }>
  total: number
}

export class DashboardMockService {
  
  /**
   * 获取汇总数据Mock
   */
  getSummaryData(timeRange: string = 'total'): Promise<SummaryData> {
    const mockData: Record<string, SummaryData> = {
      total: { 
        productCount: 156, 
        documentCount: 1247, 
        ruleCount: 89, 
        errorCount: 23 
      },
      today: { 
        productCount: 5, 
        documentCount: 42, 
        ruleCount: 3, 
        errorCount: 1 
      },
      week: { 
        productCount: 23, 
        documentCount: 187, 
        ruleCount: 12, 
        errorCount: 7 
      },
      month: { 
        productCount: 67, 
        documentCount: 523, 
        ruleCount: 28, 
        errorCount: 15 
      }
    }
    
    return new Promise(resolve => {
      setTimeout(() => {
        resolve(mockData[timeRange] || mockData.total)
      }, 300) // 模拟网络延迟
    })
  }
  
  /**
   * 获取应用概要Mock数据
   */
  getOverviewData(): Promise<OverviewData> {
    const mockData: OverviewData = {
      cumulative: {
        totalProducts: 156,
        totalRules: 89,
        errorDetectionRate: "85.6%",
        avgAuditTime: "00:02:27"
      },
      recent: {
        products7Days: 23,
        products30Days: 67,
        rules7Days: 12,
        rules30Days: 28,
        productGrowthRate: "+12.5%",
        ruleGrowthRate: "+8.3%"
      }
    }
    
    return new Promise(resolve => {
      setTimeout(() => {
        resolve(mockData)
      }, 200)
    })
  }
  
  /**
   * 获取关注任务Mock数据（支持搜索筛选）
   */
  getFocusTasks(params: { 
    keyword?: string
    timeFilter?: string
    typeFilter?: string
    page?: number
    size?: number
  } = {}): Promise<{
    records: FocusTask[]
    total: number
    current: number
    size: number
  }> {
    let tasks: FocusTask[] = [
      {
        id: "1",
        name: "财产保险产品A检核任务",
        submitter: "胡潇禹",
        submitTime: "2022/10/31",
        type: "产品检核",
        status: "进行中",
        icon: "file-text"
      },
      {
        id: "2",
        name: "意外险产品B规则配置",
        submitter: "张三",
        submitTime: "2022/10/30",
        type: "规则配置", 
        status: "待审核",
        icon: "setting"
      },
      {
        id: "3",
        name: "车险产品C合规检查",
        submitter: "李四",
        submitTime: "2022/10/29",
        type: "合规检查",
        status: "已完成",
        icon: "check-circle"
      },
      {
        id: "4",
        name: "健康险产品D风险评估",
        submitter: "王五",
        submitTime: "2022/10/28",
        type: "风险评估",
        status: "进行中",
        icon: "safety"
      },
      {
        id: "5",
        name: "农业险产品E条款审核",
        submitter: "赵六",
        submitTime: "2022/10/27",
        type: "条款审核",
        status: "待审核",
        icon: "audit"
      },
      {
        id: "6",
        name: "责任险产品F费率计算",
        submitter: "孙七",
        submitTime: "2022/10/26",
        type: "费率计算",
        status: "已完成",
        icon: "calculator"
      }
    ]
    
    // 模拟搜索筛选
    if (params.keyword) {
      tasks = tasks.filter(task => 
        task.name.includes(params.keyword!) || 
        task.submitter.includes(params.keyword!)
      )
    }
    
    if (params.typeFilter && params.typeFilter !== '全部任务') {
      tasks = tasks.filter(task => task.type === params.typeFilter)
    }
    
    if (params.timeFilter && params.timeFilter !== '全部时间') {
      // 这里可以添加时间筛选逻辑
      const now = new Date()
      const filterDate = new Date()
      
      switch (params.timeFilter) {
        case '本周':
          filterDate.setDate(now.getDate() - 7)
          break
        case '本月':
          filterDate.setMonth(now.getMonth() - 1)
          break
      }
      
      tasks = tasks.filter(task => {
        const taskDate = new Date(task.submitTime.replace(/\//g, '-'))
        return taskDate >= filterDate
      })
    }
    
    // 分页处理
    const page = params.page || 1
    const size = params.size || 10
    const start = (page - 1) * size
    const end = start + size
    const paginatedTasks = tasks.slice(start, end)
    
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          records: paginatedTasks,
          total: tasks.length,
          current: page,
          size: size
        })
      }, 400)
    })
  }
  
  /**
   * 获取近期动态Mock数据
   */
  getRecentActivities(): Promise<{
    activities: RecentActivity[]
  }> {
    const activities: RecentActivity[] = [
      {
        id: "1",
        type: "upload",
        user: "胡潇禹", 
        content: "上传了",
        productName: "财产保险产品A",
        productId: "prod_001",
        time: "2022-10-31 14:30:00",
        relativeTime: "14分钟前"
      },
      {
        id: "2",
        type: "rule_update",
        user: "张三",
        content: "更新了3条规则",
        ruleCount: 3,
        time: "2022-10-31 14:10:00",
        relativeTime: "20分钟前"
      },
      {
        id: "3",
        type: "rule_approve", 
        user: "李四",
        content: "审核通过了5条规则",
        ruleCount: 5,
        time: "2022-10-31 13:50:00",
        relativeTime: "40分钟前"
      },
      {
        id: "4",
        type: "upload",
        user: "王五",
        content: "上传了",
        productName: "意外险产品B",
        productId: "prod_002",
        time: "2022-10-31 13:30:00",
        relativeTime: "68分钟前"
      },
      {
        id: "5",
        type: "rule_config",
        user: "赵六",
        content: "配置了2条规则",
        ruleCount: 2,
        time: "2022-10-31 11:30:00",
        relativeTime: "3小时前"
      },
      {
        id: "6",
        type: "rule_update",
        user: "孙七",
        content: "更新了1条规则",
        ruleCount: 1,
        time: "2022-10-31 10:15:00",
        relativeTime: "4小时前"
      }
    ]
    
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({ activities })
      }, 250)
    })
  }
  
  /**
   * 获取统计图表Mock数据
   */
  getStatisticsData(type: string = 'productType'): Promise<StatisticsData> {
    const mockData: Record<string, StatisticsData> = {
      productType: {
        chartData: [
          { name: "种植险", value: 45, percentage: "28.8%", color: "#1890ff" },
          { name: "意健险", value: 32, percentage: "20.5%", color: "#52c41a" },
          { name: "车险", value: 28, percentage: "17.9%", color: "#faad14" },
          { name: "家财险", value: 25, percentage: "16.0%", color: "#f5222d" },
          { name: "养殖险", value: 18, percentage: "11.5%", color: "#722ed1" },
          { name: "其他", value: 8, percentage: "5.1%", color: "#13c2c2" }
        ],
        total: 156
      },
      department: {
        chartData: [
          { name: "产品部", value: 68, percentage: "43.6%", color: "#1890ff" },
          { name: "核保部", value: 42, percentage: "26.9%", color: "#52c41a" },
          { name: "理赔部", value: 28, percentage: "17.9%", color: "#faad14" },
          { name: "其他部门", value: 18, percentage: "11.5%", color: "#f5222d" }
        ],
        total: 156
      },
      ruleType: {
        chartData: [
          { name: "基础规则", value: 35, percentage: "39.3%", color: "#1890ff" },
          { name: "业务规则", value: 28, percentage: "31.5%", color: "#52c41a" },
          { name: "合规规则", value: 16, percentage: "18.0%", color: "#faad14" },
          { name: "自定义规则", value: 10, percentage: "11.2%", color: "#f5222d" }
        ],
        total: 89
      }
    }
    
    return new Promise(resolve => {
      setTimeout(() => {
        resolve(mockData[type] || mockData.productType)
      }, 350)
    })
  }
  
  /**
   * 模拟网络错误
   */
  simulateNetworkError(): Promise<never> {
    return new Promise((_, reject) => {
      setTimeout(() => {
        reject(new Error('Network Error'))
      }, 1000)
    })
  }
  
  /**
   * 模拟服务器错误
   */
  simulateServerError(): Promise<never> {
    return new Promise((_, reject) => {
      setTimeout(() => {
        reject(new Error('Internal Server Error'))
      }, 500)
    })
  }
}

// 导出Mock服务实例
export const dashboardMockService = new DashboardMockService()