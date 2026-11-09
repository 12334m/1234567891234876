package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GuideScreen(modifier: Modifier = Modifier) {
    val appColors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("guide_screen")
    ) {
        Text(
            text = "Easy Setup & Compatibility Guide",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Step-by-step guides for PC SteamVR, Meta Quest, OBS Studio, and Heart Monitors",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Guide 1: PC SteamVR VRChat Setup
        GuideCard(
            title = "1. PC SteamVR Setup (VRChat)",
            subtitle = "Transmit heart rate & chatbox to VRChat on PC",
            icon = Icons.Default.Computer,
            iconTint = NeonCyan,
            steps = listOf(
                "Open VRChat on your PC and open your Action Menu (Radial Menu).",
                "Navigate to Options -> OSC -> Enable (make sure it shows Enabled).",
                "Find your PC's local IP address (open CMD on Windows, type 'ipconfig', look for IPv4 Address e.g. 192.168.1.50).",
                "Ensure your phone and PC are connected to the SAME Wi-Fi network.",
                "In Pulsoid VR -> Setup tab, enter your PC's IP into Target IP (default port 9000).",
                "Tap 'Test OSC Ping' to verify connection! You will see the chatbox message appear above your avatar."
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guide 2: Meta Quest Standalone Setup
        GuideCard(
            title = "2. Meta Quest / Standalone VR Setup",
            subtitle = "Zero PC required · Run directly or via phone",
            icon = Icons.Default.Headset,
            iconTint = NeonGreen,
            steps = listOf(
                "In VRChat on your Quest, open the Action Menu -> Options -> OSC -> Enable.",
                "If running Pulsoid VR on your phone: Connect phone and Quest to the same Wi-Fi, enter your Quest's local IP into Target IP.",
                "If sideloading this APK directly onto Meta Quest: Select 'Quest Local (127.0.0.1)' preset.",
                "VRChat on Quest listens on UDP port 9000. Avatar parameters and Magic Chatbox work identically to PC!"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guide 3: PC OBS / Streamlabs Overlay Setup
        GuideCard(
            title = "3. PC OBS Stream Overlay Setup",
            subtitle = "0% PC CPU load · Free Pulsoid overlay alternative",
            icon = Icons.Default.LiveTv,
            iconTint = NeonCrimson,
            steps = listOf(
                "In Pulsoid VR -> Dashboard, ensure 'PC OBS Stream Overlay' is toggled ON.",
                "Tap the URL box to copy your local overlay link (e.g. http://192.168.1.120:9090/overlay).",
                "Open OBS Studio or Streamlabs Desktop on your PC.",
                "Click '+' under Sources -> Add 'Browser' Source.",
                "Paste the URL into the URL field. Set Width: 420, Height: 160.",
                "Done! The animated neon heart rate widget with real-time pulse and zones will render on your stream with ZERO CPU impact on your PC!"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guide 4: Heart Rate Monitors (Arm & Chest)
        GuideCard(
            title = "4. Connecting Chest & Arm Monitors",
            subtitle = "Supports Polar, Garmin, Wahoo, CooSpo, Whoop, etc.",
            icon = Icons.Default.Favorite,
            iconTint = NeonCrimson,
            steps = listOf(
                "Moisten the electrodes on chest straps (e.g. Polar H10, Garmin HRM-Pro, CooSpo H8) or strap arm monitor snugly (e.g. Polar Verity Sense, Scosche Rhythm+).",
                "Ensure Bluetooth is enabled on your phone.",
                "Go to Pulsoid VR -> Setup tab -> Tap 'Scan for BLE Monitors'.",
                "Tap 'Pair' next to your monitor. Live BPM and RR interval data will immediately start reading.",
                "Tip: You can also use the built-in 'Heart Rate Simulator' anytime to test animations and VRChat sync!"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guide 5: Windows Firewall & Troubleshooting
        GuideCard(
            title = "5. Troubleshooting & Firewall",
            subtitle = "If VRChat is not receiving packets",
            icon = Icons.Default.Security,
            iconTint = NeonYellow,
            steps = listOf(
                "Windows Firewall: Ensure VRChat is allowed on Private networks in Windows Defender Firewall.",
                "Network Type: On Windows, make sure your Wi-Fi network profile is set to 'Private' rather than 'Public'.",
                "OSC Port: Default VRChat receiving port is UDP 9000.",
                "Background Service: Leave Pulsoid VR's ongoing notification active so Android does not put the app to sleep while you are in VR."
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guide 6: Hearted Status Box & Auto-Cycling
        GuideCard(
            title = "6. Hearted Status Box & Auto-Cycling",
            subtitle = "Express your mood and dance status dynamically",
            icon = Icons.Default.Favorite,
            iconTint = NeonCrimson,
            steps = listOf(
                "Select from hearted status presets: 💖 Vibing, 💓 Rave/Dancing, 💘 Horror/Scared, 💗 Cardio, 🤍 Chill, 💔 Dead Inside, 🖤 AFK Sleep.",
                "Tap 'Cycle' to manually switch to your next hearted status with 1 tap.",
                "Toggle 'Auto-Cycle' to automatically rotate through your statuses every 3-20 seconds in VRChat.",
                "Create unlimited custom hearted statuses by tapping '+ New Status', picking your heart emoji, and typing your custom label.",
                "In your Chatbox Custom Template, use '{status}' for the full emoji + text, or '{heart}' for just the heart emoji."
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guide 7: Voice to Text (VR Speech Input)
        GuideCard(
            title = "7. Voice-to-Text (VR Hands-Free Speech)",
            subtitle = "Speak to your VRChat Chatbox without laser-pointer typing",
            icon = Icons.Default.Cast,
            iconTint = NeonCyan,
            steps = listOf(
                "Typing with VR controllers is slow and frustrating. Use Voice-to-Text to speak hands-free!",
                "Tap the Microphone button and grant Audio permission.",
                "Speak clearly into your phone or headset microphone. Live speech recognition transcribes your words in real time.",
                "Tap 'Send to VRChat' to immediately broadcast '[❤️ 85 BPM] Your Message' into your avatar's speech bubble.",
                "Tap 'Set Status' to instantly convert your spoken phrase into your active Hearted Status!"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guide 8: Installing on Device & Windows 11 Desktop
        GuideCard(
            title = "8. Install on Windows 11 Desktop, Phone & Quest",
            subtitle = "Download APK directly to your PC desktop or devices",
            icon = Icons.Default.Computer,
            iconTint = NeonCyan,
            steps = listOf(
                "Windows 11 Desktop (Option A): Open PowerShell and run the 1-click command from the Dashboard or Setup tab to download PulsoidVR.apk directly into your Desktop folder.",
                "Windows 11 Desktop (Option B): Visit 'http://<your-ip>:9090/windows' in Edge/Chrome on your PC to download the APK or the 1-click batch installer script.",
                "Windows Subsystem for Android (WSA): Double-click the downloaded PulsoidVR.apk on your Windows 11 desktop to run it as a native desktop window!",
                "BlueStacks / LDPlayer: Drag and drop PulsoidVR.apk directly into your emulator window.",
                "Physical Android Phone: Export the project from AI Studio, transfer the APK, enable 'Install unknown apps', and tap to install.",
                "Meta Quest: Download the APK and sideload via SideQuest or 'adb install -r PulsoidVR.apk'. Run standalone on Quest with zero PC overhead!"
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GuideCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    steps: List<String>
) {
    var isExpanded by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.border, RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = title, color = appColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = subtitle, color = appColors.textSecondary, fontSize = 11.sp)
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = appColors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    steps.forEachIndexed { idx, step ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(iconTint.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    color = iconTint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step,
                                color = appColors.textPrimary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
