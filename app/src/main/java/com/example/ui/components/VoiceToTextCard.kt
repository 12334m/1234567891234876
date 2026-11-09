package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.LocalAppColors
import com.example.voice.VoiceState

@Composable
fun VoiceToTextCard(
    voiceState: VoiceState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onClearText: () -> Unit,
    onSendToChatbox: (String) -> Unit,
    onSetAsStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            onStartListening()
        } else {
            Toast.makeText(context, "Microphone permission is needed for Voice-to-Text", Toast.LENGTH_SHORT).show()
        }
    }

    var manualTextInput by remember(voiceState.text) { mutableStateOf(voiceState.text) }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_waves")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, appColors.border, RoundedCornerShape(20.dp))
            .testTag("voice_to_text_card")
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
                            .background(appColors.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = appColors.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "VOICE TO TEXT (VR SPEECH)",
                            color = appColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Hands-free speech to VRChat chatbox & status",
                            color = appColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (voiceState.isListening) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(appColors.primary.copy(alpha = 0.2f))
                            .border(1.dp, appColors.primary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LISTENING...",
                            color = appColors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Mic Button with animated audio pulse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (voiceState.isListening) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .scale(waveScale)
                                .clip(CircleShape)
                                .background(appColors.primary.copy(alpha = 0.25f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (voiceState.isListening) appColors.primary else appColors.surfaceVariant)
                            .border(
                                2.dp,
                                if (voiceState.isListening) appColors.primary else appColors.secondary,
                                CircleShape
                            )
                            .clickable {
                                if (voiceState.isListening) {
                                    onStopListening()
                                } else {
                                    if (hasPermission) {
                                        onStartListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("mic_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (voiceState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (voiceState.isListening) "Stop Listening" else "Start Listening",
                            tint = if (voiceState.isListening) Color.White else appColors.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Text(
                text = if (voiceState.isListening) "Tap to finish listening" else "Tap microphone to speak",
                color = appColors.textMuted,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (voiceState.errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = voiceState.errorMessage,
                    color = Color(0xFFEF4444),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live speech transcription display / editor
            val displaySpeech = if (voiceState.isListening && voiceState.partialText.isNotBlank()) {
                voiceState.partialText
            } else {
                manualTextInput
            }

            OutlinedTextField(
                value = displaySpeech,
                onValueChange = {
                    manualTextInput = it
                },
                placeholder = {
                    Text(
                        text = if (voiceState.isListening) "Listening to your voice..." else "Spoken text appears here, or type manually...",
                        fontSize = 12.sp,
                        color = appColors.textMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_transcript_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.secondary,
                    unfocusedBorderColor = appColors.border,
                    focusedTextColor = appColors.textPrimary,
                    unfocusedTextColor = appColors.textPrimary
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Send to VRChat Chatbox, Set as Status, Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Send to Chatbox Button
                Button(
                    onClick = {
                        val textToSend = if (manualTextInput.isNotBlank()) manualTextInput else voiceState.partialText
                        if (textToSend.isNotBlank()) {
                            onSendToChatbox(textToSend)
                            Toast.makeText(context, "Spoken message sent to VRChat!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("send_voice_to_vrchat_button"),
                    enabled = (manualTextInput.isNotBlank() || voiceState.partialText.isNotBlank())
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Send to VRChat",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Set as Status Button
                Button(
                    onClick = {
                        val statusText = if (manualTextInput.isNotBlank()) manualTextInput else voiceState.partialText
                        if (statusText.isNotBlank()) {
                            onSetAsStatus(statusText)
                            Toast.makeText(context, "Updated Hearted Status Box!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("set_voice_as_status_button"),
                    enabled = (manualTextInput.isNotBlank() || voiceState.partialText.isNotBlank())
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = appColors.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Set Status",
                        color = appColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Clear Button
                Button(
                    onClick = {
                        manualTextInput = ""
                        onClearText()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.cardSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, appColors.border, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear text",
                        tint = appColors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
