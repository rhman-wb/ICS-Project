#!/bin/bash

# =====================================================
# 规则模块数据库性能测试脚本
# Description: 验证规则管理相关查询的性能表现
# Usage: ./performance_test.sh
# =====================================================

# 数据库连接配置 - 从环境变量获取敏感信息
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-insurance_audit}"
DB_USER="${DB_USER:-insurance_user}"
DB_PASSWORD="${DB_PASSWORD:-}"

# 检查必要的环境变量
if [ -z "$DB_PASSWORD" ]; then
    echo -e "${RED}错误: 请设置环境变量 DB_PASSWORD${NC}"
    echo -e "${YELLOW}使用示例: export DB_PASSWORD='your_password' && ./performance_test.sh${NC}"
    exit 1
fi

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}规则模块数据库性能测试${NC}"
echo -e "${BLUE}===========================================${NC}"

# 检查MySQL连接
echo -e "${YELLOW}检查数据库连接...${NC}"
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} -e "SELECT 1;" ${DB_NAME} > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo -e "${RED}数据库连接失败，请检查配置${NC}"
    exit 1
fi
echo -e "${GREEN}数据库连接成功${NC}"

# 创建测试结果目录
TEST_RESULTS_DIR="performance_test_results"
mkdir -p ${TEST_RESULTS_DIR}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULT_FILE="${TEST_RESULTS_DIR}/performance_test_${TIMESTAMP}.txt"

echo "性能测试报告 - $(date)" > ${RESULT_FILE}
echo "========================================" >> ${RESULT_FILE}

# 测试函数
test_query() {
    local test_name="$1"
    local query="$2"
    local threshold_ms="$3"

    echo -e "${BLUE}测试: ${test_name}${NC}"
    echo "测试: ${test_name}" >> ${RESULT_FILE}

    # 执行查询并记录时间
    start_time=$(date +%s%3N)
    mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} -e "${query}" ${DB_NAME} > /dev/null 2>&1
    end_time=$(date +%s%3N)

    execution_time=$((end_time - start_time))

    echo "执行时间: ${execution_time}ms" >> ${RESULT_FILE}
    echo "阈值: ${threshold_ms}ms" >> ${RESULT_FILE}

    if [ ${execution_time} -le ${threshold_ms} ]; then
        echo -e "${GREEN}✓ 通过 (${execution_time}ms <= ${threshold_ms}ms)${NC}"
        echo "结果: ✓ 通过" >> ${RESULT_FILE}
    else
        echo -e "${RED}✗ 不达标 (${execution_time}ms > ${threshold_ms}ms)${NC}"
        echo "结果: ✗ 不达标" >> ${RESULT_FILE}
    fi

    echo "----------------------------------------" >> ${RESULT_FILE}
    echo ""
}

# 测试执行计划
test_explain() {
    local test_name="$1"
    local query="$2"

    echo -e "${BLUE}执行计划分析: ${test_name}${NC}"
    echo "执行计划分析: ${test_name}" >> ${RESULT_FILE}

    explain_result=$(mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} -e "EXPLAIN ${query}" ${DB_NAME} 2>/dev/null)
    echo "${explain_result}" >> ${RESULT_FILE}

    # 检查是否使用了索引
    if echo "${explain_result}" | grep -q "key.*idx"; then
        echo -e "${GREEN}✓ 使用了索引${NC}"
        echo "索引使用: ✓ 是" >> ${RESULT_FILE}
    else
        echo -e "${YELLOW}⚠ 未使用索引或使用PRIMARY${NC}"
        echo "索引使用: ⚠ 否" >> ${RESULT_FILE}
    fi

    echo "----------------------------------------" >> ${RESULT_FILE}
    echo ""
}

# 1. 分页列表查询测试
echo -e "${YELLOW}1. 分页列表查询性能测试${NC}"
test_query "分页列表查询" \
"SELECT * FROM rule
WHERE is_deleted = 0
  AND audit_status IN ('PENDING_TECH_EVALUATION', 'APPROVED')
  AND effective_status = 'EFFECTIVE'
ORDER BY last_updated_at DESC
LIMIT 20 OFFSET 0;" \
3000

test_explain "分页列表查询执行计划" \
"SELECT * FROM rule
WHERE is_deleted = 0
  AND audit_status IN ('PENDING_TECH_EVALUATION', 'APPROVED')
  AND effective_status = 'EFFECTIVE'
ORDER BY last_updated_at DESC
LIMIT 20 OFFSET 0;"

# 2. 筛选查询测试
echo -e "${YELLOW}2. 按类型和部门筛选查询测试${NC}"
test_query "筛选查询" \
"SELECT * FROM rule
WHERE is_deleted = 0
  AND rule_type = 'SINGLE'
  AND manage_department = '技术部门'
