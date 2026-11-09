package com.example.model

/**
 * Real-time heart rate measurement from BLE sensor or simulator.
 */
data class HeartRateData(
    val bpm: Int = 0,
    val rrIntervalMs: Int? = null,
    val isBeat: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Heart Rate Zone based on user's max heart rate (default 190).
     */
    fun getZone(maxHr: Int = 190): HeartRateZone {
        val pct = if (maxHr > 0) (bpm.toFloat() / maxHr.toFloat()) * 100f else 0f
        return when {
            bpm <= 0 || pct < 50f -> HeartRateZone.REST
            pct < 60f -> HeartRateZone.ZONE_1 // Warm Up / Recovery
            pct < 70f -> HeartRateZone.ZONE_2 // Fat Burn / Light
            pct < 80f -> HeartRateZone.ZONE_3 // Aerobic / Moderate
            pct < 90f -> HeartRateZone.ZONE_4 // Anaerobic / Threshold
            else -> HeartRateZone.ZONE_5 // Peak / Maximum
        }
    }
}

enum class HeartRateZone(
    val label: String,
    val shortName: String,
    val minPercent: Int,
    val maxPercent: Int,
    val colorHex: Long
) {
    REST("Resting", "Rest", 0, 50, 0xFF9E9E9E),
    ZONE_1("Warm Up", "Z1", 50, 60, 0xFF9E9E9E),
    ZONE_2("Fat Burn", "Z2", 60, 70, 0xFF4ADE80),
    ZONE_3("Aerobic", "Z3", 70, 80, 0xFFFACC15),
    ZONE_4("Anaerobic", "Z4", 80, 90, 0xFFFB923C),
    ZONE_5("Peak", "Z5", 90, 100, 0xFFF43F5E)
}
