package com.example.androidapp_part22

import android.content.SharedPreferences
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
    private lateinit var languageSpinner: Spinner
    private lateinit var themeSpinner: Spinner
    private lateinit var fontStyleSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    // Language data (display name, language code)
    private val languages = listOf(
        "English" to "en",
        "Spanish" to "es",
        "French" to "fr",
        "German" to "de",
        "Japanese" to "ja"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI elements
        textSizeInput = findViewById(R.id.textSizeInput)
        languageSpinner = findViewById(R.id.languageSpinner)
        themeSpinner = findViewById(R.id.themeSpinner)
        fontStyleSpinner = findViewById(R.id.fontStyleSpinner)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Setup spinners
        setupLanguageSpinner()
        setupThemeSpinner()
        setupFontStyleSpinner()

        // Load saved preferences
        loadPreferences()

        // Set up button click listeners
        saveButton.setOnClickListener { savePreferences() }
        backButton.setOnClickListener { finish() }
    }

    private fun setupLanguageSpinner() {
        // Create adapter with display names
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages.map { it.first } // Show display names
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    private fun setupThemeSpinner() {
        val themes = arrayOf("Light", "Dark")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter
    }

    private fun setupFontStyleSpinner() {
        val fontStyles = arrayOf("Normal", "Bold", "Italic")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fontStyles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fontStyleSpinner.adapter = adapter
    }

    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Load text size
        val textSize = sharedPreferences.getFloat("textSize", 18f)
        textSizeInput.setText(textSize.toString())

        // Load language
        val savedLanguage = sharedPreferences.getString("language", Locale.getDefault().language)
        val languageIndex = languages.indexOfFirst { it.second == savedLanguage }
        if (languageIndex != -1) languageSpinner.setSelection(languageIndex)

        // Load theme
        val theme = sharedPreferences.getString("theme", "Light")
        themeSpinner.setSelection((themeSpinner.adapter as ArrayAdapter<String>).getPosition(theme))

        // Load font style
        val fontStyle = sharedPreferences.getString("fontStyle", "Normal")
        fontStyleSpinner.setSelection((fontStyleSpinner.adapter as ArrayAdapter<String>).getPosition(fontStyle))
    }

    private fun savePreferences() {
        val textSize = textSizeInput.text.toString().toFloatOrNull()
        val languageCode = getSelectedLanguageCode()

        if (textSize != null && languageCode != null) {
            val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putFloat("textSize", textSize)
                putString("language", languageCode)
                putString("theme", themeSpinner.selectedItem.toString())
                putString("fontStyle", fontStyleSpinner.selectedItem.toString())
                apply()
            }
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSelectedLanguageCode(): String? {
        val selectedPosition = languageSpinner.selectedItemPosition
        return if (selectedPosition in languages.indices) {
            languages[selectedPosition].second
        } else {
            null
        }
    }
}