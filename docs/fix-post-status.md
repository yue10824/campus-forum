# 帖子下线失效修复记录

## 问题描述
管理员下线帖子后前台仍显示

## 原因
application.yml全局逻辑删除 logic-not-delete-value:1 导致updateById附加WHERE status=1

## 修复
1. 移除全局逻辑删除配置
2. PostMapper新增updateStatus直接SQL方法绕过拦截器

## 修复人
潘樾
## 日期
2025-05-12
