package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "hr_sessions")
data class HeartRateSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val avgBpm: Int,
    val maxBpm: Int,
    val minBpm: Int,
    val totalBeats: Int,
    val estimatedCalories: Int,
    val z1Seconds: Long = 0,
    val z2Seconds: Long = 0,
    val z3Seconds: Long = 0,
    val z4Seconds: Long = 0,
    val z5Seconds: Long = 0,
    val samplesCsv: String = "" // Sampled BPM values for chart plotting
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
        return sdf.format(Date(startTime))
    }

    fun getFormattedDuration(): String {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        return if (mins >= 60) {
            val hours = mins / 60
            val remMins = mins % 60
            "${hours}h ${remMins}m ${secs}s"
        } else {
            "${mins}m ${secs}s"
        }
    }

    fun parseSamples(): List<Int> {
        if (samplesCsv.isBlank()) return emptyList()
        return samplesCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
