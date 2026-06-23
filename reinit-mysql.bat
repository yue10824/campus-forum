@echo off
chcp 65001 >nul
echo ============================================
echo    MySQL 数据重新初始化（相当于重装）
echo ============================================
echo.

REM 检查管理员权限
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 请右键此文件，选择以管理员身份运行！
    pause
    exit /b 1
)

set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin
set DATA_DIR=C:\ProgramData\MySQL\MySQL Server 8.0\Data
set MY_INI=C:\ProgramData\MySQL\MySQL Server 8.0\my.ini

echo [1/6] 停止 MySQL 服务...
net stop MySQL80 2>nul
timeout /t 3 /nobreak >nul

echo [2/6] 备份并清空旧数据目录...
if exist "%DATA_DIR%_old" rmdir /s /q "%DATA_DIR%_old"
rename "%DATA_DIR%" "Data_old"
echo     已备份到 Data_old

echo [3/6] 重新初始化 MySQL（root 无密码）...
"%MYSQL_BIN%\mysqld.exe" --defaults-file="%MY_INI%" --initialize-insecure --console
if %errorlevel% neq 0 (
    echo [错误] 初始化失败！
    pause
    exit /b 1
)
echo     初始化完成

echo [4/6] 启动 MySQL 服务...
net start MySQL80
timeout /t 3 /nobreak >nul

echo [5/6] 设置 root 密码为 123456...
"%MYSQL_BIN%\mysql.exe" -u root --skip-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '123456'; FLUSH PRIVILEGES;"
if %errorlevel% neq 0 (
    echo [错误] 设置密码失败！
    pause
    exit /b 1
)

echo [6/6] 验证连接...
"%MYSQL_BIN%\mysql.exe" -u root -p123456 -e "SELECT 'OK' AS result;"
if %errorlevel% neq 0 (
    echo [警告] 验证失败
) else (
    echo.
    echo ============================================
    echo    重置成功！
    echo    用户名: root
    echo    密码:   123456
    echo ============================================
)

echo.
echo 清理备份数据...
rmdir /s /q "%DATA_DIR%_old" 2>nul

pause
