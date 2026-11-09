package com.example.data

import com.example.model.HeartRateData
import com.example.model.HeartRateZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ActiveSessionStats(
    val title: String = "VRChat Session",
    val isRecording: Boolean = false,
    val durationSeconds: Long = 0,
    val currentBpm: Int = 0,
    val avgBpm: Int = 0,
    val maxBpm: Int = 0,
    val minBpm: Int = 0,
    val totalBeats: Int = 0,
    val calories: Int = 0,
    val z1Sec: Long = 0,
    val z2Sec: Long = 0,
    val z3Sec: Long = 0,
    val z4Sec: Long = 0,
    val z5Sec: Long = 0
)

class SessionRepository(private val sessionDao: SessionDao) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    private val _activeStats = MutableStateFlow(ActiveSessionStats())
    val activeStats: StateFlow<ActiveSessionStats> = _activeStats.asStateFlow()

    private val samples = mutableListOf<Int>()
    private var lastSampleTime = 0L

    val allSessions: Flow<List<HeartRateSession>> = sessionDao.getAllSessions()

    fun startRecording(title: String = "VRChat Session") {
        samples.clear()
        _activeStats.value = ActiveSessionStats(
            title = title,
            isRecording = true,
            durationSeconds = 0,
            minBpm = 0,
            maxBpm = 0,
            avgBpm = 0,
            totalBeats = 0,
            calories = 0
        )
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _activeStats.value.isRecording) {
                delay(1000L)
                val current = _activeStats.value
                val newDur = current.durationSeconds + 1
                val bpm = current.currentBpm
                val zone = if (bpm > 0) HeartRateData(bpm).getZone() else HeartRateZone.REST

                // Approximate calories burned (standard formula for moderate activity)
                val addedCals = if (bpm > 60) (bpm * 0.015f / 60f) else 0f
                val newCals = (current.calories + addedCals).toInt()

                _activeStats.value = current.copy(
                    durationSeconds = newDur,
                    calories = newCals,
                    z1Sec = current.z1Sec + if (zone == HeartRateZone.ZONE_1) 1 else 0,
                    z2Sec = current.z2Sec + if (zone == HeartRateZone.ZONE_2) 1 else 0,
                    z3Sec = current.z3Sec + if (zone == HeartRateZone.ZONE_3) 1 else 0,
                    z4Sec = current.z4Sec + if (zone == HeartRateZone.ZONE_4) 1 else 0,
                    z5Sec = current.z5Sec + if (zone == HeartRateZone.ZONE_5) 1 else 0
                )
            }
        }
    }

    fun onHeartRateTick(data: HeartRateData) {
        val bpm = data.bpm
        if (bpm <= 0) return

        val now = System.currentTimeMillis()
        if (now - lastSampleTime >= 4000L) { // Sample every 4s for chart
            samples.add(bpm)
            lastSampleTime = now
        }

        val current = _activeStats.value
        val newMin = if (current.minBpm == 0) bpm else minOf(current.minBpm, bpm)
        val newMax = maxOf(current.maxBpm, bpm)
        val newTotalBeats = current.totalBeats + 1
        val newAvg = if (samples.isNotEmpty()) samples.sum() / samples.size else bpm

        _activeStats.value = current.copy(
            currentBpm = bpm,
            minBpm = newMin,
            maxBpm = newMax,
            avgBpm = newAvg,
            totalBeats = newTotalBeats
        )
    }

    suspend fun stopAndSaveRecording(): HeartRateSession? = withContext(Dispatchers.IO) {
        timerJob?.cancel()
        timerJob = null
        val current = _activeStats.value
        if (!current.isRecording || current.durationSeconds < 2) {
            _activeStats.value = _activeStats.value.copy(isRecording = false)
            return@withContext null
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (current.durationSeconds * 1000L)
        val samplesCsv = samples.takeLast(100).joinToString(",")

        val session = HeartRateSession(
            title = current.title,
            startTime = startTime,
            endTime = endTime,
            durationSeconds = current.durationSeconds,
            avgBpm = if (current.avgBpm > 0) current.avgBpm else current.currentBpm,
            maxBpm = if (current.maxBpm > 0) current.maxBpm else current.currentBpm,
            minBpm = if (current.minBpm > 0) current.minBpm else current.currentBpm,
            totalBeats = current.totalBeats,
            estimatedCalories = current.calories,
            z1Seconds = current.z1Sec,
            z2Seconds = current.z2Sec,
            z3Seconds = current.z3Sec,
            z4Seconds = current.z4Sec,
            z5Seconds = current.z5Sec,
            samplesCsv = samplesCsv
        )

        val id = sessionDao.insertSession(session)
        _activeStats.value = _activeStats.value.copy(isRecording = false)
        session.copy(id = id)
    }

    suspend fun deleteSession(session: HeartRateSession) = withContext(Dispatchers.IO) {
        sessionDao.deleteSession(session)
    }

    suspend fun seedSampleSessionsIfEmpty() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dummy1 = HeartRateSession(
            title = "VRChat Horror Night (Spookality)",
            startTime = now - 86400000L,
            endTime = now - 86400000L + 3600000L,
            durationSeconds = 3600L,
            avgBpm = 118,
            maxBpm = 168,
            minBpm = 68,
            totalBeats = 7080,
            estimatedCalories = 420,
            z1Seconds = 600,
            z2Seconds = 1200,
            z3Seconds = 1000,
            z4Seconds = 500,
            z5Seconds = 300,
            samplesCsv = "72,75,80,95,115,142,168,155,130,110,88,82,90,120,158,162,140,112,98,85"
        )
        val dummy2 = HeartRateSession(
            title = "VR Dance Club Rave Session",
            startTime = now - 172800000L,
            endTime = now - 172800000L + 5400000L,
            durationSeconds = 5400L,
            avgBpm = 138,
            maxBpm = 175,
            minBpm = 85,
            totalBeats = 12420,
            estimatedCalories = 680,
            z1Seconds = 300,
            z2Seconds = 900,
            z3Seconds = 2400,
            z4Seconds = 1500,
            z5Seconds = 300,
            samplesCsv = "85,92,108,125,138,145,152,165,172,175,160,148,140,135,142,150,158,162,145,130"
        )
        sessionDao.insertSession(dummy1)
        sessionDao.insertSession(dummy2)
    }
}
