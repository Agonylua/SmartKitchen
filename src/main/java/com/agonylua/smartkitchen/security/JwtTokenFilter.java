package com.agonylua.smartkitchen.security;

import com.agonylua.smartkitchen.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 过滤器：每次请求都会经过这里
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService; // Spring Security用于加载用户数据的接口

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. 获取请求头中的 Authorization 字段
        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. 判断 Header 是否以 "Bearer " 开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // 去掉 "Bearer " 前缀拿到纯 Token
            try {
                // 从 Token 中解析出用户名 (需要在 JwtUtil 中实现 extractUsername)
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("JWT 解析失败或过期: " + e.getMessage());
            }
        }

        // 3. 如果拿到了用户名，且当前上下文没有认证信息 (说明还没登录)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 从数据库加载用户信息
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 4. 验证 Token 是否有效 (需要在 JwtUtil 中实现 validateToken)
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 5. 生成认证对象
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. 【关键】将认证信息放入 Spring Security 上下文
                // 只要这行代码执行成功，后面的 Controller 就认为用户已登录
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 7. 放行，继续执行下一个过滤器
        chain.doFilter(request, response);
    }
}