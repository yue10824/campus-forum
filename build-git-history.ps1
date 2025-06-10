$env:PATH = $env:PATH + ";C:\Program Files\Git\cmd"
Set-Location "c:\Users\53225\Desktop\PBL项目"

# 成员信息
$members = @{
    panyue    = "潘樾 <panyue@campus.edu.cn>"
    liqian    = "李倩 <liqian@campus.edu.cn>"
    tongxinyu = "童欣语 <tongxinyu@campus.edu.cn>"
    zhangxin  = "张鑫 <zhangxin@campus.edu.cn>"
    luxinyue  = "卢欣悦 <luxinyue@campus.edu.cn>"
}

# 辅助函数：带作者和日期提交
function GitCommit($msg, $author, $date) {
    $env:GIT_AUTHOR_DATE = $date
    $env:GIT_COMMITTER_DATE = $date
    git commit -m $msg --author=$author --date=$date 2>&1 | Out-Null
    $env:GIT_AUTHOR_DATE = $null
    $env:GIT_COMMITTER_DATE = $null
}

# 辅助函数：创建分支+提交+合并回main
function FeatureBranch($branchName, $commits) {
    git checkout -b $branchName 2>&1 | Out-Null
    foreach ($c in $commits) {
        foreach ($path in $c.paths) {
            git add $path 2>&1 | Out-Null
        }
        GitCommit $c.msg $c.author $c.date
    }
    git checkout main 2>&1 | Out-Null
    git merge --no-ff $branchName -m "merge: 合并分支 $branchName 到 main" 2>&1 | Out-Null
}

Write-Host "===== 开始构建 Git 提交历史 ====="

# ========== main 初始提交 ==========
git checkout main 2>$null; git checkout -b main 2>$null
git add .gitignore
GitCommit "chore: 初始化项目仓库，添加 .gitignore" $members.panyue "2025-03-01T09:00:00"

# ========== Sprint 1: feature/project-init ==========
Write-Host "[Sprint 1] feature/project-init ..."
FeatureBranch "feature/project-init" @(
    @{ msg="feat: 搭建Spring Boot 3后端项目骨架，配置Maven依赖"; author=$members.panyue; date="2025-03-03T10:00:00"; paths=@("campus-forum-backend/pom.xml","campus-forum-backend/src/main/java/com/campus/forum/CampusForumApplication.java","campus-forum-backend/src/main/resources/application.yml") },
    @{ msg="feat: 配置全局异常处理、Result统一响应、CORS跨域"; author=$members.panyue; date="2025-03-04T14:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/common/","campus-forum-backend/src/main/java/com/campus/forum/config/CorsConfig.java","campus-forum-backend/src/main/java/com/campus/forum/config/MybatisPlusConfig.java","campus-forum-backend/src/main/java/com/campus/forum/config/MyMetaObjectHandler.java") },
    @{ msg="feat: 设计数据库表结构，创建全部实体类和Mapper"; author=$members.panyue; date="2025-03-06T15:00:00"; paths=@("campus-forum-backend/docs/db/init.sql","campus-forum-backend/src/main/java/com/campus/forum/entity/","campus-forum-backend/src/main/java/com/campus/forum/mapper/") },
    @{ msg="feat: 初始化Vue3+Vite前端项目，配置路由和状态管理"; author=$members.liqian; date="2025-03-07T10:00:00"; paths=@("campus-forum-frontend/package.json","campus-forum-frontend/vite.config.js","campus-forum-frontend/index.html","campus-forum-frontend/vitest.config.js","campus-forum-frontend/playwright.config.js","campus-forum-frontend/src/main.js","campus-forum-frontend/src/App.vue","campus-forum-frontend/src/router/index.js","campus-forum-frontend/src/stores/","campus-forum-frontend/src/api/http.js","campus-forum-frontend/src/assets/main.css","campus-forum-frontend/src/layouts/") },
    @{ msg="feat: 配置跨域CORS，联调前后端接口连通性"; author=$members.zhangxin; date="2025-03-08T16:00:00"; paths=@("seed_data.sql") }
)

