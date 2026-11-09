package com.example.osc

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets

/**
 * Lightweight, zero-dependency Open Sound Control (OSC) 1.0 / 1.1 packet builder
 * optimized for VRChat OSC avatar parameters and Magic Chatbox.
 */
object OscPacket {

    fun buildMessage(address: String, vararg args: Any?): ByteArray {
        val out = ByteArrayOutputStream()
        val dataOut = DataOutputStream(out)

        // 1. Address string (null-terminated and padded to 4-byte boundary)
        writePaddedString(dataOut, address)

        // 2. Type tag string (starts with comma ',')
        val typeTagBuilder = StringBuilder(",")
        for (arg in args) {
            when (arg) {
                is Int -> typeTagBuilder.append('i')
                is Float -> typeTagBuilder.append('f')
                is Double -> typeTagBuilder.append('f')
                is String -> typeTagBuilder.append('s')
                is Boolean -> if (arg) typeTagBuilder.append('T') else typeTagBuilder.append('F')
                else -> typeTagBuilder.append('s')
            }
        }
        writePaddedString(dataOut, typeTagBuilder.toString())

        // 3. Arguments payload
        for (arg in args) {
            when (arg) {
                is Int -> dataOut.writeInt(arg)
                is Float -> dataOut.writeFloat(arg)
                is Double -> dataOut.writeFloat(arg.toFloat())
                is String -> writePaddedString(dataOut, arg)
                is Boolean -> {
                    // In OSC standard, 'T' and 'F' have no payload data in argument buffer!
                }
                else -> writePaddedString(dataOut, arg?.toString() ?: "")
            }
        }

        dataOut.flush()
        return out.toByteArray()
    }

    private fun writePaddedString(dataOut: DataOutputStream, str: String) {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)
        dataOut.write(bytes)
        // Null termination
        dataOut.writeByte(0)
        // Pad to next 4-byte boundary
        val totalLen = bytes.size + 1
        val pad = (4 - (totalLen % 4)) % 4
        for (i in 0 until pad) {
            dataOut.writeByte(0)
        }
    }
}
