@echo off
rem Stops the application started via start.bat.
setlocal enabledelayedexpansion
cd /d "%~dp0.."

if "%APP_NAME%"=="" set "APP_NAME=demo"

set "PID_FILE=%CD%\%APP_NAME%.pid"

if not exist "%PID_FILE%" (
    echo No PID file found for %APP_NAME% - is the service running?
    exit /b 1
)

set /p PID=<"%PID_FILE%"

tasklist /FI "PID eq !PID!" 2>nul | find "!PID!" >nul
if not errorlevel 1 (
    echo Stopping the service ^(PID !PID!^)...
    taskkill /PID !PID! /F >nul
    del /f /q "%PID_FILE%"
    echo Stopped.
) else (
    echo Process !PID! not running. Removing stale PID file.
    del /f /q "%PID_FILE%"
)
endlocal
