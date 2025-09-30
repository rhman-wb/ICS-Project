/**
 * Template Parsing Utility
 * Handles Excel file parsing and form data transformation
 */

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
  return templateType === 'backup'
    ? BACKUP_TEMPLATE_MAPPINGS
    : AGRICULTURAL_TEMPLATE_MAPPINGS
}

/**
 * Validate file format
 */
export function validateFileFormat(file: File, templateType: TemplateType): ParseError[] {
  const errors: ParseError[] = []
  const expectedFormat = templateType === 'backup' ? '.xlsx' : '.xls'
  const fileName = file.name.toLowerCase()

  if (!fileName.endsWith(expectedFormat)) {
    errors.push({
      field: 'file',
      message: `文件格式不正确，${
        templateType === 'backup' ? '备案产品' : '农险产品'
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
 * Parse template file
 * Note: This is a simplified version. In production, you would use a library like xlsx or exceljs
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

    // In a real implementation, you would use a library like xlsx to parse the Excel file
    // For now, we'll create a mock implementation that demonstrates the structure

    // Create form data based on template type
    const formData: Partial<ProductFormData> = {
      productName: '',
      reportType: '',
      templateType: templateType,
      year: new Date().getFullYear()
    }

    // Get field mappings
    const mappings = getFieldMappings(templateType)

    // Simulate parsing - in production, you would read actual Excel data
    // and map it to form fields using the mappings defined above

    // Check for missing required fields
    const missingFields = mappings
      .filter((m) => m.required)
      .filter((m) => !formData[m.formField as keyof ProductFormData])

    if (missingFields.length > 0) {
      missingFields.forEach((field) => {
        errors.push({
          field: field.formField,
          message: `必填字段 "${field.formField}" 未填写`,
          severity: 'error'
        })
      })
    }

    return {
      success: errors.length === 0,
      data: formData,
      errors,
      warnings,
      metadata: {
        fileName: file.name,
        fileSize: file.size,
        parseTime: Date.now() - startTime,
        rowsProcessed: 1
      }
    }
  } catch (error: any) {
    console.error('Template parsing error:', error)

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
  if (templateType === 'backup') {
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
  } else if (templateType === 'agricultural') {
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