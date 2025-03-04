package com.example.androidapp_part22

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
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
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var voiceInput: EditText
    private lateinit var micButton: Button
    private lateinit var clearButton: Button
    private lateinit var settingsButton: Button
    private lateinit var showEntriesButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private val idleTimeout = 10000L
    private val voiceFileName = "voice.txt"
    private var lastSavedText = ""

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
        private const val SETTINGS_REQUEST_CODE = 101
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
        startIdleTimer()
    }

    private fun initializeViews() {
        voiceInput = findViewById(R.id.voiceInput)
        micButton = findViewById(R.id.micButton)
        clearButton = findViewById(R.id.clearButton)
        settingsButton = findViewById(R.id.settingsButton)
        showEntriesButton = findViewById(R.id.showEntriesButton)
    }

    private fun setupListeners() {
        micButton.setOnClickListener { checkAudioPermission() }
        clearButton.setOnClickListener { clearText() }
        settingsButton.setOnClickListener { openSettings() }
        showEntriesButton.setOnClickListener { showSavedEntries() }

        voiceInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                resetIdleTimer()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let {
                    voiceInput.setText(it)
                    saveVoiceEntry(it)
                    sendTextToApi(it)  // Send text to your API endpoint.
                    resetIdleTimer()
                }
            }
            requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK -> {
                // Recreate the activity to apply new theme and font settings immediately.
                recreate()
            }
        }
    }

    private fun saveVoiceEntry(text: String) {
        if (text == lastSavedText) return

        try {
            val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
            val count = prefs.getInt("voice_entry_count", 0) + 1

            File(filesDir, voiceFileName).appendText("voice $count text: $text\n")

            prefs.edit().apply {
                putInt("voice_entry_count", count)
                apply()
            }
            lastSavedText = text
            Toast.makeText(this, "Saved: Entry $count", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSavedEntries() {
        val file = File(filesDir, voiceFileName)
        if (file.exists()) {
            val content = file.readText().ifEmpty { "No entries found" }
            AlertDialog.Builder(this)
                .setTitle("Saved Entries")
                .setMessage(content)
                .setPositiveButton("OK", null)
                .show()
        } else {
            Toast.makeText(this, "No entries found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearText() {
        voiceInput.text.clear()
        lastSavedText = ""
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

    private fun startIdleTimer() = handler.postDelayed(::saveCurrentText, idleTimeout)
    private fun resetIdleTimer() = handler.apply {
        removeCallbacksAndMessages(null)
        postDelayed(::saveCurrentText, idleTimeout)
    }

    private fun saveCurrentText() {
        val currentText = voiceInput.text.toString().trim()
        if (currentText.isNotEmpty() && currentText != lastSavedText) {
            saveVoiceEntry(currentText)
        }
    }

    private fun getPreferredLanguage(): String {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        return prefs.getString("language", Locale.getDefault().language) ?: "en"
    }

    // Function to send text to an API endpoint using OkHttp and parse the JSON response.
    private fun sendTextToApi(text: String) {
        val client = OkHttpClient()

        val requestBody: RequestBody = FormBody.Builder()
            .add("text", text)
            .build()

        val request = Request.Builder()
            .url("https://voicetotext.free.beeceptor.com") // Replace with your API endpoint.
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to send text: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                // Safely extract the response body.
                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Empty response received", Toast.LENGTH_SHORT).show()
                    }
                    return
                }
                try {
                    // Parse the response assuming it is in JSON format.
                    val jsonObject = JSONObject(responseBody)
                    val message = jsonObject.getString("message")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Response: $message", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
