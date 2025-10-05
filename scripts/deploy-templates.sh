#!/bin/bash

# =====================================================
# 保险产品智能检核系统 - 模板文件部署脚本
# 用途：将新的产品导入模板文件复制到后端文件存储目录
# =====================================================

set -e  # Exit on error

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}产品模板文件部署脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
echo -e "${YELLOW}项目根目录: ${PROJECT_ROOT}${NC}"

# 源文件目录（Docs目录中的新模板文件）
SOURCE_DIR="${PROJECT_ROOT}/Docs"

# 目标目录（backend文件上传目录）
TARGET_DIR="${PROJECT_ROOT}/backend/uploads/templates"

# 创建目标目录
echo -e "${YELLOW}创建目标目录: ${TARGET_DIR}${NC}"
mkdir -p "${TARGET_DIR}"

# 文件列表
FILES=(
    "附件3_备案产品自主注册信息登记表.xlsx"
    "附件5_农险产品信息登记表.xls"
)

# 复制文件
echo -e "${YELLOW}开始复制模板文件...${NC}"
for file in "${FILES[@]}"; do
    SOURCE_FILE="${SOURCE_DIR}/${file}"
    TARGET_FILE="${TARGET_DIR}/${file}"

    if [ -f "${SOURCE_FILE}" ]; then
        cp "${SOURCE_FILE}" "${TARGET_FILE}"
        echo -e "${GREEN}✓ 已复制: ${file}${NC}"

        # 显示文件信息
        FILE_SIZE=$(stat -f%z "${TARGET_FILE}" 2>/dev/null || stat -c%s "${TARGET_FILE}" 2>/dev/null)
        echo -e "  大小: ${FILE_SIZE} 字节"
    else
        echo -e "${RED}✗ 源文件不存在: ${file}${NC}"
        exit 1
    fi
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}模板文件部署完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}下一步操作：${NC}"
echo "1. 运行数据库迁移脚本 V9__Update_Template_Files.sql"
echo "2. 启动后端服务"
echo "3. 在前端测试模板下载功能"
echo ""
