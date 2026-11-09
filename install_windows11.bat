@echo off
setlocal enabledelayedexpansion
title Pulsoid VR - Windows 11 Desktop Setup

echo ========================================================
echo       PULSOID VR - WINDOWS 11 DESKTOP INSTALLER
echo ========================================================
echo.

set DESKTOP=%USERPROFILE%\Desktop
set TARGET=%DESKTOP%\PulsoidVR.apk

echo [1/3] Locating / Downloading Pulsoid VR APK to Windows 11 Desktop...

if exist "%~dp0app\build\outputs\apk\debug\app-debug.apk" (
    echo [OK] Found local compiled APK in repository build output!
    copy /y "%~dp0app\build\outputs\apk\debug\app-debug.apk" "%TARGET%" >nul
    echo Copied to %TARGET%
) else (
    echo Downloading from local Pulsoid VR server...
    powershell -Command "Invoke-WebRequest -Uri 'http://127.0.0.1:9090/download' -OutFile '%TARGET%'" 2>nul
    if not exist "%TARGET%" (
        echo Could not connect to localhost:9090.
        set /p PHONE_IP="Enter your phone IP address (e.g. 192.168.1.50): "
        powershell -Command "Invoke-WebRequest -Uri 'http://!PHONE_IP!:9090/download' -OutFile '%TARGET%'" 2>nul
    )
)

if exist "%TARGET%" (
    echo.
    echo [OK] Pulsoid VR APK is ready on your Windows 11 Desktop:
    echo      %TARGET%
) else (
    echo [!] Could not locate or download APK automatically.
    echo Please use the 'Generate APK' or 'Export' option in AI Studio.
)

echo.
echo [2/3] Checking for Android Debug Bridge (ADB) / Windows Subsystem for Android (WSA)...
where adb >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] ADB detected in PATH.
    echo Installing Pulsoid VR to Windows 11 / connected device...
    adb install -r "%TARGET%"
    echo.
    echo [3/3] Setting up VRChat OSC Port 9000 forward...
    adb forward udp:9000 udp:9000 2>nul
    echo.
    echo [SUCCESS] Pulsoid VR is installed!
) else (
    echo [INFO] ADB command line tool not in PATH.
    echo You can easily run Pulsoid VR on Windows 11 using:
    echo.
    echo   1. Windows Subsystem for Android (WSA):
    echo      Double-click 'PulsoidVR.apk' directly on your Desktop to install!
    echo.
    echo   2. BlueStacks / LDPlayer / Nox Emulators:
    echo      Drag and drop 'PulsoidVR.apk' into the emulator window.
    echo.
    echo   3. Standalone Phone / Meta Quest:
    echo      Keep Pulsoid VR running on your phone, and enter your Windows 11 PC IP.
    echo      VRChat receives OSC packets automatically over Wi-Fi!
)

echo.
echo ========================================================
pause
