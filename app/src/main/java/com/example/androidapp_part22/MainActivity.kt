package com.example.androidapp_part22

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognizerIntent
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
import org.json.JSONArray
import org.json.JSONObject // Added missing import
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var voiceInput: EditText
    private lateinit var micButton: Button
    private lateinit var clearButton: Button
    private lateinit var settingsButton: Button
    private lateinit var historyButton: Button

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
        private const val SETTINGS_REQUEST_CODE = 101
        private const val API_ENDPOINT = "https://voicetotext.free.beeceptor.com"
        private const val API_HISTORY_PATH = "/history"
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
        voiceInput = findViewById(R.id.voiceInput)
        micButton = findViewById(R.id.micButton)
        clearButton = findViewById(R.id.clearButton)
        settingsButton = findViewById(R.id.settingsButton)
        historyButton = findViewById(R.id.historyButton)
    }

    private fun setupListeners() {
        micButton.setOnClickListener { checkAudioPermission() }
        clearButton.setOnClickListener { clearText() }
        settingsButton.setOnClickListener { openSettings() }
        historyButton.setOnClickListener { fetchHistoryFromApi() }
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
                    sendTextToApi(it)
                }
            }
            requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK -> {
                recreate()
            }
        }
    }

    private fun clearText() {
        voiceInput.text.clear()
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
                // Handle response if needed
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