@echo off
:: ============================================================
:: DigitalHub — Сборка и запуск
:: Платформа: Windows
:: Требования: Java JDK 21+, Apache Maven 3.8+
:: ============================================================
chcp 65001 >nul 2>&1
cd /d "%~dp0\.."
title DigitalHub — Сборка и запуск
echo ================================================
echo   DigitalHub — Сборка и запуск
echo ================================================
echo.

:: Указываем JDK 21 для сборки
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

java --version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Java не найдена! Установите Java 21+
    pause & exit /b 1
)

call mvn --version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Maven не найден! Добавьте mvn в PATH
    pause & exit /b 1
)

echo [1/2] Сборка проекта...
call mvn clean package -q
if errorlevel 1 (
    echo [ОШИБКА] Сборка не удалась! Повтор с подробным выводом:
    call mvn clean package
    pause & exit /b 1
)

echo [2/2] Копирование JAR в dist\ и запуск...
copy /Y target\techhaven-1.0.0.jar dist\DigitalHub.jar >nul

echo [OK] Запуск приложения из dist\...
java -Ddb.path=dist/digitalhub.db -jar dist\DigitalHub.jar
if errorlevel 1 (
    echo [ОШИБКА] Приложение завершилось с ошибкой.
    pause
)
