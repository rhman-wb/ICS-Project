import { test, expect } from '@playwright/test'

test.describe('规则管理端到端测试', () => {
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

    // 等待导航到仪表板 - 使用稳定的等待方式
    await page.waitForURL('/dashboard', { waitUntil: 'networkidle' })
    await expect(page.getByTestId('dashboard-container')).toBeVisible()
  })

  test.describe('规则列表页面', () => {
    test('应该正确显示规则列表页面', async ({ page }) => {
      // 从工作台导航到规则管理
      await page.getByTestId('rules-nav-link').click()
      await page.waitForURL('/rules', { waitUntil: 'networkidle' })

      // 检查页面标题
      await expect(page).toHaveTitle(/规则管理/)

      // 检查主要页面元素 - 使用data-testid
      await expect(page.getByTestId('page-header')).toContainText('规则管理')
      await expect(page.getByTestId('rules-filter-bar')).toBeVisible()
      await expect(page.getByTestId('rules-table')).toBeVisible()
      await expect(page.getByTestId('rules-pagination')).toBeVisible()

      // 检查搜索表单
      await expect(page.getByTestId('rule-name-input')).toBeVisible()
      await expect(page.getByTestId('rule-type-select')).toBeVisible()
      await expect(page.getByTestId('audit-status-select')).toBeVisible()
      await expect(page.getByTestId('effective-status-select')).toBeVisible()

      // 检查操作按钮
      await expect(page.getByTestId('search-button')).toBeVisible()
      await expect(page.getByTestId('reset-button')).toBeVisible()
      await expect(page.getByTestId('create-rule-button')).toBeVisible()
      await expect(page.getByTestId('batch-delete-button')).toBeVisible()
    })

    test('应该支持规则搜索功能', async ({ page }) => {
      await page.goto('/rules')
      await page.waitForLoadState('networkidle')

      // 输入搜索关键词
      await page.getByTestId('rule-name-input').fill('测试规则')
      await page.getByTestId('search-button').click()

      // 等待搜索结果 - 使用网络空闲等待
      await page.waitForLoadState('networkidle')

      // 验证URL包含搜索参数
      expect(page.url()).toContain('ruleName=测试规则')

      // 验证表格更新 - 等待表格内容加载完成
      await expect(page.getByTestId('rules-table').locator('.ant-table-tbody')).toBeVisible()

      // 验证loading状态消失
      await expect(page.getByTestId('rules-table').locator('.ant-spin-spinning')).toBeHidden()
    })

    test('应该支持筛选功能', async ({ page }) => {
      await page.goto('/rules')
      await page.waitForLoadState('networkidle')

      // 选择规则类型 - 使用更稳定的选择器
      await page.getByTestId('rule-type-select').click()
      await page.getByRole('option', { name: '单句规则' }).click()

      // 选择审核状态
      await page.getByTestId('audit-status-select').click()
      await page.getByRole('option', { name: '已通过' }).click()

      // 点击搜索
      await page.getByTestId('search-button').click()

      // 等待筛选结果 - 使用API响应等待
      await page.waitForResponse(response =>
        response.url().includes('/api/v1/rules') && response.status() === 200
      )

      // 验证筛选参数反映在URL中
      expect(page.url()).toContain('ruleType=SINGLE')
      expect(page.url()).toContain('auditStatus=APPROVED')

      // 验证筛选条件显示在已选条件中
      await expect(page.getByTestId('selected-filters')).toContainText('单句规则')
      await expect(page.getByTestId('selected-filters')).toContainText('已通过')
    })

    test('应该支持重置筛选功能', async ({ page }) => {
      await page.goto('/rules')
      await page.waitForLoadState('networkidle')

      // 填写筛选条件
      await page.getByTestId('rule-name-input').fill('测试')

      await page.getByTestId('rule-type-select').click()
      await page.getByRole('option', { name: '单句规则' }).click()

      // 点击重置
      await page.getByTestId('reset-button').click()

      // 验证表单被重置 - 使用具体的等待条件
      await expect(page.getByTestId('rule-name-input')).toHaveValue('')
      await expect(page.getByTestId('rule-type-select')).toContainText('全部')

      // 验证URL也被重置
      expect(page.url()).not.toContain('ruleName')
      expect(page.url()).not.toContain('ruleType')

      // 验证已选条件区域为空或隐藏
      const selectedFilters = page.getByTestId('selected-filters')
      await expect(selectedFilters).toBeHidden().or(expect(selectedFilters).toBeEmpty())
    })

    test('应该支持批量操作', async ({ page }) => {
      await page.goto('/rules')
      await page.waitForLoadState('networkidle')

      // 等待表格数据加载
      await expect(page.getByTestId('rules-table').locator('tbody tr')).toHaveCountGreaterThan(0)

      // 选择第一项
      await page.getByTestId('rules-table').locator('tbody tr').first().getByRole('checkbox').check()

      // 验证批量操作按钮可用
      await expect(page.getByTestId('batch-delete-button')).toBeEnabled()
      await expect(page.getByTestId('batch-submit-oa-button')).toBeEnabled()

      // 测试全选功能
      await page.getByTestId('select-all-checkbox').check()

      // 验证所有行都被选中
      const checkboxes = page.getByTestId('rules-table').locator('tbody tr input[type="checkbox"]')
      const checkboxCount = await checkboxes.count()

      for (let i = 0; i < checkboxCount; i++) {
        await expect(checkboxes.nth(i)).toBeChecked()
      }

      // 验证选中数量显示正确
      await expect(page.getByTestId('selected-count')).toContainText(`已选择 ${checkboxCount} 项`)
    })
  })

  test.describe('规则创建页面', () => {
    test('应该正确创建单句规则', async ({ page }) => {
      await page.goto('/rules')
      await page.waitForLoadState('networkidle')

      // 点击新增规则按钮
      await page.getByTestId('create-rule-button').click()
      await page.waitForURL('/rules/new?type=single', { waitUntil: 'networkidle' })

      // 验证页面标题
      await expect(page).toHaveTitle(/添加规则/)
      await expect(page.getByTestId('page-title')).toContainText('添加规则')

      // 验证Tab显示
      await expect(page.getByTestId('rule-type-tabs')).toContainText('单句规则')
      await expect(page.getByTestId('rule-type-tabs')).toContainText('双句规则')
      await expect(page.getByTestId('rule-type-tabs')).toContainText('格式规则')
      await expect(page.getByTestId('rule-type-tabs')).toContainText('高级规则')

      // 填写表单数据
      await page.getByTestId('rule-description-input').fill('E2E测试规则')

      await page.getByTestId('match-chapter-select').click()
      await page.getByRole('option', { name: '第一章' }).click()

      await page.getByTestId('match-type-select').click()
      await page.getByRole('option', { name: '精确匹配' }).click()

      await page.getByTestId('match-content-input').fill('测试匹配内容')

      await page.getByTestId('trigger-condition-select').click()
      await page.getByRole('option', { name: '包含' }).click()

      await page.getByTestId('hint-type-select').click()
      await page.getByRole('option', { name: '警告' }).click()

      await page.getByTestId('hint-content-textarea').fill('测试提示内容')

      // 填写适用要件等必填字段
      await page.getByTestId('applicable-requirement-input').fill('测试要件')

      await page.getByTestId('insurance-type-select').click()
      await page.getByRole('option', { name: '财产险' }).click()

      // 提交表单
      await page.getByTestId('submit-button').click()

      // 等待提交完成 - 等待成功页面或列表页面
      await Promise.race([
        page.waitForURL('/rules/success', { waitUntil: 'networkidle' }),
        page.waitForURL('/rules', { waitUntil: 'networkidle' })
      ])

      // 如果跳转到成功页面，验证成功信息
      if (page.url().includes('/rules/success')) {
        await expect(page.getByTestId('success-message')).toContainText('提交成功')

        // 点击返回列表
        await page.getByTestId('back-to-list-button').click()
        await page.waitForURL('/rules', { waitUntil: 'networkidle' })
      }

      // 验证新规则出现在列表中
      await expect(page.getByTestId('rules-table')).toContainText('E2E测试规则')
    })
  })

  test.describe('检核跳转页面', () => {
    test('应该正确显示检核跳转页面', async ({ page }) => {
      // 直接导航到检核跳转页面
      await page.goto('/rules/jump')
      await page.waitForLoadState('networkidle')

      // 验证页面标题
      await expect(page.getByTestId('jump-title')).toContainText('产品检核 - 选择待检核产品')

      // 验证操作按钮
      await expect(page.getByTestId('close-button')).toBeVisible()
      await expect(page.getByTestId('confirm-button')).toBeVisible()
      await expect(page.getByTestId('cancel-button')).toBeVisible()

      // 验证筛选控件
      await expect(page.getByTestId('product-category-input')).toBeVisible()
      await expect(page.getByTestId('development-type-select')).toBeVisible()
      await expect(page.getByTestId('report-type-select')).toBeVisible()
      await expect(page.getByTestId('audit-status-select')).toBeVisible()
      await expect(page.getByTestId('keyword-input')).toBeVisible()

      // 测试筛选功能
      await page.getByTestId('product-category-input').fill('财产险')

      await page.getByTestId('development-type-select').click()
      await page.getByRole('option', { name: '自主研发' }).click()

      // 验证标签显示
      await expect(page.getByTestId('filter-tags')).toContainText('财产险')
      await expect(page.getByTestId('filter-tags')).toContainText('自主研发')

      // 测试取消操作
      await page.getByTestId('cancel-button').click()

      // 应该返回到上一页面或关闭弹窗
      // 这里的行为取决于具体实现
    })
  })

  test.describe('移动端响应式测试', () => {
    test('应该在移动设备上正常显示', async ({ page }) => {
      // 设置为移动设备视口
      await page.setViewportSize({ width: 375, height: 667 })

      await page.goto('/rules')
      await page.waitForLoadState('networkidle')

      // 验证移动端搜索切换按钮
      await expect(page.getByTestId('mobile-search-toggle')).toBeVisible()

      // 点击展开移动端搜索
      await page.getByTestId('mobile-search-toggle').click()

      // 验证搜索表单在移动端正常显示
      await expect(page.getByTestId('mobile-search-form')).toBeVisible()

      // 验证表格在移动端的响应式布局
      await expect(page.getByTestId('rules-table')).toBeVisible()
    })
  })

  test.describe('错误处理测试', () => {
    test('应该正确处理网络错误', async ({ page }) => {
      // 模拟网络离线状态
      await page.context().setOffline(true)

      await page.goto('/rules')

      // 尝试加载数据
      await page.getByTestId('search-button').click()

      // 应该显示错误提示
      await expect(page.getByTestId('error-message')).toBeVisible()
      await expect(page.getByTestId('error-message')).toContainText('网络连接失败')

      // 恢复网络连接
      await page.context().setOffline(false)

      // 重试应该成功
      await page.getByTestId('retry-button').click()
      await page.waitForLoadState('networkidle')

      await expect(page.getByTestId('rules-table')).toBeVisible()
    })

    test('应该正确处理权限不足', async ({ page }) => {
      // 模拟权限不足的用户登录
      await page.goto('/login')
      await page.getByTestId('username-input').fill('viewer')
      await page.getByTestId('password-input').fill('password123')
      await page.getByTestId('login-submit').click()

      await page.waitForURL('/dashboard', { waitUntil: 'networkidle' })

      // 尝试访问规则管理
      await page.goto('/rules')

      // 应该显示权限不足的提示或跳转到无权限页面
      await Promise.race([
        expect(page.getByTestId('permission-denied')).toBeVisible(),
        page.waitForURL('/403'),
        page.waitForURL('/dashboard')
      ])
    })
  })
})