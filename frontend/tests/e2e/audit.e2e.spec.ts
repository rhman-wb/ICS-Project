import { test, expect } from '@playwright/test'

test.describe('检核功能端到端测试', () => {
  test.beforeEach(async ({ page }) => {
    // 清除所有存储数据
    await page.context().clearCookies()
    await page.evaluate(() => {
      localStorage.clear()
      sessionStorage.clear()
    })

    // 模拟登录状态
    await page.goto('/login')
    await page.fill('[data-testid="username-input"]', 'admin')
    await page.fill('[data-testid="password-input"]', 'password123')
    await page.click('[data-testid="login-submit"]')

    // 等待导航到仪表板
    await page.waitForURL('/dashboard', { waitUntil: 'networkidle' })
    await expect(page.getByTestId('dashboard-container')).toBeVisible()
  })

  test.describe('产品管理页面检核入口', () => {
    test('应该正确显示检核入口并支持产品选择', async ({ page }) => {
      // 导航到产品管理页面
      await page.goto('/product/management')
      await page.waitForLoadState('networkidle')

      // 验证产品管理页面加载
      await expect(page.getByTestId('product-management-container')).toBeVisible()
      await expect(page.getByTestId('product-list-table')).toBeVisible()

      // 选择产品进行检核
      await page.getByTestId('product-checkbox').first().check()
      await page.getByTestId('product-checkbox').nth(1).check()

      // 验证批量操作按钮可用
      await expect(page.getByTestId('batch-audit-button')).toBeEnabled()

      // 点击检核按钮
      await page.getByTestId('batch-audit-button').click()

      // 验证跳转到检核规则选择页面
      await page.waitForURL('/audit/rule-selection*', { waitUntil: 'networkidle' })
      await expect(page.getByTestId('audit-rule-selection-container')).toBeVisible()
    })

    test('应该在未选择产品时显示警告', async ({ page }) => {
      await page.goto('/product/management')
      await page.waitForLoadState('networkidle')

      // 不选择任何产品，直接点击检核按钮
      await page.getByTestId('batch-audit-button').click()

      // 验证显示警告消息
      await expect(page.getByText('请先选择要检核的产品')).toBeVisible()
    })
  })

  test.describe('检核规则选择页面', () => {
    test('应该正确显示规则选择页面并支持规则筛选', async ({ page }) => {
      // 直接导航到规则选择页面（模拟从产品管理页面跳转）
      await page.goto('/audit/rule-selection?productIds=product1,product2')
      await page.waitForLoadState('networkidle')

      // 验证页面基本元素
      await expect(page.getByTestId('audit-rule-selection-container')).toBeVisible()
      await expect(page.getByTestId('selection-steps')).toBeVisible()
      await expect(page.getByTestId('product-info')).toContainText('已选择 2 个产品')

      // 验证筛选表单
      await expect(page.getByTestId('category-filter')).toBeVisible()
      await expect(page.getByTestId('severity-filter')).toBeVisible()
      await expect(page.getByTestId('keyword-filter')).toBeVisible()

      // 验证规则表格
      await expect(page.getByTestId('rules-table')).toBeVisible()
      await expect(page.getByTestId('rules-table').locator('tbody tr')).toHaveCountGreaterThan(0)
    })

    test('应该支持规则筛选功能', async ({ page }) => {
      await page.goto('/audit/rule-selection?productIds=product1,product2')
      await page.waitForLoadState('networkidle')

      // 按类别筛选
      await page.getByTestId('category-filter').click()
      await page.getByRole('option', { name: '基础信息' }).click()

      // 按严重程度筛选
      await page.getByTestId('severity-filter').click()
      await page.getByRole('option', { name: '高' }).click()

      // 关键词搜索
      await page.getByTestId('keyword-filter').fill('产品代码')

      // 等待筛选结果
      await page.waitForLoadState('networkidle')

      // 验证筛选条件已应用
      await expect(page.getByTestId('selected-filters')).toContainText('基础信息')
      await expect(page.getByTestId('selected-filters')).toContainText('高')
      await expect(page.getByTestId('selected-filters')).toContainText('产品代码')
    })

    test('应该支持规则选择和全选功能', async ({ page }) => {
      await page.goto('/audit/rule-selection?productIds=product1,product2')
      await page.waitForLoadState('networkidle')

      // 选择单个规则
      await page.getByTestId('rules-table').locator('tbody tr').first().getByRole('checkbox').check()

      // 验证选择统计更新
      await expect(page.getByTestId('selection-stats')).toContainText('已选择 1 条规则')

      // 测试全选功能
      await page.getByTestId('select-all-button').click()

      // 验证所有规则被选中
      const checkboxCount = await page.getByTestId('rules-table').locator('tbody tr input[type="checkbox"]').count()
      await expect(page.getByTestId('selection-stats')).toContainText(`已选择 ${checkboxCount} 条规则`)

      // 验证下一步按钮可用
      await expect(page.getByTestId('next-button')).toBeEnabled()
    })

    test('应该在未选择规则时禁用下一步按钮', async ({ page }) => {
      await page.goto('/audit/rule-selection?productIds=product1,product2')
      await page.waitForLoadState('networkidle')

      // 验证未选择规则时下一步按钮被禁用
      await expect(page.getByTestId('next-button')).toBeDisabled()

      // 清除所有选择
      await page.getByTestId('clear-selection-button').click()

      // 验证下一步按钮仍然被禁用
      await expect(page.getByTestId('next-button')).toBeDisabled()
    })

    test('应该支持进入检核流程', async ({ page }) => {
      await page.goto('/audit/rule-selection?productIds=product1,product2')
      await page.waitForLoadState('networkidle')

      // 选择规则
      await page.getByTestId('rules-table').locator('tbody tr').first().getByRole('checkbox').check()
      await page.getByTestId('rules-table').locator('tbody tr').nth(1).getByRole('checkbox').check()

      // 点击下一步
      await page.getByTestId('next-button').click()

      // 验证跳转到进度监控页面
      await page.waitForURL('/audit/process-monitor*', { waitUntil: 'networkidle' })
      await expect(page.getByTestId('audit-process-monitor-container')).toBeVisible()
    })
  })

  test.describe('检核进度监控页面', () => {
    test('应该正确显示检核进度信息', async ({ page }) => {
      // 导航到进度监控页面（模拟从规则选择页面跳转）
      await page.goto('/audit/process-monitor?taskId=task-123')
      await page.waitForLoadState('networkidle')

      // 验证页面基本元素
      await expect(page.getByTestId('audit-process-monitor-container')).toBeVisible()
      await expect(page.getByTestId('progress-section')).toBeVisible()
      await expect(page.getByTestId('statistics-section')).toBeVisible()
      await expect(page.getByTestId('logs-section')).toBeVisible()

      // 验证进度显示
      await expect(page.getByTestId('progress-bar')).toBeVisible()
      await expect(page.getByTestId('status-display')).toBeVisible()
      await expect(page.getByTestId('time-info')).toBeVisible()

      // 验证统计信息
      await expect(page.getByTestId('total-files-stat')).toBeVisible()
      await expect(page.getByTestId('processed-files-stat')).toBeVisible()
      await expect(page.getByTestId('passed-files-stat')).toBeVisible()
      await expect(page.getByTestId('failed-files-stat')).toBeVisible()
    })

    test('应该支持检核过程控制', async ({ page }) => {
      await page.goto('/audit/process-monitor?taskId=task-123')
      await page.waitForLoadState('networkidle')

      // 验证控制按钮
      await expect(page.getByTestId('pause-button')).toBeVisible()
      await expect(page.getByTestId('stop-button')).toBeVisible()
      await expect(page.getByTestId('refresh-button')).toBeVisible()

      // 测试暂停功能
      await page.getByTestId('pause-button').click()
      await expect(page.getByTestId('resume-button')).toBeVisible()

      // 测试恢复功能
      await page.getByTestId('resume-button').click()
      await expect(page.getByTestId('pause-button')).toBeVisible()

      // 测试刷新功能
      await page.getByTestId('refresh-button').click()
      await page.waitForLoadState('networkidle')
    })

    test('应该实时显示检核日志', async ({ page }) => {
      await page.goto('/audit/process-monitor?taskId=task-123')
      await page.waitForLoadState('networkidle')

      // 验证日志时间线
      await expect(page.getByTestId('logs-timeline')).toBeVisible()

      // 验证日志条目
      const logItems = page.getByTestId('logs-timeline').locator('.timeline-item')
      await expect(logItems).toHaveCountGreaterThan(0)

      // 验证日志级别标识
      await expect(page.getByTestId('log-level-info')).toBeVisible()
      await expect(page.getByTestId('log-level-success')).toBeVisible()
      await expect(page.getByTestId('log-level-warning')).toBeVisible()
    })

    test('应该在检核完成后显示结果入口', async ({ page }) => {
      // 模拟检核完成状态
      await page.goto('/audit/process-monitor?taskId=task-123&status=completed')
      await page.waitForLoadState('networkidle')

      // 验证完成状态显示
      await expect(page.getByTestId('status-display')).toContainText('检核完成')

      // 验证查看结果按钮
      await expect(page.getByTestId('view-results-button')).toBeVisible()
      await expect(page.getByTestId('view-results-button')).toBeEnabled()

      // 点击查看结果
      await page.getByTestId('view-results-button').click()

      // 验证跳转到结果展示页面
      await page.waitForURL('/audit/results*', { waitUntil: 'networkidle' })
    })
  })

  test.describe('检核结果展示页面', () => {
    test('应该正确显示检核结果统计信息', async ({ page }) => {
      await page.goto('/audit/results?taskId=task-123')
      await page.waitForLoadState('networkidle')

      // 验证页面基本元素
      await expect(page.getByTestId('audit-result-display-container')).toBeVisible()
      await expect(page.getByTestId('statistics-section')).toBeVisible()
      await expect(page.getByTestId('results-table')).toBeVisible()

      // 验证统计卡片
      await expect(page.getByTestId('total-documents-stat')).toBeVisible()
      await expect(page.getByTestId('passed-documents-stat')).toBeVisible()
      await expect(page.getByTestId('failed-documents-stat')).toBeVisible()
      await expect(page.getByTestId('warning-documents-stat')).toBeVisible()

      // 验证文档类型统计
      await expect(page.getByTestId('document-type-stats')).toBeVisible()

      // 验证严重程度统计
      await expect(page.getByTestId('severity-stats')).toBeVisible()
    })

    test('应该支持结果筛选和搜索', async ({ page }) => {
      await page.goto('/audit/results?taskId=task-123')
      await page.waitForLoadState('networkidle')

      // 验证筛选表单
      await expect(page.getByTestId('document-type-filter')).toBeVisible()
      await expect(page.getByTestId('severity-filter')).toBeVisible()
      await expect(page.getByTestId('status-filter')).toBeVisible()
      await expect(page.getByTestId('keyword-filter')).toBeVisible()

      // 按文档类型筛选
      await page.getByTestId('document-type-filter').click()
      await page.getByRole('option', { name: '保险条款' }).click()

      // 按严重程度筛选
      await page.getByTestId('severity-filter').click()
      await page.getByRole('option', { name: '高' }).click()

      // 关键词搜索
      await page.getByTestId('keyword-filter').fill('产品代码')

      // 等待筛选结果
      await page.waitForLoadState('networkidle')

      // 验证结果表格更新
      await expect(page.getByTestId('results-table')).toBeVisible()
    })

    test('应该支持导出功能', async ({ page }) => {
      await page.goto('/audit/results?taskId=task-123')
      await page.waitForLoadState('networkidle')

      // 验证导出按钮
      await expect(page.getByTestId('export-button')).toBeVisible()
      await expect(page.getByTestId('export-button')).toBeEnabled()

      // 点击导出按钮
      await page.getByTestId('export-button').click()

      // 验证导出选项菜单
      await expect(page.getByTestId('export-excel-option')).toBeVisible()
      await expect(page.getByTestId('export-pdf-option')).toBeVisible()

      // 测试Excel导出
      const downloadPromise = page.waitForEvent('download')
      await page.getByTestId('export-excel-option').click()
      const download = await downloadPromise

      // 验证下载文件
      expect(download.suggestedFilename()).toContain('.xlsx')
    })

    test('应该支持查看结果详情', async ({ page }) => {
      await page.goto('/audit/results?taskId=task-123')
      await page.waitForLoadState('networkidle')

      // 点击结果行查看详情
      await page.getByTestId('results-table').locator('tbody tr').first().click()

      // 验证详情弹窗或页面
      await expect(page.getByTestId('result-detail-modal')).toBeVisible()
      await expect(page.getByTestId('result-detail-content')).toBeVisible()

      // 验证详情信息
      await expect(page.getByTestId('detail-file-name')).toBeVisible()
      await expect(page.getByTestId('detail-rule-name')).toBeVisible()
      await expect(page.getByTestId('detail-message')).toBeVisible()
      await expect(page.getByTestId('detail-suggestion')).toBeVisible()
    })
  })

  test.describe('检核历史管理页面', () => {
    test('应该正确显示历史记录列表', async ({ page }) => {
      await page.goto('/audit/history')
      await page.waitForLoadState('networkidle')

      // 验证页面基本元素
      await expect(page.getByTestId('audit-history-manager-container')).toBeVisible()
      await expect(page.getByTestId('search-form')).toBeVisible()
      await expect(page.getByTestId('history-table')).toBeVisible()

      // 验证搜索表单
      await expect(page.getByTestId('product-name-input')).toBeVisible()
      await expect(page.getByTestId('status-select')).toBeVisible()
      await expect(page.getByTestId('date-range-picker')).toBeVisible()

      // 验证历史记录表格
      await expect(page.getByTestId('history-table').locator('tbody tr')).toHaveCountGreaterThan(0)
    })

    test('应该支持历史记录搜索和筛选', async ({ page }) => {
      await page.goto('/audit/history')
      await page.waitForLoadState('networkidle')

      // 按产品名称搜索
      await page.getByTestId('product-name-input').fill('终身寿险')

      // 按状态筛选
      await page.getByTestId('status-select').click()
      await page.getByRole('option', { name: '已完成' }).click()

      // 按日期范围筛选
      await page.getByTestId('date-range-picker').click()
      await page.getByText('最近一周').click()

      // 执行搜索
      await page.getByTestId('search-button').click()
      await page.waitForLoadState('networkidle')

      // 验证搜索结果
      await expect(page.getByTestId('history-table')).toBeVisible()
    })

    test('应该支持查看历史记录详情', async ({ page }) => {
      await page.goto('/audit/history')
      await page.waitForLoadState('networkidle')

      // 点击查看按钮
      await page.getByTestId('history-table').locator('tbody tr').first().getByTestId('view-button').click()

      // 验证跳转到结果页面
      await page.waitForURL('/audit/results*', { waitUntil: 'networkidle' })
      await expect(page.getByTestId('audit-result-display-container')).toBeVisible()
    })

    test('应该支持下载历史报告', async ({ page }) => {
      await page.goto('/audit/history')
      await page.waitForLoadState('networkidle')

      // 点击下载按钮
      const downloadPromise = page.waitForEvent('download')
      await page.getByTestId('history-table').locator('tbody tr').first().getByTestId('download-button').click()
      const download = await downloadPromise

      // 验证下载文件
      expect(download.suggestedFilename()).toContain('.pdf')
    })

    test('应该支持删除历史记录', async ({ page }) => {
      await page.goto('/audit/history')
      await page.waitForLoadState('networkidle')

      // 获取删除前的记录数量
      const initialRowCount = await page.getByTestId('history-table').locator('tbody tr').count()

      // 点击删除按钮
      await page.getByTestId('history-table').locator('tbody tr').first().getByTestId('delete-button').click()

      // 确认删除
      await page.getByTestId('delete-confirm-button').click()

      // 等待删除完成
      await page.waitForLoadState('networkidle')

      // 验证记录被删除
      const finalRowCount = await page.getByTestId('history-table').locator('tbody tr').count()
      expect(finalRowCount).toBe(initialRowCount - 1)

      // 验证成功消息
      await expect(page.getByText('删除成功')).toBeVisible()
    })
  })

  test.describe('完整检核流程测试', () => {
    test('应该支持完整的检核流程', async ({ page }) => {
      // 1. 从产品管理页面开始
      await page.goto('/product/management')
      await page.waitForLoadState('networkidle')

      // 选择产品
      await page.getByTestId('product-checkbox').first().check()
      await page.getByTestId('product-checkbox').nth(1).check()

      // 启动检核
      await page.getByTestId('batch-audit-button').click()

      // 2. 规则选择阶段
      await page.waitForURL('/audit/rule-selection*', { waitUntil: 'networkidle' })

      // 选择规则
      await page.getByTestId('select-all-button').click()
      await page.getByTestId('next-button').click()

      // 3. 进度监控阶段
      await page.waitForURL('/audit/process-monitor*', { waitUntil: 'networkidle' })

      // 等待检核完成（模拟或等待实际完成）
      await expect(page.getByTestId('view-results-button')).toBeVisible({ timeout: 30000 })

      // 查看结果
      await page.getByTestId('view-results-button').click()

      // 4. 结果查看阶段
      await page.waitForURL('/audit/results*', { waitUntil: 'networkidle' })
      await expect(page.getByTestId('audit-result-display-container')).toBeVisible()

      // 导出结果
      await page.getByTestId('export-button').click()
      await page.getByTestId('export-excel-option').click()

      // 5. 历史记录验证
      await page.goto('/audit/history')
      await page.waitForLoadState('networkidle')

      // 验证新的检核记录出现在历史中
      await expect(page.getByTestId('history-table').locator('tbody tr')).toHaveCountGreaterThan(0)
    })
  })

  test.describe('错误处理和边界情况', () => {
    test('应该正确处理网络错误', async ({ page }) => {
      // 模拟网络离线
      await page.context().setOffline(true)

      await page.goto('/audit/rule-selection?productIds=product1')

      // 应该显示网络错误提示
      await expect(page.getByTestId('network-error-message')).toBeVisible()

      // 恢复网络
      await page.context().setOffline(false)

      // 重试加载
      await page.getByTestId('retry-button').click()
      await page.waitForLoadState('networkidle')

      await expect(page.getByTestId('audit-rule-selection-container')).toBeVisible()
    })

    test('应该处理检核任务失败情况', async ({ page }) => {
      // 模拟检核失败状态
      await page.goto('/audit/process-monitor?taskId=failed-task')
      await page.waitForLoadState('networkidle')

      // 验证失败状态显示
      await expect(page.getByTestId('status-display')).toContainText('检核失败')
      await expect(page.getByTestId('error-message')).toBeVisible()

      // 验证重新开始按钮
      await expect(page.getByTestId('restart-button')).toBeVisible()
    })

    test('应该处理无权限访问情况', async ({ page }) => {
      // 模拟无权限用户
      await page.goto('/login')
      await page.fill('[data-testid="username-input"]', 'viewer')
      await page.fill('[data-testid="password-input"]', 'password123')
      await page.click('[data-testid="login-submit"]')

      await page.waitForURL('/dashboard', { waitUntil: 'networkidle' })

      // 尝试访问检核功能
      await page.goto('/audit/rule-selection')

      // 应该显示权限不足或重定向
      await Promise.race([
        expect(page.getByTestId('permission-denied')).toBeVisible(),
        page.waitForURL('/403'),
        page.waitForURL('/dashboard')
      ])
    })
  })

  test.describe('响应式设计测试', () => {
    test('应该在移动设备上正常工作', async ({ page }) => {
      // 设置移动设备视口
      await page.setViewportSize({ width: 375, height: 667 })

      await page.goto('/audit/rule-selection?productIds=product1')
      await page.waitForLoadState('networkidle')

      // 验证移动端布局
      await expect(page.getByTestId('mobile-filter-toggle')).toBeVisible()
      await expect(page.getByTestId('mobile-table-view')).toBeVisible()

      // 测试移动端筛选
      await page.getByTestId('mobile-filter-toggle').click()
      await expect(page.getByTestId('mobile-filter-form')).toBeVisible()
    })

    test('应该在平板设备上正常工作', async ({ page }) => {
      // 设置平板设备视口
      await page.setViewportSize({ width: 768, height: 1024 })

      await page.goto('/audit/results?taskId=task-123')
      await page.waitForLoadState('networkidle')

      // 验证平板端布局
      await expect(page.getByTestId('tablet-layout')).toBeVisible()
      await expect(page.getByTestId('statistics-cards')).toBeVisible()
    })
  })
})