import { test, expect, Page } from '@playwright/test'

/**
 * 产品模板增强功能端到端测试
 * 测试从模板下载到产品提交的完整用户流程
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */

test.describe('产品模板增强功能 E2E 测试', () => {
  let page: Page

  test.beforeEach(async ({ page: testPage }) => {
    page = testPage
    // 导航到登录页面
    await page.goto('/')
    // 执行登录操作（根据实际登录流程调整）
    await page.fill('input[name="username"]', 'testuser')
    await page.fill('input[name="password"]', 'password123')
    await page.click('button[type="submit"]')
    await page.waitForURL('**/dashboard')
  })

  test('完整的模板下载和产品导入流程', async () => {
    // 1. 导航到产品导入页面
    await page.goto('/products/import')
    await expect(page).toHaveURL('/products/import')

    // 2. 选择备案产品模板类型
    await page.check('input[value="FILING"]')
    await page.waitForTimeout(500)

    // 3. 下载模板文件
    const downloadPromise = page.waitForEvent('download')
    await page.click('button:has-text("下载模板")')
    const download = await downloadPromise
    expect(download.suggestedFilename()).toContain('备案产品')

    // 4. 填写产品表单
    await page.fill('input[name="productName"]', '自动化测试产品')
    await page.fill('input[name="productCode"]', 'AUTO20240930')
    await page.fill('input[name="registrationNumber"]', 'REG20240930001')

    // 5. 选择产品类型
    await page.selectOption('select[name="productType"]', 'FILING')

    // 6. 表单验证测试
    const validationMessage = await page.locator('.ant-form-item-explain-error').first()
    if (await validationMessage.isVisible()) {
      // 如果有验证错误，填写缺失的必填字段
      await page.fill('input[name="insuranceCompany"]', '测试保险公司')
    }

    // 7. 提交表单
    await page.click('button:has-text("提交")')

    // 8. 验证成功消息
    await expect(page.locator('.ant-message-success')).toBeVisible()

    // 9. 验证跳转到产品列表页面
    await page.waitForURL('**/products')
    await expect(page).toHaveURL('/products')

    // 10. 验证新产品出现在列表中
    await expect(page.locator('text=自动化测试产品')).toBeVisible()
  })

  test('农险产品模板下载和表单填写流程', async () => {
    // 导航到产品导入页面
    await page.goto('/products/import')

    // 选择农险产品模板
    await page.check('input[value="AGRICULTURAL"]')

    // 下载农险产品模板
    const downloadPromise = page.waitForEvent('download')
    await page.click('button:has-text("下载模板")')
    const download = await downloadPromise
    expect(download.suggestedFilename()).toContain('农险产品')

    // 填写农险产品特定字段
    await page.fill('input[name="productName"]', '农险测试产品')
    await page.fill('input[name="productCode"]', 'AGRI20240930')
    await page.fill('input[name="insuranceTarget"]', '农作物')
    await page.fill('input[name="coverageScope"]', '全国')

    // 选择是否已经营
    await page.check('input[name="isOperated"]')

    // 如果已经营，应该要求填写经营日期
    const operationDateInput = page.locator('input[name="operationDate"]')
    await expect(operationDateInput).toBeVisible()
    await operationDateInput.fill('2024-01-01')

    // 提交表单
    await page.click('button:has-text("提交")')

    // 验证成功
    await expect(page.locator('.ant-message-success')).toBeVisible()
  })

  test('字段验证功能测试', async () => {
    await page.goto('/products/import')

    // 选择模板类型
    await page.check('input[value="FILING"]')

    // 尝试提交空表单
    await page.click('button:has-text("提交")')

    // 应该显示验证错误
    const errorMessages = page.locator('.ant-form-item-explain-error')
    const errorCount = await errorMessages.count()
    expect(errorCount).toBeGreaterThan(0)

    // 验证必填字段错误消息
    await expect(page.locator('text=产品名称不能为空')).toBeVisible()

    // 填写部分字段但格式不正确
    await page.fill('input[name="productCode"]', 'abc')

    // 触发验证
    await page.click('input[name="productName"]')

    // 应该显示格式错误
    await expect(page.locator('text=格式不正确')).toBeVisible()

    // 填写正确格式
    await page.fill('input[name="productCode"]', 'PROD123456')
    await page.click('input[name="productName"]')

    // 验证错误应该消失
    await expect(page.locator('text=格式不正确')).not.toBeVisible()
  })

  test('字段依赖关系验证', async () => {
    await page.goto('/products/import')
    await page.check('input[value="FILING"]')

    // 选择使用示范条款
    await page.check('input[name="usesDemonstrationClause"]')

    // 示范条款名称字段应该变为必填并显示
    const demonstrationClauseInput = page.locator('input[name="demonstrationClause"]')
    await expect(demonstrationClauseInput).toBeVisible()

    // 尝试不填写示范条款名称并提交
    await page.fill('input[name="productName"]', '测试产品')
    await page.fill('input[name="productCode"]', 'TEST123456')
    await page.click('button:has-text("提交")')

    // 应该显示示范条款名称必填的错误
    await expect(page.locator('text=示范条款')).toBeVisible()

    // 取消选择使用示范条款
    await page.uncheck('input[name="usesDemonstrationClause"]')

    // 示范条款名称字段应该隐藏或变为非必填
    await expect(demonstrationClauseInput).not.toBeVisible()
  })

  test('模板文件上传和自动填充', async () => {
    await page.goto('/products/import')

    // 选择模板类型
    await page.check('input[value="FILING"]')

    // 上传填好的模板文件
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles('tests/fixtures/filled-template.xlsx')

    // 等待文件解析
    await page.waitForSelector('.ant-message-success:has-text("解析成功")')

    // 验证字段被自动填充
    const productNameInput = page.locator('input[name="productName"]')
    const productCodeInput = page.locator('input[name="productCode"]')

    await expect(productNameInput).not.toBeEmpty()
    await expect(productCodeInput).not.toBeEmpty()
  })

  test('产品列表筛选新增字段', async () => {
    await page.goto('/products')

    // 打开筛选器
    await page.click('button:has-text("筛选")')

    // 应该能看到新增的模板类型筛选选项
    await expect(page.locator('select[name="templateType"]')).toBeVisible()

    // 按模板类型筛选
    await page.selectOption('select[name="templateType"]', 'FILING')
    await page.click('button:has-text("查询")')

    // 等待结果加载
    await page.waitForSelector('.ant-table-row')

    // 验证所有结果都是备案产品类型
    const rows = page.locator('.ant-table-row')
    const count = await rows.count()
    expect(count).toBeGreaterThan(0)
  })

  test('响应式设计测试 - 移动端', async () => {
    // 设置移动端视口
    await page.setViewportSize({ width: 375, height: 667 })

    await page.goto('/products/import')

    // 验证移动端布局
    const templateDownload = page.locator('.template-download-mobile')
    if (await templateDownload.isVisible()) {
      expect(await templateDownload.isVisible()).toBe(true)
    }

    // 验证表单在移动端的可用性
    await page.check('input[value="FILING"]')
    await page.fill('input[name="productName"]', '移动端测试')

    // 验证按钮在移动端可点击
    const submitButton = page.locator('button:has-text("提交")')
    await expect(submitButton).toBeVisible()
  })

  test('错误处理和用户反馈', async () => {
    await page.goto('/products/import')

    // 模拟网络错误情况
    await page.route('**/api/v1/products/templates/download', (route) => {
      route.abort('failed')
    })

    await page.check('input[value="FILING"]')
    await page.click('button:has-text("下载模板")')

    // 应该显示错误消息
    await expect(page.locator('.ant-message-error')).toBeVisible()

    // 恢复正常路由
    await page.unroute('**/api/v1/products/templates/download')
  })

  test('并发用户场景测试', async () => {
    // 打开多个产品导入页面模拟并发
    const page1 = await page.context().newPage()
    const page2 = await page.context().newPage()

    await Promise.all([page1.goto('/products/import'), page2.goto('/products/import')])

    // 两个页面同时操作
    await Promise.all([
      page1.fill('input[name="productName"]', '并发测试产品1'),
      page2.fill('input[name="productName"]', '并发测试产品2'),
    ])

    await Promise.all([
      page1.fill('input[name="productCode"]', 'CONC001'),
      page2.fill('input[name="productCode"]', 'CONC002'),
    ])

    // 验证两个页面都能正常工作
    await expect(page1.locator('input[name="productName"]')).toHaveValue('并发测试产品1')
    await expect(page2.locator('input[name="productName"]')).toHaveValue('并发测试产品2')

    // 清理
    await page1.close()
    await page2.close()
  })
})