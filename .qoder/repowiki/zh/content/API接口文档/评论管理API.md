# 评论管理API

<cite>
**本文档引用的文件**
- [CommentController.java](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java)
- [CommentCreateRequest.java](file://campus-forum-backend/src/main/java/com/campus/forum/dto/request/CommentCreateRequest.java)
- [Comment.java](file://campus-forum-backend/src/main/java/com/campus/forum/entity/Comment.java)
- [CommentMapper.java](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/CommentMapper.java)
- [PostLikeMapper.java](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/PostLikeMapper.java)
- [PostLike.java](file://campus-forum-backend/src/main/java/com/campus/forum/entity/PostLike.java)
- [NotificationService.java](file://campus-forum-backend/src/main/java/com/campus/forum/service/NotificationService.java)
- [NotificationServiceImpl.java](file://campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java)
- [Result.java](file://campus-forum-backend/src/main/java/com/campus/forum/common/Result.java)
</cite>

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构概览](#架构概览)
5. [详细组件分析](#详细组件分析)
6. [依赖分析](#依赖分析)
7. [性能考虑](#性能考虑)
8. [故障排除指南](#故障排除指南)
9. [结论](#结论)

## 简介
本文件为校园论坛系统的评论管理模块提供完整的API文档。该模块支持评论的基础操作（创建、读取、删除）、树形结构查询与嵌套回复展示、评论互动（点赞/取消点赞）、以及评论状态管理等功能。系统采用统一响应包装器，并通过通知服务实现实时消息推送。

## 项目结构
评论管理模块位于后端工程的控制器、数据传输对象、实体类、持久层映射及服务层之间，形成清晰的分层架构：

- 控制器层：负责HTTP请求处理与响应封装
- DTO层：定义请求参数校验规则
- 实体层：描述数据库表结构及业务属性
- 映射层：提供SQL查询方法
- 服务层：处理业务逻辑与外部集成

```mermaid
graph TB
subgraph "控制器层"
CC["CommentController<br/>评论接口控制器"]
end
subgraph "DTO层"
Dto["CommentCreateRequest<br/>评论创建请求参数"]
end
subgraph "实体层"
E1["Comment<br/>评论实体"]
E2["PostLike<br/>点赞实体"]
end
subgraph "映射层"
M1["CommentMapper<br/>评论查询"]
M2["PostLikeMapper<br/>点赞查询"]
end
subgraph "服务层"
S1["NotificationService<br/>通知服务接口"]
S2["NotificationServiceImpl<br/>通知服务实现"]
end
subgraph "工具层"
U1["Result<br/>统一响应包装器"]
end
CC --> Dto
CC --> M1
CC --> M2
CC --> S1
S1 --> S2
M1 --> E1
M2 --> E2
CC --> U1
```

**图表来源**
- [CommentController.java:25-115](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L25-L115)
- [CommentCreateRequest.java:8-21](file://campus-forum-backend/src/main/java/com/campus/forum/dto/request/CommentCreateRequest.java#L8-L21)
- [Comment.java:11-31](file://campus-forum-backend/src/main/java/com/campus/forum/entity/Comment.java#L11-L31)
- [PostLike.java:7-16](file://campus-forum-backend/src/main/java/com/campus/forum/entity/PostLike.java#L7-L16)
- [CommentMapper.java:9-19](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/CommentMapper.java#L9-L19)
- [PostLikeMapper.java:7-16](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/PostLikeMapper.java#L7-L16)
- [NotificationService.java:7-14](file://campus-forum-backend/src/main/java/com/campus/forum/service/NotificationService.java#L7-L14)
- [NotificationServiceImpl.java:16-58](file://campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java#L16-L58)
- [Result.java:8-37](file://campus-forum-backend/src/main/java/com/campus/forum/common/Result.java#L8-L37)

**章节来源**
- [CommentController.java:25-115](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L25-L115)
- [CommentMapper.java:9-19](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/CommentMapper.java#L9-L19)

## 核心组件
- 评论控制器：提供评论树查询、评论创建、评论删除、评论点赞等接口
- 请求参数DTO：对评论创建请求进行参数校验
- 评论实体：描述评论的数据模型，包含目标类型、父子关系、状态与计数等
- 点赞实体：记录用户对评论的点赞行为
- 通知服务：在评论或回复时向被@用户发送通知
- 统一响应包装器：标准化接口返回格式

**章节来源**
- [CommentController.java:25-115](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L25-L115)
- [CommentCreateRequest.java:8-21](file://campus-forum-backend/src/main/java/com/campus/forum/dto/request/CommentCreateRequest.java#L8-L21)
- [Comment.java:11-31](file://campus-forum-backend/src/main/java/com/campus/forum/entity/Comment.java#L11-L31)
- [PostLike.java:7-16](file://campus-forum-backend/src/main/java/com/campus/forum/entity/PostLike.java#L7-L16)
- [NotificationService.java:7-14](file://campus-forum-backend/src/main/java/com/campus/forum/service/NotificationService.java#L7-L14)
- [Result.java:8-37](file://campus-forum-backend/src/main/java/com/campus/forum/common/Result.java#L8-L37)

## 架构概览
评论管理模块遵循典型的MVC+分层架构，控制器接收请求后调用映射层执行数据库操作，并通过服务层完成跨模块协作（如通知）。所有接口返回统一包装结果。

```mermaid
sequenceDiagram
participant C as "客户端"
participant Ctrl as "CommentController"
participant Mapper as "CommentMapper/PostLikeMapper"
participant Noti as "NotificationServiceImpl"
participant DB as "数据库"
C->>Ctrl : "POST /api/comments/like/{id}"
Ctrl->>Mapper : "checkLiked(userId, commentId, 'comment')"
Mapper-->>Ctrl : "已点赞/未点赞"
alt "已点赞"
Ctrl->>Mapper : "deleteLike(userId, commentId, 'comment')"
Ctrl->>Mapper : "更新评论点赞数"
Mapper-->>Ctrl : "成功"
Ctrl-->>C : "返回{liked : false, likeCount}"
else "未点赞"
Ctrl->>Mapper : "insert PostLike"
Ctrl->>Mapper : "更新评论点赞数"
Mapper-->>Ctrl : "成功"
Ctrl-->>C : "返回{liked : true, likeCount}"
end
Note over Ctrl,Noti : "当存在回复用户时触发通知"
```

**图表来源**
- [CommentController.java:88-115](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L88-L115)
- [PostLikeMapper.java:10-14](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/PostLikeMapper.java#L10-L14)
- [NotificationServiceImpl.java:23-37](file://campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java#L23-L37)

## 详细组件分析

### 评论树形结构查询
- 接口：GET /api/comments
- 功能：按目标ID与目标类型查询根评论，并为每个根评论加载其子回复
- 查询策略：
  - 根评论：parent_id为空且状态正常
  - 子回复：按parent_id查询并按创建时间升序排列
- 返回：根评论列表，其中每个根评论包含replies子列表

```mermaid
flowchart TD
Start(["请求进入"]) --> Parse["解析参数<br/>targetId, targetType"]
Parse --> LoadRoots["查询根评论<br/>parent_id IS NULL"]
LoadRoots --> ForEachRoot{"遍历根评论"}
ForEachRoot --> |是| LoadReplies["查询子回复<br/>parent_id = root.id"]
LoadReplies --> AttachReplies["附加到根评论.replies"]
AttachReplies --> ForEachRoot
ForEachRoot --> |否| Return["返回根评论列表"]
```

**图表来源**
- [CommentController.java:35-44](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L35-L44)
- [CommentMapper.java:12-17](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/CommentMapper.java#L12-L17)

**章节来源**
- [CommentController.java:35-44](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L35-L44)
- [CommentMapper.java:12-17](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/CommentMapper.java#L12-L17)

### 发表评论/回复
- 接口：POST /api/comments
- 功能：创建评论或楼中楼回复
- 参数校验：目标ID、目标类型、评论内容必填；可选parentId与replyUid
- 权限控制：需登录用户，使用当前登录用户ID作为作者
- 状态默认：创建时状态设为正常
- 通知触发：若存在replyUid且与当前用户不同，则向被回复用户发送通知

```mermaid
sequenceDiagram
participant C as "客户端"
participant Ctrl as "CommentController"
participant Mapper as "CommentMapper"
participant Noti as "NotificationServiceImpl"
participant DB as "数据库"
C->>Ctrl : "POST /api/comments"
Ctrl->>Ctrl : "校验CommentCreateRequest"
Ctrl->>Mapper : "insert 评论记录"
Mapper-->>Ctrl : "成功"
Ctrl->>Noti : "sendNotification(replyUid, REPLY/COMMENT, ...)"
Noti-->>Ctrl : "异步推送"
Ctrl-->>C : "返回创建的评论"
```

**图表来源**
- [CommentController.java:46-71](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L46-L71)
- [CommentCreateRequest.java:8-21](file://campus-forum-backend/src/main/java/com/campus/forum/dto/request/CommentCreateRequest.java#L8-L21)
- [NotificationServiceImpl.java:23-37](file://campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java#L23-L37)

**章节来源**
- [CommentController.java:46-71](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L46-L71)
- [CommentCreateRequest.java:8-21](file://campus-forum-backend/src/main/java/com/campus/forum/dto/request/CommentCreateRequest.java#L8-L21)
- [NotificationServiceImpl.java:23-37](file://campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java#L23-L37)

### 删除评论
- 接口：DELETE /api/comments/{id}
- 功能：软删除评论（更新状态为禁用）
- 权限控制：仅评论作者可删除
- 安全性：先查询再判断，防止越权

```mermaid
flowchart TD
Start(["请求进入"]) --> Fetch["根据id查询评论"]
Fetch --> Exists{"是否存在？"}
Exists --> |否| Err["返回错误：评论不存在"]
Exists --> |是| CheckOwner{"是否本人？"}
CheckOwner --> |否| Forbidden["返回错误：无权删除"]
CheckOwner --> |是| SoftDel["status=0 更新"]
SoftDel --> Done["返回成功"]
```

**图表来源**
- [CommentController.java:73-86](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L73-L86)

**章节来源**
- [CommentController.java:73-86](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L73-L86)

### 评论点赞/取消点赞
- 接口：POST /api/comments/{id}/like
- 功能：对评论进行点赞或取消点赞
- 逻辑：
  - 若已点赞则取消并扣减计数
  - 若未点赞则新增点赞并增加计数
- 返回：包含liked状态与当前点赞数

```mermaid
sequenceDiagram
participant C as "客户端"
participant Ctrl as "CommentController"
participant LikeMapper as "PostLikeMapper"
participant CommentMapper as "CommentMapper"
participant DB as "数据库"
C->>Ctrl : "POST /api/comments/{id}/like"
Ctrl->>LikeMapper : "checkLiked(userId, commentId, 'comment')"
LikeMapper-->>Ctrl : "结果"
alt "已点赞"
Ctrl->>LikeMapper : "deleteLike(...)"
Ctrl->>CommentMapper : "likeCount--"
LikeMapper-->>Ctrl : "成功"
CommentMapper-->>Ctrl : "成功"
Ctrl-->>C : "{liked : false, likeCount}"
else "未点赞"
Ctrl->>LikeMapper : "insert PostLike"
Ctrl->>CommentMapper : "likeCount++"
LikeMapper-->>Ctrl : "成功"
CommentMapper-->>Ctrl : "成功"
Ctrl-->>C : "{liked : true, likeCount}"
end
```

**图表来源**
- [CommentController.java:88-115](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L88-L115)
- [PostLikeMapper.java:10-14](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/PostLikeMapper.java#L10-L14)
- [CommentMapper.java:12-17](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/CommentMapper.java#L12-L17)

**章节来源**
- [CommentController.java:88-115](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L88-L115)
- [PostLikeMapper.java:10-14](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/PostLikeMapper.java#L10-L14)

### 数据模型
```mermaid
erDiagram
COMMENT {
bigint id PK
bigint target_id
varchar target_type
bigint user_id
bigint parent_id
bigint reply_uid
text content
int like_count
int status
datetime created_at
}
POST_LIKE {
bigint user_id
bigint target_id
varchar target_type
datetime created_at
}
```

**图表来源**
- [Comment.java:11-31](file://campus-forum-backend/src/main/java/com/campus/forum/entity/Comment.java#L11-L31)
- [PostLike.java:7-16](file://campus-forum-backend/src/main/java/com/campus/forum/entity/PostLike.java#L7-L16)

**章节来源**
- [Comment.java:11-31](file://campus-forum-backend/src/main/java/com/campus/forum/entity/Comment.java#L11-L31)
- [PostLike.java:7-16](file://campus-forum-backend/src/main/java/com/campus/forum/entity/PostLike.java#L7-L16)

## 依赖分析
- 控制器依赖映射层与服务层，实现业务编排
- 映射层依赖MyBatis-Plus，提供SQL查询能力
- 通知服务通过WebSocket向用户推送实时消息
- 统一响应包装器贯穿各层，保证接口一致性

```mermaid
graph LR
CC["CommentController"] --> CM["CommentMapper"]
CC --> PLM["PostLikeMapper"]
CC --> NS["NotificationService"]
NS --> NSI["NotificationServiceImpl"]
CC --> R["Result"]
```

**图表来源**
- [CommentController.java:31-33](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L31-L33)
- [CommentMapper.java:9-19](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/CommentMapper.java#L9-L19)
- [PostLikeMapper.java:7-16](file://campus-forum-backend/src/main/java/com/campus/forum/mapper/PostLikeMapper.java#L7-L16)
- [NotificationService.java:7-14](file://campus-forum-backend/src/main/java/com/campus/forum/service/NotificationService.java#L7-L14)
- [NotificationServiceImpl.java:16-58](file://campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java#L16-L58)
- [Result.java:8-37](file://campus-forum-backend/src/main/java/com/campus/forum/common/Result.java#L8-L37)

**章节来源**
- [CommentController.java:31-33](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L31-L33)
- [NotificationServiceImpl.java:20-37](file://campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java#L20-L37)

## 性能考虑
- 评论树查询：根评论与子回复分别查询，建议在高并发场景下对热点目标ID添加缓存
- 点赞操作：单条记录更新，注意在高并发下可能产生竞态，可引入乐观锁或队列异步化
- 通知推送：WebSocket推送为异步，但数据库写入仍需关注写入延迟

## 故障排除指南
- 评论不存在：删除接口在查询不到评论时返回错误
- 权限不足：删除接口对非作者本人返回无权删除
- 参数校验失败：创建接口对空值参数返回校验错误
- 通知未送达：检查通知服务实现与WebSocket配置

**章节来源**
- [CommentController.java:77-82](file://campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java#L77-L82)
- [CommentCreateRequest.java:8-21](file://campus-forum-backend/src/main/java/com/campus/forum/dto/request/CommentCreateRequest.java#L8-L21)
- [NotificationServiceImpl.java:23-37](file://campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java#L23-L37)

## 结论
评论管理模块提供了完整的评论生命周期管理能力，包括树形结构展示、嵌套回复、点赞互动与权限控制。通过统一响应包装器与通知服务，系统实现了良好的用户体验与可维护性。后续可在热点数据缓存、点赞异步化与敏感词过滤等方面进一步优化。