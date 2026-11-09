package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoiceState(
    val isListening: Boolean = false,
    val text: String = "",
    val partialText: String = "",
    val rmsDb: Float = 0f,
    val errorMessage: String? = null,
    val isAvailable: Boolean = true
)

class VoiceToTextManager(private val context: Context) {

    private val _voiceState = MutableStateFlow(
        VoiceState(isAvailable = SpeechRecognizer.isRecognitionAvailable(context))
    )
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _voiceState.value = _voiceState.value.copy(
                isListening = true,
                errorMessage = null
            )
        }

        override fun onBeginningOfSpeech() {
            _voiceState.value = _voiceState.value.copy(isListening = true)
        }

        override fun onRmsChanged(rmsdB: Float) {
            _voiceState.value = _voiceState.value.copy(rmsDb = rmsdB.coerceIn(0f, 10f))
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _voiceState.value = _voiceState.value.copy(isListening = false, rmsDb = 0f)
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking again."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                else -> "Speech recognition error ($error)"
            }
            _voiceState.value = _voiceState.value.copy(
                isListening = false,
                errorMessage = message,
                rmsDb = 0f
            )
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognized = matches?.firstOrNull() ?: ""
            _voiceState.value = _voiceState.value.copy(
                isListening = false,
                text = recognized,
                partialText = recognized,
                rmsDb = 0f,
                errorMessage = null
            )
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull() ?: ""
            if (partial.isNotBlank()) {
                _voiceState.value = _voiceState.value.copy(partialText = partial)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = _voiceState.value.copy(
                errorMessage = "Speech recognition is not available on this device",
                isAvailable = false
            )
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            _voiceState.value = _voiceState.value.copy(
                isListening = true,
                errorMessage = null,
                partialText = ""
            )
        } catch (e: Exception) {
            _voiceState.value = _voiceState.value.copy(
                isListening = false,
                errorMessage = "Failed to start listening: ${e.localizedMessage}"
            )
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _voiceState.value = _voiceState.value.copy(isListening = false, rmsDb = 0f)
    }

    fun clearText() {
        _voiceState.value = _voiceState.value.copy(text = "", partialText = "", errorMessage = null)
    }

    fun setText(newText: String) {
        _voiceState.value = _voiceState.value.copy(text = newText, partialText = newText, errorMessage = null)
    }

    fun destroy() {
        stopListening()
    }
}
