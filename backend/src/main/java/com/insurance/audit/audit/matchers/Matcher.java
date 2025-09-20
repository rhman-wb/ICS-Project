package com.insurance.audit.audit.matchers;

import com.insurance.audit.audit.service.DocumentProvider.DocumentChunk;
import com.insurance.audit.audit.service.RuleProvider.Rule;
import lombok.Data;
import lombok.Builder;
import java.util.List;
import java.util.Map;

/**
 * 匹配器接口
 * 定义各种匹配器的通用接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface Matcher {

    /**
     * 执行匹配
     *
     * @param rule 检核规则
     * @param chunks 文档块列表
     * @return 匹配结果列表
     */
    List<MatchResult> match(Rule rule, List<DocumentChunk> chunks);

    /**
     * 匹配结果
     */
    @Data
    @Builder
    class MatchResult {
        private String ruleId;
        private String chunkId;
        private String text;
        private Double score;
        private Double threshold;
        private String status; // PASSED, FAILED, WARNING
        private List<Evidence> evidences;
        private Map<String, Object> metadata;
    }

    /**
     * 证据
     */
    @Data
    @Builder
    class Evidence {
        private String text;
        private Integer startPos;
        private Integer endPos;
        private String matchType;
        private Map<String, Object> context;
    }
}