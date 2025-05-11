package com.campus.forum.service.impl;

import com.campus.forum.entity.AiChatHistory;
import com.campus.forum.mapper.AiChatHistoryMapper;
import com.campus.forum.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AI 大模型服务实现（DeepSeek V3）
 *
 * 踩坑记录：
 * 1. 大模型 API 流式输出（SSE）前端不能用普通 axios，需要用 fetch + ReadableStream 解析
 * 2. DeepSeek 兼容 OpenAI 格式，使用 Bearer Token 认证，比文心一言更简单
 * 3. 流式响应中 delta.content 可能为 null（最后一条），需要判断
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.base-url:https://api.deepseek.com/v1/chat/completions}")
    private String baseUrl;

    private final AiChatHistoryMapper chatHistoryMapper;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SseEmitter chat(Long userId, String sessionId, String userMessage) {
        SseEmitter emitter = new SseEmitter(90000L);

        // 保存用户消息
        saveHistory(userId, sessionId, "user", userMessage);

        // 获取历史消息（上下文）
        List<Map<String, String>> messages = buildMessages(userId, sessionId, userMessage);

        // 异步调用 AI API
        new Thread(() -> {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("model", model);
                body.put("messages", messages);
                body.put("stream", true);
                body.put("max_tokens", 1024);

                RequestBody requestBody = RequestBody.create(
                        objectMapper.writeValueAsString(body),
                        MediaType.get("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(baseUrl)
                        .post(requestBody)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .build();

                StringBuilder fullReply = new StringBuilder();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        String errBody = response.body() != null ? response.body().string() : "无响应";
                        log.error("DeepSeek API 响应异常: {} - {}", response.code(), errBody);
                        emitter.send(SseEmitter.event().data("[ERROR] AI服务暂时不可用，状态码：" + response.code()));
                        emitter.complete();
                        return;
                    }
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream(), "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) break;
                            if (data.isEmpty()) continue;
                            try {
                                JsonNode node = objectMapper.readTree(data);
                                JsonNode delta = node.path("choices").path(0).path("delta");
                                if (delta.has("content") && !delta.get("content").isNull()) {
                                    String chunk = delta.get("content").asText();
                                    if (!chunk.isEmpty()) {
                                        fullReply.append(chunk);
                                        emitter.send(SseEmitter.event().data(chunk));
                                    }
                                }
                            } catch (Exception parseEx) {
                                log.debug("解析SSE行失败（忽略）: {}", data);
                            }
                        }
                    }
                }
                // 保存 AI 回复
                if (fullReply.length() > 0) {
                    saveHistory(userId, sessionId, "assistant", fullReply.toString());
                }
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI 对话出错: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event().data("[ERROR] " + e.getMessage()));
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        }).start();

        return emitter;
    }

    @Override
    public String generateActivityDesc(String keyword) {
        String prompt = "请根据以下关键词，生成一段150字左右的校园活动简介，语言活泼有趣，吸引同学参与：\n关键词：" + keyword;
        return callAiSync(prompt);
    }

    @Override
    public Map<String, Object> reviewContent(String content) {
        String prompt = "请判断以下内容是否存在违规情况（广告垃圾/色情/政治敏感/人身攻击），" +
                "严格只返回一个JSON对象，不要markdown代码块，不要任何解释文字，格式：" +
                "{\"violationScore\": 整数0-100, \"violationType\": \"违规类型，无违规填null\", \"suggestion\": \"一句话审核建议\"}" +
                "\n待审核内容：" + content;
        String result = callAiSync(prompt);
        try {
            // 去掉可能包裹的 markdown 代码块
            String json = result.replaceAll("```json", "").replaceAll("```", "").trim();
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}') + 1;
            if (start >= 0 && end > start) {
                return objectMapper.readValue(json.substring(start, end), Map.class);
            }
        } catch (Exception e) {
            log.warn("解析 AI 审核结果失败: {}, 原始结果: {}", e.getMessage(), result);
        }
        return Map.of("violationScore", 0, "violationType", null, "suggestion", result);
    }

    @Override
    public List<AiChatHistory> getChatHistory(Long userId, String sessionId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatHistory> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatHistory>()
                .eq(AiChatHistory::getUserId, userId)
                .eq(AiChatHistory::getSessionId, sessionId)
                .orderByAsc(AiChatHistory::getCreatedAt);
        return chatHistoryMapper.selectList(wrapper);
    }

    // ======================== 私有方法 ========================

    private String callAiSync(String prompt) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", prompt);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", List.of(msg));
            body.put("stream", false);
            body.put("max_tokens", 512);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(baseUrl)
                    .post(requestBody)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() == null) return "AI服务暂时不可用";
                JsonNode node = objectMapper.readTree(response.body().string());
                return node.path("choices").path(0).path("message").path("content").asText("AI未返回结果");
            }
        } catch (Exception e) {
            log.error("同步调用AI失败: {}", e.getMessage());
            return "AI服务异常，请稍后重试";
        }
    }

    private List<Map<String, String>> buildMessages(Long userId, String sessionId, String userMessage) {
        List<AiChatHistory> history = getChatHistory(userId, sessionId);
        List<Map<String, String>> messages = new ArrayList<>();
        // 系统提示词（DeepSeek 支持 system 角色）
        messages.add(Map.of("role", "system", "content",
                "你是校园活动发布平台的AI助手小Campus，负责帮助用户了解和参与校园活动。" +
                "你熟悉各类校园活动（运动会、社团、讲座、竞赛、文艺演出等），" +
                "回答简洁友好，适当使用emoji增加亲和力。"));
        // 历史消息（最近10条，避免token超限）
        int startIdx = Math.max(0, history.size() - 10);
        for (int i = startIdx; i < history.size(); i++) {
            AiChatHistory h = history.get(i);
            messages.add(Map.of("role", h.getRole(), "content", h.getContent()));
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        return messages;
    }

    private void saveHistory(Long userId, String sessionId, String role, String content) {
        AiChatHistory history = new AiChatHistory();
        history.setUserId(userId);
        history.setSessionId(sessionId);
        history.setRole(role);
        history.setContent(content);
        history.setCreatedAt(LocalDateTime.now());
        chatHistoryMapper.insert(history);
    }
}
