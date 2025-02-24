package com.example.androidapp_part22

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var voiceInput: EditText
    private lateinit var micButton: Button
    private lateinit var clearButton: Button
    private lateinit var settingsButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private val idleTimeout = 10000L // 10 seconds

    // Permission request code
    private val REQUEST_RECORD_AUDIO_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        voiceInput = findViewById(R.id.voiceInput)
        micButton = findViewById(R.id.micButton)
        clearButton = findViewById(R.id.clearButton)
        settingsButton = findViewById(R.id.settingsButton)

        // Check and request audio permission
        checkAndRequestAudioPermission()

        // Apply saved preferences
        applyPreferences()

        // Mic button click listener
        micButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show()
            }
        }

        // Clear button click listener
        clearButton.setOnClickListener {
            voiceInput.text.clear()
            Toast.makeText(this, "Text cleared", Toast.LENGTH_SHORT).show()
        }

        // Settings button click listener
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Reset idle timer on user input
        voiceInput.setOnKeyListener { _, _, _ ->
            resetIdleTimer()
            false
        }

        // Start the idle timer
        startIdleTimer()
    }

    // Check and request audio permission
    private fun checkAndRequestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Start voice recognition
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        // Apply the saved language preference
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val language = sharedPreferences.getString("language", Locale.getDefault().language)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle the result of voice recognition
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            voiceInput.setText(result?.get(0))
            resetIdleTimer()
            Toast.makeText(this, "Voice input received", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Voice input failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Start the idle timer
    private fun startIdleTimer() {
        handler.postDelayed({ saveText() }, idleTimeout)
    }

    // Reset the idle timer
    private fun resetIdleTimer() {
        handler.removeCallbacksAndMessages(null)
        startIdleTimer()
    }

    // Save the text when idle timeout is reached
    private fun saveText() {
        val text = voiceInput.text.toString()
        if (text.isNotEmpty()) {
            Toast.makeText(this, "Text saved: $text", Toast.LENGTH_SHORT).show()
        }
    }

    // Apply saved preferences (text size and language)
    private fun applyPreferences() {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val textSize = sharedPreferences.getFloat("textSize", 18f) // Default text size is 18sp
        val language = sharedPreferences.getString("language", Locale.getDefault().language)

        voiceInput.textSize = textSize
    }

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
    }
}