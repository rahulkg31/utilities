@echo off
rem Starts the application in the background and records its PID.
setlocal enabledelayedexpansion
cd /d "%~dp0.."

if "%APP_NAME%"=="" set "APP_NAME=demo"
if "%APP_VERSION%"=="" set "APP_VERSION=1.0.0"

set "APP_HOME=%CD%"
set "JAR=%APP_HOME%\lib\%APP_NAME%-%APP_VERSION%.jar"
set "CONFIG_DIR=%APP_HOME%\conf"
set "PID_FILE=%APP_HOME%\%APP_NAME%.pid"

if not exist "%JAR%" (
    echo ERROR: %JAR% not found 1>&2
    exit /b 1
)

if not exist "%CONFIG_DIR%\" (
    echo ERROR: config directory %CONFIG_DIR% not found 1>&2
    exit /b 1
)

if exist "%PID_FILE%" (
    set /p OLD_PID=<"%PID_FILE%"
    tasklist /FI "PID eq !OLD_PID!" 2>nul | find "!OLD_PID!" >nul
    if not errorlevel 1 (
        echo %APP_NAME% is already running ^(PID !OLD_PID!^).
        exit /b 1
    )
)

rem Spring accepts forward slashes; this also avoids a trailing backslash escaping the closing quote.
set "CONFIG_URI=%CONFIG_DIR:\=/%/"

echo Starting the service...
powershell -NoProfile -Command "$p = Start-Process -FilePath 'java' -ArgumentList '-Dspring.config.location=%CONFIG_URI%','-jar','\"%JAR%\"' -WindowStyle Hidden -PassThru; [IO.File]::WriteAllText('%PID_FILE%', [string]$p.Id)"
if errorlevel 1 (
    echo ERROR: failed to start the service 1>&2
    exit /b 1
)

set /p NEW_PID=<"%PID_FILE%"
echo Service started with PID %NEW_PID%.
endlocal
