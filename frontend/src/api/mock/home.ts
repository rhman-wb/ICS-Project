/**
 * Dashboard Home Mock Data
 * 
 * 只读 mock 对象，字段与数值严格对齐原型与需求文档
 * 为所有组件提供静态数据源（MVP 不接后端）
 */

export interface HomeMock {
  topNav: {
    brand: string;
    menus: string[];
    active: string;
    username: string;
  };
  appOverview: {
    totalProducts: number;
    last7DaysProducts: {
      value: number;
      yoy: number;
      trend: 'up' | 'down';
    };
    last30DaysProducts: {
      value: number;
      yoy: number;
      trend: 'up' | 'down';
    };
    errorRate: string;
    totalRules: number;
    last7DaysRules: {
      value: number;
      yoy: number;
      trend: 'up' | 'down';
    };
    last30DaysRules: {
      value: number;
      yoy: number;
      trend: 'up' | 'down';
    };
    avgAuditTime: string;
  };
  myFavorites: {
    filters: {
      keywordPlaceholder: string;
      timeLabel: string;
      typeLabel: string;
    };
    items: Array<{
      name: string;
      type: '审核' | '配置';
      submitter: string;
      submittedAt: string;
      icon: string;
      id?: string;
    }>;
  };
  ratioChart: {
    tabs: string[];
    active: string;
    productTypes: Array<{
      label: string;
      percent: number;
      count: number;
      color: string;
    }>;
  };
  quickStart: {
    items: Array<{
      label: string;
      route: string;
      badge?: number;
    }>;
  };
  recentActivities: Array<{
    type: string;
    text: string;
    time: string;
    link?: string;
  }>;
  summaryTotals: {
    activeRange: string;
    ranges: string[];
    metrics: Array<{
      label: string;
      value: number;
    }>;
  };
}

export const homeMock: HomeMock = {
  // 顶部导航数据 - Requirement 1
  topNav: {
    brand: '产品检核系统',
    menus: ['主页', '产品管理', '规则管理', '设置'],
    active: '主页',
    username: 'Jay.Liu'
  },

  // 应用概要统计数据 - Requirement 7
  appOverview: {
    totalProducts: 2805,
    last7DaysProducts: {
      value: 672,
      yoy: 0.30,
      trend: 'up'
    },
    last30DaysProducts: {
      value: 1603,
      yoy: 0.30,
      trend: 'up'
    },
    errorRate: '57%',
    totalRules: 141,
    last7DaysRules: {
      value: 36,
      yoy: -2.44,
      trend: 'down'
    },
    last30DaysRules: {
      value: 107,
      yoy: 0.30,
      trend: 'up'
    },
    avgAuditTime: '00:02:27'
  },

  // 我的关注任务列表 - Requirement 3
  myFavorites: {
    filters: {
      keywordPlaceholder: '请输入任务名称',
      timeLabel: '按时间',
      typeLabel: '全部任务'
    },
    items: [
      {
        name: '中国人寿财产保险股份有限公司安徽省地方财政补贴型鸭养殖保险',
        type: '审核',
        submitter: '胡潇禹',
        submittedAt: '2022/10/31',
        icon: 'fa-cube'
      },
      {
        name: '中国人寿财产保险股份有限公司安徽省地方财政补贴型能繁母猪养殖保险',
        type: '配置',
        submitter: '胡潇禹',
        submittedAt: '2022/10/31',
        icon: 'fa-user'
      },
      {
        name: '中国人寿财产保险股份有限公司安徽省地方财政补贴型林木火灾保险',
        type: '审核',
        submitter: '胡潇禹',
        submittedAt: '2022/10/31',
        icon: 'fa-cube'
      },
      {
        name: '中国人寿财产保险股份有限公司安徽省地方财政补贴型水稻种植保险',
        type: '配置',
        submitter: '胡潇禹',
        submittedAt: '2022/10/31',
        icon: 'fa-user'
      },
      {
        name: '中国人寿财产保险股份有限公司安徽省地方财政补贴型小麦种植保险',
        type: '审核',
        submitter: '胡潇禹',
        submittedAt: '2022/10/31',
        icon: 'fa-cube'
      }
    ]
  },

  // 数量统计占比图表 - Requirement 8
  ratioChart: {
    tabs: ['产品管理部门', '产品类型', '规则类型'],
    active: '产品类型',
    productTypes: [
      { label: '种植险', percent: 36, count: 1010, color: '#5470c6' },
      { label: '意健险', percent: 20, count: 561, color: '#91cc75' },
      { label: '车险', percent: 16, count: 445, color: '#fac858' },
      { label: '家财险', percent: 10, count: 280, color: '#ee6666' },
      { label: '养殖险', percent: 9, count: 280, color: '#73c0de' },
      { label: '其他', percent: 9, count: 280, color: '#3ba272' }
    ]
  },

  // 快速开始/便捷导航 - Requirement 5
  quickStart: {
    items: [
      { label: '导入产品', route: '/product/import', badge: 24 },
      { label: '创建规则', route: '/rules/create', badge: 24 },
      { label: '批量导入', route: '/product/import/batch' },
      { label: '下载模板', route: '/template/download' },
      { label: '操作四', route: '/operation/four' },
      { label: '操作五', route: '/operation/five' }
    ]
  },

  // 近期动态 - Requirement 6
  recentActivities: [
    {
      type: 'upload',
      text: 'Jay.Liu上传了中国人寿财产保险股份有限公司安徽省地方财政补贴型鸭养殖保险',
      time: '几秒前',
      link: '/product/detail/1'
    },
    {
      type: 'update',
      text: '胡潇禹 更新了12条规则',
      time: '14分钟前'
    },
    {
      type: 'approve',
      text: 'Jay.Liu 审核通过了8条规则',
      time: '20分钟前'
    },
    {
      type: 'config',
      text: '胡潇禹 配置了15条规则',
      time: '40分钟前'
    },
    {
      type: 'update',
      text: 'Jay.Liu 更新了6条规则',
      time: '68分钟前'
    },
    {
      type: 'upload',
      text: '胡潇禹上传了中国人寿财产保险股份有限公司安徽省地方财政补贴型能繁母猪养殖保险',
      time: '3小时前',
      link: '/product/detail/2'
    }
  ],

  // 汇总数据 - Requirement 2
  summaryTotals: {
    activeRange: '总计',
    ranges: ['总计', '本日', '本周', '本月'],
    metrics: [
      { label: '产品数量', value: 20000 },
      { label: '要件数量', value: 20000 },
      { label: '规则数量', value: 20000 },
      { label: '已检核错误数量', value: 20000 }
    ]
  }
};