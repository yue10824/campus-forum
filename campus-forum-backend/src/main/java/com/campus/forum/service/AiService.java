package com.campus.forum.service;

import com.campus.forum.entity.AiChatHistory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface AiService {
    SseEmitter chat(Long userId, String sessionId, String userMessage);
    String generateActivityDesc(String keyword);
    Map<String, Object> reviewContent(String content);
    List<AiChatHistory> getChatHistory(Long userId, String sessionId);
}
