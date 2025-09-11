import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PermissionWrapper from '@/components/common/PermissionWrapper.vue'

// Mock the auth store
vi.mock('@/stores/modules/auth', () => ({
  useAuthStore: vi.fn(() => ({
    hasPermission: vi.fn(),
    hasRole: vi.fn()
  }))
}))

import { useAuthStore } from '@/stores/modules/auth'

describe('PermissionWrapper', () => {
  let mockAuthStore: any
  
  beforeEach(() => {
    setActivePinia(createPinia())
    mockAuthStore = {
      hasPermission: vi.fn(),
      hasRole: vi.fn()
    }
    vi.mocked(useAuthStore).mockReturnValue(mockAuthStore)
  })

  it('should render content when user has required permissions', () => {
    mockAuthStore.hasPermission.mockReturnValue(true)
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        permissions: 'user:create'
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(true)
    expect(wrapper.find('.permission-denied').exists()).toBe(false)
  })

  it('should not render content when user lacks required permissions', () => {
    mockAuthStore.hasPermission.mockReturnValue(false)
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        permissions: 'user:create'
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(false)
  })

  it('should show fallback content when showFallback is true', () => {
    mockAuthStore.hasPermission.mockReturnValue(false)
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        permissions: 'user:create',
        showFallback: true,
        fallbackText: 'Access Denied'
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(false)
    expect(wrapper.find('.permission-denied').exists()).toBe(true)
    expect(wrapper.find('.permission-denied__text').text()).toBe('Access Denied')
  })

  it('should handle multiple permissions with some mode (OR logic)', () => {
    mockAuthStore.hasPermission
      .mockReturnValueOnce(false) // user:create
      .mockReturnValueOnce(true)  // user:update
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        permissions: ['user:create', 'user:update'],
        mode: 'some'
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(true)
  })

  it('should handle multiple permissions with every mode (AND logic)', () => {
    mockAuthStore.hasPermission
      .mockReturnValueOnce(true)  // user:create
      .mockReturnValueOnce(false) // user:update
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        permissions: ['user:create', 'user:update'],
        mode: 'every'
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(false)
  })

  it('should handle role-based access control', () => {
    mockAuthStore.hasRole.mockReturnValue(true)
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        roles: 'admin'
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(true)
    expect(mockAuthStore.hasRole).toHaveBeenCalledWith('admin')
  })

  it('should handle both permissions and roles', () => {
    mockAuthStore.hasPermission.mockReturnValue(true)
    mockAuthStore.hasRole.mockReturnValue(true)
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        permissions: 'user:create',
        roles: 'admin'
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(true)
  })

  it('should not render when user has permission but lacks role', () => {
    mockAuthStore.hasPermission.mockReturnValue(true)
    mockAuthStore.hasRole.mockReturnValue(false)
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        permissions: 'user:create',
        roles: 'admin'
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(false)
  })

  it('should render custom fallback content', () => {
    mockAuthStore.hasPermission.mockReturnValue(false)
    
    const wrapper = mount(PermissionWrapper, {
      props: {
        permissions: 'user:create',
        showFallback: true
      },
      slots: {
        default: '<div data-testid="content">Protected Content</div>',
        fallback: '<div data-testid="custom-fallback">Custom Fallback</div>'
      }
    })
    
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="custom-fallback"]').exists()).toBe(true)
  })
})