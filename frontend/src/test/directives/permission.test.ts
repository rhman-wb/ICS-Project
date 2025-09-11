import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { permission } from '@/directives/permission'

// Mock the auth store
vi.mock('@/stores/modules/auth', () => ({
  useAuthStore: vi.fn(() => ({
    hasPermission: vi.fn(),
    hasRole: vi.fn()
  }))
}))

import { useAuthStore } from '@/stores/modules/auth'

// Mock component for testing directive
const TestComponent = {
  template: `
    <div>
      <div v-permission="'user:create'" data-testid="single-permission">Single Permission</div>
      <div v-permission="['user:create', 'user:update']" data-testid="multiple-permissions">Multiple Permissions (OR)</div>
      <div v-permission.all="['user:create', 'user:update']" data-testid="all-permissions">All Permissions (AND)</div>
      <div v-permission:role="'admin'" data-testid="admin-role">Admin Role</div>
      <div v-permission:role="['admin', 'manager']" data-testid="multiple-roles">Multiple Roles</div>
      <div v-permission.hide="'user:delete'" data-testid="hide-permission">Hide Permission</div>
    </div>
  `,
  directives: {
    permission
  }
}

describe('Permission Directive', () => {
  let mockAuthStore: any
  
  beforeEach(() => {
    setActivePinia(createPinia())
    mockAuthStore = {
      hasPermission: vi.fn(),
      hasRole: vi.fn()
    }
    vi.mocked(useAuthStore).mockReturnValue(mockAuthStore)
  })

  it('should show element when user has required permission', () => {
    mockAuthStore.hasPermission.mockReturnValue(true)
    
    const wrapper = mount(TestComponent)
    
    expect(wrapper.find('[data-testid="single-permission"]').exists()).toBe(true)
    expect(mockAuthStore.hasPermission).toHaveBeenCalledWith('user:create')
  })

  it('should remove element when user lacks required permission', () => {
    mockAuthStore.hasPermission.mockReturnValue(false)
    
    const wrapper = mount(TestComponent)
    
    expect(wrapper.find('[data-testid="single-permission"]').exists()).toBe(false)
  })

  it('should handle multiple permissions with OR logic', () => {
    // Mock to return false for first permission, true for second
    mockAuthStore.hasPermission.mockImplementation((permission: string) => {
      if (permission === 'user:create') return false
      if (permission === 'user:update') return true
      return false
    })
    
    const wrapper = mount(TestComponent)
    
    expect(wrapper.find('[data-testid="multiple-permissions"]').exists()).toBe(true)
  })

  it('should handle multiple permissions with AND logic', () => {
    // Mock to return true for first permission, false for second
    mockAuthStore.hasPermission.mockImplementation((permission: string) => {
      if (permission === 'user:create') return true
      if (permission === 'user:update') return false
      return false
    })
    
    const wrapper = mount(TestComponent)
    
    expect(wrapper.find('[data-testid="all-permissions"]').exists()).toBe(false)
  })

  it('should show element when user has required role', () => {
    mockAuthStore.hasRole.mockReturnValue(true)
    
    const wrapper = mount(TestComponent)
    
    expect(wrapper.find('[data-testid="admin-role"]').exists()).toBe(true)
    expect(mockAuthStore.hasRole).toHaveBeenCalledWith('admin')
  })

  it('should handle multiple roles', () => {
    // Mock to return false for admin, true for manager
    mockAuthStore.hasRole.mockImplementation((role: string) => {
      if (role === 'admin') return false
      if (role === 'manager') return true
      return false
    })
    
    const wrapper = mount(TestComponent)
    
    expect(wrapper.find('[data-testid="multiple-roles"]').exists()).toBe(true)
  })

  it('should hide element instead of removing when using hide modifier', () => {
    mockAuthStore.hasPermission.mockReturnValue(false)
    
    const wrapper = mount(TestComponent)
    const element = wrapper.find('[data-testid="hide-permission"]')
    
    expect(element.exists()).toBe(true)
    expect(element.element.style.display).toBe('none')
  })
})