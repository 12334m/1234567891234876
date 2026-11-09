package com.example.osc

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicLong

class OscSender(
    var targetIp: String = "127.0.0.1",
    var targetPort: Int = 9000
) {
    companion object {
        private const val TAG = "OscSender"
    }

    private var socket: DatagramSocket? = null
    val packetsSent = AtomicLong(0)
    var lastSentTime: Long = 0L
        private set
    var lastError: String? = null
        private set

    init {
        try {
            socket = DatagramSocket()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create DatagramSocket", e)
            lastError = e.message
        }
    }

    suspend fun sendAvatarHeartRate(
        bpm: Int,
        isBeating: Boolean,
        customParam: String = "",
        sendFloat: Boolean = true
    ) = withContext(Dispatchers.IO) {
        try {
            // 1. Standard Int Avatar Parameter
            sendRaw(OscPacket.buildMessage("/avatar/parameters/Heartrate", bpm))

            // 2. Standard Float (normalized 0.0 - 1.0 for 40-200 BPM)
            if (sendFloat) {
                val normalized = ((bpm - 40f) / 160f).coerceIn(0f, 1f)
                sendRaw(OscPacket.buildMessage("/avatar/parameters/HeartrateFloat", normalized))
                sendRaw(OscPacket.buildMessage("/avatar/parameters/HR", bpm.toFloat()))
            }

            // 3. Heartbeat Toggle Pulse Boolean
            sendRaw(OscPacket.buildMessage("/avatar/parameters/isBeating", isBeating))

            // 4. Custom avatar parameter if specified
            if (customParam.isNotBlank()) {
                val cleanParam = customParam.trim().removePrefix("/")
                sendRaw(OscPacket.buildMessage("/avatar/parameters/$cleanParam", bpm))
            }

            lastError = null
        } catch (e: Exception) {
            Log.e(TAG, "Error sending avatar HR OSC", e)
            lastError = e.message
        }
    }

    suspend fun sendChatbox(
        text: String,
        sendImmediately: Boolean = true,
        playSound: Boolean = false
    ) = withContext(Dispatchers.IO) {
        try {
            // VRChat OSC Chatbox specification:
            // Address: /chatbox/input
            // Argument 1: String message
            // Argument 2: Boolean sendImmediately (true)
            // Argument 3: Boolean playNotificationSound (false/true)
            val packet = OscPacket.buildMessage("/chatbox/input", text, sendImmediately, playSound)
            sendRaw(packet)
            lastError = null
        } catch (e: Exception) {
            Log.e(TAG, "Error sending chatbox OSC", e)
            lastError = e.message
        }
    }

    suspend fun sendChatboxTyping(isTyping: Boolean) = withContext(Dispatchers.IO) {
        try {
            val packet = OscPacket.buildMessage("/chatbox/typing", isTyping)
            sendRaw(packet)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending typing OSC", e)
        }
    }

    suspend fun sendTestPing(): Boolean = withContext(Dispatchers.IO) {
        try {
            val ping = OscPacket.buildMessage("/chatbox/input", "❤️ Pulsoid VR OSC Connected!", true, false)
            sendRaw(ping)
            lastError = null
            true
        } catch (e: Exception) {
            Log.e(TAG, "Test ping failed", e)
            lastError = e.message
            false
        }
    }

    private fun sendRaw(data: ByteArray) {
        try {
            if (socket == null || socket?.isClosed == true) {
                socket = DatagramSocket()
            }
            val address = InetAddress.getByName(targetIp)
            val packet = DatagramPacket(data, data.size, address, targetPort)
            socket?.send(packet)
            packetsSent.incrementAndGet()
            lastSentTime = System.currentTimeMillis()
        } catch (e: Exception) {
            lastError = e.message
            Log.w(TAG, "Send raw failed: ${e.message}")
        }
    }

    fun close() {
        try {
            socket?.close()
            socket = null
        } catch (e: Exception) {
            Log.e(TAG, "Socket close error", e)
        }
    }
}
