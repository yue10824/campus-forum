# 校园论坛项目一键启动脚本
# 用法: 右键 PowerShell 执行，或双击 start-project.bat

$ErrorActionPreference = "Stop"
$global:projectRoot = "c:\Users\53225\Desktop\PBL项目"
$global:backendPort = 8080
$global:frontendPort = 5173

function Write-Info($msg)  { Write-Host "[INFO]  $msg" -ForegroundColor Cyan }
function Write-Ok($msg)    { Write-Host "[OK]    $msg" -ForegroundColor Green }
function Write-Warn($msg)  { Write-Host "[WARN]  $msg" -ForegroundColor Yellow }
function Write-ErrorMsg($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }

# 1. 设置必要的环境路径
function Initialize-Environment {
    $gitPath = "C:\Program Files\Git\cmd"
    $mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin"
    $mavenPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\plugins\maven\lib\maven3\bin"

    foreach ($p in @($gitPath, $mysqlPath, $mavenPath)) {
        if (Test-Path $p) {
            $env:Path = "$p;$($env:Path)"
        }
    }
}

# 2. 启动 MySQL
function Start-MySql {
    $svc = Get-Service -Name "MySQL80" -ErrorAction SilentlyContinue
    if (-not $svc) {
        Write-Warn "未找到 MySQL80 服务，请确认 MySQL 已安装"
        return
    }
    if ($svc.Status -eq "Running") {
        Write-Ok "MySQL 已在运行"
        return
    }
    Write-Info "正在启动 MySQL 服务..."
    Start-Service -Name "MySQL80"
    Start-Sleep -Seconds 3
    Write-Ok "MySQL 启动完成"
}

# 3. 停止已有的 Java/Node 进程
function Stop-ExistingProcesses {
    Write-Info "清理已有的 Java / Node 进程..."
    Get-Process -Name "java" -ErrorAction SilentlyContinue | ForEach-Object {
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
        Write-Warn "已终止 Java 进程 PID=$($_.Id)"
    }
    Get-Process -Name "node" -ErrorAction SilentlyContinue | ForEach-Object {
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
        Write-Warn "已终止 Node 进程 PID=$($_.Id)"
    }
    Start-Sleep -Seconds 2
}

# 4. 构建后端（如缺少 JAR）
function Build-Backend {
    $backendDir = Join-Path $projectRoot "campus-forum-backend"
    $targetDir = Join-Path $backendDir "target"
    $jarFile = Get-ChildItem -Path $targetDir -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1

    if ($jarFile) {
        Write-Ok "后端 JAR 已存在: $($jarFile.Name)"
        return $jarFile.FullName
    }

    Write-Info "正在构建后端项目，请稍候..."
    $mvn = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\plugins\maven\lib\maven3\bin\mvn.cmd"
    if (-not (Test-Path $mvn)) {
        Write-ErrorMsg "未找到 Maven，路径: $mvn"
        exit 1
    }

    Set-Location $backendDir
    & $mvn clean package "-DskipTests" "-Dmaven.test.skip=true" -q
    if ($LASTEXITCODE -ne 0) {
        Write-ErrorMsg "后端构建失败"
        exit 1
    }

    $jarFile = Get-ChildItem -Path $targetDir -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    Write-Ok "后端构建完成: $($jarFile.Name)"
    return $jarFile.FullName
}

# 5. 等待端口可用
function Wait-ForPort($port, $timeoutSeconds = 60) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSeconds) {
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            $tcp.Connect("127.0.0.1", $port)
            $tcp.Close()
            return $true
        } catch {
            Start-Sleep -Milliseconds 500
        }
    }
    return $false
}

# 6. 启动后端
function Start-Backend($jarPath) {
    Write-Info "正在启动 Spring Boot 后端..."
    $logOut = Join-Path $projectRoot "backend.log"
    $logErr = Join-Path $projectRoot "backend-err.log"

    $jarDir = Split-Path -Parent $jarPath
    Set-Location $jarDir

    Start-Process -FilePath "java" -ArgumentList "-jar", (Split-Path -Leaf $jarPath), "--server.port=$backendPort" `
        -RedirectStandardOutput $logOut -RedirectStandardError $logErr -WindowStyle Hidden

    Write-Info "等待后端启动 (端口 $backendPort)..."
    if (Wait-ForPort $backendPort 60) {
        Write-Ok "后端启动成功: http://localhost:$backendPort"
    } else {
        Write-ErrorMsg "后端启动超时，请查看 $logErr"
        exit 1
    }
}

# 7. 启动前端
function Start-Frontend {
    Write-Info "正在启动 Vue3 前端..."
    $frontendDir = Join-Path $projectRoot "campus-forum-frontend"
    Set-Location $frontendDir

    Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev" -WindowStyle Hidden

    Write-Info "等待前端启动 (端口 $frontendPort)..."
    if (Wait-ForPort $frontendPort 60) {
        Write-Ok "前端启动成功: http://localhost:$frontendPort"
    } else {
        Write-Warn "前端启动可能失败，请检查 npm 依赖是否已安装"
    }
}

# 8. 打开浏览器
function Open-Browser {
    $url = "http://localhost:$frontendPort"
    Write-Info "正在打开浏览器: $url"
    Start-Process $url
}

# ===== 主流程 =====
Write-Host "======================================" -ForegroundColor Magenta
Write-Host "  校园论坛项目一键启动脚本" -ForegroundColor Magenta
Write-Host "======================================" -ForegroundColor Magenta

Initialize-Environment
Start-MySql
Stop-ExistingProcesses
$jar = Build-Backend
Start-Backend $jar
Start-Frontend
Open-Browser

Write-Host "======================================" -ForegroundColor Magenta
Write-Ok "项目已启动，按任意键关闭所有服务..."
[void][System.Console]::ReadKey($true)

# 关闭进程
Write-Info "正在关闭服务..."
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Get-Process -Name "node" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Write-Ok "服务已关闭"
