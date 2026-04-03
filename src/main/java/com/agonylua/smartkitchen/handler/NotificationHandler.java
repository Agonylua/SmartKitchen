package com.agonylua.smartkitchen.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationHandler extends TextWebSocketHandler {

    // 核心：这是一个线程安全的 Map，用来存放所有当前在线的 "用户ID" 和对应的 "WebSocket会话"
    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /**
     * 3. 供我们自己的业务代码调用的方法：定向发送消息给指定用户
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
            }
        } else {
            // 这里可以处理离线情况（比如在数据库里打个标记，等他下次上线时拉取）
            System.out.println("发送失败：用户 [" + targetUserId + "] 不在线");
        }
    }

    /**
     * 1. 客户端连接成功后触发
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从连接的 URL 中获取当前用户的 ID
        String userId = extractUserIdFromSession(session);
        if (userId != null) {
            // 将用户 ID 和他的会话存入集合
            userSessions.put(userId, session);
            System.out.println("用户 [" + userId + "] 已上线。当前在线人数: " + userSessions.size());
        } else {
            // 如果没传用户 ID，拒绝连接
            session.close(CloseStatus.BAD_DATA);
        }
    }

    /**
     * 2. 客户端断开连接后触发 (正常关闭或网络异常)
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = extractUserIdFromSession(session);
        if (userId != null) {
            // 从在线集合中移除该用户
            userSessions.remove(userId);
            System.out.println("用户 [" + userId + "] 已离线。当前在线人数: " + userSessions.size());
        }
    }

    /**
     * 辅助方法：从 WebSocket 的 URI 中解析 userId 参数
     * 例如 URI 是: ws://localhost:8080/ws/notification?userId=1001
     */
    private String extractUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
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