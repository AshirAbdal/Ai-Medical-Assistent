package com.example.androidapp_part22


import android.widget.ImageButton
import com.google.android.material.button.MaterialButton
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.util.Log

import android.text.TextWatcher
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject // Added missing import
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {



    private lateinit var scrollView: ScrollView
    private lateinit var voiceInput: EditText
    private lateinit var micButton: ImageButton
    private lateinit var clearButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var historyButton: MaterialButton
    private lateinit var sendButton: ImageButton

    // Track text selection positions
    private var selectionStart: Int = 0
    private var selectionEnd: Int = 0

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
        private const val SETTINGS_REQUEST_CODE = 101
        private const val API_ENDPOINT = "https://voicetotext.free.beeceptor.com"
        private const val API_HISTORY_PATH = "/"
        private const val PREFS_NAME = "VoiceToTextPrefs"
        private const val KEY_SAVED_TEXT = "saved_text"
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
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
        applyPreferences()
    }

    private fun initializeViews() {
        try {
            voiceInput = findViewById(R.id.voiceInput)
            // Load saved text
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            voiceInput.setText(prefs.getString(KEY_SAVED_TEXT, ""))

            // Add TextWatcher
            voiceInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    saveTextToPreferences()
                }
            })

            // Rest of your existing initialization code
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
        micButton.setOnClickListener { checkAudioPermission() }
        clearButton.setOnClickListener { clearText() }
        settingsButton.setOnClickListener { openSettings() }
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
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> startVoiceRecognition()

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) -> Toast.makeText(this, "Microphone access needed", Toast.LENGTH_LONG).show()

            else -> requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }


    private fun startVoiceRecognition() {
        // Capture current text selection before starting voice input
        selectionStart = voiceInput.selectionStart
        selectionEnd = voiceInput.selectionEnd

        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getPreferredLanguage())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
            try {
                startActivityForResult(this, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Speech recognition unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK -> {
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { newText ->
                    val currentText = voiceInput.text.toString()

                    // Ensure selection positions are valid
                    val safeStart = selectionStart.coerceIn(0, currentText.length)
                    val safeEnd = selectionEnd.coerceIn(safeStart, currentText.length)

                    // Replace selected text or insert at cursor position
                    val updatedText = if (safeStart != safeEnd) {
                        currentText.replaceRange(safeStart, safeEnd, newText)
                    } else {
                        currentText.substring(0, safeStart) + newText + currentText.substring(safeStart)
                    }

                    // Update the EditText and maintain cursor position
                    voiceInput.setText(updatedText)
                    voiceInput.setSelection(safeStart + newText.length)

// Add auto-scroll here (new code)
//                    voiceInput.post {
//                        val scrollView = findViewById<ScrollView>(R.id.scrollView)
//                        scrollView.post {
//                            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
//                        }
//                    }

                    scrollView.post {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }

                    // Send updated text to API
                    sendTextToApi(updatedText)
                }
            }
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
        startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_REQUEST_CODE)
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
                    Toast.makeText(this@MainActivity, "Failed to send text: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@MainActivity,
                            "Text sent successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
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
                        this@MainActivity,
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
                            this@MainActivity,
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
                                this@MainActivity,
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
                                this@MainActivity,
                                "History is empty",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@runOnUiThread
                        }

                        showHistoryDialog(historyList)

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
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