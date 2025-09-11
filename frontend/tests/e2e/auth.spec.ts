import { test, expect } from '@playwright/test'

test.describe('用户认证流程', () => {
  test.beforeEach(async ({ page }) => {
    // 清除所有存储数据
    await page.context().clearCookies()
    await page.evaluate(() => {
      localStorage.clear()
      sessionStorage.clear()
    })
  })

  test.describe('登录页面', () => {
    test('应该正确显示登录页面', async ({ page }) => {
      await page.goto('/login')

      // 检查页面标题
      await expect(page).toHaveTitle(/保险产品智能检核系统/)

      // 检查系统标题
      await expect(page.locator('.system-title')).toHaveText('保险产品智能检核系统')

      // 检查登录表单元素
      await expect(page.locator('input[placeholder="请输入用户账号"]')).toBeVisible()
      await expect(page.locator('input[placeholder="请输入登录密码"]')).toBeVisible()
      await expect(page.locator('button[type="submit"]')).toBeVisible()
      await expect(page.locator('button[type="submit"]')).toHaveText('登录')

      // 检查版权信息
      await expect(page.locator('.footer')).toContainText('保险产品智能检核系统')
    })

    test('空表单时登录按钮应该被禁用', async ({ page }) => {
      await page.goto('/login')

      const loginButton = page.locator('button[type="submit"]')
      await expect(loginButton).toBeDisabled()
    })

    test('只填写用户名时登录按钮应该被禁用', async ({ page }) => {
      await page.goto('/login')

      await page.fill('input[placeholder="请输入用户账号"]', 'testuser')

      const loginButton = page.locator('button[type="submit"]')
      await expect(loginButton).toBeDisabled()
    })

    test('只填写密码时登录按钮应该被禁用', async ({ page }) => {
      await page.goto('/login')

      await page.fill('input[placeholder="请输入登录密码"]', 'password123')

      const loginButton = page.locator('button[type="submit"]')
      await expect(loginButton).toBeDisabled()
    })

    test('填写完整信息后登录按钮应该可用', async ({ page }) => {
      await page.goto('/login')

      await page.fill('input[placeholder="请输入用户账号"]', 'testuser')
      await page.fill('input[placeholder="请输入登录密码"]', 'password123')

      const loginButton = page.locator('button[type="submit"]')
      await expect(loginButton).toBeEnabled()
    })

    test('应该支持清除输入内容', async ({ page }) => {
      await page.goto('/login')

      const usernameInput = page.locator('input[placeholder="请输入用户账号"]')
      await usernameInput.fill('testuser')
      await expect(usernameInput).toHaveValue('testuser')

      // 点击清除按钮
      await page.locator('.ant-input-clear-icon').first().click()
      await expect(usernameInput).toHaveValue('')
    })

    test('应该支持Tab键切换焦点', async ({ page }) => {
      await page.goto('/login')

      const usernameInput = page.locator('input[placeholder="请输入用户账号"]')
      const passwordInput = page.locator('input[placeholder="请输入登录密码"]')

      await usernameInput.focus()
      await page.keyboard.press('Tab')

      await expect(passwordInput).toBeFocused()
    })

    test('应该支持Enter键提交表单', async ({ page }) => {
      await page.goto('/login')

      // Mock API响应
      await page.route('**/api/v1/auth/login', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              token: 'mock-token',
              refreshToken: 'mock-refresh-token',
              user: {
                id: 'user-123',
                username: 'testuser',
                realName: '测试用户',
                email: 'test@example.com'
              }
            }
          })
        })
      })

      await page.fill('input[placeholder="请输入用户账号"]', 'testuser')
      await page.fill('input[placeholder="请输入登录密码"]', 'password123')

      // 按Enter键提交
      await page.keyboard.press('Enter')

      // 等待跳转到工作台
      await expect(page).toHaveURL('/dashboard')
    })
  })

  test.describe('登录功能', () => {
    test('登录成功应该跳转到工作台', async ({ page }) => {
      await page.goto('/login')

      // Mock成功的登录API响应
      await page.route('**/api/v1/auth/login', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              token: 'mock-token',
              refreshToken: 'mock-refresh-token',
              user: {
                id: 'user-123',
                username: 'testuser',
                realName: '测试用户',
                email: 'test@example.com'
              }
            }
          })
        })
      })

      // 填写登录信息
      await page.fill('input[placeholder="请输入用户账号"]', 'testuser')
      await page.fill('input[placeholder="请输入登录密码"]', 'password123')

      // 点击登录按钮
      await page.click('button[type="submit"]')

      // 等待跳转到工作台
      await expect(page).toHaveURL('/dashboard')

      // 检查localStorage中是否保存了token
      const token = await page.evaluate(() => localStorage.getItem('token'))
      expect(token).toBe('mock-token')
    })

    test('登录失败应该显示错误信息', async ({ page }) => {
      await page.goto('/login')

      // Mock失败的登录API响应
      await page.route('**/api/v1/auth/login', async route => {
        await route.fulfill({
          status: 400,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            message: '用户名或密码不正确'
          })
        })
      })

      // 填写错误的登录信息
      await page.fill('input[placeholder="请输入用户账号"]', 'testuser')
      await page.fill('input[placeholder="请输入登录密码"]', 'wrongpassword')

      // 点击登录按钮
      await page.click('button[type="submit"]')

      // 检查错误信息
      await expect(page.locator('.ant-message-error')).toContainText('用户名或密码不正确')

      // 确保没有跳转
      await expect(page).toHaveURL('/login')
    })

    test('登录过程中应该显示加载状态', async ({ page }) => {
      await page.goto('/login')

      // Mock慢速的登录API响应
      await page.route('**/api/v1/auth/login', async route => {
        await new Promise(resolve => setTimeout(resolve, 1000))
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              token: 'mock-token',
              refreshToken: 'mock-refresh-token',
              user: {
                id: 'user-123',
                username: 'testuser',
                realName: '测试用户',
                email: 'test@example.com'
              }
            }
          })
        })
      })

      await page.fill('input[placeholder="请输入用户账号"]', 'testuser')
      await page.fill('input[placeholder="请输入登录密码"]', 'password123')

      // 点击登录按钮
      const loginButton = page.locator('button[type="submit"]')
      await loginButton.click()

      // 检查加载状态
      await expect(loginButton).toHaveClass(/ant-btn-loading/)
      await expect(loginButton).toHaveText('登录中...')
      await expect(loginButton).toBeDisabled()

      // 等待登录完成
      await expect(page).toHaveURL('/dashboard')
    })

    test('网络错误应该显示相应提示', async ({ page }) => {
      await page.goto('/login')

      // Mock网络错误
      await page.route('**/api/v1/auth/login', async route => {
        await route.abort('failed')
      })

      await page.fill('input[placeholder="请输入用户账号"]', 'testuser')
      await page.fill('input[placeholder="请输入登录密码"]', 'password123')

      await page.click('button[type="submit"]')

      // 检查网络错误提示
      await expect(page.locator('.ant-message-error')).toContainText('网络连接失败')
    })
  })

  test.describe('工作台页面', () => {
    test.beforeEach(async ({ page }) => {
      // 设置已登录状态
      await page.addInitScript(() => {
        localStorage.setItem('token', 'mock-token')
        localStorage.setItem('userInfo', JSON.stringify({
          id: 'user-123',
          username: 'testuser',
          realName: '测试用户',
          email: 'test@example.com'
        }))
      })

      // Mock工作台相关API
      await page.route('**/api/v1/dashboard/**', async route => {
        const url = route.request().url()
        
        if (url.includes('/summary')) {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                productCount: 156,
                documentCount: 1247,
                ruleCount: 89,
                errorCount: 23
              }
            })
          })
        } else if (url.includes('/tasks')) {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                records: [
                  {
                    id: '1',
                    name: '财产保险产品A检核任务',
                    submitter: '胡潇禹',
                    submitTime: '2022/10/31',
                    type: '产品检核',
                    status: '进行中'
                  }
                ],
                total: 1
              }
            })
          })
        }
      })
    })

    test('应该正确显示工作台页面', async ({ page }) => {
      await page.goto('/dashboard')

      // 检查顶部导航
      await expect(page.locator('.dashboard-header')).toBeVisible()
      await expect(page.locator('.system-brand')).toHaveText('产品检核系统')

      // 检查主导航菜单
      await expect(page.locator('.header-nav')).toBeVisible()
      await expect(page.locator('text=主页')).toBeVisible()
      await expect(page.locator('text=产品管理')).toBeVisible()
      await expect(page.locator('text=规则管理')).toBeVisible()
      await expect(page.locator('text=设置')).toBeVisible()

      // 检查用户信息
      await expect(page.locator('.user-info')).toContainText('测试用户')
    })

    test('应该显示汇总数据', async ({ page }) => {
      await page.goto('/dashboard')

      // 等待数据加载
      await page.waitForSelector('.summary-data-section')

      // 检查汇总数据卡片
      await expect(page.locator('text=产品数量')).toBeVisible()
      await expect(page.locator('text=要件数量')).toBeVisible()
      await expect(page.locator('text=规则数量')).toBeVisible()
      await expect(page.locator('text=已检核错误数量')).toBeVisible()

      // 检查数据值
      await expect(page.locator('text=156')).toBeVisible() // 产品数量
      await expect(page.locator('text=1247')).toBeVisible() // 要件数量
      await expect(page.locator('text=89')).toBeVisible() // 规则数量
      await expect(page.locator('text=23')).toBeVisible() // 错误数量
    })

    test('应该显示我的关注任务', async ({ page }) => {
      await page.goto('/dashboard')

      // 等待任务列表加载
      await page.waitForSelector('.my-focus-section')

      // 检查任务列表
      await expect(page.locator('text=我的关注')).toBeVisible()
      await expect(page.locator('text=财产保险产品A检核任务')).toBeVisible()
      await expect(page.locator('text=胡潇禹')).toBeVisible()
      await expect(page.locator('text=2022/10/31')).toBeVisible()
    })

    test('应该支持任务搜索', async ({ page }) => {
      await page.goto('/dashboard')

      // 等待页面加载
      await page.waitForSelector('.my-focus-section')

      // 输入搜索关键词
      const searchInput = page.locator('input[placeholder="请输入任务名称"]')
      await searchInput.fill('财产保险')

      // 按Enter搜索
      await searchInput.press('Enter')

      // 检查搜索结果
      await expect(page.locator('text=财产保险产品A检核任务')).toBeVisible()
    })

    test('应该显示快速开始导航', async ({ page }) => {
      await page.goto('/dashboard')

      // 检查快速开始区域
      await expect(page.locator('text=快速开始')).toBeVisible()
      await expect(page.locator('text=导入产品')).toBeVisible()
      await expect(page.locator('text=创建规则')).toBeVisible()
      await expect(page.locator('text=批量导入')).toBeVisible()
      await expect(page.locator('text=下载模板')).toBeVisible()
    })

    test('应该支持时间筛选', async ({ page }) => {
      await page.goto('/dashboard')

      // 等待页面加载
      await page.waitForSelector('.summary-data-section')

      // 点击不同的时间筛选选项
      await page.click('text=本日')
      await page.waitForTimeout(500)

      await page.click('text=本周')
      await page.waitForTimeout(500)

      await page.click('text=本月')
      await page.waitForTimeout(500)

      // 回到总计
      await page.click('text=总计')
      await page.waitForTimeout(500)

      // 检查数据是否正确显示
      await expect(page.locator('text=156')).toBeVisible()
    })
  })

  test.describe('退出登录', () => {
    test.beforeEach(async ({ page }) => {
      // 设置已登录状态
      await page.addInitScript(() => {
        localStorage.setItem('token', 'mock-token')
        localStorage.setItem('userInfo', JSON.stringify({
          id: 'user-123',
          username: 'testuser',
          realName: '测试用户',
          email: 'test@example.com'
        }))
      })

      // Mock退出登录API
      await page.route('**/api/v1/auth/logout', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            message: '退出登录成功'
          })
        })
      })
    })

    test('应该能够成功退出登录', async ({ page }) => {
      await page.goto('/dashboard')

      // 点击用户下拉菜单
      await page.click('.user-info')

      // 点击退出登录
      await page.click('text=退出登录')

      // 等待跳转到登录页
      await expect(page).toHaveURL('/login')

      // 检查localStorage是否被清除
      const token = await page.evaluate(() => localStorage.getItem('token'))
      expect(token).toBeNull()

      const userInfo = await page.evaluate(() => localStorage.getItem('userInfo'))
      expect(userInfo).toBeNull()
    })
  })

  test.describe('路由守卫', () => {
    test('未登录访问受保护页面应该跳转到登录页', async ({ page }) => {
      // 直接访问工作台页面
      await page.goto('/dashboard')

      // 应该被重定向到登录页
      await expect(page).toHaveURL('/login')
    })

    test('已登录访问登录页应该跳转到工作台', async ({ page }) => {
      // 设置已登录状态
      await page.addInitScript(() => {
        localStorage.setItem('token', 'mock-token')
        localStorage.setItem('userInfo', JSON.stringify({
          id: 'user-123',
          username: 'testuser',
          realName: '测试用户',
          email: 'test@example.com'
        }))
      })

      // 访问登录页
      await page.goto('/login')

      // 应该被重定向到工作台
      await expect(page).toHaveURL('/dashboard')
    })

    test('token过期应该自动跳转到登录页', async ({ page }) => {
      // 设置已登录状态
      await page.addInitScript(() => {
        localStorage.setItem('token', 'expired-token')
        localStorage.setItem('userInfo', JSON.stringify({
          id: 'user-123',
          username: 'testuser',
          realName: '测试用户',
          email: 'test@example.com'
        }))
      })

      // Mock token验证失败
      await page.route('**/api/v1/**', async route => {
        await route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            message: 'Token已过期'
          })
        })
      })

      // 访问工作台
      await page.goto('/dashboard')

      // 应该被重定向到登录页
      await expect(page).toHaveURL('/login')

      // 检查是否显示过期提示
      await expect(page.locator('.ant-message-warning')).toContainText('登录已过期')
    })
  })

  test.describe('响应式设计', () => {
    test('在移动设备上应该正确显示', async ({ page }) => {
      // 设置移动设备视口
      await page.setViewportSize({ width: 375, height: 667 })

      await page.goto('/login')

      // 检查登录面板在移动设备上的显示
      const loginPanel = page.locator('.login-panel')
      await expect(loginPanel).toBeVisible()

      // 检查响应式样式
      const panelBox = await loginPanel.boundingBox()
      expect(panelBox?.width).toBeLessThan(375)
    })

    test('在平板设备上应该正确显示', async ({ page }) => {
      // 设置平板设备视口
      await page.setViewportSize({ width: 768, height: 1024 })

      await page.goto('/login')

      // 检查登录面板在平板设备上的显示
      const loginPanel = page.locator('.login-panel')
      await expect(loginPanel).toBeVisible()

      // 检查响应式样式
      const panelBox = await loginPanel.boundingBox()
      expect(panelBox?.width).toBeLessThan(768)
    })
  })

  test.describe('无障碍访问', () => {
    test('应该支持键盘导航', async ({ page }) => {
      await page.goto('/login')

      // 使用Tab键导航
      await page.keyboard.press('Tab')
      await expect(page.locator('input[placeholder="请输入用户账号"]')).toBeFocused()

      await page.keyboard.press('Tab')
      await expect(page.locator('input[placeholder="请输入登录密码"]')).toBeFocused()

      await page.keyboard.press('Tab')
      await expect(page.locator('button[type="submit"]')).toBeFocused()
    })

    test('应该有正确的ARIA标签', async ({ page }) => {
      await page.goto('/login')

      // 检查输入框的ARIA标签
      const usernameInput = page.locator('input[placeholder="请输入用户账号"]')
      await expect(usernameInput).toHaveAttribute('aria-label', '用户账号')

      const passwordInput = page.locator('input[placeholder="请输入登录密码"]')
      await expect(passwordInput).toHaveAttribute('aria-label', '登录密码')

      // 检查按钮的角色
      const loginButton = page.locator('button[type="submit"]')
      await expect(loginButton).toHaveAttribute('role', 'button')
    })
  })
})