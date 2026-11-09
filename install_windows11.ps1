# Pulsoid VR - Windows 11 Desktop Downloader & Setup
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "      PULSOID VR - WINDOWS 11 DESKTOP INSTALLER" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$Desktop = [Environment]::GetFolderPath("Desktop")
$TargetApk = Join-Path $Desktop "PulsoidVR.apk"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$LocalApk = Join-Path $ScriptDir "app\build\outputs\apk\debug\app-debug.apk"

Write-Host "[1/3] Setting up Pulsoid VR APK on your Windows 11 Desktop..." -ForegroundColor Yellow

if (Test-Path $LocalApk) {
    Copy-Item -Path $LocalApk -Destination $TargetApk -Force
    Write-Host "[OK] Copied compiled APK directly to Desktop: $TargetApk" -ForegroundColor Green
} else {
    Write-Host "Downloading from local Pulsoid VR HTTP server..." -ForegroundColor White
    try {
        Invoke-WebRequest -Uri "http://127.0.0.1:9090/download" -OutFile $TargetApk -ErrorAction Stop
        Write-Host "[OK] Downloaded to $TargetApk" -ForegroundColor Green
    } catch {
        $PhoneIp = Read-Host "Could not reach localhost:9090. Enter phone IP (e.g. 192.168.1.50)"
        if ($PhoneIp) {
            Invoke-WebRequest -Uri "http://${PhoneIp}:9090/download" -OutFile $TargetApk
            Write-Host "[OK] Downloaded to $TargetApk" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "[2/3] Checking for ADB / Windows Subsystem for Android (WSA)..." -ForegroundColor Yellow
$Adb = Get-Command adb -ErrorAction SilentlyContinue
if ($Adb) {
    Write-Host "[OK] ADB detected. Installing Pulsoid VR on Windows 11..." -ForegroundColor Green
    & adb install -r $TargetApk
    & adb forward udp:9000 udp:9000
    Write-Host "[SUCCESS] Pulsoid VR is installed and VRChat OSC Port 9000 is ready!" -ForegroundColor Green
} else {
    Write-Host "ADB not found in system PATH. Ways to run on Windows 11 Desktop:" -ForegroundColor White
    Write-Host "1. Windows Subsystem for Android (WSA): Double-click PulsoidVR.apk on your Desktop." -ForegroundColor Cyan
    Write-Host "2. BlueStacks or LDPlayer: Drag and drop PulsoidVR.apk into the emulator." -ForegroundColor Cyan
    Write-Host "3. Phone to PC: Run on phone, enter your PC's IP into Target IP, VRChat receives OSC over Wi-Fi." -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
