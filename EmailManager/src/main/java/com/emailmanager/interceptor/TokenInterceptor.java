package com.emailmanager.interceptor;

import com.emailmanager.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT Token 认证拦截器
 */
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行浏览器的 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头中获取 Authorization
        String authHeader = request.getHeader("Authorization");

        // 检查请求头是否存在，且是否以 "Bearer " 开头
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header is missing or invalid");
            return false;
        }

        // 提取Token字符串 (去掉 "Bearer " 前缀)
        String token = authHeader.substring(7);

        try {
            // 解析Token
            Claims claims = JwtUtil.parseToken(token);

            // 将从Token中解析出的用户信息存入request的attribute中，
            // 以便后续的Controller或其他组件可以使用
            request.setAttribute("userId", claims.get("userId", Long.class));
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", claims.get("role", String.class));

            // Token验证通过，放行请求
            return true;
        } catch (Exception e) {
            // Token无效（过期、格式错误、签名不匹配等）
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return false;
        }
    }
}