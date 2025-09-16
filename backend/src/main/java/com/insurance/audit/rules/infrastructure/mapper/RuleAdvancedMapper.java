package com.insurance.audit.rules.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insurance.audit.rules.domain.ruletypes.AdvancedRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 高级规则数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Mapper
public interface RuleAdvancedMapper extends BaseMapper<AdvancedRule> {

    /**
     * 根据规则ID查询高级规则
     *
     * @param ruleId 规则ID
     * @return 高级规则
     */
    @Select("SELECT * FROM rule_advanced WHERE rule_id = #{ruleId}")
    AdvancedRule selectByRuleId(@Param("ruleId") String ruleId);

    /**
     * 根据脚本类型查询高级规则
     *
     * @param scriptType 脚本类型
     * @return 高级规则列表
     */
    @Select("SELECT * FROM rule_advanced WHERE script_type = #{scriptType}")
    List<AdvancedRule> selectByScriptType(@Param("scriptType") String scriptType);

    /**
     * 根据脚本引擎查询高级规则
     *
     * @param scriptEngine 脚本引擎
     * @return 高级规则列表
     */
    @Select("SELECT * FROM rule_advanced WHERE script_engine = #{scriptEngine}")
    List<AdvancedRule> selectByScriptEngine(@Param("scriptEngine") String scriptEngine);

    /**
     * 统计各脚本类型的使用次数
     *
     * @return 脚本类型统计
     */
    @Select("SELECT script_type, COUNT(*) as count FROM rule_advanced GROUP BY script_type")
    List<Object> countByScriptType();

    /**
     * 统计各脚本引擎的使用次数
     *
     * @return 脚本引擎统计
     */
    @Select("SELECT script_engine, COUNT(*) as count FROM rule_advanced GROUP BY script_engine")
    List<Object> countByScriptEngine();

    /**
     * 查询包含特定函数的高级规则
     *
     * @param functionName 函数名称
     * @return 高级规则列表
     */
    @Select("SELECT * FROM rule_advanced WHERE script_content LIKE CONCAT('%', #{functionName}, '%')")
    List<AdvancedRule> selectByFunction(@Param("functionName") String functionName);

    /**
     * 查询需要外部依赖的高级规则
     *
     * @return 高级规则列表
     */
    @Select("SELECT * FROM rule_advanced WHERE external_dependencies IS NOT NULL AND external_dependencies != ''")
    List<AdvancedRule> selectWithExternalDependencies();

    /**
     * 根据超时时间查询高级规则
     *
     * @param timeoutMs 超时毫秒数
     * @return 高级规则列表
     */
    @Select("SELECT * FROM rule_advanced WHERE timeout_ms <= #{timeoutMs}")
    List<AdvancedRule> selectByTimeout(@Param("timeoutMs") Integer timeoutMs);

    /**
     * 查询可缓存的高级规则
     *
     * @return 高级规则列表
     */
    @Select("SELECT * FROM rule_advanced WHERE cacheable = true")
    List<AdvancedRule> selectCacheableRules();

    /**
     * 根据参数配置查询高级规则
     *
     * @param paramKey 参数键
     * @return 高级规则列表
     */
    @Select("SELECT * FROM rule_advanced WHERE JSON_EXTRACT(parameters_config, CONCAT('$.', #{paramKey})) IS NOT NULL")
    List<AdvancedRule> selectByParameterKey(@Param("paramKey") String paramKey);
}