/**
 * 检核模块Mock数据服务
 * 提供符合原型页面的完整检核功能数据支持
 */

export interface AuditTask {
  id: string
  productIds: string[]
  ruleIds: string[]
  status: 'pending' | 'running' | 'completed' | 'failed'
  createdBy: string
  createdTime: Date
  completedTime?: Date
  totalProducts: number
  completedProducts: number
  totalErrors: number
  progressPercent: number
}

export interface AuditRule {
  id: string
  code: string // CC001-CC017
  name: string
  source: string
  applicableInsuranceType: string[]
  applicableChapter: string
  managementDepartment: string
  ruleType: 'single' | 'double' | 'format' | 'advanced'
  effectiveStatus: 'active' | 'inactive'
  auditStatus: 'approved' | 'pending' | 'rejected'
  effectiveStartTime: Date
  effectiveEndTime?: Date
  description: string
}

export interface AuditResult {
  id: string
  taskId: string
  productId: string
  documentType: 'terms' | 'feasibility' | 'comparison'
  ruleId: string
  ruleCode: string
  suggestion: string
  applicableChapter: string
  ruleSource: string
  managementDepartment: string
  ruleType: string
  originalContent: string
  pageNumber: string
  highlightPosition?: {
    start: number
    end: number
    page: number
  }
  severity: 'high' | 'medium' | 'low'
  createdTime: Date
}

export interface AuditStatistics {
  id: string
  taskId: string
  documentType: 'terms' | 'feasibility' | 'comparison'
  ruleTypeStats: {
    single: number
    double: number
    format: number
    advanced: number
  }
  totalCount: number
}

export interface AuditHistory {
  id: string
  taskId: string
  productId: string
  productName: string
  executionTime: Date
  totalErrors: number
  status: 'completed' | 'failed'
  reportDownloadUrl?: string
}

export class AuditMockService {

  /**
   * 创建检核任务Mock
   */
  createAuditTask(productIds: string[], ruleIds: string[]): Promise<{ taskId: string }> {
    const taskId = `task_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

    return new Promise(resolve => {
      setTimeout(() => {
        resolve({ taskId })
      }, 500) // 模拟创建任务的网络延迟
    })
  }

  /**
   * 获取检核任务进度Mock
   */
  getTaskProgress(taskId: string): Promise<AuditTask> {
    // 模拟进度变化
    const mockProgress = Math.min(100, Math.floor(Math.random() * 100) + 10)
    const totalProducts = 10
    const completedProducts = Math.floor((mockProgress / 100) * totalProducts)

    const mockTask: AuditTask = {
      id: taskId,
      productIds: ['prod_001', 'prod_002', 'prod_003'],
      ruleIds: ['rule_cc001', 'rule_cc002', 'rule_cc003'],
      status: mockProgress < 100 ? 'running' : 'completed',
      createdBy: 'Jay.Liu',
      createdTime: new Date(Date.now() - 300000), // 5分钟前
      completedTime: mockProgress === 100 ? new Date() : undefined,
      totalProducts,
      completedProducts,
      totalErrors: Math.floor(Math.random() * 50) + 10, // 随机10-60个错误
      progressPercent: mockProgress
    }

    return new Promise(resolve => {
      setTimeout(() => {
        resolve(mockTask)
      }, 200)
    })
  }

  /**
   * 模拟启动检核任务
   */
  startAuditTask(taskId: string): Promise<{ success: boolean }> {
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({ success: true })
      }, 300)
    })
  }

  /**
   * 模拟停止检核任务
   */
  stopAuditTask(taskId: string): Promise<{ success: boolean }> {
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({ success: true })
      }, 200)
    })
  }

  /**
   * 模拟重新开始检核任务
   */
  restartAuditTask(taskId: string): Promise<{ taskId: string }> {
    const newTaskId = `task_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

    return new Promise(resolve => {
      setTimeout(() => {
        resolve({ taskId: newTaskId })
      }, 400)
    })
  }

  /**
   * 导出检核结果Mock
   */
  exportAuditResults(taskId: string, format: 'excel' | 'pdf' = 'excel'): Promise<{
    downloadUrl: string
    fileName: string
  }> {
    const timestamp = new Date().toISOString().slice(0, 19).replace(/[-:T]/g, '')
    const fileName = `audit_results_${taskId}_${timestamp}.${format === 'excel' ? 'xlsx' : 'pdf'}`

    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          downloadUrl: `/api/mock/downloads/${fileName}`,
          fileName
        })
      }, 1000) // 模拟文件生成时间
    })
  }

  /**
   * 模拟网络错误
   */
  simulateNetworkError(): Promise<never> {
    return new Promise((_, reject) => {
      setTimeout(() => {
        reject(new Error('网络连接失败'))
      }, 1000)
    })
  }

  /**
   * 模拟服务器错误
   */
  simulateServerError(): Promise<never> {
    return new Promise((_, reject) => {
      setTimeout(() => {
        reject(new Error('服务器内部错误'))
      }, 500)
    })
  }
}

// 导出Mock服务实例
export const auditMockService = new AuditMockService()