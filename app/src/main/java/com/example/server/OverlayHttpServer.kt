package com.example.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.HeartRateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class OverlayHttpServer(
    private val context: Context,
    private val port: Int = 9090
) {
    companion object {
        private const val TAG = "OverlayHttpServer"
    }

    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var serverJob: Job? = null

    private val sseClients = CopyOnWriteArrayList<OutputStream>()

    @Volatile
    private var currentData: HeartRateData = HeartRateData(bpm = 0)
    @Volatile
    private var sessionMax = 0
    @Volatile
    private var sessionAvg = 0

    var isRunning = false
        private set

    fun updateData(data: HeartRateData, max: Int = 0, avg: Int = 0) {
        currentData = data
        sessionMax = max
        sessionAvg = avg

        // Broadcast to any active SSE browser connections
        if (sseClients.isNotEmpty()) {
            val json = getJsonPayload()
            val msg = "data: $json\n\n".toByteArray(Charsets.UTF_8)
            val toRemove = mutableListOf<OutputStream>()
            for (out in sseClients) {
                try {
                    out.write(msg)
                    out.flush()
                } catch (e: Exception) {
                    toRemove.add(out)
                }
            }
            if (toRemove.isNotEmpty()) {
                sseClients.removeAll(toRemove)
            }
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                Log.d(TAG, "Overlay server started on port $port")
                while (isActive && isRunning) {
                    val client = serverSocket?.accept() ?: break
                    scope.launch {
                        handleClient(client)
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e(TAG, "Server socket error", e)
                }
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            for (out in sseClients) {
                try { out.close() } catch (_: Exception) {}
            }
            sseClients.clear()
            serverSocket?.close()
            serverSocket = null
            serverJob?.cancel()
            serverJob = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }

    private suspend fun handleClient(client: Socket) = withContext(Dispatchers.IO) {
        try {
            val reader = BufferedReader(InputStreamReader(client.getInputStream()))
            val out = client.getOutputStream()
            val requestLine = reader.readLine() ?: return@withContext

            val parts = requestLine.split(" ")
            if (parts.size < 2) return@withContext
            val path = parts[1]
            val localIp = getLocalIpAddress(context)

            when {
                // Direct APK Download for Windows 11 Desktop
                path.startsWith("/download") || path == "/pulsoid.apk" || path == "/PulsoidVR.apk" -> {
                    serveApkFile(out)
                }

                // 1-Click Windows 11 Batch Setup Script
                path.startsWith("/install.bat") || path.startsWith("/install_windows11.bat") -> {
                    serveWindowsBatchScript(out, localIp)
                }

                // 1-Click Windows 11 PowerShell Setup Script
                path.startsWith("/install.ps1") || path.startsWith("/install_windows11.ps1") -> {
                    serveWindowsPowerShellScript(out, localIp)
                }

                // Server-Sent Events (SSE) Live Feed
                path.startsWith("/api/stream") -> {
                    val header = ("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/event-stream\r\n" +
                            "Cache-Control: no-cache\r\n" +
                            "Connection: keep-alive\r\n" +
                            "Access-Control-Allow-Origin: *\r\n\r\n").toByteArray()
                    out.write(header)
                    out.flush()
                    sseClients.add(out)
                    return@withContext
                }

                // JSON API
                path.startsWith("/api/hr") -> {
                    val json = getJsonPayload()
                    val response = ("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Length: ${json.toByteArray().size}\r\n\r\n" +
                            json).toByteArray()
                    out.write(response)
                    out.flush()
                }

                // Windows 11 Desktop Hub Webpage
                path.startsWith("/windows") || path.startsWith("/desktop") -> {
                    val html = getWindowsDesktopHtml(localIp)
                    val response = ("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Length: ${html.toByteArray(Charsets.UTF_8).size}\r\n\r\n" +
                            html).toByteArray(Charsets.UTF_8)
                    out.write(response)
                    out.flush()
                }

                // OBS Stream Overlay Widget or Root Landing Page
                path.startsWith("/overlay") || path == "/" -> {
                    val html = getOverlayHtml(path, localIp)
                    val response = ("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Length: ${html.toByteArray(Charsets.UTF_8).size}\r\n\r\n" +
                            html).toByteArray(Charsets.UTF_8)
                    out.write(response)
                    out.flush()
                }

                else -> {
                    val notFound = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n".toByteArray()
                    out.write(notFound)
                    out.flush()
                }
            }
            client.close()
        } catch (e: Exception) {
            // Client disconnected
        }
    }

    private fun serveApkFile(out: OutputStream) {
        val apkFile = try {
            val sourceDir = context.applicationInfo?.sourceDir
            if (!sourceDir.isNullOrBlank()) File(sourceDir) else null
        } catch (_: Exception) { null }

        if (apkFile != null && apkFile.exists() && apkFile.canRead()) {
            val header = ("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/vnd.android.package-archive\r\n" +
                    "Content-Disposition: attachment; filename=\"PulsoidVR.apk\"\r\n" +
                    "Content-Length: ${apkFile.length()}\r\n" +
                    "Connection: close\r\n\r\n").toByteArray(Charsets.UTF_8)
            out.write(header)
            apkFile.inputStream().use { input ->
                input.copyTo(out)
            }
            out.flush()
        } else {
            val msg = "Pulsoid VR APK download is available via the project export or build outputs. Source path: ${context.applicationInfo?.sourceDir}"
            val header = ("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Length: ${msg.toByteArray(Charsets.UTF_8).size}\r\n" +
                    "Connection: close\r\n\r\n" +
                    msg).toByteArray(Charsets.UTF_8)
            out.write(header)
            out.flush()
        }
    }

    private fun serveWindowsBatchScript(out: OutputStream, localIp: String) {
        val script = """
@echo off
setlocal enabledelayedexpansion
title Pulsoid VR - Windows 11 Desktop Downloader

echo ========================================================
echo       PULSOID VR - WINDOWS 11 DESKTOP INSTALLER
echo ========================================================
echo.

set DESKTOP=%USERPROFILE%\Desktop
set TARGET=%DESKTOP%\PulsoidVR.apk
set DOWNLOAD_URL=http://$localIp:$port/download

echo [1/3] Downloading Pulsoid VR APK to your Windows 11 Desktop...
echo URL: %DOWNLOAD_URL%
powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%TARGET%'"

if exist "%TARGET%" (
    echo [OK] Successfully downloaded Pulsoid VR to:
    echo      %TARGET%
) else (
    echo [!] Download failed. Trying localhost...
    powershell -Command "Invoke-WebRequest -Uri 'http://127.0.0.1:$port/download' -OutFile '%TARGET%'"
)

echo.
echo [2/3] Checking for Windows Subsystem for Android / ADB...
where adb >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] ADB found! Installing Pulsoid VR directly into Windows 11...
    adb install -r "%TARGET%"
    echo [3/3] Setting up VRChat OSC Port 9000 forward...
    adb forward udp:9000 udp:9000 2>nul
    echo.
    echo [SUCCESS] Pulsoid VR is installed on your Windows 11 PC!
) else (
    echo [INFO] ADB not detected in PATH.
    echo Next steps on Windows 11 Desktop:
    echo   1. Double-click PulsoidVR.apk on your Desktop if WSA is installed.
    echo   2. Or drag-and-drop PulsoidVR.apk into BlueStacks or LDPlayer.
    echo   3. Or keep running Pulsoid VR on your phone with target IP set to your PC!
)

echo.
echo ========================================================
pause
        """.trimIndent()

        val bytes = script.toByteArray(Charsets.UTF_8)
        val header = ("HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/x-bat\r\n" +
                "Content-Disposition: attachment; filename=\"Install_PulsoidVR_Windows11.bat\"\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n").toByteArray(Charsets.UTF_8)
        out.write(header)
        out.write(bytes)
        out.flush()
    }

    private fun serveWindowsPowerShellScript(out: OutputStream, localIp: String) {
        val script = StringBuilder().apply {
            appendLine("# Pulsoid VR - Windows 11 Desktop Downloader & Setup")
            appendLine("Write-Host \"========================================================\" -ForegroundColor Cyan")
            appendLine("Write-Host \"      PULSOID VR - WINDOWS 11 DESKTOP INSTALLER\" -ForegroundColor Cyan")
            appendLine("Write-Host \"========================================================\" -ForegroundColor Cyan")
            appendLine("")
            appendLine("\$Desktop = [Environment]::GetFolderPath(\"Desktop\")")
            appendLine("\$ApkPath = Join-Path \$Desktop \"PulsoidVR.apk\"")
            appendLine("\$DownloadUrl = \"http://$localIp:$port/download\"")
            appendLine("")
            appendLine("Write-Host \"[1/3] Downloading Pulsoid VR APK directly to Windows 11 Desktop...\" -ForegroundColor Yellow")
            appendLine("try {")
            appendLine("    Invoke-WebRequest -Uri \$DownloadUrl -OutFile \$ApkPath -ErrorAction Stop")
            appendLine("    Write-Host \"[OK] Saved to: \$ApkPath\" -ForegroundColor Green")
            appendLine("} catch {")
            appendLine("    Write-Host \"[!] Could not download from \$DownloadUrl, trying localhost...\" -ForegroundColor Yellow")
            appendLine("    Invoke-WebRequest -Uri \"http://127.0.0.1:$port/download\" -OutFile \$ApkPath")
            appendLine("}")
            appendLine("")
            appendLine("Write-Host \"\"")
            appendLine("Write-Host \"[2/3] Checking ADB / Windows Subsystem for Android...\" -ForegroundColor Yellow")
            appendLine("\$Adb = Get-Command adb -ErrorAction SilentlyContinue")
            appendLine("if (\$Adb) {")
            appendLine("    Write-Host \"[OK] ADB detected. Installing to Windows 11...\" -ForegroundColor Green")
            appendLine("    & adb install -r \$ApkPath")
            appendLine("    & adb forward udp:9000 udp:9000")
            appendLine("    Write-Host \"[SUCCESS] Pulsoid VR is installed and VRChat OSC Port 9000 is ready!\" -ForegroundColor Green")
            appendLine("} else {")
            appendLine("    Write-Host \"Tips for Windows 11 Desktop:\" -ForegroundColor White")
            appendLine("    Write-Host \"1. If using WSA (Windows Subsystem for Android), double-click PulsoidVR.apk on your Desktop.\"")
            appendLine("    Write-Host \"2. If using BlueStacks or LDPlayer, drag and drop PulsoidVR.apk into the emulator window.\"")
            appendLine("    Write-Host \"3. If running on your phone, VRChat on PC receives OSC over Wi-Fi automatically.\"")
            appendLine("}")
            appendLine("")
            appendLine("Write-Host \"Press any key to finish...\"")
            appendLine("\$null = \$Host.UI.RawUI.ReadKey(\"NoEcho,IncludeKeyDown\")")
        }.toString()

        val bytes = script.toByteArray(Charsets.UTF_8)
        val header = ("HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "Content-Disposition: attachment; filename=\"Install_PulsoidVR_Windows11.ps1\"\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n").toByteArray(Charsets.UTF_8)
        out.write(header)
        out.write(bytes)
        out.flush()
    }

    private fun getJsonPayload(): String {
        val zone = currentData.getZone()
        val colorHex = String.format("#%06X", (0xFFFFFF and zone.colorHex.toInt()))
        return """{"bpm":${currentData.bpm},"zone":"${zone.label}","shortZone":"${zone.shortName}","max":$sessionMax,"avg":$sessionAvg,"color":"$colorHex","isBeat":${currentData.isBeat}}"""
    }

    private fun getWindowsDesktopHtml(localIp: String): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Pulsoid VR — Windows 11 Desktop Hub</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background: #090d16;
      color: #f1f5f9;
      font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
      padding: 32px 16px;
      display: flex;
      justify-content: center;
      min-height: 100vh;
    }
    .container {
      max-width: 860px;
      width: 100%;
      display: flex;
      flex-direction: column;
      gap: 24px;
    }
    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      padding-bottom: 20px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 14px;
    }
    .logo-badge {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: linear-gradient(135deg, #ff2a5f, #7928ca);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      box-shadow: 0 0 20px rgba(255, 42, 95, 0.5);
    }
    h1 { font-size: 24px; font-weight: 800; letter-spacing: 0.5px; }
    .subtitle { font-size: 13px; color: #94a3b8; margin-top: 4px; }
    .pill {
      background: rgba(16, 185, 129, 0.15);
      border: 1px solid #10b981;
      color: #10b981;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 700;
    }
    .card {
      background: rgba(15, 23, 42, 0.85);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 20px;
      padding: 24px;
      backdrop-filter: blur(16px);
      box-shadow: 0 10px 30px rgba(0,0,0,0.4);
    }
    .highlight-card {
      border: 1.5px solid rgba(255, 42, 95, 0.5);
      background: linear-gradient(180deg, rgba(255, 42, 95, 0.08), rgba(15, 23, 42, 0.9));
    }
    .btn-group {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-top: 18px;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      padding: 14px 24px;
      border-radius: 12px;
      font-size: 14px;
      font-weight: 700;
      text-decoration: none;
      cursor: pointer;
      transition: all 0.2s ease;
      border: none;
    }
    .btn-primary {
      background: #ff2a5f;
      color: #fff;
      box-shadow: 0 4px 20px rgba(255, 42, 95, 0.4);
    }
    .btn-primary:hover {
      background: #e01e50;
      transform: translateY(-2px);
      box-shadow: 0 6px 25px rgba(255, 42, 95, 0.6);
    }
    .btn-secondary {
      background: #00f2fe;
      color: #0b1120;
    }
    .btn-secondary:hover {
      background: #00d2de;
      transform: translateY(-2px);
    }
    .btn-outline {
      background: rgba(255, 255, 255, 0.06);
      color: #f1f5f9;
      border: 1px solid rgba(255, 255, 255, 0.2);
    }
    .btn-outline:hover {
      background: rgba(255, 255, 255, 0.12);
    }
    .cmd-box {
      background: #050811;
      border: 1px solid rgba(0, 242, 254, 0.3);
      border-radius: 12px;
      padding: 12px 16px;
      font-family: 'Consolas', 'Courier New', monospace;
      font-size: 13px;
      color: #00f2fe;
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 12px;
      word-break: break-all;
    }
    .copy-btn {
      background: rgba(0, 242, 254, 0.15);
      border: 1px solid #00f2fe;
      color: #00f2fe;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 12px;
      cursor: pointer;
      font-weight: bold;
      white-space: nowrap;
      margin-left: 10px;
    }
    .copy-btn:hover { background: #00f2fe; color: #000; }
    .live-monitor {
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: rgba(0,0,0,0.5);
      border-radius: 14px;
      padding: 16px 20px;
      border: 1px solid rgba(255, 42, 95, 0.3);
    }
    .live-bpm {
      font-size: 44px;
      font-weight: 900;
      color: #ff2a5f;
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .grid-2 {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    @media (max-width: 680px) {
      .grid-2 { grid-template-columns: 1fr; }
    }
    ol { margin-left: 20px; margin-top: 12px; font-size: 13px; color: #cbd5e1; line-height: 1.8; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div class="brand">
        <div class="logo-badge">❤️</div>
        <div>
          <h1>Pulsoid VR — Windows 11 Desktop Hub</h1>
          <div class="subtitle">Direct PC Download, 1-Click Installer, and VRChat OSC Integration</div>
        </div>
      </div>
      <div class="pill">Connected to PC</div>
    </div>

    <!-- Live Real-Time Heart Monitor Widget -->
    <div class="card">
      <div style="font-size: 12px; font-weight: bold; letter-spacing: 1px; color: #94a3b8; margin-bottom: 12px;">LIVE OSC STATUS & STREAM</div>
      <div class="live-monitor">
        <div class="live-bpm">
          <span id="heartIcon">❤️</span>
          <span id="bpmText">--</span>
          <span style="font-size: 14px; color: #94a3b8; font-weight: bold;">BPM</span>
        </div>
        <div>
          <div id="zoneBadge" style="background:#ff2a5f; color:#fff; font-size:11px; font-weight:bold; padding:4px 10px; border-radius:6px; text-align:center;">STANDBY</div>
          <div id="statsText" style="font-size:11px; color:#94a3b8; margin-top:4px; text-align:right;">MAX: -- · AVG: --</div>
        </div>
      </div>
    </div>

    <!-- Direct Download Card -->
    <div class="card highlight-card">
      <h2>⬇️ Download to Windows 11 Desktop</h2>
      <p style="font-size: 13px; color: #cbd5e1; margin-top: 6px;">
        Click below to download the latest compiled <strong>PulsoidVR.apk</strong> directly to your Windows 11 PC, or download the 1-click batch setup script.
      </p>

      <div class="btn-group">
        <a href="/download" class="btn btn-primary" download="PulsoidVR.apk">
          <span>📦</span> Download PulsoidVR.apk
        </a>
        <a href="/install.bat" class="btn btn-secondary" download="Install_PulsoidVR_Windows11.bat">
          <span>⚡</span> 1-Click Windows 11 Installer (.bat)
        </a>
        <a href="/overlay" target="_blank" class="btn btn-outline">
          <span>🎥</span> Open OBS Browser Overlay
        </a>
      </div>

      <div style="margin-top: 20px;">
        <div style="font-size: 12px; font-weight: bold; color: #94a3b8;">1-CLICK POWERSHELL COMMAND (Downloads APK directly to your Windows 11 Desktop folder):</div>
        <div class="cmd-box">
          <span id="psCmd">powershell -Command "Invoke-WebRequest -Uri 'http://$localIp:$port/download' -OutFile \"${'$'}([Environment]::GetFolderPath('Desktop'))\PulsoidVR.apk\""</span>
          <button class="copy-btn" onclick="copyCommand()">Copy Command</button>
        </div>
      </div>
    </div>

    <!-- Windows 11 Setup Options -->
    <div class="grid-2">
      <div class="card">
        <h3>🪟 Windows Subsystem for Android (WSA)</h3>
        <ol>
          <li>Download <strong>PulsoidVR.apk</strong> using the button above.</li>
          <li>Right-click the APK on your Desktop and select <em>Open with Windows Subsystem for Android</em> (or run <code>adb install PulsoidVR.apk</code>).</li>
          <li>Pulsoid VR opens as a native Windows 11 desktop window!</li>
          <li>In Setup, set Target IP to <code>127.0.0.1:9000</code> for 0ms VRChat sync.</li>
        </ol>
      </div>

      <div class="card">
        <h3>🎮 BlueStacks / LDPlayer Emulators</h3>
        <ol>
          <li>Open BlueStacks, LDPlayer, or Nox on your Windows 11 desktop.</li>
          <li>Drag and drop the downloaded <strong>PulsoidVR.apk</strong> directly into the emulator window.</li>
          <li>The app installs instantly with zero setup.</li>
          <li>Pair your Bluetooth heart monitor or turn on the simulator!</li>
        </ol>
      </div>
    </div>

    <!-- PC SteamVR & OBS Instructions -->
    <div class="card">
      <h3>📡 Connecting to VRChat on PC</h3>
      <ol>
        <li>In VRChat on PC, open the <strong>Action Menu -> Options -> OSC -> Enable</strong>.</li>
        <li>Ensure your Windows 11 PC and mobile device are on the same Wi-Fi.</li>
        <li>Pulsoid VR transmits real-time heart rate, zone progress bars, and voice text bubbles to UDP port 9000!</li>
        <li>For OBS Studio: Add a <strong>Browser Source</strong> with URL <code>http://$localIp:$port/overlay</code>.</li>
      </ol>
    </div>
  </div>

  <script>
    function copyCommand() {
      const text = document.getElementById('psCmd').textContent;
      navigator.clipboard.writeText(text);
      alert('Copied PowerShell command to clipboard! Paste it into PowerShell on Windows 11 to download the APK straight to your Desktop.');
    }

    // Connect SSE for live heart rate
    const bpmEl = document.getElementById('bpmText');
    const zoneEl = document.getElementById('zoneBadge');
    const statsEl = document.getElementById('statsText');
    const heartEl = document.getElementById('heartIcon');

    function connectSSE() {
      try {
        const es = new EventSource('/api/stream');
        es.onmessage = function(e) {
          try {
            const data = JSON.parse(e.data);
            bpmEl.textContent = data.bpm > 0 ? data.bpm : '--';
            zoneEl.textContent = data.shortZone || 'READY';
            if (data.color) zoneEl.style.backgroundColor = data.color;
            if (data.max && data.avg) statsEl.textContent = 'MAX: ' + data.max + ' · AVG: ' + data.avg;
          } catch(err) {}
        };
        es.onerror = function() {
          es.close();
          setTimeout(connectSSE, 2000);
        };
      } catch(e) {}
    }
    connectSSE();
  </script>
</body>
</html>
        """.trimIndent()
    }

    private fun getOverlayHtml(path: String, localIp: String): String {
        val isCyber = path.contains("theme=cyber")
        val isRpg = path.contains("theme=rpg")

        val bgStyle = "background: transparent;"
        val fontFamily = if (isRpg) "'Press Start 2P', monospace, sans-serif" else "'Inter', -apple-system, sans-serif"

        return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Pulsoid VR Stream Overlay</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      $bgStyle
      font-family: $fontFamily;
      overflow: hidden;
      display: flex;
      align-items: center;
      padding: 12px;
    }
    .widget-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 12px 24px;
      border-radius: 18px;
      background: rgba(12, 16, 26, 0.88);
      border: 1px solid rgba(255, 60, 90, 0.4);
      box-shadow: 0 0 25px rgba(255, 30, 80, 0.35), inset 0 0 15px rgba(255, 30, 80, 0.15);
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;
    }
    .heart-icon {
      font-size: 42px;
      line-height: 1;
      filter: drop-shadow(0 0 10px #ff2a5f);
      transform-origin: center;
    }
    .pulse-beat {
      animation: heartBeat 0.35s ease-out;
    }
    @keyframes heartBeat {
      0% { transform: scale(1); }
      30% { transform: scale(1.35); filter: drop-shadow(0 0 20px #ff1744); }
      100% { transform: scale(1); }
    }
    .bpm-box {
      display: flex;
      flex-direction: column;
    }
    .bpm-number {
      font-size: 52px;
      font-weight: 900;
      color: #ffffff;
      line-height: 1;
      text-shadow: 0 0 15px rgba(255, 255, 255, 0.6);
      font-variant-numeric: tabular-nums;
    }
    .bpm-label {
      font-size: 13px;
      font-weight: 700;
      color: #ff3b68;
      letter-spacing: 2px;
      text-transform: uppercase;
      margin-top: 2px;
    }
    .stats-col {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding-left: 12px;
      border-left: 1px solid rgba(255, 255, 255, 0.15);
      font-size: 11px;
      color: #b0bec5;
      font-weight: 600;
    }
    .zone-badge {
      display: inline-block;
      padding: 3px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 800;
      background: rgba(255, 255, 255, 0.1);
      color: #fff;
      text-align: center;
    }
  </style>
</head>
<body>
  <div class="widget-card" id="card">
    <div class="heart-icon" id="heart">❤️</div>
    <div class="bpm-box">
      <div class="bpm-number" id="bpm">--</div>
      <div class="bpm-label" id="label">BPM</div>
    </div>
    <div class="stats-col">
      <div class="zone-badge" id="zone">STANDBY</div>
      <div id="max-stat">MAX: --</div>
      <div id="avg-stat">AVG: --</div>
    </div>
  </div>

  <script>
    const heart = document.getElementById('heart');
    const bpmEl = document.getElementById('bpm');
    const zoneEl = document.getElementById('zone');
    const maxEl = document.getElementById('max-stat');
    const avgEl = document.getElementById('avg-stat');
    const card = document.getElementById('card');

    function applyData(data) {
      if (!data) return;
      bpmEl.textContent = data.bpm > 0 ? data.bpm : '--';
      zoneEl.textContent = data.shortZone || 'READY';
      if (data.color) {
        zoneEl.style.backgroundColor = data.color;
        card.style.borderColor = data.color;
        card.style.boxShadow = '0 0 20px ' + data.color + '55';
      }
      if (data.max) maxEl.textContent = 'MAX: ' + data.max;
      if (data.avg) avgEl.textContent = 'AVG: ' + data.avg;

      // Pulse animation trigger
      heart.classList.remove('pulse-beat');
      void heart.offsetWidth; // trigger reflow
      heart.classList.add('pulse-beat');
    }

    // Connect to SSE stream
    let eventSource = null;
    function connectSSE() {
      try {
        eventSource = new EventSource('/api/stream');
        eventSource.onmessage = function(e) {
          try {
            const data = JSON.parse(e.data);
            applyData(data);
          } catch(err) {}
        };
        eventSource.onerror = function() {
          eventSource.close();
          setTimeout(pollFallback, 1500);
        };
      } catch(e) {
        pollFallback();
      }
    }

    function pollFallback() {
      fetch('/api/hr')
        .then(r => r.json())
        .then(applyData)
        .catch(() => {})
        .finally(() => {
          setTimeout(pollFallback, 800);
        });
    }

    connectSSE();
  </script>
</body>
</html>
        """.trimIndent()
    }

    fun getLocalIpAddress(context: Context): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiIp = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (wifiIp != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    wifiIp and 0xff,
                    wifiIp shr 8 and 0xff,
                    wifiIp shr 16 and 0xff,
                    wifiIp shr 24 and 0xff
                )
            }
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP", e)
        }
        return "127.0.0.1"
    }
}
