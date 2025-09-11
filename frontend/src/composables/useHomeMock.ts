/**
 * Dashboard Home Mock Data Composable
 * 
 * 统一数据入口，为组件提供只读的 mock 数据
 * 便于后续替换为真实 API 而不影响视图层
 */

import { readonly } from 'vue'
import { homeMock, type HomeMock } from '@/api/mock/home'

/**
 * 使用首页 mock 数据的组合函数
 * 
 * @returns {HomeMock} 只读的首页数据对象
 */
export function useHomeMock(): Readonly<HomeMock> {
  // 使用 readonly 确保数据不会被意外修改
  // 后续替换为 API 调用时，只需修改此函数内部实现
  return readonly(homeMock)
}