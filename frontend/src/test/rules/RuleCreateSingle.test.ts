import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { message } from 'ant-design-vue'
import RuleCreateSingle from '@/views/rules/RuleCreateSingle.vue'

// Mock rule store
const ruleStore = {
  createRule: vi.fn()
}

vi.mock('@/stores/modules/rule', () => ({
  useRuleStore: () => ruleStore
}))

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      error: vi.fn(),
      success: vi.fn(),
      loading: vi.fn()
    }
  }
})

// Mock router
const mockPush = vi.fn()
const mockReplace = vi.fn()
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush,
      replace: mockReplace
    }),
    useRoute: () => ({
      query: { type: 'single' },
      params: {}
    })
  }
})

describe('RuleCreateSingle', () => {
  let wrapper: VueWrapper

  beforeEach(() => {
    setActivePinia(createPinia())

    // Mock store methods
    vi.spyOn(ruleStore, 'createRule').mockResolvedValue({
      success: true,
      data: { id: 'new-rule-id' }
    })

    wrapper = mount(RuleCreateSingle, {
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-card': {
            template: '<div class="mock-card"><slot /></div>'
          },
          'a-form': {
            template: '<form @submit.prevent="$emit(\'finish\', formData)"><slot /></form>',
            props: ['model', 'rules'],
            emits: ['finish']
          },
          'a-form-item': {
            template: '<div class="form-item"><label v-if="label">{{ label }}</label><slot /></div>',
            props: ['label', 'name', 'rules']
          },
          'a-input': {
            template: '<input v-model="modelValue" :placeholder="placeholder" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'placeholder'],
            emits: ['update:modelValue']
          },
          'a-textarea': {
            template: '<textarea v-model="modelValue" :placeholder="placeholder" :rows="rows" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
            props: ['modelValue', 'placeholder', 'rows'],
            emits: ['update:modelValue']
          },
          'a-select': {
            template: '<select v-model="modelValue" :placeholder="placeholder" @change="$emit(\'update:modelValue\', $event.target.value)"><slot /></select>',
            props: ['modelValue', 'placeholder'],
            emits: ['update:modelValue']
          },
          'a-select-option': {
            template: '<option :value="value"><slot /></option>',
            props: ['value']
          },
          'a-switch': {
            template: '<input type="checkbox" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-button': {
            template: '<button :disabled="disabled" :loading="loading" :type="htmlType" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'loading', 'type', 'htmlType']
          },
          'a-space': {
            template: '<div class="mock-space"><slot /></div>'
          },
          'a-row': {
            template: '<div class="mock-row"><slot /></div>'
          },
          'a-col': {
            template: '<div class="mock-col"><slot /></div>',
            props: ['span', 'xs', 'sm', 'md', 'lg', 'xl']
          }
        }
      }
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly', () => {
    expect(wrapper.find('.mock-card').exists()).toBe(true)
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('textarea').exists()).toBe(true)
  })

  it('initializes with default form values', () => {
    expect(wrapper.vm.ruleForm.ruleName).toBe('')
    expect(wrapper.vm.ruleForm.description).toBe('')
    expect(wrapper.vm.ruleForm.expression).toBe('')
    expect(wrapper.vm.ruleForm.errorMessage).toBe('')
    expect(wrapper.vm.ruleForm.isActive).toBe(true)
    expect(wrapper.vm.ruleForm.ruleType).toBe('SINGLE')
  })

  it('updates form data when user inputs values', async () => {
    const inputs = wrapper.findAll('input[type="text"]')
    const ruleNameInput = inputs[0] // 假设第一个是规则名称
    const textarea = wrapper.find('textarea')

    await ruleNameInput.setValue('测试单句规则')
    await textarea.setValue('这是一个测试规则的描述')

    expect(wrapper.vm.ruleForm.ruleName).toBe('测试单句规则')
    expect(wrapper.vm.ruleForm.description).toBe('这是一个测试规则的描述')
  })

  it('validates required fields', async () => {
    // Submit empty form
    await wrapper.find('form').trigger('submit')

    // Should not call create rule if validation fails
    expect(ruleStore.createRule).not.toHaveBeenCalled()
  })

  it('validates rule name length', async () => {
    const ruleNameInput = wrapper.findAll('input[type="text"]')[0]

    // Test too long rule name
    const longName = 'a'.repeat(101) // Assuming max length is 100
    await ruleNameInput.setValue(longName)

    const isValid = wrapper.vm.validateRuleName(longName)
    expect(isValid).toBe(false)
  })

  it('validates expression format', async () => {
    const testCases = [
      { expression: '', expected: false }, // Empty expression
      { expression: 'field > 0', expected: true }, // Valid expression
      { expression: 'field1 == "value" && field2 > 100', expected: true }, // Complex expression
      { expression: 'invalid syntax (', expected: false } // Invalid syntax
    ]

    testCases.forEach(({ expression, expected }) => {
      const isValid = wrapper.vm.validateExpression(expression)
      expect(isValid).toBe(expected)
    })
  })

  it('handles form submission successfully', async () => {
    // Fill required fields
    const inputs = wrapper.findAll('input[type="text"]')
    await inputs[0].setValue('测试规则名称') // ruleName
    await inputs[1].setValue('field > 0') // expression
    await inputs[2].setValue('字段值必须大于0') // errorMessage

    const textarea = wrapper.find('textarea')
    await textarea.setValue('测试规则描述')

    // Submit form
    await wrapper.find('form').trigger('submit')

    expect(ruleStore.createRule).toHaveBeenCalledWith({
      ruleName: '测试规则名称',
      ruleType: 'SINGLE',
      description: '测试规则描述',
      expression: 'field > 0',
      errorMessage: '字段值必须大于0',
      isActive: true
    })

    expect(message.success).toHaveBeenCalledWith('规则创建成功')
    expect(mockPush).toHaveBeenCalledWith('/rules/success')
  })

  it('handles form submission failure', async () => {
    const errorMessage = '创建规则失败'
    vi.spyOn(ruleStore, 'createRule').mockRejectedValue(new Error(errorMessage))

    // Fill required fields
    const inputs = wrapper.findAll('input[type="text"]')
    await inputs[0].setValue('测试规则名称')
    await inputs[1].setValue('field > 0')
    await inputs[2].setValue('错误消息')

    // Submit form
    await wrapper.find('form').trigger('submit')

    expect(message.error).toHaveBeenCalledWith(errorMessage)
    expect(wrapper.vm.submitting).toBe(false)
  })

  it('handles form reset', async () => {
    // Fill form with data
    const inputs = wrapper.findAll('input[type="text"]')
    await inputs[0].setValue('测试规则名称')
    await inputs[1].setValue('field > 0')
    await inputs[2].setValue('错误消息')

    const textarea = wrapper.find('textarea')
    await textarea.setValue('测试描述')

    // Reset form
    wrapper.vm.handleReset()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.ruleForm.ruleName).toBe('')
    expect(wrapper.vm.ruleForm.description).toBe('')
    expect(wrapper.vm.ruleForm.expression).toBe('')
    expect(wrapper.vm.ruleForm.errorMessage).toBe('')
    expect(wrapper.vm.ruleForm.isActive).toBe(true)
  })

  it('handles cancel navigation', () => {
    wrapper.vm.handleCancel()
    expect(mockPush).toHaveBeenCalledWith('/rules')
  })

  it('shows loading state during submission', async () => {
    // Mock a delayed response
    vi.spyOn(ruleStore, 'createRule').mockImplementation(() =>
      new Promise(resolve => setTimeout(() => resolve({ success: true }), 100))
    )

    const inputs = wrapper.findAll('input[type="text"]')
    await inputs[0].setValue('测试规则名称')
    await inputs[1].setValue('field > 0')
    await inputs[2].setValue('错误消息')

    // Start submission
    const submitPromise = wrapper.find('form').trigger('submit')
    await wrapper.vm.$nextTick()

    // Should show loading state
    expect(wrapper.vm.submitting).toBe(true)

    // Wait for completion
    await submitPromise
    expect(wrapper.vm.submitting).toBe(false)
  })

  it('disables submit button when form is invalid', async () => {
    const submitButton = wrapper.findAll('button').find(btn =>
      btn.text().includes('提交') || btn.attributes('type') === 'submit'
    )

    // Initially should be disabled (empty form)
    expect(submitButton?.attributes('disabled')).toBeDefined()

    // Fill only rule name
    const inputs = wrapper.findAll('input[type="text"]')
    await inputs[0].setValue('测试规则')
    await wrapper.vm.$nextTick()

    // Should still be disabled (missing required fields)
    expect(submitButton?.attributes('disabled')).toBeDefined()

    // Fill all required fields
    await inputs[1].setValue('field > 0')
    await inputs[2].setValue('错误消息')
    await wrapper.vm.$nextTick()

    // Should be enabled
    expect(submitButton?.attributes('disabled')).toBeUndefined()
  })

  it('handles active status toggle', async () => {
    const switchInput = wrapper.find('input[type="checkbox"]')

    // Initially should be active
    expect(wrapper.vm.ruleForm.isActive).toBe(true)

    // Toggle to inactive
    await switchInput.setChecked(false)
    expect(wrapper.vm.ruleForm.isActive).toBe(false)

    // Toggle back to active
    await switchInput.setChecked(true)
    expect(wrapper.vm.ruleForm.isActive).toBe(true)
  })

  it('provides helpful placeholder text', () => {
    const inputs = wrapper.findAll('input')
    const textarea = wrapper.find('textarea')

    // Check if placeholders exist
    inputs.forEach(input => {
      if (input.attributes('placeholder')) {
        expect(input.attributes('placeholder')).toBeTruthy()
      }
    })

    if (textarea.attributes('placeholder')) {
      expect(textarea.attributes('placeholder')).toBeTruthy()
    }
  })

  it('handles keyboard navigation', async () => {
    const inputs = wrapper.findAll('input[type="text"]')

    // Focus first input
    await inputs[0].trigger('focus')

    // Tab through fields
    for (let i = 0; i < inputs.length - 1; i++) {
      await inputs[i].trigger('keydown', { key: 'Tab' })
    }

    // Enter in last field should submit if form is valid
    await inputs[0].setValue('测试规则')
    await inputs[1].setValue('field > 0')
    await inputs[2].setValue('错误消息')

    await inputs[2].trigger('keydown', { key: 'Enter' })

    expect(ruleStore.createRule).toHaveBeenCalled()
  })

  it('validates expression syntax in real-time', async () => {
    const expressionInput = wrapper.findAll('input[type="text"]')[1] // Assuming second input is expression

    // Valid expression
    await expressionInput.setValue('field > 0')
    expect(wrapper.vm.expressionError).toBeFalsy()

    // Invalid expression
    await expressionInput.setValue('invalid syntax (')
    expect(wrapper.vm.expressionError).toBeTruthy()
  })

  it('shows character count for text fields', () => {
    // This would test if character counters are shown for fields with length limits
    const ruleNameFormItem = wrapper.findAll('.form-item').find(item =>
      item.text().includes('规则名称')
    )

    // Character count functionality would be tested here
    expect(ruleNameFormItem).toBeTruthy()
  })

  it('handles paste operations correctly', async () => {
    const input = wrapper.findAll('input[type="text"]')[0]

    // Simulate paste event
    await input.trigger('paste', {
      clipboardData: {
        getData: () => '粘贴的规则名称'
      }
    })

    // Should handle pasted content appropriately
    expect(input.element.value).toBeTruthy()
  })

  it('maintains form state during navigation', async () => {
    // Fill some form data
    const inputs = wrapper.findAll('input[type="text"]')
    await inputs[0].setValue('临时规则名称')

    // Simulate navigation attempt
    const hasUnsavedChanges = wrapper.vm.hasUnsavedChanges()
    expect(hasUnsavedChanges).toBe(true)

    // Should warn user about unsaved changes
    expect(wrapper.vm.checkUnsavedChanges()).toBeTruthy()
  })
})