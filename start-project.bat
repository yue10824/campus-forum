@echo off
chcp 65001 >nul
title 校园论坛一键启动

:: 获取脚本所在目录
set "scriptDir=%~dp0"
set "ps1Path=%scriptDir%start-project.ps1"

:: 检查 PowerShell 脚本是否存在
if not exist "%ps1Path%" (
    echo [ERROR] 未找到 start-project.ps1
    pause
    exit /b 1
)

:: 以管理员权限调用 PowerShell 执行脚本（绕过执行策略）
powershell -ExecutionPolicy Bypass -File "%ps1Path%"

pause
