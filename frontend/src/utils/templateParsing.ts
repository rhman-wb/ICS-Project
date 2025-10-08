/**
 * Template Parsing Utility
 * Handles Excel file parsing and form data transformation
 */

import * as XLSX from 'xlsx'
import logger from './logger'
import type {
  TemplateType,
  TemplateParseResult,
  ProductFormData,
  BackupProductFormData,
  AgriculturalProductFormData,
  ParseError,
  ParseWarning
} from '@/types/product/template'

/**
 * Field mapping configuration for template parsing
 */
interface FieldMapping {
  excelColumn: string
  formField: string
  required?: boolean
  transform?: (value: any) => any
}

/**
 * Backup product template field mappings
 * Maps Excel columns to form fields
 */
const BACKUP_TEMPLATE_MAPPINGS: FieldMapping[] = [
  { excelColumn: 'A', formField: 'productName', required: true },
  { excelColumn: 'B', formField: 'developmentType', required: true },
  { excelColumn: 'C', formField: 'revisionType' },
  { excelColumn: 'D', formField: 'originalProductName' },
  { excelColumn: 'E', formField: 'demonstrationClauseName' },
  { excelColumn: 'F', formField: 'productCategory', required: true },
  { excelColumn: 'G', formField: 'primaryAdditional', required: true },
  { excelColumn: 'H', formField: 'primarySituation' },
  { excelColumn: 'I', formField: 'productAttribute' },
  { excelColumn: 'J', formField: 'insurancePeriod' },
  { excelColumn: 'K', formField: 'insuranceResponsibility' },
  {
    excelColumn: 'L',
    formField: 'baseRate',
    transform: (value: any) => (value ? parseFloat(value) : undefined)
  },
  { excelColumn: 'M', formField: 'deductible' },
  { excelColumn: 'N', formField: 'compensationLimit' },
  { excelColumn: 'O', formField: 'operatingScope' },
  { excelColumn: 'P', formField: 'operatingRegion1' },
  { excelColumn: 'Q', formField: 'operatingRegion2' },
  { excelColumn: 'R', formField: 'salesArea' },
  { excelColumn: 'S', formField: 'hasElectronicPolicy' },
  { excelColumn: 'T', formField: 'isDomesticEncryption' },
  { excelColumn: 'U', formField: 'remarks' }
]

/**
 * Agricultural product template field mappings
 */
const AGRICULTURAL_TEMPLATE_MAPPINGS: FieldMapping[] = [
  { excelColumn: 'A', formField: 'productName', required: true },
  { excelColumn: 'B', formField: 'developmentMethod', required: true },
  { excelColumn: 'C', formField: 'revisionType' },
  { excelColumn: 'D', formField: 'originalProductInfo' },
  {
    excelColumn: 'E',
    formField: 'revisionCount',
    transform: (value: any) => (value ? parseInt(value, 10) : undefined)
  },
  { excelColumn: 'F', formField: 'primaryAdditional', required: true },
  { excelColumn: 'G', formField: 'primarySituation' },
  { excelColumn: 'H', formField: 'productCategory', required: true },
  { excelColumn: 'I', formField: 'productNature', required: true },
  { excelColumn: 'J', formField: 'insurancePeriod' },
  { excelColumn: 'K', formField: 'insuranceResponsibility' },
  {
    excelColumn: 'L',
    formField: 'baseRate',
    transform: (value: any) => (value ? parseFloat(value) : undefined)
  },
  { excelColumn: 'M', formField: 'compensationMethod' },
  { excelColumn: 'N', formField: 'operatingRegion' },
  { excelColumn: 'O', formField: 'isOperated' },
  { excelColumn: 'P', formField: 'hasElectronicPolicy' },
  { excelColumn: 'Q', formField: 'isDomesticCrypto' },
  { excelColumn: 'R', formField: 'remarks' }
]

/**
 * Get field mappings for template type
 */
function getFieldMappings(templateType: TemplateType): FieldMapping[] {
  return templateType === 'FILING'
    ? BACKUP_TEMPLATE_MAPPINGS
    : AGRICULTURAL_TEMPLATE_MAPPINGS
}

/**
 * Validate file format
 */
export function validateFileFormat(file: File, templateType: TemplateType): ParseError[] {
  const errors: ParseError[] = []
  const expectedFormat = templateType === 'FILING' ? '.xlsx' : '.xls'
  const fileName = file.name.toLowerCase()

  if (!fileName.endsWith(expectedFormat)) {
    errors.push({
      field: 'file',
      message: `文件格式不正确，${
        templateType === 'FILING' ? '备案产品' : '农险产品'
      }模板应为 ${expectedFormat} 格式`,
      severity: 'critical'
    })
  }

  // Check file size (max 10MB)
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    errors.push({
      field: 'file',
      message: '文件大小超过限制（最大10MB）',
      severity: 'critical'
    })
  }

  return errors
}

/**
 * Convert Excel column letter to index (A=0, B=1, ..., Z=25, AA=26, etc.)
 * 将Excel列字母转换为索引
 */
function columnToIndex(column: string): number {
  let index = 0
  for (let i = 0; i < column.length; i++) {
    index = index * 26 + (column.charCodeAt(i) - 'A'.charCodeAt(0) + 1)
  }
  return index - 1
}

/**
 * Extract form data from Excel sheet data
 * 从Excel表格数据中提取表单数据
 */
