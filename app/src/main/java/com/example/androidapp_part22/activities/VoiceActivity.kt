package com.example.androidapp_part22.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidapp_part22.R
import com.example.androidapp_part22.fragments.SettingsFragment
import com.google.android.material.button.MaterialButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class VoiceActivity : AppCompatActivity() {

    private var shouldContinueListening = true
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private var isListening = false
    private lateinit var scrollView: ScrollView
    private lateinit var voiceInput: EditText
    private lateinit var micButton: ImageButton
    private lateinit var clearButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var historyButton: MaterialButton
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton
    private var selectionStart: Int = 0
    private var selectionEnd: Int = 0

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT=100
        private const val SETTINGS_REQUEST_CODE = 101
        private const val API_ENDPOINT = "https://voicetotext.free.beeceptor.com"
        private const val API_HISTORY_PATH = "/"
        private const val PREFS_NAME = "VoiceToTextPrefs"
        private const val KEY_SAVED_TEXT = "saved_text"
        private const val TAG = "VoiceRecognition"

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)

        initializeSpeechRecognizer()
        initializeViews()
        setupBackButton()
        setupListeners()
        applyPreferences()
    }

    private fun setupBackButton() {
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        val currentText = voiceInput.text.toString()
        if (currentText.isEmpty()) {
            // If text is empty, redirect directly
            navigateToDashboard()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Save before exiting?")
            .setMessage("Do you want to save your current text?")
            .setPositiveButton("Save Text") { _, _ ->
                sendTextToApi(currentText)
                navigateToDashboard()
            }
            .setNegativeButton("Don't Save") { _, _ ->
                clearText()
                navigateToDashboard()
            }
            .setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getPreferredLanguage())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false) // Prevent duplicates
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                runOnUiThread { updateMicButtonState() }
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
                    runOnUiThread { updateMicButtonState() }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Removed partial handling to avoid duplication issues
            }

            override fun onError(error: Int) {
                isListening = false
                runOnUiThread {
                    updateMicButtonState()
                    if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                        Toast.makeText(
                            this@VoiceActivity,
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


    private fun handleRecognitionResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.firstOrNull()?.let { result ->
            runOnUiThread {
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

    private fun initializeViews() {
        try {
            voiceInput = findViewById(R.id.voiceInput)
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
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

            micButton = findViewById(R.id.micButton)
            clearButton = findViewById(R.id.clearButton)
            settingsButton = findViewById(R.id.settingsButton)
            historyButton = findViewById(R.id.historyButton)
            sendButton = findViewById(R.id.sendButton)
            scrollView = findViewById(R.id.scrollView)
        } catch (e: Exception) {
            Toast.makeText(this, "View Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("MainActivity", "View initialization failed", e)
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

        settingsButton.setOnClickListener { openSettings() }
        clearButton.setOnClickListener { clearText() }
        historyButton.setOnClickListener { fetchHistoryFromApi() }
        sendButton.setOnClickListener { onSendButtonClicked() }
    }

    private fun onSendButtonClicked() {
        val text = voiceInput.text.toString().trim()
        if (text.isNotEmpty()) {
            sendTextToApi(text)
            Toast.makeText(this, "Sending text...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter some text first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                speechRecognizer.startListening(speechIntent)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                Toast.makeText(this, "Microphone access is required for speech recognition.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            else -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Add this as a class property
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            runOnUiThread { updateMicButtonState() }
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onResults(results: Bundle?) {
            handleRecognitionResults(results)
            isListening = shouldContinueListening
            runOnUiThread { updateMicButtonState() }
        }

        override fun onError(error: Int) {
            isListening = false
            runOnUiThread {
                updateMicButtonState()
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> {}
                    else -> Toast.makeText(
                        this@VoiceActivity,
                        "Error: ${getErrorText(error)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            handleRecognitionResults(partialResults)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        speechRecognizer.apply {
            stopListening()
            destroy()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {

            requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK -> {
                recreate()
            }
        }
    }

    private fun saveTextToPreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVED_TEXT, voiceInput.text.toString()).apply()
    }
    private fun clearText() {
        voiceInput.text.clear()
        saveTextToPreferences() // Clear saved text
        Toast.makeText(this, "Text cleared", Toast.LENGTH_SHORT).show()
    }

    private fun openSettings() {
        startActivityForResult(Intent(this, SettingsFragment::class.java), SETTINGS_REQUEST_CODE)
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        when (prefs.getString("theme", "Light")) {
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun applyPreferences() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
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
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
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
                runOnUiThread {
                    Toast.makeText(this@VoiceActivity, "Failed to send text: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@VoiceActivity,
                            "Text sent successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@VoiceActivity,
                            "Server error: ${response.code}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun fetchHistoryFromApi() {
        val client = OkHttpClient()
        val historyUrl = "$API_ENDPOINT$API_HISTORY_PATH"

        val request = Request.Builder()
            .url(historyUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@VoiceActivity,
                        "History error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        val error = "HTTP ${response.code} - ${response.message}"
                        Toast.makeText(
                            this@VoiceActivity,
                            "History error: $error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return
                }

                val responseBody = response.body?.string()
                runOnUiThread {
                    try {
                        if (responseBody.isNullOrEmpty()) {
                            Toast.makeText(
                                this@VoiceActivity,
                                "No history entries found",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@runOnUiThread
                        }

                        val historyArray = JSONArray(responseBody)
                        val historyList = mutableListOf<String>()

                        for (i in 0 until historyArray.length()) {
                            when (val entry = historyArray.get(i)) {
                                is String -> historyList.add(entry)
                                is JSONObject -> historyList.add(entry.optString("text", "Invalid entry"))
                                else -> historyList.add("Unknown format")
                            }
                        }

                        if (historyList.isEmpty()) {
                            Toast.makeText(
                                this@VoiceActivity,
                                "History is empty",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@runOnUiThread
                        }

                        showHistoryDialog(historyList)

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@VoiceActivity,
                            "Failed to parse history",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun showHistoryDialog(history: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("API History")
            .setItems(history.toTypedArray()) { _, _ -> }
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}