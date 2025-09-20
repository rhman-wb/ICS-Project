package com.insurance.audit.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规则提供者服务
 * 从 rule-configuration-system 拉取规则快照（版本/阈值），含缓存与版本绑定
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class RuleProvider {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${audit.rule-service.base-url:http://localhost:8080}")
    private String ruleServiceBaseUrl;

    @Value("${audit.rule-service.timeout:5000}")
    private int timeoutMs;

    /**
     * 获取有效规则集（带缓存）
     *
     * @param ruleSetId 规则集ID
     * @return 规则集快照
     */
    @Cacheable(value = "rulesets", key = "#ruleSetId", unless = "#result == null")
    public RuleSet getEffectiveRuleSet(String ruleSetId) {
        log.info("获取有效规则集: ruleSetId={}", ruleSetId);

        try {
            // 构建请求URL
            String url = UriComponentsBuilder.fromHttpUrl(ruleServiceBaseUrl)
                    .path("/api/v1/rules/rulesets/{ruleSetId}/effective")
                    .buildAndExpand(ruleSetId)
                    .toUriString();

            log.debug("请求规则服务: url={}", url);

            // 发送HTTP请求
            ResponseEntity<RuleSetResponse> response = restTemplate.getForEntity(url, RuleSetResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                RuleSetResponse ruleSetResponse = response.getBody();

                log.info("成功获取规则集: ruleSetId={}, version={}, ruleCount={}",
                        ruleSetId, ruleSetResponse.getVersion(),
                        ruleSetResponse.getRules() != null ? ruleSetResponse.getRules().size() : 0);

                return convertToRuleSet(ruleSetResponse);
            } else {
                log.warn("规则服务返回异常状态: ruleSetId={}, status={}", ruleSetId, response.getStatusCode());
                throw new RuntimeException("规则服务返回异常状态: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("获取规则集失败: ruleSetId={}, error={}", ruleSetId, e.getMessage(), e);

            // 返回默认规则集以确保系统可用性
            return getDefaultRuleSet(ruleSetId);
        }
    }

    /**
     * 获取规则集版本信息
     *
     * @param ruleSetId 规则集ID
     * @return 版本信息
     */
    @Cacheable(value = "ruleset-versions", key = "#ruleSetId")
    public String getRuleSetVersion(String ruleSetId) {
        log.debug("获取规则集版本: ruleSetId={}", ruleSetId);

        try {
            String url = UriComponentsBuilder.fromHttpUrl(ruleServiceBaseUrl)
                    .path("/api/v1/rules/rulesets/{ruleSetId}/version")
                    .buildAndExpand(ruleSetId)
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return (String) body.get("version");
            }

        } catch (Exception e) {
            log.warn("获取规则集版本失败: ruleSetId={}, error={}", ruleSetId, e.getMessage());
        }

        return "unknown";
    }

    /**
     * 检查规则集是否存在
     *
     * @param ruleSetId 规则集ID
     * @return 是否存在
     */
    public boolean ruleSetExists(String ruleSetId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ruleServiceBaseUrl)
                    .path("/api/v1/rules/rulesets/{ruleSetId}/exists")
                    .buildAndExpand(ruleSetId)
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return Boolean.TRUE.equals(body.get("exists"));
            }

        } catch (Exception e) {
            log.warn("检查规则集存在性失败: ruleSetId={}, error={}", ruleSetId, e.getMessage());
        }

        return false;
    }

    /**
     * 转换响应为内部数据结构
     */
    private RuleSet convertToRuleSet(RuleSetResponse response) {
        List<Rule> rules = response.getRules() != null ?
                response.getRules().stream()
                        .map(this::convertToRule)
                        .toList() : List.of();

        return RuleSet.builder()
                .id(response.getId())
                .version(response.getVersion())
                .name(response.getName())
                .description(response.getDescription())
                .createTime(LocalDateTime.now())
                .rules(rules)
                .build();
    }

    /**
     * 转换规则响应为内部数据结构
     */
    private Rule convertToRule(RuleResponse ruleResponse) {
        return Rule.builder()
                .id(ruleResponse.getId())
                .name(ruleResponse.getName())
                .type(ruleResponse.getType())
                .description(ruleResponse.getDescription())
                .threshold(ruleResponse.getThreshold())
                .parameters(ruleResponse.getParameters())
                .build();
    }

    /**
     * 获取默认规则集（降级处理）
     */
    private RuleSet getDefaultRuleSet(String ruleSetId) {
        log.info("使用默认规则集: ruleSetId={}", ruleSetId);

        return RuleSet.builder()
                .id(ruleSetId)
                .version("default-1.0.0")
                .name("默认规则集")
                .description("规则服务不可用时的默认规则集")
                .createTime(LocalDateTime.now())
                .rules(List.of(
                    Rule.builder()
                        .id("default-rule-001")
                        .name("默认检核规则")
                        .type("KEYWORD")
                        .description("默认关键词检核规则")
                        .threshold(0.8)
                        .parameters(Map.of("keywords", List.of("保险", "条款")))
                        .build()
                ))
                .build();
    }

    /**
     * 规则集响应DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RuleSetResponse {
        private String id;
        private String version;
        private String name;
        private String description;
        private String status;
        private List<RuleResponse> rules;
    }

    /**
     * 规则响应DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RuleResponse {
        private String id;
        private String name;
        private String type;
        private String description;
        private Double threshold;
        private Map<String, Object> parameters;
    }

    /**
     * 规则集数据结构
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RuleSet {
        private String id;
        private String version;
        private String name;
        private String description;
        private LocalDateTime createTime;
        private List<Rule> rules;
    }

    /**
     * 规则数据结构
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Rule {
        private String id;
        private String name;
        private String type;
        private String description;
        private Double threshold;
        private Map<String, Object> parameters;
    }
}