package com.example.androidapp_part22

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var textSizeInput: EditText
    private lateinit var languageInput: EditText
    private lateinit var themeSpinner: Spinner
    private lateinit var fontStyleSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI elements
        textSizeInput = findViewById(R.id.textSizeInput)
        languageInput = findViewById(R.id.languageInput)
        themeSpinner = findViewById(R.id.themeSpinner)
        fontStyleSpinner = findViewById(R.id.fontStyleSpinner)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Set up Spinners
        setupThemeSpinner()
        setupFontStyleSpinner()

        // Load saved preferences
        loadPreferences()

        // Save button click listener
        saveButton.setOnClickListener {
            savePreferences()
        }

        // Back button click listener
        backButton.setOnClickListener {
            finish() // Close the settings activity and return to MainActivity
        }
    }

    // Set up theme Spinner
    private fun setupThemeSpinner() {
        val themes = arrayOf("Light", "Dark")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter
    }

    // Set up font style Spinner
    private fun setupFontStyleSpinner() {
        val fontStyles = arrayOf("Normal", "Bold", "Italic")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fontStyles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fontStyleSpinner.adapter = adapter
    }

    // Load saved preferences from SharedPreferences
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val textSize = sharedPreferences.getFloat("textSize", 18f) // Default text size is 18sp
        val language = sharedPreferences.getString("language", Locale.getDefault().language) // Default language is system language
        val theme = sharedPreferences.getString("theme", "Light") // Default theme is Light
        val fontStyle = sharedPreferences.getString("fontStyle", "Normal") // Default font style is Normal

        textSizeInput.setText(textSize.toString())
        languageInput.setText(language)
        themeSpinner.setSelection((themeSpinner.adapter as ArrayAdapter<String>).getPosition(theme))
        fontStyleSpinner.setSelection((fontStyleSpinner.adapter as ArrayAdapter<String>).getPosition(fontStyle))
    }

    // Save preferences to SharedPreferences
    private fun savePreferences() {
        val textSize = textSizeInput.text.toString().toFloatOrNull()
        val language = languageInput.text.toString()
        val theme = themeSpinner.selectedItem.toString()
        val fontStyle = fontStyleSpinner.selectedItem.toString()

        if (textSize != null && language.isNotEmpty()) {
            val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putFloat("textSize", textSize)
                putString("language", language)
                putString("theme", theme)
                putString("fontStyle", fontStyle)
                apply()
            }
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
        }
    }
}