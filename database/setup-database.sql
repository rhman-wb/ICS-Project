-- 补充操作日志表缺失列，以兼容后端记录字段
ALTER TABLE `operation_logs`
  ADD COLUMN `operation_desc` varchar(255) NULL COMMENT '操作描述' AFTER `operation_type`,
  ADD COLUMN `operation_status` varchar(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作状态' AFTER `response_result`,
  ADD COLUMN `module` varchar(100) NULL COMMENT '所属模块' AFTER `operation_status`,
  ADD COLUMN `business_type` varchar(50) NULL COMMENT '业务类型' AFTER `module`;

-- =====================================================
-- 数据库和用户创建脚本
-- 请以root用户身份执行此脚本
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `insurance_audit` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 创建用户（如果不存在）
CREATE USER IF NOT EXISTS 'insurance_user'@'localhost' IDENTIFIED BY '20150826';
CREATE USER IF NOT EXISTS 'insurance_user'@'%' IDENTIFIED BY '20150826';

-- 授予权限
GRANT ALL PRIVILEGES ON `insurance_audit`.* TO 'insurance_user'@'localhost';
GRANT ALL PRIVILEGES ON `insurance_audit`.* TO 'insurance_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 显示创建结果
SHOW DATABASES LIKE 'insurance_audit';
SELECT User, Host FROM mysql.user WHERE User = 'insurance_user';