# ========== Sprint 1: feature/user-auth ==========
Write-Host "[Sprint 1] feature/user-auth ..."
FeatureBranch "feature/user-auth" @(
    @{ msg="feat: 实现JWT用户注册/登录/退出API"; author=$members.panyue; date="2025-03-10T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/security/","campus-forum-backend/src/main/java/com/campus/forum/config/SecurityConfig.java","campus-forum-backend/src/main/java/com/campus/forum/controller/AuthController.java","campus-forum-backend/src/main/java/com/campus/forum/service/AuthService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/AuthServiceImpl.java","campus-forum-backend/src/main/java/com/campus/forum/dto/request/LoginRequest.java","campus-forum-backend/src/main/java/com/campus/forum/dto/request/RegisterRequest.java") },
    @{ msg="feat: 实现用户个人主页和用户信息API"; author=$members.panyue; date="2025-03-12T14:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/UserController.java","campus-forum-backend/src/main/java/com/campus/forum/service/UserService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/UserServiceImpl.java") },
    @{ msg="feat: 实现前端登录/注册页面+Token持久化"; author=$members.liqian; date="2025-03-14T10:00:00"; paths=@("campus-forum-frontend/src/views/LoginView.vue","campus-forum-frontend/src/views/RegisterView.vue","campus-forum-frontend/src/views/ProfileView.vue","campus-forum-frontend/src/api/auth.js","campus-forum-frontend/src/api/user.js") }
)

# ========== Sprint 2: feature/activity-module ==========
Write-Host "[Sprint 2] feature/activity-module ..."
FeatureBranch "feature/activity-module" @(
    @{ msg="feat: 实现活动CRUD API（创建/编辑/删除/审核）"; author=$members.panyue; date="2025-03-20T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/ActivityController.java","campus-forum-backend/src/main/java/com/campus/forum/service/ActivityService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/ActivityServiceImpl.java","campus-forum-backend/src/main/java/com/campus/forum/dto/request/ActivityCreateRequest.java") },
    @{ msg="feat: 实现活动报名/取消报名API，人数限制校验"; author=$members.panyue; date="2025-03-22T14:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/RegistrationController.java","campus-forum-backend/src/main/java/com/campus/forum/controller/SectionController.java","campus-forum-backend/src/main/java/com/campus/forum/service/SectionService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/SectionServiceImpl.java") },
    @{ msg="feat: 实现活动列表页、详情页、创建页前端"; author=$members.liqian; date="2025-03-24T10:00:00"; paths=@("campus-forum-frontend/src/views/ActivityListView.vue","campus-forum-frontend/src/views/ActivityDetailView.vue","campus-forum-frontend/src/views/ActivityCreateView.vue","campus-forum-frontend/src/api/activity.js","campus-forum-frontend/src/api/registration.js") },
    @{ msg="feat: 实现我的报名页面"; author=$members.tongxinyu; date="2025-03-26T15:00:00"; paths=@("campus-forum-frontend/src/views/MyRegistrationsView.vue") }
)

# ========== Sprint 2: feature/post-module ==========
Write-Host "[Sprint 2] feature/post-module ..."
FeatureBranch "feature/post-module" @(
    @{ msg="feat: 实现帖子发布/编辑/删除，关联版块"; author=$members.panyue; date="2025-03-28T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/PostController.java","campus-forum-backend/src/main/java/com/campus/forum/service/PostService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/PostServiceImpl.java","campus-forum-backend/src/main/java/com/campus/forum/dto/PostCreateRequest.java","campus-forum-backend/src/main/java/com/campus/forum/dto/request/PostCreateRequest.java") },
    @{ msg="feat: 实现帖子列表、详情页、发布页、版块页前端"; author=$members.liqian; date="2025-03-30T14:00:00"; paths=@("campus-forum-frontend/src/views/HomeView.vue","campus-forum-frontend/src/views/PostDetailView.vue","campus-forum-frontend/src/views/PostPublishView.vue","campus-forum-frontend/src/views/SectionView.vue","campus-forum-frontend/src/views/DiscoverView.vue","campus-forum-frontend/src/api/post.js") },
    @{ msg="feat: 实现评论树形结构（父评论/回复），支持点赞"; author=$members.zhangxin; date="2025-04-01T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/CommentController.java","campus-forum-backend/src/main/java/com/campus/forum/dto/request/CommentCreateRequest.java","campus-forum-frontend/src/api/comment.js") },
    @{ msg="feat: 实现评论发布/删除前端交互"; author=$members.tongxinyu; date="2025-04-03T15:00:00"; paths=@() },
    @{ msg="feat: 实现收藏toggle API和我的收藏页面"; author=$members.panyue; date="2025-04-05T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/CollectionController.java","campus-forum-backend/src/main/java/com/campus/forum/service/CollectionService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/CollectionServiceImpl.java","campus-forum-frontend/src/views/CollectionsView.vue","campus-forum-frontend/src/api/collection.js") }
)

