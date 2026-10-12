@echo off
chcp 65001 >nul
title Dung Ung Dung Food Delivery
echo ========================================================
echo    DANG TAT UNG DUNG FOOD DELIVERY (PORT 8080)
echo ========================================================

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Dang tat tien trinh PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo Da dung thanh cong!
pause
