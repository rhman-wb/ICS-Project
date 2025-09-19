import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AuditProcessMonitor from '../AuditProcessMonitor.vue'

// Mock useAuditProgress composable
const mockUseAuditProgress = {
  state: {
    taskId: 'task-123',
    status: 'running',
    progress: 65,
    totalSteps: 10,
    currentStep: 7,
    startTime: new Date('2024-01-01T10:00:00Z'),
    estimatedEndTime: new Date('2024-01-01T11:00:00Z'),
    logs: [
      { id: '1', timestamp: '2024-01-01T10:05:00Z', level: 'info', message: '开始检核产品基本信息' },
      { id: '2', timestamp: '2024-01-01T10:10:00Z', level: 'success', message: '产品基本信息检核完成' },
      { id: '3', timestamp: '2024-01-01T10:15:00Z', level: 'warning', message: '发现部分条款格式问题' }
    ],
    statistics: {
      totalFiles: 25,
      processedFiles: 16,
      passedFiles: 14,
      failedFiles: 2,
      errors: 3,
      warnings: 8
    }
  },
  progressPercent: 65,
  isCompleted: false,
  isRunning: true,
  statusText: '检核进行中',
  pauseAudit: vi.fn(),
  resumeAudit: vi.fn(),
  stopAudit: vi.fn(),
  restartAudit: vi.fn(),
  refreshProgress: vi.fn()
}

vi.mock('@/composables/audit/useAuditProgress', () => ({
  useAuditProgress: () => mockUseAuditProgress
}))

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    }
  }
})