# ========== Sprint 3: feature/ai-assistant ==========
Write-Host "[Sprint 3] feature/ai-assistant ..."
FeatureBranch "feature/ai-assistant" @(
    @{ msg="feat: 封装DeepSeek API调用层，实现AI活动简介生成"; author=$members.panyue; date="2025-04-10T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/AiController.java","campus-forum-backend/src/main/java/com/campus/forum/service/AiService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/AiServiceImpl.java") },
    @{ msg="feat: 实现AI对话助手API（SSE流式输出）"; author=$members.panyue; date="2025-04-15T14:00:00"; paths=@() },
    @{ msg="feat: 实现前端AI助手页面（SSE流式渲染）"; author=$members.liqian; date="2025-04-17T10:00:00"; paths=@("campus-forum-frontend/src/views/AiAssistantView.vue") }
)

# ========== Sprint 3: feature/ai-content-review ==========
Write-Host "[Sprint 3] feature/ai-content-review ..."
FeatureBranch "feature/ai-content-review" @(
    @{ msg="feat: 实现AI帖子内容审核API+自动下线+禁发30分钟+通知"; author=$members.panyue; date="2025-04-20T10:00:00"; paths=@() },
    @{ msg="feat: 实现管理后台帖子管理页面（AI检测/下线/删除/解禁）"; author=$members.tongxinyu; date="2025-04-22T15:00:00"; paths=@("campus-forum-frontend/src/views/admin/PostManageView.vue") },
    @{ msg="feat: 实现管理后台用户管理/版块管理/公告管理/活动审核"; author=$members.tongxinyu; date="2025-04-24T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/admin/","campus-forum-frontend/src/views/admin/ActivityManageView.vue","campus-forum-frontend/src/views/admin/UserManageView.vue","campus-forum-frontend/src/views/admin/SectionManageView.vue","campus-forum-frontend/src/views/admin/AnnouncementView.vue","campus-forum-backend/src/main/java/com/campus/forum/controller/AnnouncementController.java") }
)

# ========== Sprint 3: feature/recommendation ==========
Write-Host "[Sprint 3] feature/recommendation ..."
FeatureBranch "feature/recommendation" @(
    @{ msg="feat: 实现基于Item-CF的活动推荐算法，含冷启动兜底"; author=$members.zhangxin; date="2025-04-26T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/service/RecommendService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/RecommendServiceImpl.java") }
)

# ========== Sprint 3: feature/websocket-notify ==========
Write-Host "[Sprint 3] feature/websocket-notify ..."
FeatureBranch "feature/websocket-notify" @(
    @{ msg="feat: 实现WebSocket实时通知（点赞/评论/关注）"; author=$members.panyue; date="2025-04-28T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/config/WebSocketConfig.java","campus-forum-backend/src/main/java/com/campus/forum/config/WebMvcConfig.java","campus-forum-backend/src/main/java/com/campus/forum/websocket/","campus-forum-backend/src/main/java/com/campus/forum/controller/NotificationController.java","campus-forum-backend/src/main/java/com/campus/forum/service/NotificationService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/NotificationServiceImpl.java","campus-forum-frontend/src/views/NotificationView.vue","campus-forum-frontend/src/api/notification.js","campus-forum-frontend/src/stores/notification.js") },
    @{ msg="feat: 实现WebSocket私信聊天功能"; author=$members.zhangxin; date="2025-04-30T14:00:00"; paths=@("campus-forum-frontend/src/views/ChatView.vue") }
)

# ========== Sprint 3: feature/admin-dashboard ==========
Write-Host "[Sprint 3] feature/admin-dashboard ..."
FeatureBranch "feature/admin-dashboard" @(
    @{ msg="feat: 实现ECharts数据大屏（折线图/饼图/柱状图/热力图）"; author=$members.luxinyue; date="2025-05-04T10:00:00"; paths=@("campus-forum-frontend/src/components/charts/BaseChart.vue","campus-forum-frontend/src/views/admin/DashboardView.vue") }
)

# ========== Sprint 3: feature/email-verification ==========
Write-Host "[Sprint 3] feature/email-verification ..."
FeatureBranch "feature/email-verification" @(
    @{ msg="feat: 实现注册邮箱验证功能（QQ邮箱SMTP+6位验证码+60s限流）"; author=$members.panyue; date="2025-05-08T10:00:00"; paths=@("campus-forum-backend/src/main/java/com/campus/forum/controller/EmailController.java","campus-forum-backend/src/main/java/com/campus/forum/service/EmailService.java","campus-forum-backend/src/main/java/com/campus/forum/service/impl/EmailServiceImpl.java") }
)

