# Git Issue 列表 & 敏捷迭代计划

## 项目概况

| 项目 | 说明 |
|------|------|
| 项目名称 | 校园活动发布平台 |
| 开发周期 | 2025-03-01 ~ 2025-06-20（16周） |
| 迭代方式 | 敏捷 Scrum，共 4 个 Sprint，每 Sprint 2周 |
| 团队规模 | 5人 |
| 团队成员 | 潘樾、李倩、童欣语、张鑫、卢欣悦 |
| 代码仓库 | GitHub: campus-forum-platform |

---

## Sprint 规划总览

| Sprint | 时间 | 主题 | 目标 |
|--------|------|------|------|
| Sprint 1 | Week 1-4 | 基础框架搭建 | 项目初始化、数据库设计、用户认证、基础CRUD |
| Sprint 2 | Week 5-8 | 核心功能开发 | 活动模块、帖子模块、评论、报名、收藏 |
| Sprint 3 | Week 9-12 | AI集成与高级功能 | AI助手、推荐算法、WebSocket实时通信、管理后台 |
| Sprint 4 | Week 13-16 | 测试与优化 | 单元测试、E2E测试、性能优化、文档编写 |

---

## Sprint 1（Week 1-4）：基础框架搭建

### Issue 列表

| # | Issue | 类型 | 负责人 | 状态 | 关联PR |
|---|-------|------|--------|------|--------|
| #1 | 初始化 Spring Boot 3 项目骨架，配置 Maven 依赖 | feat | 潘樾 | ✅ Closed | #PR-1 |
| #2 | 设计并创建数据库表结构（users/activities/posts等） | feat | 潘樾 | ✅ Closed | #PR-2 |
| #3 | 初始化 Vue3 + Vite 前端项目，配置路由和状态管理 | feat | 李倩 | ✅ Closed | #PR-3 |
| #4 | 实现 JWT 用户注册/登录/退出 API | feat | 潘樾 | ✅ Closed | #PR-4 |
| #5 | 配置 Spring Security 白名单和权限控制 | feat | 潘樾 | ✅ Closed | #PR-5 |
| #6 | 实现前端登录/注册页面 + Token 持久化 | feat | 李倩 | ✅ Closed | #PR-6 |
| #7 | 配置 MyBatis-Plus 分页插件和全局异常处理 | feat | 潘樾 | ✅ Closed | #PR-7 |
| #8 | 搭建前端 axios 封装层（统一拦截、401跳转） | feat | 李倩 | ✅ Closed | #PR-8 |
| #9 | 配置跨域 CORS，联调前后端接口连通性 | feat | 张鑫 | ✅ Closed | #PR-9 |

**Sprint 1 回顾：**
- ✅ 完成所有计划 Issue
- ⚠️ 遇到问题：Spring Boot 3 废弃 `WebSecurityConfigurerAdapter`，改用 `SecurityFilterChain` Bean 方式，花费额外2天调研
- 📝 经验：Spring Boot 3 迁移需提前查阅官方文档

---

## Sprint 2（Week 5-8）：核心功能开发

### Issue 列表

| # | Issue | 类型 | 负责人 | 状态 | 关联PR |
|---|-------|------|--------|------|--------|
| #10 | 实现活动 CRUD API（创建/编辑/删除/审核状态） | feat | 潘樾 | ✅ Closed | #PR-10 |
| #11 | 实现活动列表页、详情页前端 | feat | 李倩 | ✅ Closed | #PR-11 |
| #12 | 实现活动报名/取消报名 API，人数限制校验 | feat | 潘樾 | ✅ Closed | #PR-12 |
| #13 | 实现报名记录查询和前端"我的报名"页面 | feat | 童欣语 | ✅ Closed | #PR-13 |
| #14 | 实现帖子发布/编辑/删除，关联版块 | feat | 潘樾 | ✅ Closed | #PR-14 |
| #15 | 实现帖子列表和详情页（含 Markdown 渲染） | feat | 李倩 | ✅ Closed | #PR-15 |
| #16 | 实现评论树形结构（父评论/回复），支持点赞 | feat | 张鑫 | ✅ Closed | #PR-16 |
| #17 | 实现评论发布/删除前端交互 | feat | 童欣语 | ✅ Closed | #PR-17 |
| #18 | 实现收藏 toggle API（活动/帖子） | feat | 潘樾 | ✅ Closed | #PR-18 |
| #19 | 实现"我的收藏"页面，分活动和帖子两个Tab | feat | 李倩 | ✅ Closed | #PR-19 |
| #20 | 实现用户个人主页（基本信息/关注/粉丝） | feat | 张鑫 | ✅ Closed | #PR-20 |
| #21 | 实现关注/取关功能 API + 前端按钮 | feat | 潘樾 | ✅ Closed | #PR-21 |
| #22 | Bug: 活动报名时间判断逻辑错误（截止时间校验） | bug | 潘樾 | ✅ Closed | #PR-22 |
| #23 | Bug: 评论点赞重复计数（未使用 INSERT IGNORE） | bug | 张鑫 | ✅ Closed | #PR-23 |

**Sprint 2 回顾：**
- ✅ 所有核心功能 Issue 关闭
- ⚠️ 遇到 2 个 Bug：报名时间校验逻辑 + 评论点赞幂等性
- 📝 经验：点赞/收藏等 toggle 操作必须保证幂等性，使用 `INSERT IGNORE` 或先查后写

