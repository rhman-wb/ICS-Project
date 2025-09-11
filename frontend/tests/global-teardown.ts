import { FullConfig } from '@playwright/test'

async function globalTeardown(config: FullConfig) {
  console.log('🧹 Starting global teardown...')

  try {
    // 清理测试数据
    console.log('🗑️ Cleaning up test data...')
    
    // 可以在这里进行清理工作
    // 例如：删除测试数据、清理临时文件等

    console.log('✅ Test data cleaned up')

  } catch (error) {
    console.error('❌ Global teardown failed:', error)
    // 不抛出错误，避免影响测试结果
  }

  console.log('✅ Global teardown completed')
}

export default globalTeardown