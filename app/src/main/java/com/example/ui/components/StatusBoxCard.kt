package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HeartedStatus
import com.example.ui.theme.LocalAppColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusBoxCard(
    currentStatus: HeartedStatus,
    statuses: List<HeartedStatus>,
    isAutoCycle: Boolean,
    cycleIntervalSec: Float,
    onCycleNext: () -> Unit,
    onSelectStatus: (HeartedStatus) -> Unit,
    onToggleAutoCycle: (Boolean, Float) -> Unit,
    onAddCustomStatus: (emoji: String, text: String) -> Unit,
    onDeleteCustomStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    var showAddDialog by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("💖") }

    val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isAutoCycle) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isAutoCycle) 600 else 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, appColors.border, RoundedCornerShape(20.dp))
            .testTag("status_box_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
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
                            .background(appColors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentStatus.emoji,
                            fontSize = 20.sp,
                            modifier = Modifier.scale(pulseScale)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "STATUS BOX",
                            color = appColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "In-game VRChat hearted status & tag",
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Cycle Next Button (1-tap)
                Button(
                    onClick = onCycleNext,
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("cycle_status_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Cycle Next Status",
                        tint = appColors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cycle",
                        color = appColors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Featured Active Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(appColors.background)
                    .border(1.5.dp, appColors.primary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentStatus.emoji,
                        fontSize = 26.sp,
                        modifier = Modifier.scale(pulseScale)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentStatus.text,
                            color = appColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isAutoCycle) "Auto-cycling every ${cycleIntervalSec.toInt()}s" else "Active status in chatbox",
                            color = if (isAutoCycle) appColors.secondary else appColors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isAutoCycle) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(appColors.secondary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AUTO",
                            color = appColors.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-cycle Toggle and Interval Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Auto-Cycle Statuses",
                        color = appColors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Automatically rotates hearted status",
                        color = appColors.textSecondary,
                        fontSize = 10.sp
                    )
                }

                Switch(
                    checked = isAutoCycle,
                    onCheckedChange = { onToggleAutoCycle(it, cycleIntervalSec) },
                    colors = SwitchDefaults.colors(checkedThumbColor = appColors.primary),
                    modifier = Modifier.testTag("auto_cycle_switch")
                )
            }

            if (isAutoCycle) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cycle Speed",
                        color = appColors.textMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${cycleIntervalSec.toInt()}s",
                        color = appColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = cycleIntervalSec,
                    onValueChange = { onToggleAutoCycle(true, it) },
                    valueRange = 3f..20f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = appColors.primary,
                        activeTrackColor = appColors.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status List Flow Pills
            Text(
                text = "SELECT HEARTED STATUS",
                color = appColors.textMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                statuses.forEach { status ->
                    val isSelected = status.id == currentStatus.id
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) appColors.primary.copy(alpha = 0.25f) else appColors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) appColors.primary else appColors.border,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectStatus(status) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = status.emoji, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.text,
                            color = if (isSelected) appColors.primary else appColors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (status.isCustom) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete custom status",
                                tint = appColors.textMuted,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onDeleteCustomStatus(status.id) }
                            )
                        }
                    }
                }

                // Add Custom Status Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(appColors.surfaceVariant)
                        .border(1.dp, appColors.secondary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { showAddDialog = !showAddDialog }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = appColors.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Status",
                        color = appColors.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Inline Add Custom Status Box
            AnimatedVisibility(visible = showAddDialog) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(appColors.background)
                        .border(1.dp, appColors.secondary, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Create Custom Hearted Status",
                        color = appColors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Emoji Selector
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HeartedStatus.HEART_EMOJIS.forEach { emoji ->
                            val isChosen = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isChosen) appColors.primary.copy(alpha = 0.3f) else appColors.surfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isChosen) appColors.primary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            placeholder = { Text("e.g. Speedrunning VR", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_status_text_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = appColors.secondary,
                                unfocusedBorderColor = appColors.border,
                                focusedTextColor = appColors.textPrimary,
                                unfocusedTextColor = appColors.textPrimary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (customText.isNotBlank()) {
                                    onAddCustomStatus(selectedEmoji, customText)
                                    customText = ""
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_custom_status_button")
                        ) {
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
