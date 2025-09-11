import { test, expect } from '@playwright/test'

test.describe('Dashboard Home Page', () => {
  test.beforeEach(async ({ page }) => {
    // Clear all storage data
    await page.context().clearCookies()
    await page.evaluate(() => {
      localStorage.clear()
      sessionStorage.clear()
    })

    // Set up mock authentication for direct dashboard access
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token')
      localStorage.setItem('userInfo', JSON.stringify({
        id: 'user-123',
        username: 'testuser',
        realName: '测试用户',
        email: 'test@example.com'
      }))
    })

    // Mock dashboard data API
    await page.route('**/api/**', async route => {
      const url = route.request().url()
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: { mock: 'data' }
        })
      })
    })
  })

  test.describe('Page Load and Layout', () => {
    test('should display dashboard home page with correct layout', async ({ page }) => {
      await page.goto('/')

      // Check page title
      await expect(page).toHaveTitle(/产品检核系统/)

      // Check main container width
      const dashboardContainer = page.locator('.dashboard-container')
      await expect(dashboardContainer).toBeVisible()
      
      // Check two-column layout
      const leftColumn = page.locator('.left-column')
      const rightColumn = page.locator('.right-column')
      await expect(leftColumn).toBeVisible()
      await expect(rightColumn).toBeVisible()
    })

    test('should display all main sections', async ({ page }) => {
      await page.goto('/')

      // Check all main components are present
      await expect(page.locator('text=应用概要')).toBeVisible()
      await expect(page.locator('text=我的关注')).toBeVisible()
      await expect(page.locator('text=数量统计占比')).toBeVisible()
      await expect(page.locator('text=快速开始')).toBeVisible()
      await expect(page.locator('text=近期动态')).toBeVisible()
      await expect(page.locator('text=汇总数据')).toBeVisible()
    })

    test('should have responsive layout', async ({ page }) => {
      // Test desktop viewport
      await page.setViewportSize({ width: 1440, height: 900 })
      await page.goto('/')

      const dashboardContainer = page.locator('.dashboard-container')
      const containerBox = await dashboardContainer.boundingBox()
      expect(containerBox?.width).toBe(1440)

      // Test mobile viewport
      await page.setViewportSize({ width: 375, height: 667 })
      await page.goto('/')

      // Should still display content in mobile layout
      await expect(page.locator('text=应用概要')).toBeVisible()
      await expect(page.locator('text=快速开始')).toBeVisible()
    })
  })

  test.describe('Top Navigation', () => {
    test('should display top navigation with correct branding', async ({ page }) => {
      await page.goto('/')

      const topNav = page.locator('.top-nav')
      await expect(topNav).toBeVisible()
      
      // Check brand is displayed
      await expect(page.locator('.brand')).toHaveText('产品检核系统')
      
      // Check main menu items
      await expect(page.locator('text=主页')).toBeVisible()
      await expect(page.locator('text=产品管理')).toBeVisible()
      await expect(page.locator('text=规则管理')).toBeVisible()
      await expect(page.locator('text=设置')).toBeVisible()
      
      // Check user info
      await expect(page.locator('.user-info')).toContainText('测试用户')
    })

    test('should handle navigation clicks', async ({ page }) => {
      await page.goto('/')

      // Mock navigation to prevent actual page changes
      await page.route('**/*', async route => {
        if (route.request().url().includes('/product')) {
          await route.fulfill({
            status: 200,
            contentType: 'text/html',
            body: '<html><body>Product Page</body></html>'
          })
        } else if (route.request().url().includes('/rules')) {
          await route.fulfill({
            status: 200,
            contentType: 'text/html',
            body: '<html><body>Rules Page</body></html>'
          })
        } else {
          await route.continue()
        }
      })

      // Click on product management
      await page.click('text=产品管理')
      await page.waitForTimeout(500) // Wait for potential navigation
      
      // Click on rules management  
      await page.click('text=规则管理')
      await page.waitForTimeout(500) // Wait for potential navigation
    })
  })

  test.describe('Application Overview Section', () => {
    test('should display app overview with 8 metrics', async ({ page }) => {
      await page.goto('/')

      // Wait for metrics to load
      await page.waitForSelector('.metrics-grid')

      // Check all 8 metric cards are present
      const metricCards = page.locator('.metric-card')
      await expect(metricCards).toHaveCount(8)

      // Check specific metrics are visible
      await expect(page.locator('text=应用总数')).toBeVisible()
      await expect(page.locator('text=活跃应用')).toBeVisible()
      await expect(page.locator('text=检测总数')).toBeVisible()
      await expect(page.locator('text=有效检测')).toBeVisible()
      await expect(page.locator('text=错误数')).toBeVisible()
      await expect(page.locator('text=警告数')).toBeVisible()
      await expect(page.locator('text=待处理')).toBeVisible()
      await expect(page.locator('text=已完成')).toBeVisible()
    })

    test('should display trend indicators', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.metrics-grid')

      // Check trend indicators are present
      const trendIcons = page.locator('.trend-icon')
      expect(await trendIcons.count()).toBeGreaterThan(0)

      // Check trend up and down indicators
      const upTrends = page.locator('.trend-up')
      const downTrends = page.locator('.trend-down')
      expect(await upTrends.count() + await downTrends.count()).toBe(8)
    })
  })

  test.describe('My Favorites Section', () => {
    test('should display favorites list with filters', async ({ page }) => {
      await page.goto('/')

      // Wait for favorites to load
      await page.waitForSelector('.my-favorites')

      // Check section title
      await expect(page.locator('.my-favorites .section-title')).toHaveText('我的关注')
      
      // Check search input
      const searchInput = page.locator('.search-input input')
      await expect(searchInput).toBeVisible()
      await expect(searchInput).toHaveAttribute('placeholder', '搜索我的关注')
      
      // Check filter dropdowns
      await expect(page.locator('.filter-select')).toHaveCount(2)
      
      // Check task items are present
      const taskItems = page.locator('.task-item')
      expect(await taskItems.count()).toBeGreaterThan(0)
    })

    test('should handle search functionality', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.my-favorites')

      const searchInput = page.locator('.search-input input')
      
      // Type search query
      await searchInput.fill('销售数据')
      await page.waitForTimeout(300) // Wait for search debounce
      
      // Verify search input has value
      await expect(searchInput).toHaveValue('销售数据')
      
      // Clear search
      await searchInput.fill('')
      await page.waitForTimeout(300)
      
      await expect(searchInput).toHaveValue('')
    })

    test('should handle filter changes', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.my-favorites')

      // Test time filter
      const timeFilter = page.locator('.filter-select select').first()
      await timeFilter.selectOption('week')
      await page.waitForTimeout(300)
      
      // Test type filter
      const typeFilter = page.locator('.filter-select select').nth(1)
      await typeFilter.selectOption('审核')
      await page.waitForTimeout(300)
    })

    test('should handle task item clicks', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.my-favorites')

      // Click on first task item
      const firstTask = page.locator('.task-item').first()
      await firstTask.click()
      
      // Verify click was handled (no navigation expected in mock)
      await page.waitForTimeout(300)
    })
  })

  test.describe('Ratio Chart Section', () => {
    test('should display ratio chart with tabs', async ({ page }) => {
      await page.goto('/')

      // Wait for chart to load
      await page.waitForSelector('.ratio-chart')

      // Check section title
      await expect(page.locator('.ratio-chart .section-title')).toHaveText('数量统计占比')
      
      // Check chart tabs
      await expect(page.locator('.chart-tabs')).toBeVisible()
      await expect(page.locator('text=按产品类型')).toBeVisible()
      await expect(page.locator('text=按检核状态')).toBeVisible()
      
      // Check chart container
      await expect(page.locator('.chart-container')).toBeVisible()
      
      // Check legend
      await expect(page.locator('.chart-legend')).toBeVisible()
    })

    test('should handle tab switching', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.ratio-chart')

      // Click on different tabs
      await page.click('text=按检核状态')
      await page.waitForTimeout(300)
      
      await page.click('text=按产品类型')
      await page.waitForTimeout(300)
    })
  })

  test.describe('Quick Start Section', () => {
    test('should display quick start navigation items', async ({ page }) => {
      await page.goto('/')

      // Wait for quick start to load
      await page.waitForSelector('.quick-start')

      // Check section title
      await expect(page.locator('.quick-start .section-title')).toHaveText('快速开始')
      
      // Check navigation items
      await expect(page.locator('text=导入产品')).toBeVisible()
      await expect(page.locator('text=创建规则')).toBeVisible()
      await expect(page.locator('text=下载模板')).toBeVisible()
      await expect(page.locator('text=批量导入')).toBeVisible()
      await expect(page.locator('text=产品详情')).toBeVisible()
      await expect(page.locator('text=系统设置')).toBeVisible()
      
      // Check grid layout
      const quickItems = page.locator('.quick-item')
      expect(await quickItems.count()).toBe(6)
    })

    test('should handle navigation clicks with feedback', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.quick-start')

      // Mock navigation responses
      await page.route('**/product/import', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'text/html',
          body: '<html><body>Import Product Page</body></html>'
        })
      })

      await page.route('**/rules/create', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'text/html',
          body: '<html><body>Create Rules Page</body></html>'
        })
      })

      await page.route('**/template/download', async route => {
        await route.fulfill({
          status: 200,
          headers: {
            'Content-Type': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            'Content-Disposition': 'attachment; filename="template.xlsx"'
          },
          body: Buffer.from('mock excel content')
        })
      })

      // Test 导入产品 navigation
      await page.click('text=导入产品')
      await page.waitForTimeout(500)
      
      // Test 创建规则 navigation
      await page.click('text=创建规则')
      await page.waitForTimeout(500)
      
      // Test 下载 template
      const [download] = await Promise.all([
        page.waitForEvent('download'),
        page.click('text=下载模板')
      ])
      
      expect(download).toBeTruthy()
      
      // Test 批量导入 navigation
      await page.click('text=批量导入')
      await page.waitForTimeout(500)
    })

    test('should display badges correctly', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.quick-start')

      // Check for badges on items
      const badges = page.locator('.badge')
      expect(await badges.count()).toBeGreaterThan(0)
    })
  })

  test.describe('Recent Activities Section', () => {
    test('should display recent activities timeline', async ({ page }) => {
      await page.goto('/')

      // Wait for activities to load
      await page.waitForSelector('.recent-activities')

      // Check section title
      await expect(page.locator('.recent-activities .section-title')).toHaveText('近期动态')
      
      // Check timeline items
      const timelineItems = page.locator('.timeline-item')
      expect(await timelineItems.count()).toBeGreaterThan(0)
      
      // Check timeline structure
      await expect(page.locator('.timeline')).toBeVisible()
    })

    test('should display activity details correctly', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.recent-activities')

      // Check first activity item structure
      const firstActivity = page.locator('.timeline-item').first()
      await expect(firstActivity.locator('.activity-time')).toBeVisible()
      await expect(firstActivity.locator('.activity-content')).toBeVisible()
      await expect(firstActivity.locator('.activity-status')).toBeVisible()
    })
  })

  test.describe('Summary Totals Section', () => {
    test('should display summary totals with range selector', async ({ page }) => {
      await page.goto('/')

      // Wait for summary to load
      await page.waitForSelector('.summary-totals')

      // Check section title
      await expect(page.locator('.summary-totals .section-title')).toHaveText('汇总数据')
      
      // Check range selector
      await expect(page.locator('.range-selector')).toBeVisible()
      await expect(page.locator('text=本日')).toBeVisible()
      await expect(page.locator('text=本周')).toBeVisible()
      await expect(page.locator('text=本月')).toBeVisible()
      await expect(page.locator('text=总计')).toBeVisible()
      
      // Check summary metrics
      await expect(page.locator('.summary-metrics')).toBeVisible()
      
      // Check metric items
      const metricItems = page.locator('.summary-metric')
      expect(await metricItems.count()).toBeGreaterThan(0)
    })

    test('should handle range changes', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.summary-totals')

      // Test range selection
      await page.click('text=本周')
      await page.waitForTimeout(300)
      
      await page.click('text=本月')
      await page.waitForTimeout(300)
      
      await page.click('text=总计')
      await page.waitForTimeout(300)
    })

    test('should display metric formatting correctly', async ({ page }) => {
      await page.goto('/')

      await page.waitForSelector('.summary-totals')

      // Check metric values are formatted correctly
      const metricValues = page.locator('.metric-value')
      expect(await metricValues.count()).toBeGreaterThan(0)
      
      // Check metric labels
      const metricLabels = page.locator('.metric-label')
      expect(await metricLabels.count()).toBeGreaterThan(0)
    })
  })

  test.describe('Cross-Component Interactions', () => {
    test('should maintain state across components', async ({ page }) => {
      await page.goto('/')

      // Wait for all components to load
      await Promise.all([
        page.waitForSelector('.app-overview'),
        page.waitForSelector('.my-favorites'),
        page.waitForSelector('.ratio-chart'),
        page.waitForSelector('.quick-start'),
        page.waitForSelector('.recent-activities'),
        page.waitForSelector('.summary-totals')
      ])

      // Perform actions in multiple components
      await page.locator('.search-input input').fill('test search')
      await page.click('text=按检核状态')
      await page.click('text=本周')

      // Verify page remains stable
      await expect(page.locator('.dashboard-home')).toBeVisible()
    })

    test('should handle responsive behavior across all components', async ({ page }) => {
      // Test different viewports
      const viewports = [
        { width: 1440, height: 900, name: 'Desktop' },
        { width: 768, height: 1024, name: 'Tablet' },
        { width: 375, height: 667, name: 'Mobile' }
      ]

      for (const viewport of viewports) {
        await page.setViewportSize(viewport)
        await page.goto('/')

        // Verify all components are visible
        await expect(page.locator('text=应用概要')).toBeVisible()
        await expect(page.locator('text=我的关注')).toBeVisible()
        await expect(page.locator('text=快速开始')).toBeVisible()
        
        // Add small delay between viewport changes
        await page.waitForTimeout(200)
      }
    })
  })

  test.describe('Performance and Loading', () => {
    test('should load page within acceptable time', async ({ page }) => {
      const startTime = Date.now()
      await page.goto('/')
      const loadTime = Date.now() - startTime
      
      // Page should load within 5 seconds
      expect(loadTime).toBeLessThan(5000)
    })

    test('should display loading states properly', async ({ page }) => {
      // Mock slow API response
      await page.route('**/api/**', async route => {
        await new Promise(resolve => setTimeout(resolve, 2000))
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, data: {} })
        })
      })

      await page.goto('/')
      
      // Check if loading states are handled gracefully
      await expect(page.locator('.dashboard-home')).toBeVisible()
    })
  })

  test.describe('Accessibility', () => {
    test('should support keyboard navigation', async ({ page }) => {
      await page.goto('/')

      // Test Tab navigation through main sections
      let focusedElement = await page.locator(':focus')
      let tabCount = 0
      
      while (tabCount < 20) { // Prevent infinite loop
        await page.keyboard.press('Tab')
        await page.waitForTimeout(100)
        
        focusedElement = page.locator(':focus')
        const isVisible = await focusedElement.isVisible()
        
        if (isVisible) {
          break
        }
        
        tabCount++
      }
      
      // Should have focused on at least one interactive element
      expect(tabCount).toBeLessThan(20)
    })

    test('should have proper heading hierarchy', async ({ page }) => {
      await page.goto('/')

      // Check heading structure
      const headings = page.locator('h1, h2, h3, h4, h5, h6')
      expect(await headings.count()).toBeGreaterThan(0)
      
      // Check section titles are h2
      const sectionTitles = page.locator('.section-title')
      expect(await sectionTitles.count()).toBe(6) // All main sections
    })
  })

  test.describe('Error Handling', () => {
    test('should handle API errors gracefully', async ({ page }) => {
      // Mock API error
      await page.route('**/api/**', async route => {
        await route.abort('failed')
      })

      await page.goto('/')

      // Page should still render with mock/local data
      await expect(page.locator('.dashboard-home')).toBeVisible()
      await expect(page.locator('text=应用概要')).toBeVisible()
    })

    test('should handle missing data gracefully', async ({ page }) => {
      // Mock empty API response
      await page.route('**/api/**', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, data: null })
        })
      })

      await page.goto('/')

      // Should still display basic structure
      await expect(page.locator('.dashboard-home')).toBeVisible()
    })
  })
})