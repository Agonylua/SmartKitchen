package com.agonylua.smartkitchen.security;

import com.agonylua.smartkitchen.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.checkerframework.checker.nullness.qual.NonNull;
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

    private final JwtUtil jwtUtil;

    private final UserDetailsService userDetailsService;

    public JwtTokenFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        // 获取请求头中的 Authorization 字段
        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 判断 Header 是否以 "Bearer " 开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // 去掉 "Bearer " 前缀拿到纯 Token
            logger.info("[JWT Filter] JWT token " + jwt);
            try {
                // 从 Token 中解析出用户名
                if (jwtUtil != null) {
                    username = jwtUtil.extractUsername(jwt);
                }
            } catch (Exception e) {
                logger.error("[JWT Filter] JWT 解析失败或过期: " + e.getMessage());
            }
        }

        // 如果拿到了用户名，且当前上下文没有认证信息 (说明还没登录)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 将 validateToken 也放入 try-catch 保护，或者确保它不抛出异常
            try {
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    logger.info("[JWT Filter] Token 验证成功，用户 " + username + " 已认证");
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("[JWT Filter] Token 验证过程出错: " + e.getMessage());
            }
        }

        // 放行
        chain.doFilter(request, response);
    }
}