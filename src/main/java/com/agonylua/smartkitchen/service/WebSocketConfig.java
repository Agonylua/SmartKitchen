package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.handler.NotificationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // 开启 WebSocket 支持
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private NotificationHandler notificationHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 1. 绑定处理器
        // 2. 设置访问路径为 /ws/notification
        // 3. setAllowedOrigins("*") 允许跨域（方便本地测试）
        registry.addHandler(notificationHandler, "/ws/notification")
                .setAllowedOrigins("*");
    }
}
