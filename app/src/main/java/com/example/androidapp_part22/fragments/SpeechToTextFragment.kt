package com.example.androidapp_part22.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.androidapp_part22.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Locale

class SpeechToTextFragment : Fragment() {

    private var shouldContinueListening = true
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private var isListening = false
    private lateinit var scrollView: ScrollView
    private lateinit var voiceInput: EditText
    private lateinit var micButton: ImageButton
    private lateinit var sendButton: ImageButton
    private var selectionStart: Int = 0
    private var selectionEnd: Int = 0
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val API_ENDPOINT = "https://voicetotext.free.beeceptor.com"
        private const val PREFS_NAME = "VoiceToTextPrefs"
        private const val KEY_SAVED_TEXT = "saved_text"
        private const val TAG = "SpeechToTextFragment"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            Toast.makeText(requireContext(), "Permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    // These methods should be added to the SpeechToTextFragment class
// to support the features used in the updated VoiceActivity

    fun SpeechToTextFragment.getCurrentText(): String {
        // Get the current text from the EditText
        return voiceInput.text.toString()
    }

    fun SpeechToTextFragment.clearText() {
        // Clear the text in the EditText
        voiceInput.text.clear()
        saveTextToPreferences() // Clear saved text
        Toast.makeText(requireContext(), "Text cleared", Toast.LENGTH_SHORT).show()
    }

    fun SpeechToTextFragment.setTextFromHistory(text: String) {
        // Set text from a history entry
        voiceInput.setText(text)
        voiceInput.setSelection(text.length) // Move cursor to end
        saveTextToPreferences()
        Toast.makeText(requireContext(), "Text loaded from history", Toast.LENGTH_SHORT).show()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_speech_to_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSpeechRecognizer()
        initializeViews(view)
        setupListeners()
        applyPreferences()
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        updateSpeechIntent()

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                activity?.runOnUiThread { updateMicButtonState() }
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onResults(results: Bundle?) {
                handleRecognitionResults(results)
                if (shouldContinueListening) {
                    speechRecognizer.startListening(speechIntent) // Automatically restart
                } else {
                    isListening = false
                    activity?.runOnUiThread { updateMicButtonState() }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Removed partial handling to avoid duplication issues
            }

            override fun onError(error: Int) {
                isListening = false
                activity?.runOnUiThread {
                    updateMicButtonState()
                    if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${getErrorText(error)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Restart listening automatically unless explicitly stopped or permission issue
                if (shouldContinueListening && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    speechRecognizer.cancel()
                    speechRecognizer.startListening(speechIntent)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun updateSpeechIntent() {
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getPreferredLanguage())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false) // Prevent duplicates
        }
    }

    private fun handleRecognitionResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.firstOrNull()?.let { result ->
            activity?.runOnUiThread {
                val currentText = voiceInput.text.toString()
                val safeStart = selectionStart.coerceIn(0, currentText.length)
                val safeEnd = selectionEnd.coerceIn(safeStart, currentText.length)

                val updatedText = if (safeStart != safeEnd) {
                    currentText.replaceRange(safeStart, safeEnd, result)
                } else {
                    currentText.substring(0, safeStart) + result + currentText.substring(safeStart)
                }

                voiceInput.setText(updatedText)
                voiceInput.setSelection(safeStart + result.length)
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                sendTextToApi(updatedText)
            }
        }
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions missing"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech"
            else -> "Unknown error"
        }
    }

    private fun updateMicButtonState() {
        Log.d(TAG, "Mic state - Listening: $isListening, ShouldContinue: $shouldContinueListening")
        micButton.setImageResource(
            if (isListening) R.drawable.ic_stop
            else R.drawable.ic_mic
        )
    }

    private fun initializeViews(view: View) {
        try {
            voiceInput = view.findViewById(R.id.voiceInput)
            val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            voiceInput.setText(prefs.getString(KEY_SAVED_TEXT, ""))

            voiceInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    saveTextToPreferences()
                    selectionStart = voiceInput.selectionStart
                    selectionEnd = voiceInput.selectionEnd
                }
            })

            voiceInput.setOnClickListener {
                selectionStart = voiceInput.selectionStart
                selectionEnd = voiceInput.selectionEnd
            }

            micButton = view.findViewById(R.id.micButton)
            sendButton = view.findViewById(R.id.sendButton)
            scrollView = view.findViewById(R.id.scrollView)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "View Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "View initialization failed", e)
        }
    }

    private fun setupListeners() {
        micButton.setOnClickListener {
            if (isListening) {
                stopVoiceRecognition()
            } else {
                startVoiceRecognition() // Ensure permission is checked before starting
            }
        }

        sendButton.setOnClickListener { onSendButtonClicked() }
    }

    private fun onSendButtonClicked() {
        val text = voiceInput.text.toString().trim()
        if (text.isNotEmpty()) {
            sendTextToApi(text)
            Toast.makeText(requireContext(), "Sending text...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Please enter some text first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                speechRecognizer.startListening(speechIntent)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.RECORD_AUDIO
            ) -> {
                Toast.makeText(requireContext(), "Microphone access is required for speech recognition.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            else -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceRecognition() {
        if (!isListening) {
            shouldContinueListening = true
            checkAudioPermission()
        }
    }

    private fun stopVoiceRecognition() {
        shouldContinueListening = false
        isListening = false
        speechRecognizer.stopListening()
        updateMicButtonState()
    }

    override fun onPause() {
        super.onPause()
        stopVoiceRecognition()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        speechRecognizer.apply {
            stopListening()
            destroy()
        }
    }

    private fun saveTextToPreferences() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVED_TEXT, voiceInput.text.toString()).apply()
    }

    private fun clearText() {
        voiceInput.text.clear()
        saveTextToPreferences() // Clear saved text
        Toast.makeText(requireContext(), "Text cleared", Toast.LENGTH_SHORT).show()
    }

    private fun applyPreferences() {
        with(voiceInput) {
            textSize = prefs.getFloat("textSize", 18f)
            typeface = when (prefs.getString("fontStyle", "Normal")) {
                "Bold" -> Typeface.DEFAULT_BOLD
                "Italic" -> Typeface.defaultFromStyle(Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }
        }
    }

    private fun getPreferredLanguage(): String {
        return prefs.getString("language", Locale.getDefault().language) ?: "en"
    }

    private fun sendTextToApi(text: String) {
        val client = OkHttpClient()
        val requestBody: RequestBody = FormBody.Builder()
            .add("text", text)
            .build()

        val request = Request.Builder()
            .url(API_ENDPOINT)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to send text: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Text sent successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Server error: ${response.code}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    // Method to refresh the fragment when settings change
    fun refreshSettings() {
        applyPreferences()
        updateSpeechIntent() // Update speech recognizer with new language settings

        // If we were listening, restart with new settings
        if (isListening) {
            speechRecognizer.stopListening()
            speechRecognizer.startListening(speechIntent)
        }
    }
}