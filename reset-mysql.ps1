$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.0\bin"
$myIni = "C:\ProgramData\MySQL\MySQL Server 8.0\my.ini"
$newPassword = "123456"
$tempCnf = "$env:TEMP\mysql_skip_grant.cnf"

$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "ERROR: Need admin!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "=== MySQL Password Reset v5 ===" -ForegroundColor Cyan

Write-Host "[1/6] Stopping MySQL service..." -ForegroundColor Yellow
Stop-Service -Name "MySQL80" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3
Write-Host "    Status: $((Get-Service MySQL80).Status)" -ForegroundColor Gray

Write-Host "[2/6] Copying my.ini and adding skip-grant-tables..." -ForegroundColor Yellow
Copy-Item $myIni $tempCnf -Force
Add-Content -Path $tempCnf -Value "skip-grant-tables"
Write-Host "    Temp config: $tempCnf" -ForegroundColor Gray

Write-Host "[3/6] Starting mysqld..." -ForegroundColor Yellow
$proc = Start-Process -FilePath "$mysqlBin\mysqld.exe" -ArgumentList "--defaults-file=$tempCnf", "--console" -PassThru
Start-Sleep -Seconds 12
if ($proc.HasExited) {
    Write-Host "    mysqld EXITED! ExitCode: $($proc.ExitCode)" -ForegroundColor Red
    Write-Host "    Trying with explicit basedir..." -ForegroundColor Yellow
    $proc = Start-Process -FilePath "$mysqlBin\mysqld.exe" -ArgumentList "--defaults-file=$tempCnf", "--basedir=C:/Program Files/MySQL/MySQL Server 8.0/", "--console" -PassThru
    Start-Sleep -Seconds 12
    if ($proc.HasExited) {
        Write-Host "    Still EXITED! ExitCode: $($proc.ExitCode)" -ForegroundColor Red
    } else {
        Write-Host "    mysqld running (PID: $($proc.Id))" -ForegroundColor Green
    }
} else {
    Write-Host "    mysqld running (PID: $($proc.Id))" -ForegroundColor Green
}

Write-Host "[4/6] Resetting password..." -ForegroundColor Yellow
$sql = "FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED BY '$newPassword'; FLUSH PRIVILEGES;"
$resetOut = & "$mysqlBin\mysql.exe" -u root -e $sql 2>&1
Write-Host "    Output: $resetOut" -ForegroundColor Gray

Write-Host "[5/6] Stopping mysqld..." -ForegroundColor Yellow
if (-not $proc.HasExited) { Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue }
Start-Sleep -Seconds 5

Write-Host "[6/6] Starting MySQL service and verifying..." -ForegroundColor Yellow
Remove-Item $tempCnf -ErrorAction SilentlyContinue
Start-Service -Name "MySQL80"
Start-Sleep -Seconds 5

$verify = & "$mysqlBin\mysql.exe" -u root -p$newPassword -e "SELECT 'OK' AS result;" 2>&1
if ($verify -match 'OK') {
    Write-Host "SUCCESS! root / $newPassword" -ForegroundColor Green
} else {
    Write-Host "FAILED: $verify" -ForegroundColor Red
}

Read-Host "Press Enter to exit"
