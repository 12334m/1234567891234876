package com.example.ble

import com.example.model.HeartRateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class SimulationMode(val label: String, val baseBpm: Int) {
    RESTING("Resting / Chill", 68),
    ACTIVE("Active / Dance", 132),
    HORROR_SPIKE("Horror / Jump Scare", 168),
    CUSTOM("Custom BPM Slider", 90)
}

class SimulatorManager {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var simulationJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _currentMode = MutableStateFlow(SimulationMode.RESTING)
    val currentMode: StateFlow<SimulationMode> = _currentMode.asStateFlow()

    private val _customTargetBpm = MutableStateFlow(90)
    val customTargetBpm: StateFlow<Int> = _customTargetBpm.asStateFlow()

    private val _heartRateFlow = MutableSharedFlow<HeartRateData>(extraBufferCapacity = 64)
    val heartRateFlow: SharedFlow<HeartRateData> = _heartRateFlow.asSharedFlow()

    private var currentBpm = 68
    private var beatToggle = false

    fun start(mode: SimulationMode = _currentMode.value) {
        _currentMode.value = mode
        _isRunning.value = true
        simulationJob?.cancel()
        simulationJob = scope.launch {
            var step = 0
            while (isActive) {
                step++
                val target = when (_currentMode.value) {
                    SimulationMode.RESTING -> 68 + Random.nextInt(-3, 4)
                    SimulationMode.ACTIVE -> 132 + Random.nextInt(-5, 6)
                    SimulationMode.HORROR_SPIKE -> {
                        // Cycle: 8 beats normal, spikes to 175 for 10 beats, then cools down
                        val cyclePos = step % 30
                        if (cyclePos in 8..18) {
                            172 + Random.nextInt(-4, 5)
                        } else if (cyclePos in 19..25) {
                            140 + Random.nextInt(-6, 6)
                        } else {
                            75 + Random.nextInt(-4, 4)
                        }
                    }
                    SimulationMode.CUSTOM -> _customTargetBpm.value + Random.nextInt(-2, 3)
                }

                // Smooth transition towards target
                if (currentBpm < target) currentBpm++
                else if (currentBpm > target) currentBpm--

                val rrInterval = (60_000f / currentBpm.toFloat()).toInt()
                beatToggle = !beatToggle

                val data = HeartRateData(
                    bpm = currentBpm,
                    rrIntervalMs = rrInterval,
                    isBeat = beatToggle,
                    timestamp = System.currentTimeMillis()
                )
                _heartRateFlow.emit(data)

                delay(rrInterval.toLong().coerceIn(250L, 2000L))
            }
        }
    }

    fun stop() {
        _isRunning.value = false
        simulationJob?.cancel()
        simulationJob = null
    }

    fun setMode(mode: SimulationMode) {
        _currentMode.value = mode
        if (_isRunning.value) {
            start(mode)
        }
    }

    fun setCustomBpm(bpm: Int) {
        _customTargetBpm.value = bpm.coerceIn(40, 220)
        if (_currentMode.value == SimulationMode.CUSTOM && !_isRunning.value) {
            currentBpm = _customTargetBpm.value
        }
    }
}
