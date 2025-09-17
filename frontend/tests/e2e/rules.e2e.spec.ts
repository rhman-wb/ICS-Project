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
    await page.fill('input[placeholder="请输入用户账号"]', 'admin')
    await page.fill('input[placeholder="请输入登录密码"]', 'password123')
    await page.click('button[type="submit"]')

    // 等待导航到仪表板
    await page.waitForURL('/dashboard')
    await expect(page.locator('.dashboard-container')).toBeVisible()
  })

  test.describe('规则列表页面', () => {
    test('应该正确显示规则列表页面', async ({ page }) => {
      // 从工作台导航到规则管理
      await page.click('a[href="/rules"]')
      await page.waitForURL('/rules')

      // 检查页面标题
      await expect(page).toHaveTitle(/规则管理/)

      // 检查主要页面元素
      await expect(page.locator('.page-header')).toContainText('规则管理')
      await expect(page.locator('.search-form')).toBeVisible()
      await expect(page.locator('.rule-table')).toBeVisible()
      await expect(page.locator('.pagination')).toBeVisible()

      // 检查搜索表单
      await expect(page.locator('input[placeholder="请输入规则名称"]')).toBeVisible()
      await expect(page.locator('.rule-type-select')).toBeVisible()
      await expect(page.locator('.audit-status-select')).toBeVisible()
      await expect(page.locator('.effective-status-select')).toBeVisible()

      // 检查操作按钮
      await expect(page.locator('button:has-text("搜索")')).toBeVisible()
      await expect(page.locator('button:has-text("重置")')).toBeVisible()
      await expect(page.locator('button:has-text("新增规则")')).toBeVisible()
      await expect(page.locator('button:has-text("批量删除")')).toBeVisible()
    })

    test('应该支持规则搜索功能', async ({ page }) => {
      await page.goto('/rules')

      // 输入搜索关键词
      await page.fill('input[placeholder="请输入规则名称"]', '测试规则')
      await page.click('button:has-text("搜索")')

      // 等待搜索结果
      await page.waitForTimeout(1000)

      // 验证URL包含搜索参数
      expect(page.url()).toContain('ruleName=测试规则')

      // 验证表格更新
      await expect(page.locator('.rule-table .ant-table-tbody')).toBeVisible()
    })

    test('应该支持筛选功能', async ({ page }) => {
      await page.goto('/rules')

      // 选择规则类型
      await page.click('.rule-type-select')
      await page.click('.ant-select-dropdown li:has-text("单句规则")')

      // 选择审核状态
      await page.click('.audit-status-select')
      await page.click('.ant-select-dropdown li:has-text("已通过")')

      // 点击搜索
      await page.click('button:has-text("搜索")')

      // 等待筛选结果
      await page.waitForTimeout(1000)

      // 验证筛选参数反映在URL中
      expect(page.url()).toContain('ruleType=SINGLE')
      expect(page.url()).toContain('auditStatus=APPROVED')
    })

    test('应该支持重置筛选功能', async ({ page }) => {
      await page.goto('/rules')

      // 填写筛选条件
      await page.fill('input[placeholder="请输入规则名称"]', '测试')
      await page.click('.rule-type-select')
      await page.click('.ant-select-dropdown li:has-text("单句规则")')

      // 点击重置
      await page.click('button:has-text("重置")')

      // 验证表单被清空
      await expect(page.locator('input[placeholder="请输入规则名称"]')).toHaveValue('')
      await expect(page.locator('.rule-type-select .ant-select-selection-item')).not.toBeVisible()
    })

    test('应该支持规则列表分页', async ({ page }) => {
      await page.goto('/rules')

      // 等待表格加载
      await page.waitForSelector('.rule-table .ant-table-tbody tr')

      // 检查分页组件
      await expect(page.locator('.ant-pagination')).toBeVisible()

      // 如果有多页，测试分页功能
      const nextButton = page.locator('.ant-pagination-next')
      if (await nextButton.isEnabled()) {
        await nextButton.click()
        await page.waitForTimeout(1000)

        // 验证URL包含分页参数
        expect(page.url()).toContain('page=2')
      }
    })

    test('应该支持表格行选择', async ({ page }) => {
      await page.goto('/rules')

      // 等待表格加载
      await page.waitForSelector('.rule-table .ant-table-tbody tr')

      // 选择第一行
      const firstRowCheckbox = page.locator('.rule-table .ant-table-tbody tr:first-child .ant-checkbox-input')
      await firstRowCheckbox.click()

      // 验证行被选中
      await expect(firstRowCheckbox).toBeChecked()

      // 验证批量操作按钮状态变化
      await expect(page.locator('button:has-text("批量删除")')).not.toBeDisabled()
      await expect(page.locator('button:has-text("提交OA审核")')).not.toBeDisabled()
    })
  })

  test.describe('规则创建流程', () => {
    test('应该完成单句规则创建的完整流程', async ({ page }) => {
      await page.goto('/rules')

      // 点击新增规则按钮
      await page.click('button:has-text("新增规则")')

      // 选择单句规则
      await page.click('button:has-text("单句规则")')

      // 等待导航到创建页面
      await page.waitForURL('/rules/new?type=single')

      // 检查页面标题
      await expect(page.locator('.page-header')).toContainText('创建单句规则')

      // 填写规则表单
      await page.fill('input[placeholder="请输入规则名称"]', 'E2E测试单句规则')
      await page.fill('textarea[placeholder="请输入规则描述"]', '这是一个E2E测试创建的单句规则')
      await page.fill('input[placeholder="请输入规则表达式"]', 'field > 0')
      await page.fill('input[placeholder="请输入错误提示信息"]', '字段值必须大于0')

      // 验证表单填写
      await expect(page.locator('input[placeholder="请输入规则名称"]')).toHaveValue('E2E测试单句规则')
      await expect(page.locator('textarea[placeholder="请输入规则描述"]')).toHaveValue('这是一个E2E测试创建的单句规则')

      // 提交表单
      await page.click('button[type="submit"]:has-text("提交")')

      // 等待成功页面
      await page.waitForURL('/rules/success')
      await expect(page.locator('.success-message')).toContainText('规则创建成功')

      // 返回规则列表
      await page.click('button:has-text("返回列表")')
      await page.waitForURL('/rules')

      // 验证新规则出现在列表中
      await expect(page.locator('.rule-table')).toContainText('E2E测试单句规则')
    })

    test('应该验证表单必填字段', async ({ page }) => {
      await page.goto('/rules/new?type=single')

      // 尝试提交空表单
      await page.click('button[type="submit"]:has-text("提交")')

      // 验证错误提示
      await expect(page.locator('.ant-form-item-explain-error')).toBeVisible()
      await expect(page.locator('.ant-form-item-explain-error')).toContainText('请输入')

      // 验证页面没有跳转
      expect(page.url()).toContain('/rules/new?type=single')
    })

    test('应该支持表单重置功能', async ({ page }) => {
      await page.goto('/rules/new?type=single')

      // 填写一些表单数据
      await page.fill('input[placeholder="请输入规则名称"]', '测试规则')
      await page.fill('textarea[placeholder="请输入规则描述"]', '测试描述')

      // 点击重置按钮
      await page.click('button:has-text("重置")')

      // 验证表单被清空
      await expect(page.locator('input[placeholder="请输入规则名称"]')).toHaveValue('')
      await expect(page.locator('textarea[placeholder="请输入规则描述"]')).toHaveValue('')
    })

    test('应该支持取消操作', async ({ page }) => {
      await page.goto('/rules/new?type=single')

      // 填写一些数据
      await page.fill('input[placeholder="请输入规则名称"]', '测试规则')

      // 点击取消按钮
      await page.click('button:has-text("取消")')

      // 验证返回到规则列表
      await page.waitForURL('/rules')
      await expect(page.locator('.page-header')).toContainText('规则管理')
    })
  })

  test.describe('规则检核跳转流程', () => {
    test('应该完成检核跳转的完整流程', async ({ page }) => {
      await page.goto('/rules')

      // 点击检核跳转按钮
      await page.click('button:has-text("检核跳转")')

      // 等待导航到检核跳转页面
      await page.waitForURL('/rules/jump')

      // 检查页面标题
      await expect(page.locator('.page-header')).toContainText('检核跳转')

      // 检查筛选表单
      await expect(page.locator('input[placeholder="请输入关键词搜索"]')).toBeVisible()
      await expect(page.locator('.rule-type-filter')).toBeVisible()
      await expect(page.locator('.category-filter')).toBeVisible()

      // 检查规则表格
      await expect(page.locator('.jump-rule-table')).toBeVisible()

      // 等待规则数据加载
      await page.waitForSelector('.jump-rule-table .ant-table-tbody tr')

      // 选择一些规则
      const firstRuleCheckbox = page.locator('.jump-rule-table .ant-table-tbody tr:first-child .ant-checkbox-input')
      await firstRuleCheckbox.click()
      await expect(firstRuleCheckbox).toBeChecked()

      const secondRuleCheckbox = page.locator('.jump-rule-table .ant-table-tbody tr:nth-child(2) .ant-checkbox-input')
      await secondRuleCheckbox.click()
      await expect(secondRuleCheckbox).toBeChecked()

      // 验证选中计数
      await expect(page.locator('.selected-count')).toContainText('已选择 2 条规则')

      // 点击确定按钮
      await page.click('button[type="primary"]:has-text("确定")')

      // 验证导航到检核执行页面
      await page.waitForURL('/audit/execute*')
      expect(page.url()).toContain('ruleIds=')
      expect(page.url()).toContain('source=rules')
    })

    test('应该支持规则筛选功能', async ({ page }) => {
      await page.goto('/rules/jump')

      // 输入关键词搜索
      await page.fill('input[placeholder="请输入关键词搜索"]', '检核')
      await page.click('button:has-text("搜索")')

      // 等待搜索结果
      await page.waitForTimeout(1000)

      // 验证表格更新
      await expect(page.locator('.jump-rule-table .ant-table-tbody')).toBeVisible()

      // 选择规则类型筛选
      await page.click('.rule-type-filter')
      await page.click('.ant-select-dropdown li:has-text("单句规则")')
      await page.click('button:has-text("搜索")')

      // 等待筛选结果
      await page.waitForTimeout(1000)
    })

    test('应该防止未选择规则时确定', async ({ page }) => {
      await page.goto('/rules/jump')

      // 不选择任何规则，直接点击确定
      await page.click('button[type="primary"]:has-text("确定")')

      // 验证错误提示
      await expect(page.locator('.ant-message-error')).toContainText('请至少选择一条规则')

      // 验证页面没有跳转
      expect(page.url()).toContain('/rules/jump')
    })

    test('应该支持取消操作', async ({ page }) => {
      await page.goto('/rules/jump')

      // 选择一些规则
      const firstRuleCheckbox = page.locator('.jump-rule-table .ant-table-tbody tr:first-child .ant-checkbox-input')
      await firstRuleCheckbox.click()

      // 点击取消按钮
      await page.click('button:has-text("取消")')

      // 验证返回到规则列表
      await page.waitForURL('/rules')
      await expect(page.locator('.page-header')).toContainText('规则管理')
    })
  })

  test.describe('规则操作功能', () => {
    test('应该支持规则编辑功能', async ({ page }) => {
      await page.goto('/rules')

      // 等待表格加载
      await page.waitForSelector('.rule-table .ant-table-tbody tr')

      // 点击第一行的编辑按钮
      await page.click('.rule-table .ant-table-tbody tr:first-child .edit-button')

      // 验证导航到编辑页面
      await page.waitForURL('/rules/*/edit')
      await expect(page.locator('.page-header')).toContainText('编辑规则')

      // 修改规则名称
      const ruleNameInput = page.locator('input[placeholder="请输入规则名称"]')
      await ruleNameInput.fill('')
      await ruleNameInput.fill('修改后的规则名称')

      // 提交修改
      await page.click('button[type="submit"]:has-text("提交")')

      // 等待成功提示
      await expect(page.locator('.ant-message-success')).toContainText('规则更新成功')

      // 返回列表验证修改
      await page.goto('/rules')
      await expect(page.locator('.rule-table')).toContainText('修改后的规则名称')
    })

    test('应该支持规则删除功能', async ({ page }) => {
      await page.goto('/rules')

      // 等待表格加载
      await page.waitForSelector('.rule-table .ant-table-tbody tr')

      // 点击第一行的删除按钮
      await page.click('.rule-table .ant-table-tbody tr:first-child .delete-button')

      // 确认删除
      await page.click('.ant-popconfirm .ant-btn-dangerous:has-text("确定")')

      // 等待成功提示
      await expect(page.locator('.ant-message-success')).toContainText('删除成功')

      // 验证表格数据更新
      await page.waitForTimeout(1000)
    })

    test('应该支持规则复制功能', async ({ page }) => {
      await page.goto('/rules')

      // 等待表格加载
      await page.waitForSelector('.rule-table .ant-table-tbody tr')

      // 点击第一行的复制按钮
      await page.click('.rule-table .ant-table-tbody tr:first-child .copy-button')

      // 等待成功提示
      await expect(page.locator('.ant-message-success')).toContainText('复制成功')

      // 验证新规则出现在列表中
      await page.waitForTimeout(1000)
    })

    test('应该支持规则关注功能', async ({ page }) => {
      await page.goto('/rules')

      // 等待表格加载
      await page.waitForSelector('.rule-table .ant-table-tbody tr')

      // 点击第一行的关注按钮
      await page.click('.rule-table .ant-table-tbody tr:first-child .follow-button')

      // 等待成功提示
      await expect(page.locator('.ant-message-success')).toContainText('关注状态更新成功')

      // 验证按钮状态变化
      await expect(page.locator('.rule-table .ant-table-tbody tr:first-child .follow-button')).toContainText('已关注')
    })
  })

  test.describe('批量操作功能', () => {
    test('应该支持批量删除功能', async ({ page }) => {
      await page.goto('/rules')

      // 等待表格加载
      await page.waitForSelector('.rule-table .ant-table-tbody tr')

      // 选择多行
      await page.click('.rule-table .ant-table-thead .ant-checkbox-input') // 全选

      // 点击批量删除
      await page.click('button:has-text("批量删除")')

      // 确认删除
      await page.click('.ant-modal .ant-btn-dangerous:has-text("确定")')

      // 等待成功提示
      await expect(page.locator('.ant-message-success')).toContainText('批量删除成功')
    })

    test('应该支持批量提交OA审核', async ({ page }) => {
      await page.goto('/rules')

      // 等待表格加载
      await page.waitForSelector('.rule-table .ant-table-tbody tr')

      // 选择多行
      await page.click('.rule-table .ant-table-tbody tr:first-child .ant-checkbox-input')
      await page.click('.rule-table .ant-table-tbody tr:nth-child(2) .ant-checkbox-input')

      // 点击提交OA审核
      await page.click('button:has-text("提交OA审核")')

      // 填写提交信息
      await page.fill('.oa-submit-modal textarea[placeholder="请输入提交说明"]', '批量提交规则审核')

      // 确认提交
      await page.click('.oa-submit-modal .ant-btn-primary:has-text("确定")')

      // 等待成功提示
      await expect(page.locator('.ant-message-success')).toContainText('提交OA审核成功')
    })
  })

  test.describe('响应式设计测试', () => {
    test('应该在移动设备上正常显示', async ({ page }) => {
      // 设置移动设备视口
      await page.setViewportSize({ width: 375, height: 667 })

      await page.goto('/rules')

      // 验证移动端布局
      await expect(page.locator('.mobile-search-toggle')).toBeVisible()

      // 点击展开搜索
      await page.click('.mobile-search-toggle')
      await expect(page.locator('.search-form')).toBeVisible()

      // 验证表格在移动端的响应式处理
      await expect(page.locator('.rule-table .ant-table-scroll')).toBeVisible()
    })
  })

  test.describe('无障碍访问测试', () => {
    test('应该支持键盘导航', async ({ page }) => {
      await page.goto('/rules')

      // 使用Tab键导航
      await page.keyboard.press('Tab') // 聚焦到第一个可聚焦元素
      await page.keyboard.press('Tab') // 下一个元素
      await page.keyboard.press('Tab') // 再下一个元素

      // 验证焦点可见性
      const focusedElement = await page.locator(':focus')
      await expect(focusedElement).toBeVisible()

      // 使用Enter键激活按钮
      await page.keyboard.press('Enter')
    })

    test('应该提供适当的ARIA标签', async ({ page }) => {
      await page.goto('/rules')

      // 验证重要元素有适当的ARIA标签
      await expect(page.locator('[role="main"]')).toBeVisible()
      await expect(page.locator('[aria-label="搜索表单"]')).toBeVisible()
      await expect(page.locator('[aria-label="规则列表"]')).toBeVisible()
    })
  })
})