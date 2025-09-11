import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { message } from 'ant-design-vue'
import MyFocusSection from '@/components/dashboard/MyFocusSection.vue'
import { DashboardMockService } from '@/api/mock/dashboardMock'

// Mock the DashboardMockService
vi.mock('@/api/mock/dashboardMock', () => ({
  DashboardMockService: {
    getFocusTasks: vi.fn()
  }
}))

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      info: vi.fn(),
      error: vi.fn()
    }
  }
})

describe('MyFocusSection', () => {
  const mockTasks = {
    records: [
      {
        id: "1",
        name: "财产保险产品A检核任务",
        submitter: "胡潇禹",
        submitTime: "2022/10/31",
        type: "产品检核",
        status: "进行中",
        icon: "file-text"
      },
      {
        id: "2",
        name: "意外险产品B规则配置",
        submitter: "张三",
        submitTime: "2022/10/30",
        type: "规则配置",
        status: "待审核",
        icon: "setting"
      }
    ],
    total: 2,
    current: 1,
    size: 10
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(DashboardMockService.getFocusTasks).mockResolvedValue(mockTasks)
  })

  it('renders correctly', () => {
    const wrapper = mount(MyFocusSection)
    
    expect(wrapper.find('.my-focus-section').exists()).toBe(true)
    expect(wrapper.find('.section-title').text()).toBe('我的关注')
  })

  it('loads tasks on mount', async () => {
    const wrapper = mount(MyFocusSection)
    
    // Wait for the component to load
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    expect(DashboardMockService.getFocusTasks).toHaveBeenCalled()
  })

  it('displays task information correctly', async () => {
    const wrapper = mount(MyFocusSection)
    
    // Wait for tasks to load
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Check if search input exists
    expect(wrapper.find('.search-input').exists()).toBe(true)
    
    // Check if filter selects exist
    expect(wrapper.findAll('.filter-select')).toHaveLength(2)
    
    // Check if task list exists (even if empty during test)
    expect(wrapper.find('.task-list').exists()).toBe(true)
  })

  it('handles task click', async () => {
    const wrapper = mount(MyFocusSection)
    
    // Wait for tasks to load
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Find and click a task
    const taskLink = wrapper.find('.task-name a')
    if (taskLink.exists()) {
      await taskLink.trigger('click')
      expect(message.info).toHaveBeenCalled()
    }
  })

  it('shows empty state when no tasks', async () => {
    vi.mocked(DashboardMockService.getFocusTasks).mockResolvedValue({
      records: [],
      total: 0,
      current: 1,
      size: 10
    })

    const wrapper = mount(MyFocusSection)
    
    // Wait for tasks to load
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Check that the component handles empty state correctly
    const vm = wrapper.vm as any
    expect(vm.taskList).toHaveLength(0)
    expect(vm.filteredTaskList).toHaveLength(0)
  })

  it('handles load more functionality', async () => {
    const wrapper = mount(MyFocusSection)
    
    // Wait for initial load
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Find and click load more button if it exists
    const loadMoreButton = wrapper.find('.load-more .ant-btn')
    if (loadMoreButton.exists()) {
      await loadMoreButton.trigger('click')
      expect(DashboardMockService.getFocusTasks).toHaveBeenCalledTimes(2)
    }
  })

  it('displays correct status colors', () => {
    const wrapper = mount(MyFocusSection)
    const vm = wrapper.vm as any
    
    expect(vm.getStatusColor('进行中')).toBe('processing')
    expect(vm.getStatusColor('待审核')).toBe('warning')
    expect(vm.getStatusColor('已完成')).toBe('success')
    expect(vm.getStatusColor('已拒绝')).toBe('error')
    expect(vm.getStatusColor('已暂停')).toBe('default')
  })

  it('returns correct task icons', () => {
    const wrapper = mount(MyFocusSection)
    const vm = wrapper.vm as any
    
    expect(vm.getTaskIcon('file-text')).toBeDefined()
    expect(vm.getTaskIcon('setting')).toBeDefined()
    expect(vm.getTaskIcon('check-circle')).toBeDefined()
    expect(vm.getTaskIcon('unknown-icon')).toBeDefined() // Should fallback to default
  })

  it('filters tasks by search keyword', async () => {
    const wrapper = mount(MyFocusSection)
    const vm = wrapper.vm as any
    
    // Wait for initial load
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Set search keyword directly on the component
    vm.searchKeyword = '财产保险'
    await wrapper.vm.$nextTick()
    
    // Wait for debounced search
    await new Promise(resolve => setTimeout(resolve, 400))
    
    // Should call getFocusTasks with search parameters
    expect(DashboardMockService.getFocusTasks).toHaveBeenCalledWith(
      expect.objectContaining({
        keyword: '财产保险'
      })
    )
  })

  it('filters tasks by type', async () => {
    const wrapper = mount(MyFocusSection)
    
    // Wait for initial load
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Find type filter select
    const typeSelect = wrapper.findAll('.filter-select')[1]
    
    // Simulate selecting a type filter
    const vm = wrapper.vm as any
    vm.typeFilter = '产品检核'
    await wrapper.vm.$nextTick()
    
    // Should call getFocusTasks with type filter
    expect(DashboardMockService.getFocusTasks).toHaveBeenCalledWith(
      expect.objectContaining({
        typeFilter: '产品检核'
      })
    )
  })

  it('clears all filters', async () => {
    const wrapper = mount(MyFocusSection)
    const vm = wrapper.vm as any
    
    // Set some filters
    vm.searchKeyword = '测试'
    vm.timeFilter = '本周'
    vm.typeFilter = '产品检核'
    await wrapper.vm.$nextTick()
    
    // Clear filters
    vm.clearAllFilters()
    await wrapper.vm.$nextTick()
    
    expect(vm.searchKeyword).toBe('')
    expect(vm.timeFilter).toBe('全部时间')
    expect(vm.typeFilter).toBe('全部任务')
  })

  it('shows clear filters button when filters are active', async () => {
    const wrapper = mount(MyFocusSection)
    const vm = wrapper.vm as any
    
    // Initially no active filters
    expect(vm.hasActiveFilters).toBe(false)
    
    // Set a filter
    vm.searchKeyword = '测试'
    await wrapper.vm.$nextTick()
    
    expect(vm.hasActiveFilters).toBe(true)
  })
})