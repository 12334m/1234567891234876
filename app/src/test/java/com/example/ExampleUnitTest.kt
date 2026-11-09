package com.example

import com.example.chatbox.ChatboxConfig
import com.example.chatbox.MagicChatboxFormatter
import com.example.model.HeartRateData
import com.example.model.HeartRateZone
import com.example.model.HeartedStatus
import com.example.osc.OscPacket
import com.example.voice.VoiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHeartRateZones() {
        val rest = HeartRateData(bpm = 50).getZone(190)
        assertEquals(HeartRateZone.REST, rest)

        val z1 = HeartRateData(bpm = 105).getZone(190)
        assertEquals(HeartRateZone.ZONE_1, z1)

        val z3 = HeartRateData(bpm = 145).getZone(190)
        assertEquals(HeartRateZone.ZONE_3, z3)

        val z5 = HeartRateData(bpm = 180).getZone(190)
        assertEquals(HeartRateZone.ZONE_5, z5)
    }

    @Test
    fun testOscPacketEncoding() {
        val packet = OscPacket.buildMessage("/avatar/parameters/Heartrate", 120)
        assertNotNull(packet)
        assertTrue(packet.isNotEmpty())

        val packetStr = String(packet, Charsets.ISO_8859_1)
        assertTrue(packetStr.contains("/avatar/parameters/Heartrate"))
        assertTrue(packetStr.contains(",i"))
    }

    @Test
    fun testChatboxFormatting() {
        val data = HeartRateData(bpm = 135)
        val config = ChatboxConfig(
            selectedPresetIndex = 2 // Minimal: "❤️ {bpm} BPM"
        )
        val formatted = MagicChatboxFormatter.formatMessage(data, config)
        assertTrue(formatted.contains("135 BPM"))
    }

    @Test
    fun testHeartedStatusPresetsAndFormatting() {
        val presets = HeartedStatus.DEFAULT_PRESETS
        assertTrue(presets.isNotEmpty())
        assertEquals("💖", presets.first().emoji)
        assertEquals("Vibing", presets.first().text)

        val data = HeartRateData(bpm = 128)
        val status = HeartedStatus("rave", "💓", "Rave Mode")
        val config = ChatboxConfig(
            selectedPresetIndex = 0 // "{heart} {bpm} BPM · {status}"
        )
        val formatted = MagicChatboxFormatter.formatMessage(
            hrData = data,
            config = config,
            heartedStatus = status
        )
        assertTrue(formatted.contains("💓 128 BPM · 💓 Rave Mode"))
    }

    @Test
    fun testVoiceState() {
        val state = VoiceState(isListening = true, text = "Hello VRChat", rmsDb = 5.5f)
        assertTrue(state.isListening)
        assertEquals("Hello VRChat", state.text)
        assertEquals(5.5f, state.rmsDb, 0.01f)
    }

    @Test
    fun testDarkThemeModes() {
        val oled = com.example.ui.theme.DarkThemeMode.OLED_BLACK
        assertEquals("OLED Pure Black", oled.title)
        assertTrue(com.example.ui.theme.OledBlackColors.isPureBlack)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF000000), com.example.ui.theme.OledBlackColors.background)
    }
}
