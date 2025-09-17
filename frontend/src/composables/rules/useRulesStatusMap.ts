/**
 * 规则状态映射配置
 */

export interface StatusMapItem {
  text: string
  color: string
  bgColor?: string
  borderColor?: string
}

/**
 * 规则类型映射
 */
export const ruleTypeMap: Record<string, StatusMapItem> = {
  SINGLE: {
    text: '单句规则',
    color: 'blue'
  },
  DOUBLE: {
    text: '双句规则',
    color: 'green'
  },
  FORMAT: {
    text: '格式规则',
    color: 'orange'
  },
  ADVANCED: {
    text: '高级规则',
    color: 'purple'
  }
}

/**
 * 审核状态映射
 */
export const auditStatusMap: Record<string, StatusMapItem> = {
  TECH_REVIEW: {
    text: '待技术评估',
    color: 'default'
  },
  TO_SUBMIT_OA: {
    text: '待提交OA审核',
    color: 'warning'
  },
  SUBMITTED_OA: {
    text: '已提交OA审核',
    color: 'processing'
  },
  OA_APPROVED: {
    text: 'OA审核通过',
    color: 'success'
  },
  OA_REJECTED: {
    text: 'OA审核驳回',
    color: 'error'
  }
}

/**
 * 有效状态映射
 */
export const effectiveStatusMap: Record<string, StatusMapItem> = {
  VALID: {
    text: '有效',
    color: 'success'
  },
  INVALID: {
    text: '无效',
    color: 'error'
  },
  TO_BE_DEPLOYED: {
    text: '待开发上线',
    color: 'warning'
  }
}

/**
 * 获取状态文本
 */
export const getRuleTypeText = (type: string): string => {
  return ruleTypeMap[type]?.text || type
}

export const getAuditStatusText = (status: string): string => {
  return auditStatusMap[status]?.text || status
}

export const getEffectiveStatusText = (status: string): string => {
  return effectiveStatusMap[status]?.text || status
}

/**
 * 获取状态颜色
 */
export const getRuleTypeColor = (type: string): string => {
  return ruleTypeMap[type]?.color || 'default'
}

export const getAuditStatusColor = (status: string): string => {
  return auditStatusMap[status]?.color || 'default'
}

export const getEffectiveStatusColor = (status: string): string => {
  return effectiveStatusMap[status]?.color || 'default'
}

/**
 * 状态选项列表（用于下拉选择）
 */
export const ruleTypeOptions = Object.entries(ruleTypeMap).map(([key, value]) => ({
  label: value.text,
  value: key
}))

export const auditStatusOptions = Object.entries(auditStatusMap).map(([key, value]) => ({
  label: value.text,
  value: key
}))

export const effectiveStatusOptions = Object.entries(effectiveStatusMap).map(([key, value]) => ({
  label: value.text,
  value: key
}))

/**
 * 根据状态获取完整的状态信息
 */
export const getStatusInfo = (type: 'ruleType' | 'auditStatus' | 'effectiveStatus', status: string): StatusMapItem | null => {
  switch (type) {
    case 'ruleType':
      return ruleTypeMap[status] || null
    case 'auditStatus':
      return auditStatusMap[status] || null
    case 'effectiveStatus':
      return effectiveStatusMap[status] || null
    default:
      return null
  }
}