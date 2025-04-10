package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.entity.AiChatHistory;
import com.campus.forum.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AI 大模型控制器
 *
 * 接口说明：
 * 1. /api/ai/chat：SSE 流式对话，前端必须用 fetch + ReadableStream 处理
 * 2. /api/ai/generate-desc：同步生成活动简介
 * 3. /api/ai/review：内容审核辅助
 * 4. /api/ai/history：查询对话历史
 */
@Tag(name = "AI大模型接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * AI 活动助手 - 流式对话（SSE）
     * 踩坑：前端必须用 fetch API 而不是 axios 来接收 SSE 流
     * 示例：
     * const response = await fetch('/api/ai/chat', { method: 'POST', ... })
     * const reader = response.body.getReader()
     * while(true) { const { done, value } = await reader.read(); if(done) break; ... }
     */
    @Operation(summary = "AI对话（SSE流式输出）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return aiService.chat(userId, sessionId, message);
    }

    @Operation(summary = "AI生成活动简介")
    @PostMapping("/generate-desc")
    public Result<String> generateDesc(@RequestParam String keyword) {
        return Result.success(aiService.generateActivityDesc(keyword));
    }

    @Operation(summary = "AI内容审核辅助")
    @PostMapping("/review")
    public Result<Map<String, Object>> review(@RequestParam String content) {
        return Result.success(aiService.reviewContent(content));
    }

    @Operation(summary = "查询AI对话历史")
    @GetMapping("/history")
    public Result<List<AiChatHistory>> history(
            @RequestParam(defaultValue = "default") String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(aiService.getChatHistory(userId, sessionId));
    }
}
