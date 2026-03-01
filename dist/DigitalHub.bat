@echo off
chcp 65001 >nul 2>&1
title DigitalHub

:: Определяем корень проекта (папка выше dist\)
set "PROJECT_DIR=%~dp0.."

:: Указываем JDK 21 для сборки
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

:: Проверка Java
java --version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Java не найдена. Установите Java 21 или выше.
    pause & exit /b 1
)

:: Проверка Maven
call mvn --version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Maven не найден. Добавьте mvn в PATH.
    pause & exit /b 1
)

:: Переходим в корень проекта для сборки
cd /d "%PROJECT_DIR%"

:: Сборка проекта (тихо)
echo [1/2] Сборка проекта...
call mvn clean package -q 2>nul
if errorlevel 1 (
    echo [ОШИБКА] Сборка не удалась. Запустите run.bat для подробностей.
    pause & exit /b 1
)

:: Копируем JAR
copy /Y target\techhaven-1.0.0.jar dist\DigitalHub.jar >nul

:: Запускаем из dist\ с указанием пути к БД (digitalhub.db рядом с JAR)
echo [2/2] Запуск приложения...
cd /d "%~dp0"
java -Ddb.path=digitalhub.db -jar DigitalHub.jar
if errorlevel 1 (
    echo [ОШИБКА] Приложение завершилось с ошибкой.
    pause
)
