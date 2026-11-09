package com.example.model

data class HeartedStatus(
    val id: String,
    val emoji: String,
    val text: String,
    val isCustom: Boolean = false
) {
    val displayFormatted: String
        get() = "$emoji $text"

    companion object {
        val DEFAULT_PRESETS = listOf(
            HeartedStatus(id = "vibing", emoji = "💖", text = "Vibing"),
            HeartedStatus(id = "dancing", emoji = "💓", text = "Rave / Dancing"),
            HeartedStatus(id = "scared", emoji = "💘", text = "Horror / Scared"),
            HeartedStatus(id = "cardio", emoji = "💗", text = "Cardio Rush"),
            HeartedStatus(id = "chill", emoji = "🤍", text = "Chill & Chat"),
            HeartedStatus(id = "exhausted", emoji = "💔", text = "Exhausted / Dead"),
            HeartedStatus(id = "sleep", emoji = "🖤", text = "VR Sleeping / AFK"),
            HeartedStatus(id = "gaming", emoji = "💜", text = "Sweating in Ranked"),
            HeartedStatus(id = "hyped", emoji = "💛", text = "Hype Train")
        )

        val HEART_EMOJIS = listOf("💖", "💓", "💘", "💗", "🤍", "💔", "🖤", "💜", "💛", "💙", "💚", "❤️", "🔥", "✨")
    }
}
