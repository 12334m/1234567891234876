# Pulsoid VR — Windows 11 Desktop Setup & Download Guide

This guide explains how to download, install, and run **Pulsoid VR** on your Windows 11 Desktop PC to stream your real-time heart rate and magic chatbox into **VRChat SteamVR** and **OBS Studio** with 0% PC performance overhead.

---

## 1. Quick Download to Windows 11 Desktop

### Option 1: 1-Click PowerShell Command
Open **PowerShell** on Windows 11 and paste this command to download the APK directly to your Desktop folder:
```powershell
powershell -Command "Invoke-WebRequest -Uri 'http://127.0.0.1:9090/download' -OutFile \"$([Environment]::GetFolderPath('Desktop'))\PulsoidVR.apk\""
```
*(If Pulsoid VR is running on your phone, replace `127.0.0.1` with your phone's Wi-Fi IP address shown in the app).*

### Option 2: Web Browser Download
1. In Google Chrome or Microsoft Edge on your Windows 11 Desktop, open:
   ```
   http://localhost:9090/windows
   ```
   *(or `http://<phone-ip>:9090/windows`)*
2. Click **Download PulsoidVR.apk** or **Download 1-Click Windows 11 Installer (.bat)**.

### Option 3: Export via AI Studio
- In the top-right AI Studio menu, click **Export Project** (ZIP) or **Generate APK**.
- Transfer or save `PulsoidVR.apk` to your Windows 11 Desktop.

---

## 2. Running Pulsoid VR on Windows 11 Desktop

### Method A: Windows Subsystem for Android (WSA) — Native Windows 11 App
1. With WSA enabled on Windows 11, double-click `PulsoidVR.apk` on your Desktop (or use WSA-PacMan / `adb install PulsoidVR.apk`).
2. Pulsoid VR runs in its own window just like a native Windows application.
3. In Pulsoid VR's **Setup** tab, keep Target IP set to `127.0.0.1` (UDP Port `9000`).
4. Real-time heart rate and speech bubbles will stream with 0ms latency to VRChat!

### Method B: BlueStacks / LDPlayer / Nox
1. Open your preferred Android emulator on Windows 11.
2. Drag and drop `PulsoidVR.apk` into the emulator window.
3. Connect your Bluetooth heart rate monitor or start the built-in simulator.

### Method C: Run on Phone / Meta Quest (Zero-Install PC Setup)
1. Keep Pulsoid VR running on your Android phone or Meta Quest headset.
2. In the **Setup** tab, enter your Windows 11 PC's local IP address (find it by running `ipconfig` in Command Prompt, e.g. `192.168.1.50`).
3. Your phone/Quest transmits OSC packets directly over your local Wi-Fi to VRChat on your PC!

---

## 3. Configuring VRChat on Windows 11 PC

1. Launch **VRChat** on PC (SteamVR or Desktop mode).
2. Open your in-game **Action Menu** (Radial Menu) -> **Options** -> **OSC** -> Select **Enable**.
3. VRChat automatically listens on UDP Port `9000`.
4. As soon as Pulsoid VR is running, your heart rate and chatbox avatar bubble will appear above your avatar!

---

## 4. OBS Studio Stream Overlay Setup (0% PC CPU)

1. In OBS Studio on Windows 11, click **+** under **Sources** -> Select **Browser**.
2. Enter the URL:
   ```
   http://localhost:9090/overlay
   ```
   *(or `http://<phone-ip>:9090/overlay`)*
3. Set **Width**: `420`, **Height**: `160`.
4. The animated heart rate widget with real-time zone glow and beat animation will render on your stream.
