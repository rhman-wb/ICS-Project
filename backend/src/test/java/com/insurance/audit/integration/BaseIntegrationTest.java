package com.insurance.audit.integration;

import com.insurance.audit.common.MockDataService;
import com.insurance.audit.common.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集成测试基类
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    
    @LocalServerPort
    protected int port;
    
    @Autowired
    protected TestRestTemplate restTemplate;
    
    @Autowired
    protected MockDataService mockDataService;
    
    protected String baseUrl;
    
    @BeforeEach
    void setUpBase() {
        baseUrl = "http://localhost:" + port;
        mockDataService.initMockData();
        TestSecurityUtils.clearSecurityContext();
    }
    
    @AfterEach
    void tearDownBase() {
        mockDataService.clearMockData();
        TestSecurityUtils.clearSecurityContext();
    }
    
    /**
     * 获取API基础URL
     */
    protected String getApiUrl(String path) {
        return baseUrl + "/api/v1" + path;
    }
    
    /**
     * 获取带认证头的HTTP头
     */
    protected org.springframework.http.HttpHeaders getAuthHeaders(String token) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}