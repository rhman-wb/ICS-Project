/**
 * 格式化工具函数
 */

/**
 * 格式化日期时间
 * @param dateTime 日期时间字符串
 * @returns 格式化后的日期时间字符串
 */
export const formatDateTime = (dateTime?: string): string => {
  if (!dateTime) return '-'

  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return dateTime

    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  } catch (error) {
    return dateTime
  }
}

/**
 * 格式化日期
 * @param date 日期字符串
 * @returns 格式化后的日期字符串
 */
export const formatDate = (date?: string): string => {
  if (!date) return '-'

  try {
    const dateObj = new Date(date)
    if (isNaN(dateObj.getTime())) return date

    return dateObj.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    return date
  }
}

/**
 * 格式化时间
 * @param time 时间字符串
 * @returns 格式化后的时间字符串
 */
export const formatTime = (time?: string): string => {
  if (!time) return '-'

  try {
    const date = new Date(time)
    if (isNaN(date.getTime())) return time

    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  } catch (error) {
    return time
  }
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的文件大小字符串
 */
export const formatFileSize = (bytes?: number): string => {
  if (!bytes || bytes === 0) return '0 B'

  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))

  if (i === 0) {
    return `${bytes} ${sizes[i]}`
  }

  return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${sizes[i]}`
}

/**
 * 格式化数字
 * @param num 数字
 * @param decimals 小数位数
 * @returns 格式化后的数字字符串
 */
export const formatNumber = (num?: number, decimals: number = 2): string => {
  if (num === null || num === undefined || isNaN(num)) return '-'

  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  })
}

/**
 * 格式化百分比
 * @param value 数值（0-1）
 * @param decimals 小数位数
 * @returns 格式化后的百分比字符串
 */
export const formatPercentage = (value?: number, decimals: number = 1): string => {
  if (value === null || value === undefined || isNaN(value)) return '-'

  return `${(value * 100).toFixed(decimals)}%`
}

/**
 * 格式化货币
 * @param amount 金额
 * @param currency 货币符号
 * @param decimals 小数位数
 * @returns 格式化后的货币字符串
 */
export const formatCurrency = (
  amount?: number,
  currency: string = '¥',
  decimals: number = 2
): string => {
  if (amount === null || amount === undefined || isNaN(amount)) return '-'

  const formatted = amount.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  })

  return `${currency}${formatted}`
}

/**
 * 格式化相对时间（多久之前）
 * @param dateTime 日期时间字符串
 * @returns 相对时间字符串
 */
export const formatRelativeTime = (dateTime?: string): string => {
  if (!dateTime) return '-'

  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return dateTime

    const now = new Date()
    const diff = now.getTime() - date.getTime()

    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    const months = Math.floor(days / 30)
    const years = Math.floor(days / 365)

    if (years > 0) return `${years}年前`
    if (months > 0) return `${months}个月前`
    if (days > 0) return `${days}天前`
    if (hours > 0) return `${hours}小时前`
    if (minutes > 0) return `${minutes}分钟前`
    if (seconds > 30) return `${seconds}秒前`
    return '刚刚'
  } catch (error) {
    return dateTime
  }
}

/**
 * 截断文本
 * @param text 文本
 * @param maxLength 最大长度
 * @param suffix 后缀
 * @returns 截断后的文本
 */
export const truncateText = (
  text?: string,
  maxLength: number = 50,
  suffix: string = '...'
): string => {
  if (!text) return ''
  if (text.length <= maxLength) return text

  return text.substring(0, maxLength - suffix.length) + suffix
}

/**
 * 格式化电话号码
 * @param phone 电话号码
 * @returns 格式化后的电话号码
 */
export const formatPhone = (phone?: string): string => {
  if (!phone) return '-'

  // 移除所有非数字字符
  const cleaned = phone.replace(/\D/g, '')

  // 手机号码格式化 (11位)
  if (cleaned.length === 11) {
    return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1 $2 $3')
  }

  // 固定电话格式化
  if (cleaned.length === 10) {
    return cleaned.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3')
  }

  return phone
}

/**
 * 格式化身份证号
 * @param idCard 身份证号
 * @param mask 是否遮掩
 * @returns 格式化后的身份证号
 */
export const formatIdCard = (idCard?: string, mask: boolean = true): string => {
  if (!idCard) return '-'

  if (mask && idCard.length === 18) {
    return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
  }

  return idCard
}