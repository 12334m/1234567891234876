package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PulsoidApp
import com.example.ble.ConnectionStatus
import com.example.ble.DiscoveredDevice
import com.example.ble.SimulationMode
import com.example.chatbox.ChatboxConfig
import com.example.chatbox.MagicChatboxFormatter
import com.example.data.ActiveSessionStats
import com.example.data.HeartRateSession
import com.example.model.HeartRateData
import com.example.model.HeartedStatus
import com.example.service.PulsoidForegroundService
import com.example.ui.theme.DarkThemeMode
import com.example.voice.VoiceState
import com.example.voice.VoiceToTextManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PulsoidApp
    private val bleManager = app.bleManager
    private val simulator = app.simulatorManager
    private val osc = app.oscSender
    private val overlay = app.overlayServer
    private val sessionRepo = app.sessionRepository
    private val voiceManager = VoiceToTextManager(application)

    val connectionStatus: StateFlow<ConnectionStatus> = bleManager.connectionStatus
    val connectedDeviceName: StateFlow<String?> = bleManager.connectedDeviceName
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = bleManager.discoveredDevices

    val isSimulatorRunning: StateFlow<Boolean> = simulator.isRunning
    val simulatorMode: StateFlow<SimulationMode> = simulator.currentMode
    val customSimulatorBpm: StateFlow<Int> = simulator.customTargetBpm

    private val _currentHeartRate = MutableStateFlow(HeartRateData())
    val currentHeartRate: StateFlow<HeartRateData> = _currentHeartRate.asStateFlow()

    val activeStats: StateFlow<ActiveSessionStats> = sessionRepo.activeStats
    val allSessions: StateFlow<List<HeartRateSession>> = sessionRepo.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // OSC Config
    private val _targetIp = MutableStateFlow("127.0.0.1")
    val targetIp: StateFlow<String> = _targetIp.asStateFlow()

    private val _targetPort = MutableStateFlow(9000)
    val targetPort: StateFlow<Int> = _targetPort.asStateFlow()

    private val _customParam = MutableStateFlow("")
    val customParam: StateFlow<String> = _customParam.asStateFlow()

    private val prefs = application.getSharedPreferences("pulsoid_prefs", Context.MODE_PRIVATE)

    private val _darkThemeMode = MutableStateFlow(
        try {
            val saved = prefs.getString("theme_mode", DarkThemeMode.OLED_BLACK.name)
            DarkThemeMode.valueOf(saved ?: DarkThemeMode.OLED_BLACK.name)
        } catch (_: Exception) {
            DarkThemeMode.OLED_BLACK
        }
    )
    val darkThemeMode: StateFlow<DarkThemeMode> = _darkThemeMode.asStateFlow()

    fun setDarkThemeMode(mode: DarkThemeMode) {
        _darkThemeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun cycleDarkThemeMode() {
        val next = when (_darkThemeMode.value) {
            DarkThemeMode.OLED_BLACK -> DarkThemeMode.MIDNIGHT_CYBER
            DarkThemeMode.MIDNIGHT_CYBER -> DarkThemeMode.WARM_NIGHT
            DarkThemeMode.WARM_NIGHT -> DarkThemeMode.OLED_BLACK
        }
        setDarkThemeMode(next)
    }

    private val _oscPacketsSent = MutableStateFlow(0L)
    val oscPacketsSent: StateFlow<Long> = _oscPacketsSent.asStateFlow()

    private val _lastOscStatus = MutableStateFlow<String?>("Ready")
    val lastOscStatus: StateFlow<String?> = _lastOscStatus.asStateFlow()

    // Hearted Statuses & Status Box
    private val _heartedStatuses = MutableStateFlow<List<HeartedStatus>>(HeartedStatus.DEFAULT_PRESETS)
    val heartedStatuses: StateFlow<List<HeartedStatus>> = _heartedStatuses.asStateFlow()

    private val _currentStatus = MutableStateFlow(HeartedStatus.DEFAULT_PRESETS.first())
    val currentStatus: StateFlow<HeartedStatus> = _currentStatus.asStateFlow()

    private val _isAutoCycleEnabled = MutableStateFlow(false)
    val isAutoCycleEnabled: StateFlow<Boolean> = _isAutoCycleEnabled.asStateFlow()

    private val _cycleIntervalSeconds = MutableStateFlow(6.0f)
    val cycleIntervalSeconds: StateFlow<Float> = _cycleIntervalSeconds.asStateFlow()

    private var autoCycleJob: Job? = null

    // Voice to Text
    val voiceState: StateFlow<VoiceState> = voiceManager.voiceState

    // Magic Chatbox
    private val _chatboxConfig = MutableStateFlow(ChatboxConfig())
    val chatboxConfig: StateFlow<ChatboxConfig> = _chatboxConfig.asStateFlow()

    private val _chatboxFormattedText = MutableStateFlow("")
    val chatboxFormattedText: StateFlow<String> = _chatboxFormattedText.asStateFlow()

    // OBS Stream Overlay
    private val _isOverlayServerRunning = MutableStateFlow(false)
    val isOverlayServerRunning: StateFlow<Boolean> = _isOverlayServerRunning.asStateFlow()

    private val _localIpAddress = MutableStateFlow("127.0.0.1")
    val localIpAddress: StateFlow<String> = _localIpAddress.asStateFlow()

    // Active continuous OSC Broadcast toggle
    private val _isOscBroadcasting = MutableStateFlow(true)
    val isOscBroadcasting: StateFlow<Boolean> = _isOscBroadcasting.asStateFlow()

    private var chatboxLoopJob: Job? = null
    private var lastChatboxSendTime = 0L

    init {
        val ip = overlay.getLocalIpAddress(application)
        _localIpAddress.value = ip
        _targetIp.value = if (ip != "127.0.0.1") ip else "127.0.0.1"

        // Auto-start overlay server for instant stream source access
        overlay.start()
        _isOverlayServerRunning.value = true

        // Initial preview formatting
        refreshChatboxPreview()

        // Collect BLE HR ticks
        viewModelScope.launch {
            bleManager.heartRateFlow.collect { data ->
                processHeartRateTick(data)
            }
        }

        // Collect Simulator HR ticks
        viewModelScope.launch {
            simulator.heartRateFlow.collect { data ->
                processHeartRateTick(data)
            }
        }

        // Periodic Chatbox Sender Loop
        startChatboxLoop()
    }

    private fun processHeartRateTick(data: HeartRateData) {
        _currentHeartRate.value = data
        val stats = activeStats.value
        val maxBpm = maxOf(stats.maxBpm, data.bpm)
        val avgBpm = if (stats.avgBpm > 0) stats.avgBpm else data.bpm

        // Feed session recorder
        sessionRepo.onHeartRateTick(data)

        // Update local overlay web server
        overlay.updateData(data, maxBpm, avgBpm)

        // Update Chatbox preview string
        refreshChatboxPreview(data, maxBpm, avgBpm)

        // Transmit OSC Avatar parameters
        if (_isOscBroadcasting.value && data.bpm > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                osc.targetIp = _targetIp.value
                osc.targetPort = _targetPort.value
                osc.sendAvatarHeartRate(data.bpm, data.isBeat, _customParam.value)
                _oscPacketsSent.value = osc.packetsSent.get()
                _lastOscStatus.value = if (osc.lastError != null) "Error: ${osc.lastError}" else "Streaming OK"
            }
        }
    }

    private fun refreshChatboxPreview(
        data: HeartRateData = _currentHeartRate.value,
        maxBpm: Int = activeStats.value.maxBpm,
        avgBpm: Int = activeStats.value.avgBpm
    ) {
        _chatboxFormattedText.value = MagicChatboxFormatter.formatMessage(
            hrData = data,
            config = _chatboxConfig.value,
            sessionMaxBpm = maxBpm,
            sessionAvgBpm = avgBpm,
            heartedStatus = _currentStatus.value
        )
    }

    private fun startChatboxLoop() {
        chatboxLoopJob?.cancel()
        chatboxLoopJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val config = _chatboxConfig.value
                val intervalMs = (config.intervalSeconds * 1000f).toLong().coerceIn(1200L, 10000L)
                delay(intervalMs)

                if (config.enabled && _isOscBroadcasting.value && _currentHeartRate.value.bpm > 0) {
                    val msg = _chatboxFormattedText.value
                    if (msg.isNotBlank()) {
                        osc.targetIp = _targetIp.value
                        osc.targetPort = _targetPort.value
                        osc.sendChatbox(msg, sendImmediately = true, playSound = config.playSound)
                        _oscPacketsSent.value = osc.packetsSent.get()
                        lastChatboxSendTime = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    // Status Box & Cycling
    fun cycleNextStatus() {
        val list = _heartedStatuses.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _currentStatus.value.id }
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % list.size else 0
        _currentStatus.value = list[nextIndex]
        refreshChatboxPreview()
    }

    fun selectStatus(status: HeartedStatus) {
        _currentStatus.value = status
        refreshChatboxPreview()
    }

    fun setAutoCycle(enabled: Boolean, intervalSec: Float = _cycleIntervalSeconds.value) {
        _isAutoCycleEnabled.value = enabled
        _cycleIntervalSeconds.value = intervalSec
        autoCycleJob?.cancel()
        if (enabled) {
            autoCycleJob = viewModelScope.launch {
                while (isActive) {
                    delay((_cycleIntervalSeconds.value * 1000f).toLong().coerceAtLeast(2000L))
                    cycleNextStatus()
                }
            }
        }
    }

    fun addCustomStatus(emoji: String, text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return
        val newStatus = HeartedStatus(
            id = "custom_${UUID.randomUUID()}",
            emoji = emoji.ifBlank { "💖" },
            text = cleanText,
            isCustom = true
        )
        val updatedList = _heartedStatuses.value + newStatus
        _heartedStatuses.value = updatedList
        _currentStatus.value = newStatus
        refreshChatboxPreview()
    }

    fun deleteCustomStatus(id: String) {
        val updated = _heartedStatuses.value.filterNot { it.id == id }
        _heartedStatuses.value = updated
        if (_currentStatus.value.id == id) {
            _currentStatus.value = updated.firstOrNull() ?: HeartedStatus.DEFAULT_PRESETS.first()
            refreshChatboxPreview()
        }
    }

    // Voice to Text Actions
    fun startVoiceRecognition() {
        voiceManager.startListening()
    }

    fun stopVoiceRecognition() {
        voiceManager.stopListening()
    }

    fun clearVoiceText() {
        voiceManager.clearText()
    }

    fun sendVoiceTextToChatbox(text: String, includeHeartRate: Boolean = true) {
        val clean = text.trim()
        if (clean.isBlank()) return
        val bpm = _currentHeartRate.value.bpm
        val emoji = _currentStatus.value.emoji
        val message = if (includeHeartRate && bpm > 0) {
            "[$emoji $bpm BPM] $clean"
        } else {
            clean
        }

        viewModelScope.launch(Dispatchers.IO) {
            osc.targetIp = _targetIp.value
            osc.targetPort = _targetPort.value
            osc.sendChatbox(message, sendImmediately = true, playSound = _chatboxConfig.value.playSound)
            _oscPacketsSent.value = osc.packetsSent.get()
            _lastOscStatus.value = "Voice sent to VRChat: \"$clean\""
        }
    }

    fun setVoiceAsCurrentStatus(text: String, emoji: String = "💖") {
        val clean = text.trim()
        if (clean.isNotBlank()) {
            addCustomStatus(emoji, clean)
        }
    }

    fun startBleScan() = bleManager.startScan()
    fun stopBleScan() = bleManager.stopScan()
    fun connectBleDevice(address: String) = bleManager.connect(address)
    fun disconnectBleDevice() = bleManager.disconnect()

    fun toggleSimulator(enable: Boolean? = null) {
        val target = enable ?: !simulator.isRunning.value
        if (target) {
            simulator.start()
        } else {
            simulator.stop()
        }
    }

    fun setSimulatorMode(mode: SimulationMode) = simulator.setMode(mode)
    fun setCustomSimulatorBpm(bpm: Int) = simulator.setCustomBpm(bpm)

    fun setTargetIp(ip: String) {
        _targetIp.value = ip.trim()
        osc.targetIp = _targetIp.value
    }

    fun setTargetPort(port: Int) {
        _targetPort.value = port
        osc.targetPort = port
    }

    fun setCustomParam(param: String) {
        _customParam.value = param.trim()
    }

    fun setOscBroadcasting(enabled: Boolean) {
        _isOscBroadcasting.value = enabled
        if (enabled) {
            PulsoidForegroundService.start(
                getApplication(),
                _currentHeartRate.value.bpm,
                _targetIp.value
            )
        } else {
            PulsoidForegroundService.stop(getApplication())
        }
    }

    fun updateChatboxConfig(config: ChatboxConfig) {
        _chatboxConfig.value = config
        refreshChatboxPreview()
    }

    fun sendTestPing() {
        viewModelScope.launch(Dispatchers.IO) {
            osc.targetIp = _targetIp.value
            osc.targetPort = _targetPort.value
            val success = osc.sendTestPing()
            _lastOscStatus.value = if (success) "Test Ping Sent to ${_targetIp.value}:9000!" else "Ping Failed: ${osc.lastError}"
            _oscPacketsSent.value = osc.packetsSent.get()
        }
    }

    fun toggleOverlayServer() {
        if (_isOverlayServerRunning.value) {
            overlay.stop()
            _isOverlayServerRunning.value = false
        } else {
            overlay.start()
            _isOverlayServerRunning.value = true
        }
    }

    fun startSession(title: String = "VRChat Workout") {
        sessionRepo.startRecording(title)
    }

    fun stopSession() {
        viewModelScope.launch {
            sessionRepo.stopAndSaveRecording()
        }
    }

    fun deleteSession(session: HeartRateSession) {
        viewModelScope.launch {
            sessionRepo.deleteSession(session)
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoCycleJob?.cancel()
        chatboxLoopJob?.cancel()
        voiceManager.destroy()
        osc.close()
        overlay.stop()
        simulator.stop()
        bleManager.stopScan()
    }
}
