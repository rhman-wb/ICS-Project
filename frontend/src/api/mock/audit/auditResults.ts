/**
 * 检核结果Mock数据
 * 按要件类型分类的详细检核结果数据
 */

import type { AuditResult, AuditStatistics } from './index'

export class AuditResultsMockService {

  /**
   * 获取检核结果Mock数据
   */
  getAuditResults(params: {
    taskId: string
    documentType?: 'terms' | 'feasibility' | 'comparison'
    ruleType?: 'single' | 'double' | 'format' | 'advanced'
    severity?: 'high' | 'medium' | 'low'
    page?: number
    size?: number
  }): Promise<{
    records: AuditResult[]
    total: number
    current: number
    size: number
    statistics: AuditStatistics[]
  }> {

    // 模拟检核结果数据
    let results: AuditResult[] = [
      // 条款检核结果
      {
        id: 'result_001',
        taskId: params.taskId,
        productId: 'prod_001',
        documentType: 'terms',
        ruleId: 'rule_cc001',
        ruleCode: 'CC001',
        suggestion: '保险责任条款缺少对自然灾害的具体描述，建议补充台风、冰雹等自然灾害的保险责任范围。',
        applicableChapter: '保险责任',
        ruleSource: '行政监管措施决定书（京银保监发〔2022〕36号）',
        managementDepartment: '产品创新部',
        ruleType: '单句规则',
        originalContent: '本保险承保因下列原因直接造成的保险标的的损失：(一)火灾、爆炸；(二)雷击、暴风、暴雨、洪水、暴雪、冰雹、冻灾、旱灾；',
        pageNumber: '第1页',
        highlightPosition: {
          start: 45,
          end: 78,
          page: 1
        },
        severity: 'high',
        createdTime: new Date()
      },
      {
        id: 'result_002',
        taskId: params.taskId,
        productId: 'prod_001',
        documentType: 'terms',
        ruleId: 'rule_cc002',
        ruleCode: 'CC002',
        suggestion: '除外责任条款表述不够明确，"其他不可抗力"表述过于宽泛，建议具体列明除外责任范围。',
        applicableChapter: '除外责任',
        ruleSource: '行政监管措施决定书（京银保监发〔2022〕36号）',
        managementDepartment: '产品创新部',
        ruleType: '双句规则',
        originalContent: '下列原因造成的损失、费用，保险人不负责赔偿：(一)投保人、被保险人的故意行为；(二)其他不可抗力因素。',
        pageNumber: '第2页',
        highlightPosition: {
          start: 32,
          end: 42,
          page: 2
        },
        severity: 'medium',
        createdTime: new Date()
      },
      {
        id: 'result_003',
        taskId: params.taskId,
        productId: 'prod_001',
        documentType: 'terms',
        ruleId: 'rule_cc013',
        ruleCode: 'CC013',
        suggestion: '条款格式不规范，章节标题使用不一致，建议统一使用阿拉伯数字编号。',
        applicableChapter: '条款格式',
        ruleSource: '保险法实施条例',
        managementDepartment: '产品创新部',
        ruleType: '格式规则',
        originalContent: '第一章 总则\n1.1 保险合同构成\n二、保险责任',
        pageNumber: '第1页',
        severity: 'low',
        createdTime: new Date()
      },

      // 可行性报告检核结果
      {
        id: 'result_004',
        taskId: params.taskId,
        productId: 'prod_001',
        documentType: 'feasibility',
        ruleId: 'rule_cc004',
        ruleCode: 'CC004',
        suggestion: '费率厘定缺乏充分的历史数据支撑，建议补充至少3年的历史赔付数据分析。',
        applicableChapter: '保险费率',
        ruleSource: '保险法实施条例',
        managementDepartment: '风险管控部',
        ruleType: '高级规则',
        originalContent: '根据近两年的赔付经验，确定基础费率为0.5%，考虑到风险因子调整后，最终费率为0.6%。',
        pageNumber: '第5页',
        severity: 'high',
        createdTime: new Date()
      },
      {
        id: 'result_005',
        taskId: params.taskId,
        productId: 'prod_001',
        documentType: 'feasibility',
        ruleId: 'rule_cc015',
        ruleCode: 'CC015',
        suggestion: '理赔材料清单不够详细，建议明确各类损失情况下所需的具体材料清单。',
        applicableChapter: '理赔材料',
        ruleSource: '行政监管措施决定书（京银保监发〔2022〕36号）',
        managementDepartment: '理赔服务部',
        ruleType: '高级规则',
        originalContent: '理赔时需提供：1、保险单或保险凭证；2、损失证明材料；3、其他相关材料。',
        pageNumber: '第8页',
        severity: 'medium',
        createdTime: new Date()
      },

      // 前后修改对比表检核结果
      {
        id: 'result_006',
        taskId: params.taskId,
        productId: 'prod_001',
        documentType: 'comparison',
        ruleId: 'rule_cc008',
        ruleCode: 'CC008',
        suggestion: '保险期间修改前后不一致，修改后的表述存在歧义，建议明确保险期间的起止时点。',
        applicableChapter: '总则',
        ruleSource: '保险法',
        managementDepartment: '产品创新部',
        ruleType: '单句规则',
        originalContent: '修改前：保险期间为一年\n修改后：保险期间自保险责任开始之日起至保险责任终止',
        pageNumber: '第1页',
        severity: 'high',
        createdTime: new Date()
      },
      {
        id: 'result_007',
        taskId: params.taskId,
        productId: 'prod_001',
        documentType: 'comparison',
        ruleId: 'rule_cc012',
        ruleCode: 'CC012',
        suggestion: '合同解除条件修改不当，新增的解除条件可能违反保险法相关规定。',
        applicableChapter: '合同解除',
        ruleSource: '保险法',
        managementDepartment: '法律事务部',
        ruleType: '单句规则',
        originalContent: '修改前：投保人有权在保险期间内随时解除合同\n修改后：投保人解除合同需承担30%的违约金',
        pageNumber: '第3页',
        severity: 'high',
        createdTime: new Date()
      }
    ]

    // 筛选逻辑
    if (params.documentType) {
      results = results.filter(r => r.documentType === params.documentType)
    }

    if (params.ruleType) {
      const ruleTypeMap: Record<string, string> = {
        'single': '单句规则',
        'double': '双句规则',
        'format': '格式规则',
        'advanced': '高级规则'
      }
      results = results.filter(r => r.ruleType === ruleTypeMap[params.ruleType!])
    }

    if (params.severity) {
      results = results.filter(r => r.severity === params.severity)
    }

    // 分页
    const page = params.page || 1
    const size = params.size || 10
    const start = (page - 1) * size
    const end = start + size
    const paginatedResults = results.slice(start, end)

    // 生成统计数据
    const statistics: AuditStatistics[] = [
      {
        id: 'stat_terms',
        taskId: params.taskId,
        documentType: 'terms',
        ruleTypeStats: {
          single: 12,
          double: 8,
          format: 5,
          advanced: 3
        },
        totalCount: 28
      },
      {
        id: 'stat_feasibility',
        taskId: params.taskId,
        documentType: 'feasibility',
        ruleTypeStats: {
          single: 2,
          double: 1,
          format: 0,
          advanced: 4
        },
        totalCount: 7
      },
      {
        id: 'stat_comparison',
        taskId: params.taskId,
        documentType: 'comparison',
        ruleTypeStats: {
          single: 3,
          double: 2,
          format: 1,
          advanced: 0
        },
        totalCount: 6
      }
    ]

    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          records: paginatedResults,
          total: results.length,
          current: page,
          size: size,
          statistics
        })
      }, 400) // 模拟网络延迟
    })
  }

  /**
   * 获取检核结果统计数据
   */
  getResultStatistics(taskId: string): Promise<{
    totalResults: number
    byDocumentType: {
      terms: number
      feasibility: number
      comparison: number
    }
    byRuleType: {
      single: number
      double: number
      format: number
      advanced: number
    }
    bySeverity: {
      high: number
      medium: number
      low: number
    }
  }> {
    const statistics = {
      totalResults: 41,
      byDocumentType: {
        terms: 28,
        feasibility: 7,
        comparison: 6
      },
      byRuleType: {
        single: 17,
        double: 11,
        format: 6,
        advanced: 7
      },
      bySeverity: {
        high: 15,
        medium: 18,
        low: 8
      }
    }

    return new Promise(resolve => {
      setTimeout(() => {
        resolve(statistics)
      }, 250)
    })
  }

  /**
   * 获取单个检核结果详情
   */
  getResultDetail(resultId: string): Promise<AuditResult | null> {
    return new Promise((resolve) => {
      this.getAuditResults({ taskId: 'mock_task' }).then(response => {
        const result = response.records.find(r => r.id === resultId)
        setTimeout(() => {
          resolve(result || null)
        }, 200)
      })
    })
  }

  /**
   * 获取检核结果原文内容（带高亮）
   */
  getResultContent(resultId: string): Promise<{
    content: string
    highlightRanges: Array<{
      start: number
      end: number
      ruleCode: string
      severity: string
    }>
  }> {
    // 模拟带高亮标记的原文内容
    const mockContent = {
      content: `第一章 总则

1.1 保险合同构成
本保险合同由保险条款、投保单、保险单或保险凭证、批单等组成。

第二章 保险责任

2.1 基本保险责任
在保险期间内，由于下列原因直接造成保险标的的损失，保险人按照本保险合同约定负责赔偿：
(一)火灾、爆炸；
(二)雷击、暴风、暴雨、洪水、暴雪、冰雹、冻灾、旱灾；
(三)地震、泥石流、滑坡；
(四)飞机坠落、车辆撞击。

第三章 除外责任

3.1 责任免除
下列原因造成的损失、费用，保险人不负责赔偿：
(一)投保人、被保险人的故意行为；
(二)其他不可抗力因素。`,
      highlightRanges: [
        {
          start: 156,
          end: 189,
          ruleCode: 'CC001',
          severity: 'high'
        },
        {
          start: 298,
          end: 308,
          ruleCode: 'CC002',
          severity: 'medium'
        }
      ]
    }

    return new Promise(resolve => {
      setTimeout(() => {
        resolve(mockContent)
      }, 300)
    })
  }
}

// 导出Mock服务实例
export const auditResultsMockService = new AuditResultsMockService()