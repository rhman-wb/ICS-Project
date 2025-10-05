@echo off
REM =====================================================
REM 保险产品智能检核系统 - 模板文件部署脚本 (Windows)
REM 用途：将新的产品导入模板文件复制到后端文件存储目录
REM =====================================================

setlocal enabledelayedexpansion

echo ========================================
echo 产品模板文件部署脚本
echo ========================================
echo.

REM 项目根目录
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\"
echo 项目根目录: %PROJECT_ROOT%

REM 源文件目录（Docs目录中的新模板文件）
set "SOURCE_DIR=%PROJECT_ROOT%Docs"

REM 目标目录（backend文件上传目录）
set "TARGET_DIR=%PROJECT_ROOT%backend\uploads\templates"

REM 创建目标目录
echo 创建目标目录: %TARGET_DIR%
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"
echo.

REM 复制文件
echo 开始复制模板文件...
echo.

set "FILE1=附件3_备案产品自主注册信息登记表.xlsx"
set "FILE2=附件5_农险产品信息登记表.xls"

REM 复制第一个文件
if exist "%SOURCE_DIR%\%FILE1%" (
    copy /Y "%SOURCE_DIR%\%FILE1%" "%TARGET_DIR%\%FILE1%" > nul
    if !errorlevel! equ 0 (
        echo [成功] 已复制: %FILE1%
        for %%F in ("%TARGET_DIR%\%FILE1%") do echo   大小: %%~zF 字节
    ) else (
        echo [失败] 复制失败: %FILE1%
        exit /b 1
    )
) else (
    echo [错误] 源文件不存在: %FILE1%
    exit /b 1
)
echo.

REM 复制第二个文件
if exist "%SOURCE_DIR%\%FILE2%" (
    copy /Y "%SOURCE_DIR%\%FILE2%" "%TARGET_DIR%\%FILE2%" > nul
    if !errorlevel! equ 0 (
        echo [成功] 已复制: %FILE2%
        for %%F in ("%TARGET_DIR%\%FILE2%") do echo   大小: %%~zF 字节
    ) else (
        echo [失败] 复制失败: %FILE2%
        exit /b 1
    )
) else (
    echo [错误] 源文件不存在: %FILE2%
    exit /b 1
)
echo.

echo ========================================
echo 模板文件部署完成！
echo ========================================
echo.
echo 下一步操作：
echo 1. 运行数据库迁移脚本 V9__Update_Template_Files.sql
echo 2. 启动后端服务
echo 3. 在前端测试模板下载功能
echo.

pause
