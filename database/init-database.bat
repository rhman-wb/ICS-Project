@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM =====================================================
REM 数据库初始化脚本 (Windows版本)
REM 用于快速设置开发环境数据库
REM =====================================================

echo 开始初始化数据库...

REM 检查MySQL是否可用
mysql --version >nul 2>&1
if errorlevel 1 (
    echo 错误: MySQL客户端未安装或不在PATH中
    echo 请确保MySQL已安装并添加到系统PATH中
    pause
    exit /b 1
)

REM 数据库配置
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=insurance_audit
set DB_USER=insurance_user
set DB_PASSWORD=20150826

REM 提示输入root密码
set /p ROOT_PASSWORD=请输入MySQL root密码: 

REM 1. 创建数据库和用户
echo 1. 创建数据库和用户...
mysql -h%DB_HOST% -P%DB_PORT% -uroot -p%ROOT_PASSWORD% < setup-database.sql

if errorlevel 1 (
    echo ✗ 数据库和用户创建失败
    pause
    exit /b 1
) else (
    echo ✓ 数据库和用户创建成功
)

REM 2. 创建表结构
echo 2. 创建表结构...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < ..\backend\src\main\resources\db\migration\V1__init_schema.sql

if errorlevel 1 (
    echo ✗ 表结构创建失败
    pause
    exit /b 1
) else (
    echo ✓ 表结构创建成功
)

REM 3. 插入初始数据
echo 3. 插入初始数据...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < ..\backend\src\main\resources\db\migration\V2__init_data.sql

if errorlevel 1 (
    echo ✓ 初始数据插入完成（可能已存在）
) else (
    echo ✓ 初始数据插入成功
)

REM 4. 验证数据库
echo 4. 验证数据库...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SHOW TABLES;"

if errorlevel 1 (
    echo ✗ 数据库验证失败
    pause
    exit /b 1
) else (
    echo ✓ 数据库验证成功
)

echo.
echo 🎉 数据库初始化完成！
echo.
echo 数据库信息:
echo   主机: %DB_HOST%:%DB_PORT%
echo   数据库: %DB_NAME%
echo   用户: %DB_USER%
echo   密码: %DB_PASSWORD%
echo.
echo 默认管理员账户:
echo   用户名: admin
echo   密码: admin123
echo.
echo 现在可以启动应用了: mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=dev
echo.
pause