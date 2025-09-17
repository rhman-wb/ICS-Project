@echo off
REM =====================================================
REM 规则模块数据库性能测试脚本 (Windows版本)
REM Description: 验证规则管理相关查询的性能表现
REM Usage: performance_test.bat
REM =====================================================

REM 数据库连接配置 - 从环境变量获取敏感信息
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306
if "%DB_NAME%"=="" set DB_NAME=insurance_audit
if "%DB_USER%"=="" set DB_USER=insurance_user

REM 检查必要的环境变量
if "%DB_PASSWORD%"=="" (
    echo [错误] 请设置环境变量 DB_PASSWORD
    echo 使用示例: set DB_PASSWORD=your_password ^&^& performance_test.bat
    pause
    exit /b 1
)

echo ==========================================
echo 规则模块数据库性能测试
echo ==========================================

REM 检查MySQL客户端是否可用
mysql --version >nul 2>&1
if errorlevel 1 (
    echo [错误] MySQL客户端未找到，请确保MySQL客户端已安装并在PATH中
    pause
    exit /b 1
)

REM 检查数据库连接
echo 检查数据库连接...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT 1;" %DB_NAME% >nul 2>&1
if errorlevel 1 (
    echo [错误] 数据库连接失败，请检查配置
    pause
    exit /b 1
)
echo [成功] 数据库连接成功

REM 创建测试结果目录
if not exist "performance_test_results" mkdir performance_test_results
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set RESULT_FILE=performance_test_results\performance_test_%TIMESTAMP%.txt

echo 性能测试报告 - %date% %time% > %RESULT_FILE%
echo ======================================== >> %RESULT_FILE%

echo.
echo 1. 分页列表查询性能测试
echo 测试: 分页列表查询 >> %RESULT_FILE%

REM 执行分页列表查询测试
set start_time=%time%
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT * FROM rule WHERE is_deleted = 0 AND audit_status IN ('PENDING_TECH_EVALUATION', 'APPROVED') AND effective_status = 'EFFECTIVE' ORDER BY last_updated_at DESC LIMIT 20 OFFSET 0;" %DB_NAME% >nul 2>&1
set end_time=%time%

echo 分页列表查询完成
echo 执行时间: 计算中... >> %RESULT_FILE%
echo 结果: 需要手动验证执行时间 >> %RESULT_FILE%
echo ---------------------------------------- >> %RESULT_FILE%

echo.
echo 2. 筛选查询性能测试
echo 测试: 筛选查询 >> %RESULT_FILE%

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT * FROM rule WHERE is_deleted = 0 AND rule_type = 'SINGLE' AND manage_department = '技术部门' ORDER BY priority DESC, created_at DESC LIMIT 20 OFFSET 0;" %DB_NAME% >nul 2>&1

echo 筛选查询完成
echo 执行时间: 计算中... >> %RESULT_FILE%
echo 结果: 需要手动验证执行时间 >> %RESULT_FILE%
echo ---------------------------------------- >> %RESULT_FILE%

echo.
echo 3. 统计查询性能测试
echo 测试: 统计查询 >> %RESULT_FILE%

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT audit_status, COUNT(*) as count FROM rule WHERE is_deleted = 0 GROUP BY audit_status;" %DB_NAME% >nul 2>&1

echo 统计查询完成
echo 执行时间: 计算中... >> %RESULT_FILE%
echo 结果: 需要手动验证执行时间 >> %RESULT_FILE%
echo ---------------------------------------- >> %RESULT_FILE%

echo.
echo 4. 执行计划分析
echo 执行计划分析 >> %RESULT_FILE%

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "EXPLAIN SELECT * FROM rule WHERE is_deleted = 0 AND audit_status IN ('PENDING_TECH_EVALUATION', 'APPROVED') ORDER BY last_updated_at DESC LIMIT 20;" %DB_NAME% >> %RESULT_FILE% 2>&1

echo.
echo 5. 索引使用情况检查
echo 索引使用情况检查 >> %RESULT_FILE%

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, SEQ_IN_INDEX FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = '%DB_NAME%' AND TABLE_NAME LIKE 'rule%%' ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;" %DB_NAME% >> %RESULT_FILE% 2>&1

echo.
echo 6. 表大小统计
echo 表大小统计 >> %RESULT_FILE%

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT TABLE_NAME, ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'SIZE_MB', TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%DB_NAME%' AND TABLE_NAME LIKE 'rule%%' ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;" %DB_NAME% >> %RESULT_FILE% 2>&1

echo.
echo ==========================================
echo 测试完成，结果已保存到: %RESULT_FILE%
echo ==========================================

echo.
echo 关键性能指标总结:
echo • 列表查询目标: ^< 3000ms
echo • 筛选查询目标: ^< 3000ms
echo • 统计查询目标: ^< 1000ms
echo • 搜索查询目标: ^< 5000ms
echo • 联表查询目标: ^< 2000ms

echo.
echo 详细报告请查看: %RESULT_FILE%

echo.
echo 按任意键查看详细结果...
pause >nul
type %RESULT_FILE%

echo.
echo 按任意键退出...
pause >nul