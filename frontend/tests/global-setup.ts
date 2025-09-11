import { chromium, FullConfig } from '@playwright/test'

async function globalSetup(config: FullConfig) {
  console.log('🚀 Starting global setup...')

  // 启动浏览器进行预热
  const browser = await chromium.launch()
  const page = await browser.newPage()

  try {
    // 预热应用
    console.log('🔥 Warming up application...')
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle' })
    
    // 等待应用完全加载
    await page.waitForSelector('body', { timeout: 30000 })
    
    console.log('✅ Application warmed up successfully')

    // 可以在这里进行其他全局设置
    // 例如：创建测试数据、设置环境变量等

  } catch (error) {
    console.error('❌ Global setup failed:', error)
    throw error
  } finally {
    await browser.close()
  }

  console.log('✅ Global setup completed')
}

export default globalSetup