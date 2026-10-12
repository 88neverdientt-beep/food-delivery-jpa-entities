@echo off
chcp 65001 >nul
title Food Delivery Application
cd /d "%~dp0"

echo ========================================================
echo    DANG KHOI DONG UNG DUNG FOOD DELIVERY (SPRING BOOT)
echo    Cong hoat dong: http://localhost:8080
echo ========================================================
echo.

:: Cho 2 giay de server bat dau khoi dong roi mo trinh duyet
start "" "http://localhost:8080"

:: Chay file JAR dong goi ca Frontend + Backend
java -jar "target\food-delivery-0.0.1-SNAPSHOT.jar"
