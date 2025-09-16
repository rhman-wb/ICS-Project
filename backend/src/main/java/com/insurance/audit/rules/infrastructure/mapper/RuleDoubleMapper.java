package com.insurance.audit.rules.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insurance.audit.rules.domain.ruletypes.DoubleRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 双句规则数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Mapper
public interface RuleDoubleMapper extends BaseMapper<DoubleRule> {

    /**
     * 根据规则ID查询双句规则
     *
     * @param ruleId 规则ID
     * @return 双句规则
     */
    @Select("SELECT * FROM rule_double WHERE rule_id = #{ruleId}")
    DoubleRule selectByRuleId(@Param("ruleId") String ruleId);

    /**
     * 根据第一条件字段查询双句规则
     *
     * @param firstConditionField 第一条件字段
     * @return 双句规则列表
     */
    @Select("SELECT * FROM rule_double WHERE first_condition_field = #{firstConditionField}")
    List<DoubleRule> selectByFirstConditionField(@Param("firstConditionField") String firstConditionField);

    /**
     * 根据第二条件字段查询双句规则
     *
     * @param secondConditionField 第二条件字段
     * @return 双句规则列表
     */
    @Select("SELECT * FROM rule_double WHERE second_condition_field = #{secondConditionField}")
    List<DoubleRule> selectBySecondConditionField(@Param("secondConditionField") String secondConditionField);

    /**
     * 根据逻辑操作符查询双句规则
     *
     * @param logicalOperator 逻辑操作符
     * @return 双句规则列表
     */
    @Select("SELECT * FROM rule_double WHERE logical_operator = #{logicalOperator}")
    List<DoubleRule> selectByLogicalOperator(@Param("logicalOperator") String logicalOperator);

    /**
     * 统计各逻辑操作符的使用次数
     *
     * @return 逻辑操作符统计
     */
    @Select("SELECT logical_operator, COUNT(*) as count FROM rule_double GROUP BY logical_operator")
    List<Object> countByLogicalOperator();

    /**
     * 查询包含特定字段组合的双句规则
     *
     * @param firstField 第一字段
     * @param secondField 第二字段
     * @return 双句规则列表
     */
    @Select("SELECT * FROM rule_double WHERE " +
            "(first_condition_field = #{firstField} AND second_condition_field = #{secondField}) " +
            "OR (first_condition_field = #{secondField} AND second_condition_field = #{firstField})")
    List<DoubleRule> selectByFieldCombination(@Param("firstField") String firstField,
                                            @Param("secondField") String secondField);
}