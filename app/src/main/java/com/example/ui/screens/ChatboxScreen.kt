package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.chatbox.ChatboxConfig
import com.example.ui.MainViewModel
import com.example.ui.components.ChatboxPreview
import com.example.ui.components.StatusBoxCard
import com.example.ui.components.VoiceToTextCard
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonCyan

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatboxScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.chatboxConfig.collectAsState()
    val formattedText by viewModel.chatboxFormattedText.collectAsState()
    val targetIp by viewModel.targetIp.collectAsState()

    val currentStatus by viewModel.currentStatus.collectAsState()
    val heartedStatuses by viewModel.heartedStatuses.collectAsState()
    val isAutoCycle by viewModel.isAutoCycleEnabled.collectAsState()
    val cycleIntervalSec by viewModel.cycleIntervalSeconds.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()

    val appColors = LocalAppColors.current

    var customTemplateInput by remember(config.customTemplate) { mutableStateOf(config.customTemplate) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("chatbox_screen")
    ) {
        // Master Enable Toggle
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, appColors.border, RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(appColors.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = appColors.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Magic Chatbox",
                            color = appColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time avatar text bubble in VRChat",
                            color = appColors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = config.enabled,
                    onCheckedChange = {
                        viewModel.updateChatboxConfig(config.copy(enabled = it))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = appColors.secondary),
                    modifier = Modifier.testTag("chatbox_master_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live In-Game Preview
        ChatboxPreview(
            formattedText = formattedText,
            isEnabled = config.enabled,
            targetIp = targetIp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Instant Test Button
        Button(
            onClick = {
                viewModel.sendTestPing()
                Toast.makeText(context, "Test packet sent to $targetIp:9000", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2640)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("send_test_chatbox_button")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Instant Test Message to VRChat", color = NeonCyan, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Box & Cycling Card
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

        // Voice to Text (VR Speech Input) Card
        VoiceToTextCard(
            voiceState = voiceState,
            onStartListening = { viewModel.startVoiceRecognition() },
            onStopListening = { viewModel.stopVoiceRecognition() },
            onClearText = { viewModel.clearVoiceText() },
            onSendToChatbox = { viewModel.sendVoiceTextToChatbox(it) },
            onSetAsStatus = { viewModel.setVoiceAsCurrentStatus(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Presets Selector
        Text(
            text = "MESSAGE PRESETS",
            color = appColors.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChatboxConfig.PRESETS.forEachIndexed { index, (name, template) ->
                val isSelected = config.selectedPresetIndex == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) appColors.surfaceVariant else appColors.cardSurface)
                        .border(
                            1.dp,
                            if (isSelected) appColors.secondary else appColors.border,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            viewModel.updateChatboxConfig(config.copy(selectedPresetIndex = index))
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            color = if (isSelected) appColors.secondary else appColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = template,
                            color = appColors.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = appColors.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Template Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, appColors.border, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Custom Template",
                    color = appColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customTemplateInput,
                    onValueChange = {
                        customTemplateInput = it
                        viewModel.updateChatboxConfig(
                            config.copy(
                                selectedPresetIndex = -1, // custom mode
                                customTemplate = it
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_template_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.secondary,
                        unfocusedBorderColor = appColors.border,
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Tap tag to insert:",
                    color = appColors.textMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tags = listOf("{bpm}", "{status}", "{heart}", "{zone}", "{bar}", "{max}", "{avg}", "{icon}")
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(appColors.surfaceVariant)
                                .clickable {
                                    customTemplateInput += " $tag"
                                    viewModel.updateChatboxConfig(
                                        config.copy(
                                            selectedPresetIndex = -1,
                                            customTemplate = customTemplateInput
                                        )
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = tag, color = appColors.secondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Update Interval & Sound Notification
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, appColors.border, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Broadcast Rate",
                            color = appColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Every ${String.format("%.1f", config.intervalSeconds)}s (VRChat allows >= 1.5s)",
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", config.intervalSeconds)}s",
                        color = appColors.secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = config.intervalSeconds,
                    onValueChange = {
                        viewModel.updateChatboxConfig(config.copy(intervalSeconds = it))
                    },
                    valueRange = 1.5f..5.0f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = appColors.secondary,
                        activeTrackColor = appColors.secondary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "In-Game Chime Sound",
                            color = appColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Play VRChat audio notification on update",
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = config.playSound,
                        onCheckedChange = {
                            viewModel.updateChatboxConfig(config.copy(playSound = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = appColors.secondary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
