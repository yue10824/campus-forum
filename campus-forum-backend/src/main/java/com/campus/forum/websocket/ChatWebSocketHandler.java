package com.campus.forum.websocket;

import com.campus.forum.entity.Message;
import com.campus.forum.mapper.MessageMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 私信 WebSocket 处理器
 * 连接地址：ws://localhost:8080/ws/chat?userId={userId}&token={jwt}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageMapper messageMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("私信 WebSocket 连接: userId={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode json = objectMapper.readTree(message.getPayload());
        Long senderId = getUserId(session);
        Long receiverId = json.get("receiverId").asLong();
        String content = json.get("content").asText();

        // 持久化消息
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);

        // 实时推送给接收者
        WebSocketSession receiverSession = sessions.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            Map<String, Object> resp = Map.of(
                    "type", "chat",
                    "senderId", senderId,
                    "content", content,
                    "time", LocalDateTime.now().toString()
            );
            receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
        }
    }

    private Long getUserId(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                return Long.parseLong(query.split("userId=")[1].split("&")[0]);
            }
        } catch (Exception e) {
            log.warn("获取userId失败: {}", e.getMessage());
        }
        return null;
    }
}
