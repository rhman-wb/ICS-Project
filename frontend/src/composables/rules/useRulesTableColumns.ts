import { h } from 'vue'
import type { TableColumnType } from 'ant-design-vue'
import { Tag, Button, Dropdown, Menu, MenuItem } from 'ant-design-vue'
import { StarFilled, StarOutlined, DownOutlined, EditOutlined, CopyOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { ruleTypeMap, auditStatusMap, effectiveStatusMap } from './useRulesStatusMap'
import type { Rule } from '@/types/rules'

/**
 * 规则表格列定义
 */
export const useRulesTableColumns = () => {
  const columns: TableColumnType<Rule>[] = [
    {
      title: '',
      key: 'selection',
      width: 50,
      fixed: 'left'
    },
    {
      title: '规则编号',
      dataIndex: 'ruleNumber',
      key: 'ruleNumber',
      width: 120,
      fixed: 'left',
      ellipsis: true
    },
    {
      title: '规则描述',
      dataIndex: 'ruleDescription',
      key: 'ruleDescription',
      width: 200,
      ellipsis: true
    },
    {
      title: '规则来源',
      dataIndex: 'ruleSource',
      key: 'ruleSource',
      width: 120,
      ellipsis: true
    },
    {
      title: '规则管理部门',
      dataIndex: 'ruleDept',
      key: 'ruleDept',
      width: 140,
      ellipsis: true
    },
    {
      title: '适用险种',
      dataIndex: 'insuranceType',
      key: 'insuranceType',
      width: 120,
      ellipsis: true
    },
    {
      title: '适用要件',
      dataIndex: 'applicableRequirement',
      key: 'applicableRequirement',
      width: 120,
      ellipsis: true
    },
    {
      title: '适用章节',
      dataIndex: 'chapter',
      key: 'chapter',
      width: 120,
      ellipsis: true
    },
    {
      title: '审核状态',
      dataIndex: 'auditStatus',
      key: 'auditStatus',
      width: 120,
      customRender: ({ record }) => {
        const statusInfo = auditStatusMap[record.auditStatus as keyof typeof auditStatusMap]
        return h(Tag, {
          color: statusInfo?.color || 'default'
        }, {
          default: () => statusInfo?.text || record.auditStatus
        })
      }
    },
    {
      title: '有效状态',
      dataIndex: 'effectiveStatus',
      key: 'effectiveStatus',
      width: 120,
      customRender: ({ record }) => {
        const statusInfo = effectiveStatusMap[record.effectiveStatus as keyof typeof effectiveStatusMap]
        return h(Tag, {
          color: statusInfo?.color || 'default'
        }, {
          default: () => statusInfo?.text || record.effectiveStatus
        })
      }
    },
    {
      title: '最后更新时间',
      dataIndex: 'lastUpdatedAt',
      key: 'lastUpdatedAt',
      width: 180,
      sorter: true
    },
    {
      title: '操作',
      key: 'actions',
      width: 160,
      fixed: 'right',
      customRender: ({ record }) => renderTableActions(record)
    }
  ]

  // 渲染表格操作列
  const renderTableActions = (record: Rule) => {
    const menuItems = [
      {
        key: 'edit',
        icon: h(EditOutlined),
        label: '编辑'
      },
      {
        key: 'copy',
        icon: h(CopyOutlined),
        label: '复制'
      },
      {
        key: 'delete',
        icon: h(DeleteOutlined),
        label: '删除',
        danger: true
      }
    ]

    const menu = h(Menu, {
      onClick: ({ key }: { key: string }) => handleMenuClick(key, record)
    }, {
      default: () => menuItems.map(item =>
        h(MenuItem, {
          key: item.key,
          danger: item.danger
        }, {
          icon: () => item.icon,
          default: () => item.label
        })
      )
    })

    return h('div', { class: 'table-actions' }, [
      // 关注按钮
      h(Button, {
        type: 'text',
        size: 'small',
        icon: record.isFollowed ? h(StarFilled) : h(StarOutlined),
        onClick: () => handleToggleFollow(record.id, record.isFollowed)
      }),

      // 更多操作下拉菜单
      h(Dropdown, {
        overlay: menu,
        trigger: ['click']
      }, {
        default: () => h(Button, {
          type: 'text',
          size: 'small'
        }, {
          default: () => ['更多', h(DownOutlined)]
        })
      })
    ])
  }

  // 菜单点击处理
  const handleMenuClick = (key: string, record: Rule) => {
    switch (key) {
      case 'edit':
        handleEdit(record.id)
        break
      case 'copy':
        handleCopy(record.id)
        break
      case 'delete':
        handleDelete(record.id)
        break
    }
  }

  // 这些函数需要从父组件传入或使用inject
  const handleEdit = (id: string) => {
    console.log('Edit rule:', id)
  }

  const handleCopy = (id: string) => {
    console.log('Copy rule:', id)
  }

  const handleDelete = (id: string) => {
    console.log('Delete rule:', id)
  }

  const handleToggleFollow = (id: string, isFollowed: boolean) => {
    console.log('Toggle follow:', id, isFollowed)
  }

  return {
    columns,
    renderTableActions,
    handleMenuClick
  }
}