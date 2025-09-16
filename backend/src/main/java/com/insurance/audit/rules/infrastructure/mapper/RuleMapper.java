package com.insurance.audit.rules.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.rules.domain.Rule;
import com.insurance.audit.rules.interfaces.dto.request.RuleQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 规则数据访问层
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Mapper
public interface RuleMapper extends BaseMapper<Rule> {

    /**
     * 分页查询规则列表（带条件筛选）
     *
     * @param page 分页参数
     * @param queryRequest 查询条件
     * @return 分页规则列表
     */
    IPage<Rule> selectPageWithQuery(@Param("page") Page<Rule> page,
                                  @Param("query") RuleQueryRequest queryRequest);

    /**
     * 根据规则类型查询规则
     *
     * @param ruleType 规则类型
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE rule_type = #{ruleType} AND deleted = 0")
    List<Rule> selectByRuleType(@Param("ruleType") String ruleType);

    /**
     * 根据关键词搜索规则
     *
     * @param keyword 搜索关键词
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE (rule_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR rule_description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR rule_content LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0")
    List<Rule> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 根据规则类型统计数量
     *
     * @return 类型统计Map
     */
    @Select("SELECT rule_type, COUNT(*) as count FROM rule WHERE deleted = 0 GROUP BY rule_type")
    Map<String, Long> countByType();

    /**
     * 根据审核状态统计数量
     *
     * @return 状态统计Map
     */
    @Select("SELECT audit_status, COUNT(*) as count FROM rule WHERE deleted = 0 GROUP BY audit_status")
    Map<String, Long> countByStatus();

    /**
     * 根据规则编号查询规则
     *
     * @param ruleNumber 规则编号
     * @return 规则实体
     */
    @Select("SELECT * FROM rule WHERE rule_number = #{ruleNumber} AND deleted = 0")
    Rule selectByRuleNumber(@Param("ruleNumber") String ruleNumber);

    /**
     * 根据审核状态查询规则
     *
     * @param auditStatus 审核状态
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE audit_status = #{auditStatus} AND deleted = 0")
    List<Rule> selectByAuditStatus(@Param("auditStatus") String auditStatus);

    /**
     * 根据有效状态查询规则
     *
     * @param effectiveStatus 有效状态
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE effective_status = #{effectiveStatus} AND deleted = 0")
    List<Rule> selectByEffectiveStatus(@Param("effectiveStatus") String effectiveStatus);

    /**
     * 查询关注的规则
     *
     * @param userId 用户ID
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE followed = 1 AND created_by = #{userId} AND deleted = 0")
    List<Rule> selectFollowedRules(@Param("userId") String userId);

    /**
     * 根据创建者查询规则
     *
     * @param createdBy 创建者ID
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE created_by = #{createdBy} AND deleted = 0")
    List<Rule> selectByCreatedBy(@Param("createdBy") String createdBy);

    /**
     * 查询最近更新的规则
     *
     * @param limit 限制数量
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE deleted = 0 ORDER BY last_updated_at DESC LIMIT #{limit}")
    List<Rule> selectRecentUpdated(@Param("limit") int limit);

    /**
     * 根据规则来源查询规则
     *
     * @param ruleSource 规则来源
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE rule_source = #{ruleSource} AND deleted = 0")
    List<Rule> selectByRuleSource(@Param("ruleSource") String ruleSource);

    /**
     * 查询待审核的规则数量
     *
     * @return 待审核数量
     */
    @Select("SELECT COUNT(*) FROM rule WHERE audit_status = 'PENDING' AND deleted = 0")
    Long countPendingAudit();

    /**
     * 查询即将过期的规则
     *
     * @param days 提前天数
     * @return 规则列表
     */
    @Select("SELECT * FROM rule WHERE effective_status = 'ACTIVE' " +
            "AND expired_at <= DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0")
    List<Rule> selectExpiringRules(@Param("days") int days);
}