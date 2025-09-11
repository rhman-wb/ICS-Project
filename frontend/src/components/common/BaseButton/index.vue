<template>
  <a-button
    :type="type"
    :size="size"
    :loading="loading"
    :disabled="disabled"
    :block="block"
    :danger="danger"
    @click="handleClick"
  >
    <slot />
  </a-button>
</template>

<script setup lang="ts">
import { ButtonProps } from 'ant-design-vue'

interface Props {
  type?: ButtonProps['type']
  size?: ButtonProps['size']
  loading?: boolean
  disabled?: boolean
  block?: boolean
  danger?: boolean
}

interface Emits {
  click: [event: MouseEvent]
}

defineOptions({
  name: 'BaseButton'
})

withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'middle',
  loading: false,
  disabled: false,
  block: false,
  danger: false
})

const emit = defineEmits<Emits>()

const handleClick = (event: MouseEvent) => {
  emit('click', event)
}
</script>