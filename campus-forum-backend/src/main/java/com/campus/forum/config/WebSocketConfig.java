package com.campus.forum.config;

import com.campus.forum.websocket.ChatWebSocketHandler;
import com.campus.forum.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket 配置
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;
    private final NotificationWebSocketHandler notifyHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOriginPatterns("*");
        registry.addHandler(notifyHandler, "/ws/notify")
                .setAllowedOriginPatterns("*");
    }
}
