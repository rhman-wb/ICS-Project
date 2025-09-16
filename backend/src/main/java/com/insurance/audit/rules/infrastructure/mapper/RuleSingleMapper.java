package com.insurance.audit.rules.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insurance.audit.rules.domain.ruletypes.SingleRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 单句规则数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Mapper
public interface RuleSingleMapper extends BaseMapper<SingleRule> {

    /**
     * 根据规则ID查询单句规则
     *
     * @param ruleId 规则ID
     * @return 单句规则
     */
    @Select("SELECT * FROM rule_single WHERE rule_id = #{ruleId}")
    SingleRule selectByRuleId(@Param("ruleId") String ruleId);

    /**
     * 根据条件字段查询单句规则
     *
     * @param conditionField 条件字段
     * @return 单句规则列表
     */
    @Select("SELECT * FROM rule_single WHERE condition_field = #{conditionField}")
    List<SingleRule> selectByConditionField(@Param("conditionField") String conditionField);

    /**
     * 根据操作符查询单句规则
     *
     * @param operator 操作符
     * @return 单句规则列表
     */
    @Select("SELECT * FROM rule_single WHERE operator = #{operator}")
    List<SingleRule> selectByOperator(@Param("operator") String operator);

    /**
     * 根据条件值查询单句规则
     *
     * @param conditionValue 条件值
     * @return 单句规则列表
     */
    @Select("SELECT * FROM rule_single WHERE condition_value = #{conditionValue}")
    List<SingleRule> selectByConditionValue(@Param("conditionValue") String conditionValue);

    /**
     * 统计各操作符的使用次数
     *
     * @return 操作符统计
     */
    @Select("SELECT operator, COUNT(*) as count FROM rule_single GROUP BY operator")
    List<Object> countByOperator();

    /**
     * 统计各条件字段的使用次数
     *
     * @return 条件字段统计
     */
    @Select("SELECT condition_field, COUNT(*) as count FROM rule_single GROUP BY condition_field")
    List<Object> countByConditionField();
}