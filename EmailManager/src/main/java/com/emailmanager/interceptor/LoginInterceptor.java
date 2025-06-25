// 文件路径: com/emailmanager/interceptor/LoginInterceptor.java
package com.emailmanager.interceptor;

import com.emailmanager.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

public class LoginInterceptor implements HandlerInterceptor {

    // 创建一个包含所有公共路径的白名单
    private static final Set<String> PUBLIC_URIS = new HashSet<>();

    static {
        // 将登录和注册接口添加到白名单
        PUBLIC_URIS.add("/user/login");
        PUBLIC_URIS.add("/user/register");
        // 未来如果有其他公共API，也可以加在这里
        // PUBLIC_URIS.add("/public/announcements");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // --- 核心修改：在代码层面增加白名单校验 ---
        // 获取请求路径
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // 2. 如果请求路径在我们的白名单中，直接放行
        if (PUBLIC_URIS.contains(path)) {
            return true;
        }
        // --- 修改结束 ---

        // 3. 对于非白名单路径，执行原有的登录检查
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access Denied: Please login first.");
            return false;
        }

        return true;
    }
}