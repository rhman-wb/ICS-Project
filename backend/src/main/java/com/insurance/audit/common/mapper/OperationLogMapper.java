package com.insurance.audit.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.insurance.audit.common.entity.OperationLog;
import com.insurance.audit.common.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志Mapper接口
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    
    /**
     * 根据用户ID查询操作日志
     */
    default List<OperationLog> findByUserId(String userId) {
        return selectList(Wrappers.<OperationLog>lambdaQuery()
                .eq(OperationLog::getUserId, userId)
                .orderByDesc(OperationLog::getCreatedAt));
    }
    
    /**
     * 根据操作类型查询日志
     */
    default List<OperationLog> findByOperationType(OperationType operationType) {
        return selectList(Wrappers.<OperationLog>lambdaQuery()
                .eq(OperationLog::getOperationType, operationType)
                .orderByDesc(OperationLog::getCreatedAt));
    }
    
    /**
     * 根据时间范围查询日志
     */
    default List<OperationLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(Wrappers.<OperationLog>lambdaQuery()
                .between(OperationLog::getCreatedAt, startTime, endTime)
                .orderByDesc(OperationLog::getCreatedAt));
    }
    
    /**
     * 根据用户ID和操作类型查询日志
     */
    default List<OperationLog> findByUserIdAndOperationType(String userId, OperationType operationType) {
        return selectList(Wrappers.<OperationLog>lambdaQuery()
                .eq(OperationLog::getUserId, userId)
                .eq(OperationLog::getOperationType, operationType)
                .orderByDesc(OperationLog::getCreatedAt));
    }
    
    /**
     * 统计用户操作次数
     */
    default Long countByUserId(String userId) {
        return selectCount(Wrappers.<OperationLog>lambdaQuery()
                .eq(OperationLog::getUserId, userId));
    }
    
    /**
     * 统计指定时间范围内的操作次数
     */
    default Long countByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(Wrappers.<OperationLog>lambdaQuery()
                .between(OperationLog::getCreatedAt, startTime, endTime));
    }
}