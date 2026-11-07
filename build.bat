@echo off
setlocal
cd /d "%~dp0"

echo.
echo  ==========================================
echo             Rusify Builder
echo  ==========================================
echo.

where powershell.exe >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Windows PowerShell was not found.
    echo Windows PowerShell 5.1 is included with supported Windows versions.
    echo.
    pause
    exit /b 1
)

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build.ps1" %*
set "BUILD_EXIT=%ERRORLEVEL%"

echo.
if "%BUILD_EXIT%"=="0" (
    echo [SUCCESS] Rusify build completed.
) else (
    echo [FAILED] Rusify build failed with exit code %BUILD_EXIT%.
)
echo.
pause
exit /b %BUILD_EXIT%
