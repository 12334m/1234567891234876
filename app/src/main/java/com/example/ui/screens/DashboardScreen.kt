package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble.ConnectionStatus
import com.example.ui.MainViewModel
import com.example.ui.components.ChatboxPreview
import com.example.ui.components.HeartRateGauge
import com.example.ui.components.StatusBoxCard
import com.example.ui.components.VoiceToTextCard
import com.example.ui.components.ZoneDistributionBar
import com.example.ui.theme.DarkThemeMode
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonGreen

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentHr by viewModel.currentHeartRate.collectAsState()
    val activeStats by viewModel.activeStats.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val connectedDeviceName by viewModel.connectedDeviceName.collectAsState()
    val isSimRunning by viewModel.isSimulatorRunning.collectAsState()
    val targetIp by viewModel.targetIp.collectAsState()
    val isOscBroadcasting by viewModel.isOscBroadcasting.collectAsState()
    val packetsSent by viewModel.oscPacketsSent.collectAsState()
    val chatboxFormattedText by viewModel.chatboxFormattedText.collectAsState()
    val chatboxConfig by viewModel.chatboxConfig.collectAsState()
    val localIp by viewModel.localIpAddress.collectAsState()
    val isOverlayRunning by viewModel.isOverlayServerRunning.collectAsState()
    val themeMode by viewModel.darkThemeMode.collectAsState()

    // Hearted status & Voice state
    val currentStatus by viewModel.currentStatus.collectAsState()
    val heartedStatuses by viewModel.heartedStatuses.collectAsState()
    val isAutoCycle by viewModel.isAutoCycleEnabled.collectAsState()
    val cycleIntervalSec by viewModel.cycleIntervalSeconds.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()

    val appColors = LocalAppColors.current
    val overlayUrl = "http://$localIp:9090/overlay"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("dashboard_screen")
    ) {
        // OLED Battery & VR Eye-Care Mode Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(appColors.cardSurface)
                .border(1.dp, appColors.border, RoundedCornerShape(12.dp))
                .clickable { viewModel.cycleDarkThemeMode() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.BatterySaver,
                    contentDescription = null,
                    tint = if (themeMode == DarkThemeMode.OLED_BLACK) Color(0xFF10B981) else appColors.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${themeMode.title} · ${themeMode.powerBadge}",
                        color = appColors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (themeMode == DarkThemeMode.OLED_BLACK) "0% emission on dark pixels · Zero VR lens glare" else "Tap to switch to OLED Pure Black",
                        color = appColors.textMuted,
                        fontSize = 9.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(appColors.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Switch",
                    color = appColors.secondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Status Header Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // BLE / Simulator Sensor Chip
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(appColors.cardSurface)
                    .border(1.dp, appColors.border, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToSetup() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSimRunning) Icons.Default.Sensors else Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = if (connectionStatus == ConnectionStatus.CONNECTED || isSimRunning) NeonGreen else appColors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (isSimRunning) "Simulator" else connectedDeviceName ?: "No Monitor",
                        color = appColors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = if (isSimRunning) "Sim Active" else connectionStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (isSimRunning || connectionStatus == ConnectionStatus.CONNECTED) NeonGreen else appColors.textMuted,
                        fontSize = 9.sp
                    )
                }
            }

            // VRChat OSC Status Chip
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(appColors.cardSurface)
                    .border(1.dp, appColors.border, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToSetup() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = null,
                    tint = if (isOscBroadcasting && currentHr.bpm > 0) appColors.secondary else appColors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "VRChat OSC",
                        color = appColors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$targetIp:9000",
                        color = appColors.secondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Heart Rate Gauge & ECG
        HeartRateGauge(
            data = currentHr,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hearted Status Box (Display, 1-tap manual cycle, auto-cycle, custom status)
        StatusBoxCard(
            currentStatus = currentStatus,
            statuses = heartedStatuses,
            isAutoCycle = isAutoCycle,
            cycleIntervalSec = cycleIntervalSec,
            onCycleNext = { viewModel.cycleNextStatus() },
            onSelectStatus = { viewModel.selectStatus(it) },
            onToggleAutoCycle = { enabled, sec -> viewModel.setAutoCycle(enabled, sec) },
            onAddCustomStatus = { emoji, text -> viewModel.addCustomStatus(emoji, text) },
            onDeleteCustomStatus = { viewModel.deleteCustomStatus(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hands-free Voice to Text (VR Speech Input)
        VoiceToTextCard(
            voiceState = voiceState,
            onStartListening = { viewModel.startVoiceRecognition() },
            onStopListening = { viewModel.stopVoiceRecognition() },
            onClearText = { viewModel.clearVoiceText() },
            onSendToChatbox = { viewModel.sendVoiceTextToChatbox(it) },
            onSetAsStatus = { viewModel.setVoiceAsCurrentStatus(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Session Recording & Quick Controls Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, appColors.border, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activeStats.isRecording) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(appColors.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (activeStats.isRecording) "RECORDING SESSION" else "SESSION RECORDER",
                                color = if (activeStats.isRecording) appColors.primary else appColors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = if (activeStats.isRecording) {
                                val mins = activeStats.durationSeconds / 60
                                val secs = activeStats.durationSeconds % 60
                                String.format("%02d:%02d elapsed", mins, secs)
                            } else "Track workout & VR heart analytics",
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (activeStats.isRecording) {
                                viewModel.stopSession()
                                Toast.makeText(context, "Session saved to History!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.startSession("VRChat Session")
                                Toast.makeText(context, "Session recording started!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeStats.isRecording) Color(0xFF7F1D1D) else appColors.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("record_session_button")
                    ) {
                        Icon(
                            imageVector = if (activeStats.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeStats.isRecording) "Stop" else "Record",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Metric Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricTile(
                        label = "AVG BPM",
                        value = if (activeStats.avgBpm > 0) activeStats.avgBpm.toString() else "--",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "MAX BPM",
                        value = if (activeStats.maxBpm > 0) activeStats.maxBpm.toString() else "--",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "MIN BPM",
                        value = if (activeStats.minBpm > 0) activeStats.minBpm.toString() else "--",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "CALORIES",
                        value = "${activeStats.calories} kcal",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Heart Rate Zone Distribution
                ZoneDistributionBar(
                    z1Sec = activeStats.z1Sec,
                    z2Sec = activeStats.z2Sec,
                    z3Sec = activeStats.z3Sec,
                    z4Sec = activeStats.z4Sec,
                    z5Sec = activeStats.z5Sec
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // In-game Chatbox Preview
        ChatboxPreview(
            formattedText = chatboxFormattedText,
            isEnabled = chatboxConfig.enabled && isOscBroadcasting,
            targetIp = targetIp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // OBS Stream Overlay Card (Free Pulsoid alternative with 0% PC CPU)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, appColors.border, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PC OBS Stream Overlay",
                            color = appColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Zero PC load · Add as OBS Browser Source",
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isOverlayRunning,
                        onCheckedChange = { viewModel.toggleOverlayServer() },
                        colors = SwitchDefaults.colors(checkedThumbColor = appColors.secondary)
                    )
                }

                if (isOverlayRunning) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(appColors.background)
                            .border(1.dp, appColors.border, RoundedCornerShape(10.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("OBS Overlay URL", overlayUrl))
                                Toast.makeText(context, "Copied OBS URL to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = overlayUrl,
                            color = appColors.secondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy URL",
                            tint = appColors.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Windows 11 Desktop Companion & APK Download Card
        val windowsHubUrl = "http://$localIp:9090/windows"
        val psDownloadCmd = "powershell -Command \"Invoke-WebRequest -Uri 'http://$localIp:9090/download' -OutFile \\\"$([Environment]::GetFolderPath('Desktop'))\\PulsoidVR.apk\\\"\""

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, appColors.secondary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(appColors.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = appColors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Windows 11 Desktop Download",
                                color = appColors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Download APK directly to PC · WSA & BlueStacks",
                                color = appColors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(appColors.secondary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "WIN 11",
                            color = appColors.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Open in Chrome or Edge on Windows 11 to download the APK or 1-click batch installer:",
                    color = appColors.textMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(appColors.background)
                        .border(1.dp, appColors.border, RoundedCornerShape(10.dp))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Windows Hub URL", windowsHubUrl))
                            Toast.makeText(context, "Copied Windows 11 Hub URL!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = windowsHubUrl,
                        color = appColors.secondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Windows Link",
                        tint = appColors.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("PowerShell Command", psDownloadCmd))
                        Toast.makeText(context, "Copied PowerShell download command!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = appColors.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy 1-Click PowerShell Desktop Download",
                        color = appColors.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(appColors.background)
            .border(1.dp, appColors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = appColors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = appColors.textMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
