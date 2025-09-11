/**
 * 工作台Mock数据服务测试
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { DashboardMockService } from '../../api/mock/dashboardMock'
import type { TimeFilterType, FocusTaskParams, StatisticsType } from '../../api/types/dashboard'

describe('DashboardMockService', () => {
  beforeEach(() => {
    vi.clearAllTimers()
    vi.useFakeTimers()
  })

  describe('getSummaryData', () => {
    it('should return summary data for total filter', async () => {
      const promise = DashboardMockService.getSummaryData('total')
      vi.advanceTimersByTime(500)
      
      const result = await promise
      
      expect(result).toHaveProperty('products')
      expect(result).toHaveProperty('documents')
      expect(result).toHaveProperty('rules')
      expect(result).toHaveProperty('errors')
      expect(typeof result.products).toBe('number')
      expect(result.products).toBeGreaterThan(0)
    })

    it('should return different data for different time filters', async () => {
      const filters: TimeFilterType[] = ['total', 'today', 'week', 'month']
      const results = []
      
      for (const filter of filters) {
        const promise = DashboardMockService.getSummaryData(filter)
        vi.advanceTimersByTime(500)
        results.push(await promise)
      }
      
      // Today should have less data than total
      expect(results[1].products).toBeLessThanOrEqual(results[0].products)
      expect(results[1].documents).toBeLessThanOrEqual(results[0].documents)
    })

    it('should have realistic delay', async () => {
      const startTime = Date.now()
      const promise = DashboardMockService.getSummaryData('total')
      
      // Should not resolve immediately
      let resolved = false
      promise.then(() => { resolved = true })
      expect(resolved).toBe(false)
      
      // Should resolve after advancing timers
      vi.advanceTimersByTime(500)
      await promise
      expect(resolved).toBe(true)
    })
  })

  describe('getOverviewData', () => {
    it('should return overview data with correct structure', async () => {
      const promise = DashboardMockService.getOverviewData()
      vi.advanceTimersByTime(300)
      
      const result = await promise
      
      expect(result).toHaveProperty('cumulative')
      expect(result).toHaveProperty('recent')
      expect(result.cumulative).toHaveProperty('totalProducts')
      expect(result.cumulative).toHaveProperty('totalRules')
      expect(result.cumulative).toHaveProperty('errorDetectionRate')
      expect(result.cumulative).toHaveProperty('avgAuditTime')
      expect(result.recent).toHaveProperty('products7Days')
      expect(result.recent).toHaveProperty('productGrowthRate')
    })

    it('should return percentage format for rates', async () => {
      const promise = DashboardMockService.getOverviewData()
      vi.advanceTimersByTime(300)
      
      const result = await promise
      
      expect(result.cumulative.errorDetectionRate).toMatch(/^\d+\.\d+%$/)
      expect(result.recent.productGrowthRate).toMatch(/^[+-]\d+\.\d+%$/)
      expect(result.recent.ruleGrowthRate).toMatch(/^[+-]\d+\.\d+%$/)
    })

    it('should return time format for avgAuditTime', async () => {
      const promise = DashboardMockService.getOverviewData()
      vi.advanceTimersByTime(300)
      
      const result = await promise
      
      expect(result.cumulative.avgAuditTime).toMatch(/^\d{2}:\d{2}:\d{2}$/)
    })
  })

  describe('getFocusTasks', () => {
    it('should return tasks with correct structure', async () => {
      const promise = DashboardMockService.getFocusTasks()
      vi.advanceTimersByTime(500)
      
      const result = await promise
      
      expect(result).toHaveProperty('records')
      expect(result).toHaveProperty('total')
      expect(result).toHaveProperty('current')
      expect(result).toHaveProperty('size')
      expect(Array.isArray(result.records)).toBe(true)
      
      if (result.records.length > 0) {
        const task = result.records[0]
        expect(task).toHaveProperty('id')
        expect(task).toHaveProperty('name')
        expect(task).toHaveProperty('submitter')
        expect(task).toHaveProperty('submitTime')
        expect(task).toHaveProperty('type')
        expect(task).toHaveProperty('status')
        expect(task).toHaveProperty('icon')
      }
    })

    it('should filter tasks by keyword', async () => {
      const params: FocusTaskParams = { keyword: '财产' }
      const promise = DashboardMockService.getFocusTasks(params)
      vi.advanceTimersByTime(500)
      
      const result = await promise
      
      result.records.forEach(task => {
        expect(task.name.includes('财产') || task.submitter.includes('财产')).toBe(true)
      })
    })

    it('should filter tasks by type', async () => {
      const params: FocusTaskParams = { typeFilter: '产品检核' }
      const promise = DashboardMockService.getFocusTasks(params)
      vi.advanceTimersByTime(500)
      
      const result = await promise
      
      result.records.forEach(task => {
        expect(task.type).toBe('产品检核')
      })
    })

    it('should support pagination', async () => {
      const params: FocusTaskParams = { page: 1, size: 5 }
      const promise = DashboardMockService.getFocusTasks(params)
      vi.advanceTimersByTime(500)
      
      const result = await promise
      
      expect(result.current).toBe(1)
      expect(result.size).toBe(5)
      expect(result.records.length).toBeLessThanOrEqual(5)
    })
  })

  describe('getRecentActivities', () => {
    it('should return activities with correct structure', async () => {
      const promise = DashboardMockService.getRecentActivities(5)
      vi.advanceTimersByTime(400)
      
      const result = await promise
      
      expect(result).toHaveProperty('activities')
      expect(result).toHaveProperty('total')
      expect(result).toHaveProperty('hasMore')
      expect(Array.isArray(result.activities)).toBe(true)
      expect(result.activities.length).toBeLessThanOrEqual(5)
      
      if (result.activities.length > 0) {
        const activity = result.activities[0]
        expect(activity).toHaveProperty('id')
        expect(activity).toHaveProperty('type')
        expect(activity).toHaveProperty('user')
        expect(activity).toHaveProperty('content')
        expect(activity).toHaveProperty('time')
        expect(activity).toHaveProperty('timestamp')
        expect(activity).toHaveProperty('relativeTime')
      }
    })

    it('should return activities sorted by time descending', async () => {
      const promise = DashboardMockService.getRecentActivities(10)
      vi.advanceTimersByTime(400)
      
      const result = await promise
      
      for (let i = 1; i < result.activities.length; i++) {
        expect(result.activities[i-1].timestamp).toBeGreaterThanOrEqual(
          result.activities[i].timestamp
        )
      }
    })

    it('should format relative time correctly', async () => {
      const promise = DashboardMockService.getRecentActivities(5)
      vi.advanceTimersByTime(400)
      
      const result = await promise
      
      result.activities.forEach(activity => {
        expect(activity.relativeTime).toMatch(/(秒前|分钟前|小时前|天前)$/)
      })
    })
  })

  describe('getStatisticsData', () => {
    it('should return statistics data with correct structure', async () => {
      const promise = DashboardMockService.getStatisticsData('productType')
      vi.advanceTimersByTime(300)
      
      const result = await promise
      
      expect(result).toHaveProperty('chartData')
      expect(result).toHaveProperty('total')
      expect(Array.isArray(result.chartData)).toBe(true)
      
      if (result.chartData.length > 0) {
        const item = result.chartData[0]
        expect(item).toHaveProperty('name')
        expect(item).toHaveProperty('value')
        expect(item).toHaveProperty('percentage')
        expect(item).toHaveProperty('color')
        expect(item.percentage).toMatch(/^\d+\.\d+%$/)
      }
    })

    it('should return different data for different types', async () => {
      const types: StatisticsType[] = ['productType', 'department', 'ruleType']
      const results = []
      
      for (const type of types) {
        const promise = DashboardMockService.getStatisticsData(type)
        vi.advanceTimersByTime(300)
        results.push(await promise)
      }
      
      // Should have different chart data
      expect(results[0].chartData[0].name).not.toBe(results[1].chartData[0].name)
    })

    it('should have percentages that sum to 100%', async () => {
      const promise = DashboardMockService.getStatisticsData('productType')
      vi.advanceTimersByTime(300)
      
      const result = await promise
      
      const totalPercentage = result.chartData.reduce((sum, item) => {
        return sum + parseFloat(item.percentage.replace('%', ''))
      }, 0)
      
      expect(Math.abs(totalPercentage - 100)).toBeLessThan(0.2) // Allow small rounding errors
    })
  })

  describe('utility methods', () => {
    it('should return task type options', async () => {
      const result = await DashboardMockService.getTaskTypeOptions()
      
      expect(Array.isArray(result)).toBe(true)
      expect(result).toContain('全部任务')
      expect(result).toContain('产品检核')
      expect(result).toContain('规则配置')
    })

    it('should return time filter options', async () => {
      const result = await DashboardMockService.getTimeFilterOptions()
      
      expect(Array.isArray(result)).toBe(true)
      expect(result).toContain('全部时间')
      expect(result).toContain('本周')
      expect(result).toContain('本月')
    })

    it('should return statistics type options', async () => {
      const result = await DashboardMockService.getStatisticsTypeOptions()
      
      expect(Array.isArray(result)).toBe(true)
      expect(result[0]).toHaveProperty('key')
      expect(result[0]).toHaveProperty('label')
    })

    it('should simulate network errors', () => {
      // Test with 100% error rate
      expect(DashboardMockService.simulateNetworkError(1)).toBe(true)
      
      // Test with 0% error rate
      expect(DashboardMockService.simulateNetworkError(0)).toBe(false)
    })
  })

  describe('getBatchDashboardData', () => {
    it('should return batch data with all components', async () => {
      const promise = DashboardMockService.getBatchDashboardData('total')
      vi.advanceTimersByTime(1000)
      
      const result = await promise
      
      expect(result).toHaveProperty('summary')
      expect(result).toHaveProperty('overview')
      expect(result).toHaveProperty('activities')
      expect(result.summary).toHaveProperty('products')
      expect(result.overview).toHaveProperty('cumulative')
      expect(result.activities).toHaveProperty('activities')
    })
  })
})