ORDER BY priority DESC, created_at DESC
LIMIT 20 OFFSET 0;" \
3000

test_explain "筛选查询执行计划" \
"SELECT * FROM rule
WHERE is_deleted = 0
  AND rule_type = 'SINGLE'
  AND manage_department = '技术部门'
ORDER BY priority DESC, created_at DESC
LIMIT 20 OFFSET 0;"

# 3. 统计查询测试
echo -e "${YELLOW}3. 统计查询性能测试${NC}"
test_query "统计查询" \
"SELECT audit_status, COUNT(*) as count
FROM rule
WHERE is_deleted = 0
GROUP BY audit_status;" \
1000

test_explain "统计查询执行计划" \
"SELECT audit_status, COUNT(*) as count
FROM rule
WHERE is_deleted = 0
GROUP BY audit_status;"

# 4. 搜索查询测试
echo -e "${YELLOW}4. 文本搜索查询测试${NC}"
test_query "搜索查询" \
"SELECT * FROM rule
WHERE is_deleted = 0
  AND (rule_name LIKE '%保险%' OR rule_description LIKE '%保险%')
ORDER BY last_updated_at DESC
LIMIT 20 OFFSET 0;" \
5000

test_explain "搜索查询执行计划" \
"SELECT * FROM rule
WHERE is_deleted = 0
  AND (rule_name LIKE '%保险%' OR rule_description LIKE '%保险%')
ORDER BY last_updated_at DESC
LIMIT 20 OFFSET 0;"

# 5. 联表查询测试
echo -e "${YELLOW}5. 联表查询性能测试${NC}"
test_query "联表查询" \
"SELECT r.*, rs.rule_description as single_description
FROM rule r
LEFT JOIN rule_single rs ON r.id = rs.rule_id
WHERE r.is_deleted = 0
  AND r.rule_type = 'SINGLE'
LIMIT 10;" \
2000

test_explain "联表查询执行计划" \
"SELECT r.*, rs.rule_description as single_description
FROM rule r
LEFT JOIN rule_single rs ON r.id = rs.rule_id
WHERE r.is_deleted = 0
  AND r.rule_type = 'SINGLE'
LIMIT 10;"

# 6. 复杂查询测试
echo -e "${YELLOW}6. 复杂查询性能测试${NC}"
test_query "复杂查询" \
"SELECT r.rule_type,
       COUNT(*) as total_count,
       COUNT(CASE WHEN r.audit_status = 'APPROVED' THEN 1 END) as approved_count,
       COUNT(CASE WHEN r.effective_status = 'EFFECTIVE' THEN 1 END) as effective_count,
       MAX(r.last_updated_at) as last_update
FROM rule r
WHERE r.is_deleted = 0
  AND r.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY r.rule_type
ORDER BY total_count DESC;" \
2000

# 7. 索引使用情况检查
echo -e "${YELLOW}7. 索引使用情况检查${NC}"
echo "索引使用情况检查" >> ${RESULT_FILE}

index_info=$(mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} -e "
SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = '${DB_NAME}'
  AND TABLE_NAME LIKE 'rule%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;" ${DB_NAME} 2>/dev/null)

echo "${index_info}" >> ${RESULT_FILE}
echo "索引数量: $(echo "${index_info}" | wc -l)" >> ${RESULT_FILE}

# 8. 表大小统计
echo -e "${YELLOW}8. 表大小统计${NC}"
echo "表大小统计" >> ${RESULT_FILE}

table_sizes=$(mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} -e "
SELECT TABLE_NAME,
       ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'SIZE_MB',
       TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = '${DB_NAME}'
  AND TABLE_NAME LIKE 'rule%'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;" ${DB_NAME} 2>/dev/null)

echo "${table_sizes}" >> ${RESULT_FILE}

# 生成总结报告
echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}测试完成，结果已保存到: ${RESULT_FILE}${NC}"
echo -e "${BLUE}===========================================${NC}"

# 显示关键指标
echo -e "${YELLOW}关键性能指标总结:${NC}"
echo "• 列表查询目标: < 3000ms"
echo "• 筛选查询目标: < 3000ms"
echo "• 统计查询目标: < 1000ms"
echo "• 搜索查询目标: < 5000ms"
echo "• 联表查询目标: < 2000ms"

echo -e "${GREEN}详细报告请查看: ${RESULT_FILE}${NC}"

# 可选：自动打开结果文件
if command -v cat &> /dev/null; then
    echo -e "${YELLOW}按任意键查看详细结果...${NC}"
    read -n 1 -s
    cat ${RESULT_FILE}
fi