@echo off
chcp 65001 >nul
echo ========================================
echo  校园活动论坛 - Git 仓库初始化脚本
echo ========================================
echo.

cd /d "%~dp0"

:: 检查git是否安装
git --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Git，请先安装：
    echo   下载地址: https://git-scm.com/download/win
    echo   安装完成后重新运行此脚本
    pause
    exit /b 1
)

:: 初始化仓库
if not exist ".git" (
    echo [1] 初始化本地 Git 仓库...
    git init
    git config user.name "campus-forum-dev"
    git config user.email "dev@campus-forum.com"
) else (
    echo [1] Git 仓库已存在，跳过 init
)

:: 添加所有文件
echo [2] 添加文件到暂存区...
git add .

:: 查看状态
echo [3] 当前文件状态：
git status --short

:: 首次提交
echo.
echo [4] 执行首次提交...
git commit -m "feat: 校园活动论坛完整项目 - 初始提交

功能模块：
- 用户系统：注册(邮箱验证)、登录、个人中心、等级经验值
- 帖子系统：发帖、评论、点赞、收藏、热榜推荐
- AI功能：发帖自动审核(敏感词检测/自动下线)、AI助手、活动简介生成
- 管理后台：帖子管理、用户管理、数据看板、公告管理
- 安全治理：违规自动下线+禁发30分钟+站内通知+管理员解禁

技术栈：Spring Boot 3 + MyBatis-Plus + Vue3 + Element Plus + DeepSeek AI"

echo.
echo ========================================
echo  完成！本地仓库已创建并完成初始提交
echo ========================================
echo.
echo 如需推送到远程仓库（GitHub/Gitee），执行：
echo   git remote add origin [你的仓库地址]
echo   git push -u origin main
echo.
pause
