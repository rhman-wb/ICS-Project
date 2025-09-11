#!/bin/bash

# =====================================================
# 数据库初始化脚本
# 用于快速设置开发环境数据库
# =====================================================

echo "开始初始化数据库..."

# 检查MySQL是否运行
if ! command -v mysql &> /dev/null; then
    echo "错误: MySQL客户端未安装或不在PATH中"
    exit 1
fi

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="insurance_audit"
DB_USER="insurance_user"
DB_PASSWORD="20150826"
ROOT_PASSWORD=""

# 提示输入root密码
read -s -p "请输入MySQL root密码: " ROOT_PASSWORD
echo

# 1. 创建数据库和用户
echo "1. 创建数据库和用户..."
mysql -h${DB_HOST} -P${DB_PORT} -uroot -p${ROOT_PASSWORD} < setup-database.sql

if [ $? -eq 0 ]; then
    echo "✓ 数据库和用户创建成功"
else
    echo "✗ 数据库和用户创建失败"
    exit 1
fi

# 2. 创建表结构
echo "2. 创建表结构..."
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME} < ../backend/src/main/resources/db/migration/V1__init_schema.sql

if [ $? -eq 0 ]; then
    echo "✓ 表结构创建成功"
else
    echo "✗ 表结构创建失败"
    exit 1
fi

# 3. 插入初始数据
echo "3. 插入初始数据..."
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME} < ../backend/src/main/resources/db/migration/V2__init_data.sql

if [ $? -eq 0 ]; then
    echo "✓ 初始数据插入成功"
else
    echo "✓ 初始数据插入完成（可能已存在）"
fi

# 4. 验证数据库
echo "4. 验证数据库..."
TABLES=$(mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME} -e "SHOW TABLES;" | wc -l)

if [ $TABLES -gt 1 ]; then
    echo "✓ 数据库验证成功，共创建了 $((TABLES-1)) 个表"
    mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME} -e "SHOW TABLES;"
else
    echo "✗ 数据库验证失败，未找到表"
    exit 1
fi

echo ""
echo "🎉 数据库初始化完成！"
echo ""
echo "数据库信息:"
echo "  主机: ${DB_HOST}:${DB_PORT}"
echo "  数据库: ${DB_NAME}"
echo "  用户: ${DB_USER}"
echo "  密码: ${DB_PASSWORD}"
echo ""
echo "默认管理员账户:"
echo "  用户名: admin"
echo "  密码: admin123"
echo ""
echo "现在可以启动应用了: mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=dev"