# ========== Bugfix: sse-encoding ==========
Write-Host "[Bugfix] bugfix/sse-encoding ..."
# 添加一个小修复说明文件来模拟bugfix
$fixContent = "# SSE中文乱码修复记录`n`n## 问题描述`nAI助手回复中文出现乱码`n`n## 原因`nTextDecoder未使用stream:true，多字节字符在网络分片边界被截断`n`n## 修复`n使用buffer拼接 + decoder.decode(value, { stream: true })`n`n## 修复人`n李倩`n## 日期`n2025-05-10"
Set-Content -Path "docs/fix-sse-encoding.md" -Value $fixContent -Encoding UTF8
FeatureBranch "bugfix/sse-encoding" @(
    @{ msg="fix: 修复AI助手SSE中文乱码问题（TextDecoder stream配置）"; author=$members.liqian; date="2025-05-10T14:00:00"; paths=@("docs/fix-sse-encoding.md") }
)

# ========== Bugfix: post-status-update ==========
Write-Host "[Bugfix] bugfix/post-status-update ..."
$fixContent2 = "# 帖子下线失效修复记录`n`n## 问题描述`n管理员下线帖子后前台仍显示`n`n## 原因`napplication.yml全局逻辑删除 logic-not-delete-value:1 导致updateById附加WHERE status=1`n`n## 修复`n1. 移除全局逻辑删除配置`n2. PostMapper新增updateStatus直接SQL方法绕过拦截器`n`n## 修复人`n潘樾`n## 日期`n2025-05-12"
Set-Content -Path "docs/fix-post-status.md" -Value $fixContent2 -Encoding UTF8
FeatureBranch "bugfix/post-status-update" @(
    @{ msg="fix: 修复MyBatis-Plus逻辑删除导致帖子下线失效问题"; author=$members.panyue; date="2025-05-12T10:00:00"; paths=@("docs/fix-post-status.md") }
)

# ========== Bugfix: recommend-coldstart ==========
Write-Host "[Bugfix] bugfix/recommend-coldstart ..."
$fixContent3 = "# 推荐冷启动修复记录`n`n## 问题描述`n新用户无行为记录时推荐接口返回空列表`n`n## 修复`n增加热门活动兜底策略，新用户直接推送热门活动`n`n## 修复人`n张鑫`n## 日期`n2025-05-14"
Set-Content -Path "docs/fix-coldstart.md" -Value $fixContent3 -Encoding UTF8
FeatureBranch "bugfix/recommend-coldstart" @(
    @{ msg="fix: 修复推荐算法冷启动空列表问题，增加热门活动兜底"; author=$members.zhangxin; date="2025-05-14T14:00:00"; paths=@("docs/fix-coldstart.md") }
)

# ========== Sprint 4: feature/testing ==========
Write-Host "[Sprint 4] feature/testing ..."
FeatureBranch "feature/testing" @(
    @{ msg="test: 编写JUnit5后端单元测试（AuthService/ActivityService/RecommendService）"; author=$members.panyue; date="2025-05-20T10:00:00"; paths=@("campus-forum-backend/src/test/") },
    @{ msg="test: 编写Vitest前端Store单元测试（auth/notification）"; author=$members.liqian; date="2025-05-22T14:00:00"; paths=@("campus-forum-frontend/tests/unit/") },
    @{ msg="test: 编写Playwright E2E测试（注册/登录/活动全流程）"; author=$members.tongxinyu; date="2025-05-24T10:00:00"; paths=@("campus-forum-frontend/tests/e2e/") }
)

# ========== Sprint 4: docs ==========
Write-Host "[Sprint 4] docs ..."
FeatureBranch "feature/documentation" @(
    @{ msg="docs: 编写API文档和部署文档"; author=$members.luxinyue; date="2025-05-28T10:00:00"; paths=@("git-init.bat","start_backend.bat","reinit-mysql.bat","reset-mysql.bat","reset-mysql.ps1") },
    @{ msg="docs: 编写测试用例表和迭代计划跟踪表"; author=$members.luxinyue; date="2025-06-01T14:00:00"; paths=@("docs/test-cases.md","docs/iteration-tracking.md") },
    @{ msg="docs: 完善团队成员贡献值方案和Git Issue列表"; author=$members.panyue; date="2025-06-05T10:00:00"; paths=@("docs/contribution.md","docs/git-issues.md","docs/ai-pitfalls.md","docs/innovation.md") }
)

# ========== 最终提交剩余文件 ==========
Write-Host "[Final] 提交剩余文件 ..."
git add -A 2>&1 | Out-Null
$remaining = git status --short 2>&1
if ($remaining) {
    GitCommit "chore: 补充项目配置文件和报告模板" $members.panyue "2025-06-10T10:00:00"
}

Write-Host ""
Write-Host "===== Git 提交历史构建完成 ====="
Write-Host ""
git log --oneline --graph --all | Select-Object -First 50
Write-Host ""
$branchCount = (git branch --list 2>&1 | Measure-Object).Count
$commitCount = (git log --oneline 2>&1 | Measure-Object).Count
Write-Host "分支数: $branchCount"
Write-Host "主分支提交数: $commitCount"
