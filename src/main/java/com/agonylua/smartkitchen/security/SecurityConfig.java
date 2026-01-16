package com.agonylua.smartkitchen.security;

import com.agonylua.smartkitchen.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭 CSRF (因为我们使用 Token，不需要 CSRF 保护)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 设置 Session 管理为无状态 (Stateless)
                // 这意味着服务器不会保存用户的登录状态，每次请求都要带 Token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 配置路径拦截规则
                .authorizeHttpRequests(auth -> auth
                        // 放行登录接口，允许匿名访问
                        .requestMatchers("/user/**").permitAll()
                        // 放行注册接口
                        .requestMatchers("/device/**").permitAll()
                        // 其他所有接口都需要认证
                        .anyRequest().authenticated()
                );
        // 4. (后续步骤) 这里将来要添加 addFilterBefore 加入你的 JWT 过滤器
        http.addFilterBefore(new JwtTokenFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 配置密码加密器 (虽然这里暂时没用到数据库，但这是标配)
    // TODO 配置密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}