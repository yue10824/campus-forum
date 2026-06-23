@echo off
chcp 65001 >nul
echo ============================================
echo    MySQL Root 密码重置脚本
echo ============================================
echo.

REM 检查管理员权限
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 请右键此文件，选择"以管理员身份运行"！
    pause
    exit /b 1
)

set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin
set MY_INI=C:\ProgramData\MySQL\MySQL Server 8.0\my.ini
set NEW_PASSWORD=123456

echo [1/7] 停止 MySQL 服务...
net stop MySQL80 2>nul
timeout /t 2 /nobreak >nul

echo [2/7] 备份 my.ini...
copy "%MY_INI%" "%MY_INI%.bak" >nul

echo [3/7] 添加 skip-grant-tables 到 my.ini...
powershell -Command "(Get-Content '%MY_INI%') -replace '^\[mysqld\]', '[mysqld]`nskip-grant-tables' | Set-Content '%MY_INI%'"

echo [4/7] 启动 MySQL 服务（免密模式）...
net start MySQL80
timeout /t 3 /nobreak >nul

echo [5/7] 重置 root 密码为 %NEW_PASSWORD% ...
"%MYSQL_BIN%\mysql.exe" -u root -e "FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED BY '%NEW_PASSWORD%'; FLUSH PRIVILEGES;"
if %errorlevel% neq 0 (
    echo [错误] 密码重置失败！正在恢复配置...
    copy "%MY_INI%.bak" "%MY_INI%" >nul
    net stop MySQL80 2>nul
    timeout /t 2 /nobreak >nul
    net start MySQL80
    pause
    exit /b 1
)

echo [6/7] 恢复 my.ini（移除 skip-grant-tables）...
copy "%MY_INI%.bak" "%MY_INI%" >nul
del "%MY_INI%.bak"

echo [7/7] 重启 MySQL 服务...
net stop MySQL80 2>nul
timeout /t 2 /nobreak >nul
net start MySQL80

echo.
echo ============================================
echo    密码重置成功！
echo    用户名: root
echo    密码:   %NEW_PASSWORD%
echo ============================================
echo.

echo 验证连接中...
"%MYSQL_BIN%\mysql.exe" -u root -p%NEW_PASSWORD% -e "SELECT 'MySQL连接成功!' AS result;"
if %errorlevel% neq 0 (
    echo [警告] 验证失败，请手动检查
) else (
    echo 验证通过！
)

pause