---

## Sprint 3（Week 9-12）：AI集成与高级功能

### Issue 列表

| # | Issue | 类型 | 负责人 | 状态 | 关联PR |
|---|-------|------|--------|------|--------|
| #24 | 调研 DeepSeek API，封装 AiService 调用层 | feat | 潘樾 | ✅ Closed | #PR-24 |
| #25 | 实现 AI 活动简介生成 API（同步调用） | feat | 潘樾 | ✅ Closed | #PR-25 |
| #26 | 实现 AI 对话助手 API（SSE流式输出） | feat | 潘樾 | ✅ Closed | #PR-26 |
| #27 | 实现前端 AI 助手页面（SSE 流式渲染） | feat | 李倩 | ✅ Closed | #PR-27 |
| #28 | 实现 AI 帖子内容审核 API + 自动下线+禁发 | feat | 潘樾 | ✅ Closed | #PR-28 |
| #29 | 实现基于 Item-CF 的活动推荐算法 | feat | 张鑫 | ✅ Closed | #PR-29 |
| #30 | 实现 WebSocket 实时通知（点赞/评论/关注） | feat | 潘樾 | ✅ Closed | #PR-30 |
| #31 | 实现 WebSocket 私信聊天功能 | feat | 张鑫 | ✅ Closed | #PR-31 |
| #32 | 实现管理后台活动审核/用户管理/版块管理 | feat | 童欣语 | ✅ Closed | #PR-32 |
| #33 | 实现 ECharts 数据大屏（4种图表） | feat | 卢欣悦 | ✅ Closed | #PR-33 |
| #34 | 实现前端 WebSocket 通知中心 | feat | 李倩 | ✅ Closed | #PR-34 |
| #35 | Bug: SSE 在 Nginx 反代下断流（需设置 proxy_buffering off） | bug | 潘樾 | ✅ Closed | #PR-35 |
| #36 | Bug: WebSocket Token 无法通过 Header 传递（改用 URL 参数） | bug | 张鑫 | ✅ Closed | #PR-36 |
| #37 | Bug: 推荐冷启动用户返回空列表（增加兜底逻辑） | bug | 张鑫 | ✅ Closed | #PR-37 |

**Sprint 3 回顾：**
- ✅ AI集成、实时通信全部完成
- ⚠️ 核心踩坑：SSE 在代理环境下断流、WebSocket 认证方式、推荐算法冷启动
- 📝 经验：大模型 API 集成前必须先验证 Token 限流策略

---

## Sprint 4（Week 13-16）：测试与优化

### Issue 列表

| # | Issue | 类型 | 负责人 | 状态 | 关联PR |
|---|-------|------|--------|------|--------|
| #38 | 编写 JUnit5 后端单元测试（AuthService / ActivityService / RecommendService） | test | 潘樾 | ✅ Closed | #PR-38 |
| #39 | 编写 Vitest 前端 Store 单元测试（auth/notification） | test | 李倩 | ✅ Closed | #PR-39 |
| #40 | 编写 Playwright E2E 测试（注册/登录/活动全流程） | test | 童欣语 | ✅ Closed | #PR-40 |
| #41 | 接口限流：添加 Spring AOP 限流注解（防刷） | feat | 潘樾 | ✅ Closed | #PR-41 |
| #42 | 性能优化：热点数据加入 Redis 缓存 | feat | 张鑫 | ✅ Closed | #PR-42 |
| #43 | 前端性能：路由懒加载 + 图片懒加载 | feat | 李倩 | ✅ Closed | #PR-43 |
| #44 | 编写 API 文档（Swagger/Knife4j 注解补全） | docs | 卢欣悦 | ✅ Closed | #PR-44 |
| #45 | 编写部署文档（Docker Compose 一键部署方案） | docs | 卢欣悦 | ✅ Closed | #PR-45 |

**Sprint 4 回顾：**
- ✅ 测试覆盖率达标（后端核心逻辑 > 80%）
- ✅ 所有 Issue 关闭，项目进入交付状态

---

## Issue 统计汇总

| 类型 | 数量 | 占比 |
|------|------|------|
| feat（功能） | 36 | 80% |
| bug（缺陷） | 5 | 11% |
| test（测试） | 3 | 7% |
| docs（文档） | 2 | 4% |
| **合计** | **45** | **100%** |

## Velocity 趋势

| Sprint | 计划 Issue | 完成 Issue | Velocity |
|--------|-----------|-----------|---------|
| Sprint 1 | 9 | 9 | 100% |
| Sprint 2 | 14 | 14 | 100% |
| Sprint 3 | 14 | 14 | 100% |
| Sprint 4 | 8 | 8 | 100% |

---

## 各成员 Issue 分布

| 成员 | Sprint 1 | Sprint 2 | Sprint 3 | Sprint 4 | 总计 |
|------|---------|---------|---------|---------|------|
| 潘樾 | 5 | 6 | 7 | 3 | **21** |
| 李倩 | 3 | 4 | 2 | 2 | **11** |
| 童欣语 | 0 | 2 | 1 | 1 | **4** |
| 张鑫 | 1 | 3 | 3 | 1 | **8** |
| 卢欣悦 | 0 | 0 | 1 | 3 | **4** |
