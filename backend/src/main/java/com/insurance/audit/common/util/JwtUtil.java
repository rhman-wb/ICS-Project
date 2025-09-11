package com.insurance.audit.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT工具类
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
@Slf4j
public class JwtUtil {
    
    @Value("${app.jwt.secret:insurance-audit-system-jwt-secret-key-2024}")
    private String secret;
    
    @Value("${app.jwt.expiration:86400}")
    private Long expiration; // 24小时，单位：秒
    
    @Value("${app.jwt.refresh-expiration:604800}")
    private Long refreshExpiration; // 7天，单位：秒
    
    /**
     * 生成JWT令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }
    
    /**
     * 生成JWT令牌（带额外声明）
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return createToken(claims, username);
    }
    
    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createRefreshToken(claims, username);
    }
    
    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationFromToken(String token) {
        return getExpirationDateFromToken(token);
    }
    
    /**
     * 从令牌中获取JTI
     */
    public String getJtiFromToken(String token) {
        return getClaimFromToken(token, Claims::getId);
    }
    
    /**
     * 从令牌中获取指定声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从令牌中获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查令牌是否过期
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 验证令牌
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }
    
    /**
     * 验证令牌（不检查用户名）
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证刷新令牌
     */
    public Boolean validateRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String type = (String) claims.get("type");
            return "refresh".equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成访问令牌（使用UserDetails）
     */
    public String generateAccessToken(org.springframework.security.core.userdetails.UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername());
    }
    
    /**
     * 生成刷新令牌（使用UserDetails）
     */
    public String generateRefreshToken(org.springframework.security.core.userdetails.UserDetails userDetails) {
        return generateRefreshToken(userDetails.getUsername());
    }
    
    /**
     * 获取访问令牌过期时间（秒）
     */
    public Long getAccessTokenExpiration() {
        return expiration;
    }
    
    /**
     * 获取刷新令牌过期时间（秒）
     */
    public Long getRefreshTokenExpiration() {
        return refreshExpiration;
    }
    
    /**
     * 创建令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 创建刷新令牌
     */
    private String createRefreshToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}