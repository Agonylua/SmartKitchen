package com.agonylua.smartkitchen.handler;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> suspendedMessages = new ConcurrentHashMap<>();

    /**
     * 定向发送消息给指定用户
     *
     * @param targetUserId 接收人的用户 ID
     * @param jsonMessage  要发送的 JSON 字符串内容
     */
    public static void sendMessageToUser(String targetUserId, String jsonMessage) {
        WebSocketSession session = userSessions.get(targetUserId);

        // 检查该用户是否在线，且会话处于开启状态
        if (session != null && session.isOpen()) {
            try {
                // 发送文本消息
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                System.err.println("向用户 [" + targetUserId + "] 发送消息失败: " + e.getMessage());
                suspendMessage(targetUserId, jsonMessage);
            }
        } else {
            // 用户离线情况，挂起任务
            System.out.println("发送失败：用户 [" + targetUserId + "] 不在线，消息已挂起");
            suspendMessage(targetUserId, jsonMessage);
        }
    }

    private static void suspendMessage(String targetUserId, String jsonMessage) {
        // TODO: 后续优化可以使用数据库进行持久化存储
        suspendedMessages.computeIfAbsent(targetUserId, k -> new CopyOnWriteArrayList<>()).add(jsonMessage);
    }

    /**
     * 客户端连接成功后触发
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        // 从连接的 URL 中获取当前用户的 ID
        String userId = extractUserIdFromSession(session);
        if (userId != null) {
            // 将用户 ID 和他的会话存入集合
            userSessions.put(userId, session);
            log.info("[WebSocket] 用户 [{}] 已上线。当前在线人数: {}", userId, userSessions.size());

            // 发送挂起的消息
            List<String> messages = suspendedMessages.remove(userId);
            if (messages != null && !messages.isEmpty()) {
                System.out.println("向用户 [" + userId + "] 发送 " + messages.size() + " 条挂起消息");
                for (String msg : messages) {
                    try {
                        session.sendMessage(new TextMessage(msg));
                    } catch (IOException e) {
                        log.info("[WebSocket] 向用户 [{}] 发送挂起消息失败: {}", userId, e.getMessage());
                        suspendMessage(userId, msg);
                    }
                }
            }
        } else {
            // 如果没传用户 ID，拒绝连接
            session.close(CloseStatus.BAD_DATA);
        }
    }

    /**
     * 客户端断开连接后触发
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String userId = extractUserIdFromSession(session);
        if (userId != null) {
            // 从在线集合中移除该用户
            userSessions.remove(userId, session);
            log.info("[WebSocket] 用户 [{}] 已离线。当前在线人数: {}", userId, userSessions.size());
        }
    }

    /**
     * 解析 userId 参数
     * 例如 URI 是: ws://localhost:8080/ws/notification?userId=1001
     */
    private String extractUserIdFromSession(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        if (query != null && query.contains("userId=")) {
            // 简单的字符串分割获取参数值
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    return param.substring(7);
                }
            }
        }
        return null;
    }
}