describe('AuditProcessMonitor', () => {
  let wrapper: VueWrapper

  beforeEach(() => {
    setActivePinia(createPinia())

    wrapper = mount(AuditProcessMonitor, {
      props: {
        taskId: 'task-123'
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-card': {
            template: '<div class="card"><slot /></div>'
          },
          'a-progress': {
            template: '<div class="progress" :data-percent="percent"></div>',
            props: ['percent', 'status', 'strokeColor']
          },
          'a-statistic': {
            template: '<div class="statistic" :data-value="value"><slot /></div>',
            props: ['value', 'title', 'prefix', 'suffix']
          },
          'a-button': {
            template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'type', 'danger']
          },
          'a-space': {
            template: '<div class="space"><slot /></div>'
          },
          'a-row': {
            template: '<div class="row"><slot /></div>'
          },
          'a-col': {
            template: '<div class="col"><slot /></div>'
          },
          'a-timeline': {
            template: '<div class="timeline"><slot /></div>'
          },
          'a-timeline-item': {
            template: '<div class="timeline-item" :data-color="color"><slot /></div>',
            props: ['color']
          },
          'a-tag': {
            template: '<span class="tag" :data-color="color"><slot /></span>',
            props: ['color']
          }
        }
      }
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly with task information', () => {
    expect(wrapper.find('.audit-process-monitor').exists()).toBe(true)
    expect(wrapper.find('.progress-section').exists()).toBe(true)
    expect(wrapper.find('.statistics-section').exists()).toBe(true)
    expect(wrapper.find('.logs-section').exists()).toBe(true)
  })

  it('displays correct progress percentage', () => {
    const progress = wrapper.find('.progress')
    expect(progress.attributes('data-percent')).toBe('65')
  })

  it('shows current status correctly', () => {
    const statusDisplay = wrapper.find('.status-display')
    expect(statusDisplay.text()).toContain('检核进行中')
  })

  it('displays statistics cards with correct values', () => {
    const statistics = wrapper.findAll('.statistic')

    // Should have statistics for total files, processed files, etc.
    expect(statistics.length).toBeGreaterThan(0)

    // Check specific statistics
    const totalFilesStats = statistics.find(stat =>
      stat.attributes('data-value') === '25'
    )
    expect(totalFilesStats).toBeTruthy()
  })

  it('shows control buttons when audit is running', () => {
    const pauseButton = wrapper.find('button[data-testid="pause-button"]')
    const stopButton = wrapper.find('button[data-testid="stop-button"]')

    expect(pauseButton.exists()).toBe(true)
    expect(stopButton.exists()).toBe(true)
  })

  it('handles pause action correctly', async () => {
    const pauseButton = wrapper.find('button[data-testid="pause-button"]')
    await pauseButton.trigger('click')

    expect(mockUseAuditProgress.pauseAudit).toHaveBeenCalled()
  })

  it('handles stop action correctly', async () => {
    const stopButton = wrapper.find('button[data-testid="stop-button"]')
    await stopButton.trigger('click')

    expect(mockUseAuditProgress.stopAudit).toHaveBeenCalled()
  })

  it('handles restart action correctly', async () => {
    // Set audit as completed to show restart button
    mockUseAuditProgress.isCompleted = true
    mockUseAuditProgress.isRunning = false
    await wrapper.vm.$nextTick()

    const restartButton = wrapper.find('button[data-testid="restart-button"]')
    await restartButton.trigger('click')

    expect(mockUseAuditProgress.restartAudit).toHaveBeenCalled()
  })

  it('displays logs with correct formatting', () => {
    const timeline = wrapper.find('.timeline')
    expect(timeline.exists()).toBe(true)

    const timelineItems = wrapper.findAll('.timeline-item')
    expect(timelineItems.length).toBe(3)

    // Check log level colors
    const infoLog = timelineItems.find(item =>
      item.attributes('data-color') === 'blue'
    )
    const successLog = timelineItems.find(item =>
      item.attributes('data-color') === 'green'
    )
    const warningLog = timelineItems.find(item =>
      item.attributes('data-color') === 'orange'
    )

    expect(infoLog).toBeTruthy()
    expect(successLog).toBeTruthy()
    expect(warningLog).toBeTruthy()
  })

  it('emits pause event when pause button is clicked', async () => {
    const pauseButton = wrapper.find('button[data-testid="pause-button"]')
    await pauseButton.trigger('click')

    expect(wrapper.emitted('pause')).toBeTruthy()
    expect(wrapper.emitted('pause')[0]).toEqual(['task-123'])
  })

  it('emits stop event when stop button is clicked', async () => {
    const stopButton = wrapper.find('button[data-testid="stop-button"]')
    await stopButton.trigger('click')

    expect(wrapper.emitted('stop')).toBeTruthy()
    expect(wrapper.emitted('stop')[0]).toEqual(['task-123'])
  })

  it('shows estimated completion time', () => {
    const timeInfo = wrapper.find('.time-info')
    expect(timeInfo.exists()).toBe(true)
    expect(timeInfo.text()).toContain('预计完成时间')
  })

  it('updates progress automatically', async () => {
    // Mock progress update
    mockUseAuditProgress.state.progress = 75
    mockUseAuditProgress.progressPercent = 75
    await wrapper.vm.$nextTick()

    const progress = wrapper.find('.progress')
    expect(progress.attributes('data-percent')).toBe('75')
  })

  it('handles completed state correctly', async () => {
    mockUseAuditProgress.isCompleted = true
    mockUseAuditProgress.isRunning = false
    mockUseAuditProgress.statusText = '检核完成'
    await wrapper.vm.$nextTick()

    const statusDisplay = wrapper.find('.status-display')
    expect(statusDisplay.text()).toContain('检核完成')

    // Should show view results button
    const viewResultsButton = wrapper.find('button[data-testid="view-results-button"]')
    expect(viewResultsButton.exists()).toBe(true)
  })

  it('shows error state when audit fails', async () => {
    mockUseAuditProgress.state.status = 'failed'
    mockUseAuditProgress.statusText = '检核失败'
    await wrapper.vm.$nextTick()

    const statusDisplay = wrapper.find('.status-display')
    expect(statusDisplay.text()).toContain('检核失败')
  })

  it('refreshes progress when refresh button is clicked', async () => {
    const refreshButton = wrapper.find('button[data-testid="refresh-button"]')
    await refreshButton.trigger('click')

    expect(mockUseAuditProgress.refreshProgress).toHaveBeenCalled()
  })

  it('handles real-time log updates', async () => {
    // Add new log entry
    mockUseAuditProgress.state.logs.push({
      id: '4',
      timestamp: '2024-01-01T10:20:00Z',
      level: 'error',
      message: '发现严重错误'
    })
    await wrapper.vm.$nextTick()

    const timelineItems = wrapper.findAll('.timeline-item')
    expect(timelineItems.length).toBe(4)
  })

  it('displays step progress correctly', () => {
    const stepInfo = wrapper.find('.step-info')
    expect(stepInfo.exists()).toBe(true)
    expect(stepInfo.text()).toContain('7 / 10')
  })

  it('formats timestamps correctly', () => {
    const logs = wrapper.findAll('.timeline-item')
    expect(logs[0].text()).toContain('10:05')
  })

  it('handles empty logs gracefully', async () => {
    mockUseAuditProgress.state.logs = []
    await wrapper.vm.$nextTick()

    const timeline = wrapper.find('.timeline')
    expect(timeline.exists()).toBe(true)

    const emptyState = wrapper.find('.empty-logs')
    expect(emptyState.exists()).toBe(true)
  })
})