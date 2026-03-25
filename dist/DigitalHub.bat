@echo off
cd /d "%~dp0"

rem === Priority 1: local portable JRE (jre\ folder next to this bat) ===
if exist "%~dp0jre\bin\java.exe" (
    set "JAVA_EXE=%~dp0jre\bin\java.exe"
    goto launch
)

rem === Priority 2: Eclipse Adoptium Java 21 ===
for /d %%J in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do (
    if exist "%%J\bin\java.exe" set "JAVA_EXE=%%J\bin\java.exe"
)
if defined JAVA_EXE goto launch

rem === Priority 3: Microsoft / Oracle / OpenJDK Java 21 ===
for /d %%J in ("%ProgramFiles%\Microsoft\jdk-21*") do (
    if exist "%%J\bin\java.exe" set "JAVA_EXE=%%J\bin\java.exe"
)
for /d %%J in ("%ProgramFiles%\Java\jdk-21*") do (
    if exist "%%J\bin\java.exe" set "JAVA_EXE=%%J\bin\java.exe"
)
for /d %%J in ("%ProgramFiles%\OpenJDK\jdk-21*") do (
    if exist "%%J\bin\java.exe" set "JAVA_EXE=%%J\bin\java.exe"
)
if defined JAVA_EXE goto launch

rem === Priority 4: JAVA_HOME ===
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
        goto launch
    )
)

rem === Priority 5: java from PATH ===
where java >nul 2>&1
if not errorlevel 1 (
    set "JAVA_EXE=java"
    goto launch
)

echo.
echo  [ERROR] Java 21 not found.
echo  Place portable JRE in: %~dp0jre\
echo  Or install Java 21:    https://adoptium.net
echo.
pause
exit /b 1

:launch
"%JAVA_EXE%" -Ddb.path=digitalhub.db -Dfile.encoding=UTF-8 -jar DigitalHub.jar
if errorlevel 1 (
    echo.
    echo  [ERROR] Launch failed. Java used: %JAVA_EXE%
    echo  Java 21 required: https://adoptium.net
    pause
)
