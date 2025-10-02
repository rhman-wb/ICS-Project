/**
 * Template to Product Type Mapping Configuration
 * 模板类型与产品类型映射配置
 */

/**
 * Template type to product type mapping
 * 模板类型到产品类型的映射关系
 * 现在前后端使用统一的枚举值
 */
export const TEMPLATE_TO_PRODUCT_TYPE_MAP = {
  'FILING': 'FILING',
  'AGRICULTURAL': 'AGRICULTURAL'
} as const

/**
 * Template type definition
 */
export type TemplateType = keyof typeof TEMPLATE_TO_PRODUCT_TYPE_MAP

/**
 * Product type definition
 */
export type ProductType = typeof TEMPLATE_TO_PRODUCT_TYPE_MAP[TemplateType]

/**
 * Convert template type to product type
 * @param templateType - Template type
 * @returns Product type
 */
export const getProductTypeFromTemplate = (templateType: TemplateType): ProductType => {
  return TEMPLATE_TO_PRODUCT_TYPE_MAP[templateType]
}

/**
 * Convert product type to template type
 * @param productType - Product type
 * @returns Template type
 */
export const getTemplateTypeFromProduct = (productType: ProductType): TemplateType => {
  const entries = Object.entries(TEMPLATE_TO_PRODUCT_TYPE_MAP)
  const found = entries.find(([_, value]) => value === productType)
  return found?.[0] as TemplateType || 'FILING'
}

/**
 * Get display name for template type
 * @param templateType - Template type
 * @returns Display name in Chinese
 */
export const getTemplateTypeDisplayName = (templateType: TemplateType): string => {
  const displayNames: Record<TemplateType, string> = {
    'FILING': '备案产品',
    'AGRICULTURAL': '农险产品'
  }
  return displayNames[templateType] || templateType
}

/**
 * Get display name for product type
 * @param productType - Product type
 * @returns Display name in Chinese
 */
export const getProductTypeDisplayName = (productType: ProductType): string => {
  const displayNames: Record<ProductType, string> = {
    'FILING': '备案产品',
    'AGRICULTURAL': '农险产品'
  }
  return displayNames[productType] || productType
}
