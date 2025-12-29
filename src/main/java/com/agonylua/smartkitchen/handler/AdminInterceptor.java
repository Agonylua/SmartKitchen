package com.agonylua.smartkitchen.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查 Session 中是否有登录标记
        Object admin = request.getSession().getAttribute("ADMIN_USER");
        if (admin == null) {
            // 没登录，踢回登录页
            response.sendRedirect("/admin/login");
            return false;
        }
        return true;
    }
}
