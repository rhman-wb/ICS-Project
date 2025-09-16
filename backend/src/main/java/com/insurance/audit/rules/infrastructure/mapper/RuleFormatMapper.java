package com.insurance.audit.rules.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insurance.audit.rules.domain.ruletypes.FormatRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 格式规则数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Mapper
public interface RuleFormatMapper extends BaseMapper<FormatRule> {

    /**
     * 根据规则ID查询格式规则
     *
     * @param ruleId 规则ID
     * @return 格式规则
     */
    @Select("SELECT * FROM rule_format WHERE rule_id = #{ruleId}")
    FormatRule selectByRuleId(@Param("ruleId") String ruleId);

    /**
     * 根据验证字段查询格式规则
     *
     * @param validationField 验证字段
     * @return 格式规则列表
     */
    @Select("SELECT * FROM rule_format WHERE validation_field = #{validationField}")
    List<FormatRule> selectByValidationField(@Param("validationField") String validationField);

    /**
     * 根据格式类型查询格式规则
     *
     * @param formatType 格式类型
     * @return 格式规则列表
     */
    @Select("SELECT * FROM rule_format WHERE format_type = #{formatType}")
    List<FormatRule> selectByFormatType(@Param("formatType") String formatType);

    /**
     * 根据正则表达式查询格式规则
     *
     * @param regexPattern 正则表达式
     * @return 格式规则列表
     */
    @Select("SELECT * FROM rule_format WHERE regex_pattern = #{regexPattern}")
    List<FormatRule> selectByRegexPattern(@Param("regexPattern") String regexPattern);

    /**
     * 统计各格式类型的使用次数
     *
     * @return 格式类型统计
     */
    @Select("SELECT format_type, COUNT(*) as count FROM rule_format GROUP BY format_type")
    List<Object> countByFormatType();

    /**
     * 统计各验证字段的使用次数
     *
     * @return 验证字段统计
     */
    @Select("SELECT validation_field, COUNT(*) as count FROM rule_format GROUP BY validation_field")
    List<Object> countByValidationField();

    /**
     * 查询包含特定示例的格式规则
     *
     * @param example 示例内容
     * @return 格式规则列表
     */
    @Select("SELECT * FROM rule_format WHERE format_example LIKE CONCAT('%', #{example}, '%')")
    List<FormatRule> selectByExample(@Param("example") String example);

    /**
     * 查询自定义格式的规则
     *
     * @return 格式规则列表
     */
    @Select("SELECT * FROM rule_format WHERE format_type = 'CUSTOM' AND regex_pattern IS NOT NULL")
    List<FormatRule> selectCustomFormatRules();
}