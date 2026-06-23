# AI集成踩坑记录

> 本文档记录项目开发过程中集成大模型 API（DeepSeek V4 Pro）及相关 AI 功能时遇到的 **真实问题与解决方案**，供后续类似项目参考。

---

## 踩坑 #1：SSE 在 Nginx 反向代理下频繁断流

### 问题描述

本地开发环境 AI 助手对话功能完全正常，部署到服务器（Nginx 反代 Spring Boot）后，SSE 流式输出只能收到第一个 chunk，随后连接被断开，前端出现 `ERR_INCOMPLETE_CHUNKED_ENCODING` 错误。

### 根本原因

Nginx 默认开启了 `proxy_buffering`，会将后端响应全部缓冲到内存后再一次性返回给客户端，导致 SSE 流式数据无法实时推送。

### 解决方案

在 Nginx 配置的 `/api/ai/` location 块中添加如下指令：

```nginx
location /api/ai/ {
    proxy_pass http://localhost:8080;
    proxy_buffering off;           # 关闭缓冲，允许流式响应
    proxy_cache off;               # 关闭缓存
    proxy_read_timeout 300s;       # 延长超时时间（AI响应可能较慢）
    proxy_set_header Connection '';
    proxy_http_version 1.1;
    chunked_transfer_encoding on;  # 启用分块传输编码
}
```

### 经验总结

- 凡是涉及 SSE / 流式响应的接口，**必须单独配置 Nginx location 并关闭 proxy_buffering**。
- 建议在开发阶段就在 `application.yml` 中配置独立路径前缀（如 `/api/ai/stream`），方便 Nginx 单独处理。
- 潘樾在 Sprint 3 联调时首先发现了该问题，通过抓包确认了 Nginx 缓冲导致流式响应被一次性返回。

---

## 踩坑 #2：前端用 axios 接收 SSE 流式输出失败

### 问题描述

后端 `SseEmitter` 正确推送数据，但前端使用 axios 发起 POST 请求后，`onDownloadProgress` 回调触发不一致，且整个响应在 AI 回复完成后才一次性返回，流式效果完全失效。

### 根本原因

**axios 不支持真正的流式读取**。axios 底层对响应数据做了完整缓冲，`onDownloadProgress` 只是下载进度回调，并不能实现逐 chunk 处理。

### 解决方案

放弃 axios，改用原生 **fetch API + ReadableStream** 处理 SSE：

```javascript
// ❌ 错误方式：用 axios
const res = await axios.post('/api/ai/chat', data)  // 无法流式

// ✅ 正确方式：用 fetch + ReadableStream
const res = await fetch('/api/ai/chat', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({ message, sessionId })
})

const reader = res.body.getReader()
const decoder = new TextDecoder()

while (true) {
  const { done, value } = await reader.read()
  if (done) break
  
  const chunk = decoder.decode(value)
  // SSE 格式：每行以 "data:" 开头
  for (const line of chunk.split('\n')) {
    if (line.startsWith('data:')) {
      const data = line.slice(5).trim()
      if (data && data !== '[DONE]') {
        outputText += data
        updateUI(outputText)
      }
    }
  }
}
```

### 经验总结

- SSE 前端接收必须使用 **fetch + ReadableStream** 或 `EventSource`（GET请求）。
- POST 型 SSE 只能用 fetch，不能用 EventSource（EventSource 仅支持 GET）。
- 注意 SSE 数据可能跨 chunk 分割，需要维护缓冲区处理不完整行。
- 李倩在开发前端 AI 助手页面时遇到此问题，潘樾协助排查后确认 axios 不支持流式读取。

---

## 踩坑 #3：DeepSeek API Key 管理不当导致 403 认证失败

### 问题描述

项目上线运行一段时间后，AI 功能突然全部失效，日志显示请求 DeepSeek API 返回 `403 Forbidden`，报错信息 `Invalid API key`。

### 根本原因

DeepSeek API 使用 Bearer Token 认证，API Key 存储在 `application.yml` 配置文件中。开发过程中误将含真实 Key 的配置文件提交到 Git 公开仓库，导致 Key 泄露后被平台自动封禁。

### 解决方案

1. 在 DeepSeek 平台重新生成 API Key
2. 将 `application.yml` 加入 `.gitignore`，避免敏感信息提交
3. 使用环境变量注入方式管理 Key：

```java
@Value("${ai.api-key}")
private String apiKey;
```

```yaml
# application.yml
ai:
  api-key: ${AI_API_KEY:sk-default}
```

### 经验总结

- **API Key 等敏感信息绝对不能提交到 Git 仓库**。
- 推荐使用环境变量或 `.env` 文件管理密钥，配合 `.gitignore` 隔离。
- 团队开发时，在 `application.yml.example` 中存放配置模板，实际密钥由各成员本地维护。

---

## 踩坑 #4：WebSocket 握手阶段无法携带自定义 Authorization Header

### 问题描述

WebSocket 连接时尝试在 JavaScript 客户端设置自定义 Header 传递 JWT：

