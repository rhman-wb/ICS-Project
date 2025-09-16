package com.insurance.audit.rules.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则操作日志数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Mapper
public interface RuleOperationLogMapper extends BaseMapper<Object> {

    /**
     * 根据规则ID查询操作日志
     *
     * @param ruleId 规则ID
     * @return 操作日志列表
     */
    @Select("SELECT * FROM rule_operation_log WHERE rule_id = #{ruleId} ORDER BY created_at DESC")
    List<Object> selectByRuleId(@Param("ruleId") String ruleId);

    /**
     * 分页查询操作日志
     *
     * @param page 分页参数
     * @param ruleId 规则ID（可选）
     * @param operationType 操作类型（可选）
     * @param operatorId 操作者ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页操作日志
     */
    IPage<Object> selectPageWithQuery(@Param("page") Page<Object> page,
                                    @Param("ruleId") String ruleId,
                                    @Param("operationType") String operationType,
                                    @Param("operatorId") String operatorId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 根据操作类型查询操作日志
     *
     * @param operationType 操作类型
     * @return 操作日志列表
     */
    @Select("SELECT * FROM rule_operation_log WHERE operation_type = #{operationType} ORDER BY created_at DESC")
    List<Object> selectByOperationType(@Param("operationType") String operationType);

    /**
     * 根据操作者查询操作日志
     *
     * @param operatorId 操作者ID
     * @return 操作日志列表
     */
    @Select("SELECT * FROM rule_operation_log WHERE operator_id = #{operatorId} ORDER BY created_at DESC")
    List<Object> selectByOperator(@Param("operatorId") String operatorId);

    /**
     * 根据时间范围查询操作日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作日志列表
     */
    @Select("SELECT * FROM rule_operation_log WHERE created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<Object> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各操作类型的次数
     *
     * @return 操作类型统计
     */
    @Select("SELECT operation_type, COUNT(*) as count FROM rule_operation_log GROUP BY operation_type")
    List<Object> countByOperationType();

    /**
     * 统计各操作者的操作次数
     *
     * @return 操作者统计
     */
    @Select("SELECT operator_id, COUNT(*) as count FROM rule_operation_log GROUP BY operator_id ORDER BY count DESC")
    List<Object> countByOperator();

    /**
     * 查询最近的操作日志
     *
     * @param limit 限制数量
     * @return 操作日志列表
     */
    @Select("SELECT * FROM rule_operation_log ORDER BY created_at DESC LIMIT #{limit}")
    List<Object> selectRecentLogs(@Param("limit") int limit);

    /**
     * 根据规则ID和操作类型查询最新日志
     *
     * @param ruleId 规则ID
     * @param operationType 操作类型
     * @return 操作日志
     */
    @Select("SELECT * FROM rule_operation_log WHERE rule_id = #{ruleId} AND operation_type = #{operationType} " +
            "ORDER BY created_at DESC LIMIT 1")
    Object selectLatestByRuleAndType(@Param("ruleId") String ruleId,
                                   @Param("operationType") String operationType);

    /**
     * 删除指定时间之前的操作日志
     *
     * @param beforeTime 时间阈值
     * @return 删除数量
     */
    @Select("DELETE FROM rule_operation_log WHERE created_at < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据规则ID删除操作日志
     *
     * @param ruleId 规则ID
     * @return 删除数量
     */
    @Select("DELETE FROM rule_operation_log WHERE rule_id = #{ruleId}")
    int deleteByRuleId(@Param("ruleId") String ruleId);

    /**
     * 查询指定规则的操作统计
     *
     * @param ruleId 规则ID
     * @return 操作统计
     */
    @Select("SELECT operation_type, COUNT(*) as count FROM rule_operation_log " +
            "WHERE rule_id = #{ruleId} GROUP BY operation_type")
    List<Object> countByRuleIdAndType(@Param("ruleId") String ruleId);
}