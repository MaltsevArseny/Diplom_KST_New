@echo off
:: ============================================================
:: DigitalHub - Build & Run
:: Platform: Windows
:: Requirements: Java JDK 21+, Apache Maven 3.8+
:: ============================================================
chcp 65001 >nul 2>&1
cd /d "%~dp0\.."
title DigitalHub - Build and Run
echo ================================================
echo   DigitalHub - Build and Run
echo ================================================
echo.

:: --- Locate Java 21 (priority chain, без hardcoded путей) ---
set "JAVA_EXE="

rem 1) JAVA_HOME (явно заданная пользователем)
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)
if defined JAVA_EXE goto java_found

rem 2) Portable JRE bundled in dist\jre
if exist "%~dp0\..\dist\jre\bin\java.exe" set "JAVA_EXE=%~dp0\..\dist\jre\bin\java.exe"
if defined JAVA_EXE goto java_found

rem 3) Поиск известных установок JDK 21
for /d %%J in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do (
    if exist "%%J\bin\java.exe" set "JAVA_EXE=%%J\bin\java.exe"
)
for /d %%J in ("%ProgramFiles%\Microsoft\jdk-21*") do (
    if exist "%%J\bin\java.exe" set "JAVA_EXE=%%J\bin\java.exe"
)
for /d %%J in ("%ProgramFiles%\Java\jdk-21*") do (
    if exist "%%J\bin\java.exe" set "JAVA_EXE=%%J\bin\java.exe"
)
for /d %%J in ("%ProgramFiles%\OpenJDK\jdk-21*") do (
    if exist "%%J\bin\java.exe" set "JAVA_EXE=%%J\bin\java.exe"
)
if defined JAVA_EXE goto java_found

rem 4) Fallback - java из PATH
where java >nul 2>&1
if not errorlevel 1 (
    set "JAVA_EXE=java"
    goto java_found
)

echo [ERROR] Java 21 не найдена.
echo Установите Java 21 - https://adoptium.net
echo или поместите portable JRE в dist\jre\
pause & exit /b 1

:java_found
echo [OK] Java: %JAVA_EXE%

rem Синхронизируем JAVA_HOME с найденной Java для Maven
for %%I in ("%JAVA_EXE%") do set "JAVA_BIN_DIR=%%~dpI"
set "JAVA_HOME=%JAVA_BIN_DIR%.."
set "PATH=%JAVA_BIN_DIR%;%PATH%"

:: --- Verify Maven ---
call mvn --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven не найден! Добавьте mvn в PATH.
    pause & exit /b 1
)

echo [1/2] Сборка проекта...
call mvn clean package -q
if errorlevel 1 (
    echo [ERROR] Сборка не удалась! Повтор с подробным выводом:
    call mvn clean package
    pause & exit /b 1
)

echo [2/2] Копирование JAR в dist\ и запуск...
copy /Y target\techhaven-1.0.0.jar dist\DigitalHub.jar >nul

echo [OK] Запуск приложения из dist\...
cd /d "%~dp0\..\dist"
"%JAVA_EXE%" -Dfile.encoding=UTF-8 -jar DigitalHub.jar
if errorlevel 1 (
    echo [ERROR] Приложение завершилось с ошибкой.
    pause
)
