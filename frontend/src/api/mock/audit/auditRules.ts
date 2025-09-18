/**
 * 检核规则Mock数据
 * 符合原型页面展示的CC001-CC017规则数据
 */

import type { AuditRule } from './index'

export class AuditRulesMockService {

  /**
   * 获取检核规则列表Mock数据
   */
  getAuditRules(params: {
    ruleSource?: string
    insuranceTypes?: string[]
    managementDepartment?: string
    applicableDocumentTypes?: string[]
    applicableChapter?: string
    effectiveStatus?: string
    auditStatus?: string
    startTime?: string
    endTime?: string
    keyword?: string
    page?: number
    size?: number
  } = {}): Promise<{
    records: AuditRule[]
    total: number
    current: number
    size: number
  }> {

    let rules: AuditRule[] = [
      {
        id: 'rule_cc001',
        code: 'CC001',
        name: '保险责任条款完整性检核',
        source: '行政监管措施决定书（京银保监发〔2022〕36号）',
        applicableInsuranceType: ['农业险', '财产险'],
        applicableChapter: '保险责任',
        managementDepartment: '产品创新部',
        ruleType: 'single',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险责任条款是否完整，包含所有必要的保险范围描述'
      },
      {
        id: 'rule_cc002',
        code: 'CC002',
        name: '除外责任条款规范性检核',
        source: '行政监管措施决定书（京银保监发〔2022〕36号）',
        applicableInsuranceType: ['农业险'],
        applicableChapter: '除外责任',
        managementDepartment: '产品创新部',
        ruleType: 'double',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查除外责任条款表述是否清晰明确，符合监管要求'
      },
      {
        id: 'rule_cc003',
        code: 'CC003',
        name: '保险金额确定方式检核',
        source: '保险法实施条例',
        applicableInsuranceType: ['农业险', '财产险'],
        applicableChapter: '保险金额',
        managementDepartment: '风险管控部',
        ruleType: 'format',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险金额确定方式是否明确，计算方法是否合理'
      },
      {
        id: 'rule_cc004',
        code: 'CC004',
        name: '保险费率厘定依据检核',
        source: '保险法实施条例',
        applicableInsuranceType: ['农业险'],
        applicableChapter: '保险费率',
        managementDepartment: '风险管控部',
        ruleType: 'advanced',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险费率厘定是否有充分的精算依据和数据支撑'
      },
      {
        id: 'rule_cc005',
        code: 'CC005',
        name: '赔偿处理时效规定检核',
        source: '行政监管措施决定书（京银保监发〔2022〕36号）',
        applicableInsuranceType: ['农业险', '财产险'],
        applicableChapter: '赔偿处理',
        managementDepartment: '理赔服务部',
        ruleType: 'single',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查赔偿处理时效规定是否符合监管要求'
      },
      {
        id: 'rule_cc006',
        code: 'CC006',
        name: '争议处理条款合规性检核',
        source: '保险法',
        applicableInsuranceType: ['农业险', '财产险', '意健险'],
        applicableChapter: '争议处理',
        managementDepartment: '法律事务部',
        ruleType: 'double',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查争议处理条款是否符合法律规定'
      },
      {
        id: 'rule_cc007',
        code: 'CC007',
        name: '保险标的描述准确性检核',
        source: '保险法实施条例',
        applicableInsuranceType: ['农业险'],
        applicableChapter: '总则',
        managementDepartment: '产品创新部',
        ruleType: 'format',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险标的描述是否准确、具体'
      },
      {
        id: 'rule_cc008',
        code: 'CC008',
        name: '保险期间定义明确性检核',
        source: '保险法',
        applicableInsuranceType: ['农业险', '财产险'],
        applicableChapter: '总则',
        managementDepartment: '产品创新部',
        ruleType: 'single',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险期间定义是否明确、无歧义'
      },
      {
        id: 'rule_cc009',
        code: 'CC009',
        name: '投保人义务告知完整性检核',
        source: '保险法',
        applicableInsuranceType: ['农业险', '财产险', '意健险'],
        applicableChapter: '投保人义务',
        managementDepartment: '核保审核部',
        ruleType: 'double',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查投保人义务告知条款是否完整'
      },
      {
        id: 'rule_cc010',
        code: 'CC010',
        name: '保险人义务履行规范检核',
        source: '保险法',
        applicableInsuranceType: ['农业险'],
        applicableChapter: '保险人义务',
        managementDepartment: '核保审核部',
        ruleType: 'advanced',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险人义务履行是否规范'
      },
      {
        id: 'rule_cc011',
        code: 'CC011',
        name: '保险事故定义准确性检核',
        source: '行政监管措施决定书（京银保监发〔2022〕36号）',
        applicableInsuranceType: ['农业险', '财产险'],
        applicableChapter: '保险事故',
        managementDepartment: '产品创新部',
        ruleType: 'format',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险事故定义是否准确、具体'
      },
      {
        id: 'rule_cc012',
        code: 'CC012',
        name: '保险合同解除条件检核',
        source: '保险法',
        applicableInsuranceType: ['农业险', '财产险'],
        applicableChapter: '合同解除',
        managementDepartment: '法律事务部',
        ruleType: 'single',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险合同解除条件是否符合法律规定'
      },
      {
        id: 'rule_cc013',
        code: 'CC013',
        name: '保险条款格式规范性检核',
        source: '保险法实施条例',
        applicableInsuranceType: ['农业险', '财产险', '意健险'],
        applicableChapter: '条款格式',
        managementDepartment: '产品创新部',
        ruleType: 'format',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险条款格式是否规范，表述是否清晰'
      },
      {
        id: 'rule_cc014',
        code: 'CC014',
        name: '特别约定条款合理性检核',
        source: '保险法',
        applicableInsuranceType: ['农业险'],
        applicableChapter: '特别约定',
        managementDepartment: '产品创新部',
        ruleType: 'double',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查特别约定条款是否合理、必要'
      },
      {
        id: 'rule_cc015',
        code: 'CC015',
        name: '保险理赔材料要求检核',
        source: '行政监管措施决定书（京银保监发〔2022〕36号）',
        applicableInsuranceType: ['农业险', '财产险'],
        applicableChapter: '理赔材料',
        managementDepartment: '理赔服务部',
        ruleType: 'advanced',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险理赔材料要求是否合理、明确'
      },
      {
        id: 'rule_cc016',
        code: 'CC016',
        name: '保险条款术语定义检核',
        source: '保险法实施条例',
        applicableInsuranceType: ['农业险', '财产险', '意健险'],
        applicableChapter: '术语定义',
        managementDepartment: '产品创新部',
        ruleType: 'single',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险条款中术语定义是否准确、统一'
      },
      {
        id: 'rule_cc017',
        code: 'CC017',
        name: '保险产品备案材料完整性检核',
        source: '行政监管措施决定书（京银保监发〔2022〕36号）',
        applicableInsuranceType: ['农业险'],
        applicableChapter: '备案材料',
        managementDepartment: '合规风控部',
        ruleType: 'advanced',
        effectiveStatus: 'active',
        auditStatus: 'approved',
        effectiveStartTime: new Date('2022-01-01'),
        description: '检查保险产品备案材料是否完整、符合要求'
      }
    ]

    // 模拟搜索筛选
    if (params.keyword) {
      rules = rules.filter(rule =>
        rule.name.includes(params.keyword!) ||
        rule.code.includes(params.keyword!) ||
        rule.description.includes(params.keyword!)
      )
    }

    if (params.ruleSource) {
      rules = rules.filter(rule => rule.source === params.ruleSource)
    }

    if (params.insuranceTypes && params.insuranceTypes.length > 0) {
      rules = rules.filter(rule =>
        params.insuranceTypes!.some(type => rule.applicableInsuranceType.includes(type))
      )
    }

    if (params.managementDepartment) {
      rules = rules.filter(rule => rule.managementDepartment === params.managementDepartment)
    }

    if (params.applicableChapter) {
      rules = rules.filter(rule => rule.applicableChapter === params.applicableChapter)
    }

    if (params.effectiveStatus) {
      rules = rules.filter(rule => rule.effectiveStatus === params.effectiveStatus)
    }

    if (params.auditStatus) {
      rules = rules.filter(rule => rule.auditStatus === params.auditStatus)
    }

    // 分页处理
    const page = params.page || 1
    const size = params.size || 10
    const start = (page - 1) * size
    const end = start + size
    const paginatedRules = rules.slice(start, end)

    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          records: paginatedRules,
          total: rules.length,
          current: page,
          size: size
        })
      }, 300) // 模拟网络延迟
    })
  }

  /**
   * 获取规则筛选选项Mock数据
   */
  getRuleFilterOptions(): Promise<{
    ruleSources: string[]
    insuranceTypes: string[]
    managementDepartments: string[]
    applicableChapters: string[]
    effectiveStatuses: { label: string; value: string }[]
    auditStatuses: { label: string; value: string }[]
  }> {
    const options = {
      ruleSources: [
        '保险法',
        '保险法实施条例',
        '行政监管措施决定书（京银保监发〔2022〕36号）'
      ],
      insuranceTypes: [
        '农业险',
        '财产险',
        '意健险',
        '车险',
        '责任险'
      ],
      managementDepartments: [
        '产品创新部',
        '风险管控部',
        '理赔服务部',
        '法律事务部',
        '核保审核部',
        '合规风控部'
      ],
      applicableChapters: [
        '总则',
        '保险责任',
        '除外责任',
        '保险金额',
        '保险费率',
        '赔偿处理',
        '争议处理',
        '投保人义务',
        '保险人义务',
        '保险事故',
        '合同解除',
        '条款格式',
        '特别约定',
        '理赔材料',
        '术语定义',
        '备案材料'
      ],
      effectiveStatuses: [
        { label: '有效', value: 'active' },
        { label: '无效', value: 'inactive' }
      ],
      auditStatuses: [
        { label: '通过审核', value: 'approved' },
        { label: '待审核', value: 'pending' },
        { label: '审核不通过', value: 'rejected' }
      ]
    }

    return new Promise(resolve => {
      setTimeout(() => {
        resolve(options)
      }, 200)
    })
  }

  /**
   * 根据ID获取规则详情
   */
  getRuleById(ruleId: string): Promise<AuditRule | null> {
    return new Promise((resolve) => {
      this.getAuditRules().then(response => {
        const rule = response.records.find(r => r.id === ruleId)
        setTimeout(() => {
          resolve(rule || null)
        }, 150)
      })
    })
  }

  /**
   * 批量获取规则详情
   */
  getRulesByIds(ruleIds: string[]): Promise<AuditRule[]> {
    return new Promise((resolve) => {
      this.getAuditRules().then(response => {
        const rules = response.records.filter(r => ruleIds.includes(r.id))
        setTimeout(() => {
          resolve(rules)
        }, 250)
      })
    })
  }
}

// 导出Mock服务实例
export const auditRulesMockService = new AuditRulesMockService()