package com.example.chatbox

import com.example.model.HeartRateData
import com.example.model.HeartRateZone
import com.example.model.HeartedStatus

data class ChatboxConfig(
    val enabled: Boolean = true,
    val selectedPresetIndex: Int = 1,
    val customTemplate: String = "❤️ {bpm} BPM [{bar}] {status}",
    val intervalSeconds: Float = 2.0f,
    val playSound: Boolean = false,
    val showMaxAvg: Boolean = true
) {
    companion object {
        val PRESETS = listOf(
            "Hearted Status" to "{heart} {bpm} BPM · {status}",
            "Zone Progress Bar" to "❤️ {bpm} BPM [{bar}] {zone}",
            "Minimal Heart" to "❤️ {bpm} BPM",
            "Gamer Stats HUD" to "[HR: {bpm}] {status} Max:{max} Avg:{avg} 🔥",
            "Cyber Rave Pulse" to "✨ {icon} {bpm} BPM | {status} ✨",
            "Horror / Thriller" to "⚠️ HEART RATE: {bpm} BPM ⚠️ {status}"
        )
    }
}

object MagicChatboxFormatter {

    private var beatToggle = false

    fun formatMessage(
        hrData: HeartRateData,
        config: ChatboxConfig,
        sessionMaxBpm: Int = 0,
        sessionAvgBpm: Int = 0,
        heartedStatus: HeartedStatus? = null
    ): String {
        beatToggle = !beatToggle
        val bpm = hrData.bpm
        val zone = hrData.getZone()

        val template = if (config.selectedPresetIndex in ChatboxConfig.PRESETS.indices) {
            ChatboxConfig.PRESETS[config.selectedPresetIndex].second
        } else {
            config.customTemplate
        }

        val bar = generateProgressBar(bpm, zone)
        val icon = if (beatToggle) "❤️" else "💓"
        val statusText = heartedStatus?.displayFormatted ?: when (zone) {
            HeartRateZone.REST -> "Resting"
            HeartRateZone.ZONE_1 -> "Warmup"
            HeartRateZone.ZONE_2 -> "Active"
            HeartRateZone.ZONE_3 -> "Aerobic"
            HeartRateZone.ZONE_4 -> "Anaerobic"
            HeartRateZone.ZONE_5 -> "Spike Max!"
        }
        val heartEmoji = heartedStatus?.emoji ?: icon

        return template
            .replace("{bpm}", bpm.toString())
            .replace("{zone}", zone.shortName + " " + zone.label)
            .replace("{bar}", bar)
            .replace("{max}", (if (sessionMaxBpm > 0) sessionMaxBpm else bpm).toString())
            .replace("{avg}", (if (sessionAvgBpm > 0) sessionAvgBpm else bpm).toString())
            .replace("{icon}", icon)
            .replace("{heart}", heartEmoji)
            .replace("{status}", statusText)
    }

    private fun generateProgressBar(bpm: Int, zone: HeartRateZone): String {
        val totalBlocks = 6
        val filled = when (zone) {
            HeartRateZone.REST -> 1
            HeartRateZone.ZONE_1 -> 2
            HeartRateZone.ZONE_2 -> 3
            HeartRateZone.ZONE_3 -> 4
            HeartRateZone.ZONE_4 -> 5
            HeartRateZone.ZONE_5 -> 6
        }
        val sb = StringBuilder()
        for (i in 1..totalBlocks) {
            if (i <= filled) sb.append("■") else sb.append("□")
        }
        return sb.toString()
    }
}
