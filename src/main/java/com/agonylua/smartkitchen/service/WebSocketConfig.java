package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.handler.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 1. 绑定处理器
        // 2. 设置访问路径为 /ws/notification
        // 3. setAllowedOrigins("*") 允许跨域（方便本地测试）
        registry.addHandler(webSocketHandler, "/ws/notification")
                .setAllowedOrigins("*");
    }
}
