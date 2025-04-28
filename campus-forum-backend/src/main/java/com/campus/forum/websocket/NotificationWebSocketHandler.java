package com.campus.forum.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统通知 WebSocket 处理器
 * 踩坑：浏览器 WebSocket API 不支持自定义Header，Token 通过 URL Query Parameter 传递
 * 示例：ws://localhost:8080/ws/notify?token=xxx
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    /** userId -> WebSocketSession 映射 */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("通知 WebSocket 连接建立: userId={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("通知 WebSocket 连接关闭: userId={}", userId);
        }
    }

    /**
     * 向指定用户推送通知消息
     */
    public void sendToUser(Long userId, String content) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> msg = Map.of("type", "notification", "content", content);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            } catch (IOException e) {
                log.warn("推送通知失败: userId={}, error={}", userId, e.getMessage());
            }
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                String userIdStr = query.split("userId=")[1].split("&")[0];
                return Long.parseLong(userIdStr);
            }
        } catch (Exception e) {
            log.warn("WebSocket 获取userId失败: {}", e.getMessage());
        }
        return null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 收到客户端心跳包，可处理
        log.debug("收到 WebSocket 消息: {}", message.getPayload());
    }
}