```javascript
// ❌ 浏览器报错：WebSocket 不支持自定义请求头
const ws = new WebSocket('ws://localhost:8080/ws/chat', {
  headers: { 'Authorization': 'Bearer xxx' }  // 浏览器忽略此参数
})
```

### 根本原因

**浏览器安全策略**规定：WebSocket 握手阶段不允许 JavaScript 设置自定义 HTTP 请求头（`Authorization` 等），只能设置协议（`Sec-WebSocket-Protocol`）。

### 解决方案

将 Token 作为 **URL Query Parameter** 传递：

```javascript
// ✅ 正确方式：Token 通过 URL 参数传递
const token = localStorage.getItem('token')
const ws = new WebSocket(`ws://localhost:8080/ws/chat?token=${token}`)
```

后端 Spring WebSocket 握手拦截器中读取：

```java
@Override
public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Map<String, Object> attributes) {
    if (request instanceof ServletServerHttpRequest servletRequest) {
        String token = servletRequest.getServletRequest().getParameter("token");
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            attributes.put("userId", userId);
            return true;
        }
    }
    return false;  // 拒绝未认证连接
}
```

### 经验总结

- WebSocket 认证**只能通过 URL 参数或 Cookie** 传递 Token。
- URL 参数方式存在 Token 泄露风险（出现在日志/浏览器历史），生产环境建议结合 HTTPS 并设置较短 Token 有效期。
- 也可使用 WebSocket 子协议字段传递 Token（更隐蔽，但兼容性稍差）。

---

## 踩坑 #5：推荐算法冷启动 — 新用户返回空列表

### 问题描述

Item-CF 协同过滤推荐算法上线后，新注册用户（无浏览/报名行为记录）打开首页时，推荐接口返回空数组，前端首页一片空白，用户体验极差。

### 根本原因

纯 CF 算法依赖用户历史行为数据，新用户完全没有行为记录，相似度计算结果为零，无法产生推荐结果。

### 解决方案

增加**冷启动兜底策略**，按优先级降级处理：

```java
public List<Activity> recommend(Long userId, int size) {
    // 1. 先尝试 Item-CF 个性化推荐
    List<Activity> cfResult = itemCFRecommend(userId, size);
    
    if (cfResult.size() >= size) {
        return cfResult;  // 数据充足，直接返回
    }
    
    // 2. CF 数据不足时，用热门活动补充
    List<Long> excludeIds = cfResult.stream()
        .map(Activity::getId).collect(Collectors.toList());
    
    int remaining = size - cfResult.size();
    List<Activity> hotList = activityMapper.selectHotActivities(excludeIds, remaining);
    
    cfResult.addAll(hotList);
    
    // 3. 还不够则补充最新活动
    if (cfResult.size() < size) {
        // ...获取最新活动兜底
    }
    
    return cfResult;
}
```

### 经验总结

- **推荐系统必须设计冷启动方案**：热门推荐、最新推荐、随机推荐至少要有一种兜底。
- 可以用用户注册时填写的兴趣标签做初始推荐，比纯热门推荐体验更好。
- 建议对"新用户"（行为数 < 5）单独走热门推荐逻辑，不进入 CF 计算（节省计算资源）。
- 张鑫在开发 Item-CF 推荐算法时遇到此问题，通过增加热门活动兜底逻辑解决。

---

## 踩坑 #6：AI 生成内容含有 Markdown 符号直接存入数据库，前端显示乱码

### 问题描述

AI 生成活动简介功能上线后，部分简介内容包含 `**粗体**`、`## 标题` 等 Markdown 语法，直接存入数据库后，在纯文本展示场景（如活动列表卡片）中显示为原始 Markdown 符号，影响阅读体验。

### 根本原因

文心一言默认在回复中使用 Markdown 格式，而项目部分页面使用纯文本渲染，未引入 Markdown 解析库。

### 解决方案

双层处理策略：

**方案A（前端解析）**：需要富文本展示的地方（活动详情页）引入 `marked` 库渲染 Markdown：

```javascript
import { marked } from 'marked'
// 模板中：
// <div v-html="marked(activity.description)"></div>
```

**方案B（后端清洗）**：AI 生成简介时，在 Prompt 中明确要求不使用 Markdown：

```java
String prompt = "请为以下活动生成200字以内的纯文本简介，不要使用Markdown格式，不要使用**、##等符号：\n活动名称：" + title;
```

最终采用 **方案B为主，方案A为辅**：简介字段存纯文本，详情页内容字段支持 Markdown。

### 经验总结

- 调用 AI 生成内容时，**Prompt 中要明确限定输出格式**（纯文本/JSON/特定结构），否则输出格式不可控。
- 数据库设计时需区分"纯文本字段"和"富文本字段"，前者要做 Markdown 清洗，后者要防 XSS。
- 对 AI 生成内容展示时，始终要防范 XSS 攻击（使用 `v-html` 时先过滤 HTML 标签）。
- 潘樾在设计 AI 活动简介生成 Prompt 时发现 DeepSeek 默认输出 Markdown，通过在 Prompt 中明确要求"纯文本"解决。
