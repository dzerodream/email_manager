package com.emailmanager.util;

import com.emailmanager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 生成和校验工具类
 */
public class JwtUtil {

    // 密钥: 必须足够长且安全，在生产环境中应从配置文件中读取，而不是硬编码
    // Keys.secretKeyFor会为指定的算法生成一个安全的密钥
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 过期时间（毫秒），这里设置为7天
    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 7;

    /**
     * 根据用户信息生成JWT Token
     *
     * @param user 用户实体
     * @return Token字符串
     */
    public static String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        // 创建payload的私有声明（Claims）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());

        return Jwts.builder()
                .setClaims(claims) // 设置自定义声明
                .setSubject(user.getUsername()) // 设置主题，通常是用户名
                .setIssuedAt(now) // 设置签发时间
                .setExpiration(expiryDate) // 设置过期时间
                .signWith(SECRET_KEY) // 使用我们的密钥和HS256算法进行签名
                .compact();
    }

    /**
     * 从Token中解析出Claims（包含所有信息）
     * 如果Token无效（过期、被篡改等），此方法会抛出异常
     *
     * @param token Token字符串
     * @return Claims对象
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // 必须使用相同的密钥来验证签名
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否有效
     *
     * @param token Token字符串
     * @return 如果有效返回true，否则返回false
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            // 所有解析异常（如ExpiredJwtException, SignatureException等）都表示Token无效
            return false;
        }
    }
}