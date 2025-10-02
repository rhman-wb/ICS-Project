/**
 * Logger utility for environment-aware logging
 * 日志工具 - 根据环境自动启用/禁用日志输出
 */

type LogLevel = 'debug' | 'info' | 'warn' | 'error'

interface LoggerConfig {
  /** 是否启用日志输出 */
  enabled: boolean
  /** 最小日志级别 */
  minLevel: LogLevel
  /** 是否显示时间戳 */
  showTimestamp: boolean
  /** 是否显示日志级别 */
  showLevel: boolean
}

const LOG_LEVELS: Record<LogLevel, number> = {
  debug: 0,
  info: 1,
  warn: 2,
  error: 3
}

class Logger {
  private config: LoggerConfig

  constructor() {
    // 默认配置：开发环境启用，生产环境禁用
    this.config = {
      enabled: import.meta.env.DEV,
      minLevel: import.meta.env.DEV ? 'debug' : 'error',
      showTimestamp: import.meta.env.DEV,
      showLevel: true
    }
  }

  /**
   * 配置日志行为
   */
  configure(config: Partial<LoggerConfig>): void {
    this.config = { ...this.config, ...config }
  }

  /**
   * 格式化日志消息
   */
  private formatMessage(level: LogLevel, args: any[]): any[] {
    const parts: any[] = []

    if (this.config.showTimestamp) {
      parts.push(`[${new Date().toISOString()}]`)
    }

    if (this.config.showLevel) {
      parts.push(`[${level.toUpperCase()}]`)
    }

    return [...parts, ...args]
  }

  /**
   * 检查是否应该输出日志
   */
  private shouldLog(level: LogLevel): boolean {
    if (!this.config.enabled) return false
    return LOG_LEVELS[level] >= LOG_LEVELS[this.config.minLevel]
  }

  /**
   * Debug级别日志
   */
  debug(...args: any[]): void {
    if (this.shouldLog('debug')) {
      console.log(...this.formatMessage('debug', args))
    }
  }

  /**
   * Info级别日志
   */
  info(...args: any[]): void {
    if (this.shouldLog('info')) {
      console.log(...this.formatMessage('info', args))
    }
  }

  /**
   * Warning级别日志
   */
  warn(...args: any[]): void {
    if (this.shouldLog('warn')) {
      console.warn(...this.formatMessage('warn', args))
    }
  }

  /**
   * Error级别日志
   */
  error(...args: any[]): void {
    if (this.shouldLog('error')) {
      console.error(...this.formatMessage('error', args))
    }
  }

  /**
   * 分组日志开始
   */
  group(label: string): void {
    if (this.config.enabled) {
      console.group(label)
    }
  }

  /**
   * 分组日志结束
   */
  groupEnd(): void {
    if (this.config.enabled) {
      console.groupEnd()
    }
  }

  /**
   * 表格形式输出
   */
  table(data: any): void {
    if (this.config.enabled) {
      console.table(data)
    }
  }
}

// 导出单例实例
export const logger = new Logger()

// 默认导出
export default logger
