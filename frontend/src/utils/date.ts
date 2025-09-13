import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 格式化时间显示
 * @param time 时间字符串或时间戳
 * @param format 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的时间字符串
 */
export const formatTime = (time: string | number | Date, format = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!time) return '-'
  return dayjs(time).format(format)
}

/**
 * 格式化相对时间（如：2小时前）
 * @param time 时间字符串或时间戳
 * @returns 相对时间字符串
 */
export const formatRelativeTime = (time: string | number | Date): string => {
  if (!time) return '-'
  return dayjs(time).fromNow()
}

/**
 * 判断是否为今天
 * @param time 时间字符串或时间戳
 * @returns boolean
 */
export const isToday = (time: string | number | Date): boolean => {
  if (!time) return false
  return dayjs(time).isSame(dayjs(), 'day')
}

/**
 * 判断是否为本周
 * @param time 时间字符串或时间戳
 * @returns boolean
 */
export const isThisWeek = (time: string | number | Date): boolean => {
  if (!time) return false
  return dayjs(time).isSame(dayjs(), 'week')
}

/**
 * 智能时间显示：
 * - 今天：显示时分
 * - 本年：显示月日时分
 * - 非本年：显示完整日期
 * @param time 时间字符串或时间戳
 * @returns 智能格式化的时间字符串
 */
export const formatSmartTime = (time: string | number | Date): string => {
  if (!time) return '-'
  
  const now = dayjs()
  const target = dayjs(time)
  
  if (target.isSame(now, 'day')) {
    return target.format('HH:mm')
  }
  
  if (target.isSame(now, 'year')) {
    return target.format('MM-DD HH:mm')
  }
  
  return target.format('YYYY-MM-DD')
}