function extractFormDataFromSheet(
  jsonData: any[][],
  templateType: TemplateType
): Partial<ProductFormData> {
  const formData: Partial<ProductFormData> = {
    templateType: templateType,
    year: new Date().getFullYear()
  }

  // 跳过标题行,从第二行开始读取数据
  if (jsonData.length < 2) {
    return formData
  }

  const dataRow = jsonData[1]  // 第二行是实际数据
  const mappings = getFieldMappings(templateType)

  // 根据字段映射提取数据
  mappings.forEach((mapping) => {
    // Convert Excel column letter to index (A=0, B=1, etc.)
    const columnIndex = columnToIndex(mapping.excelColumn)
    const cellValue = dataRow[columnIndex]

    if (cellValue !== undefined && cellValue !== null && cellValue !== '') {
      const fieldKey = mapping.formField as keyof ProductFormData

      // 应用转换函数(如果有)
      if (mapping.transform) {
        formData[fieldKey] = mapping.transform(cellValue) as any
      } else {
        formData[fieldKey] = cellValue as any
      }
    }
  })

  return formData
}

/**
 * Parse template file with actual Excel reading
 */
export async function parseTemplateFile(
  file: File,
  templateType: TemplateType
): Promise<TemplateParseResult> {
  const startTime = Date.now()
  const errors: ParseError[] = []
  const warnings: ParseWarning[] = []

  try {
    // Validate file format first
    const formatErrors = validateFileFormat(file, templateType)
    if (formatErrors.length > 0) {
      return {
        success: false,
        errors: formatErrors,
        metadata: {
          fileName: file.name,
          fileSize: file.size,
          parseTime: Date.now() - startTime,
          rowsProcessed: 0
        }
      }
    }

    // Read file as ArrayBuffer
    const arrayBuffer = await file.arrayBuffer()

    // Parse Excel file with UTF-8 encoding for Chinese characters
    const workbook = XLSX.read(arrayBuffer, {
      type: 'array',
      codepage: 65001  // UTF-8 encoding to properly handle Chinese characters
    })

    // Get first worksheet
    const firstSheetName = workbook.SheetNames[0]
    const worksheet = workbook.Sheets[firstSheetName]

    // Convert to JSON with row array format
    const jsonData = XLSX.utils.sheet_to_json(worksheet, {
      header: 1,  // Return array of arrays
      raw: false,  // Keep formatting
      defval: ''   // Default value for empty cells
    })

    // Extract form data based on template type
    const formData = extractFormDataFromSheet(jsonData, templateType)

    // Validate extracted data
    const validation = validateParsedData(formData, templateType)
    errors.push(...validation.errors)
    warnings.push(...validation.warnings)

    const success = errors.filter(e => e.severity === 'error' || e.severity === 'critical').length === 0

    return {
      success,
      data: formData,
      errors,
      warnings,
      metadata: {
        fileName: file.name,
        fileSize: file.size,
        parseTime: Date.now() - startTime,
        rowsProcessed: jsonData.length
      }
    }
  } catch (error: any) {
    logger.error('Template parsing error:', error)

    errors.push({
      field: 'file',
      message: `文件解析失败: ${error.message || '未知错误'}`,
      severity: 'critical'
    })

    return {
      success: false,
      errors,
      metadata: {
        fileName: file.name,
        fileSize: file.size,
        parseTime: Date.now() - startTime,
        rowsProcessed: 0
      }
    }
  }
}

/**
 * Validate parsed data against template rules
 */
export function validateParsedData(
  data: Partial<ProductFormData>,
  templateType: TemplateType
): { errors: ParseError[]; warnings: ParseWarning[] } {
  const errors: ParseError[] = []
  const warnings: ParseWarning[] = []

  // Basic validation rules
  if (!data.productName || data.productName.trim() === '') {
    errors.push({
      field: 'productName',
      message: '产品名称不能为空',
      severity: 'error'
    })
  }

  if (data.productName && data.productName.length > 200) {
    errors.push({
      field: 'productName',
      message: '产品名称长度不能超过200个字符',
      severity: 'error'
    })
  }

  // Template-specific validation
  if (templateType === 'FILING') {
    const backupData = data as Partial<BackupProductFormData>

    if (backupData.baseRate !== undefined) {
      if (backupData.baseRate < 0 || backupData.baseRate > 100) {
        errors.push({
          field: 'baseRate',
          message: '基础费率应在0-100之间',
          severity: 'error'
        })
      }
    }
  } else if (templateType === 'AGRICULTURAL') {
    const agriculturalData = data as Partial<AgriculturalProductFormData>

    if (agriculturalData.revisionCount !== undefined) {
      if (agriculturalData.revisionCount < 0) {
        errors.push({
          field: 'revisionCount',
          message: '修订次数不能为负数',
          severity: 'error'
        })
      }
    }

    if (agriculturalData.baseRate !== undefined) {
      if (agriculturalData.baseRate < 0 || agriculturalData.baseRate > 100) {
        errors.push({
          field: 'baseRate',
          message: '基础费率应在0-100之间',
          severity: 'error'
        })
      }
    }
  }

  // Add warnings for optional but recommended fields
  if (!data.reportType) {
    warnings.push({
      field: 'reportType',
      message: '建议填写报送类型',
      suggestion: '选择合适的报送类型以便更好地分类管理'
    })
  }

  return { errors, warnings }
}

/**
 * Auto-fill form with parsed data
 */
export function autoFillForm(
  formData: Partial<ProductFormData>,
  parsedData: Partial<ProductFormData>
): Partial<ProductFormData> {
  // Merge parsed data into form data, preserving existing values if not in parsed data
  return {
    ...formData,
    ...parsedData
  }
}

/**
 * Format file size for display
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

/**
 * Format parse time for display
 */
export function formatParseTime(milliseconds: number): string {
  if (milliseconds < 1000) {
    return `${milliseconds}ms`
  }

  const seconds = (milliseconds / 1000).toFixed(2)
  return `${seconds}s`
}
