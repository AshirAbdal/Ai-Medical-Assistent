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
    private lateinit var languageSpinner: Spinner
    private lateinit var themeSpinner: Spinner
    private lateinit var fontStyleSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

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

        textSizeInput = findViewById(R.id.textSizeInput)
        languageSpinner = findViewById(R.id.languageSpinner)
        themeSpinner = findViewById(R.id.themeSpinner)
        fontStyleSpinner = findViewById(R.id.fontStyleSpinner)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        setupSpinners()
        loadPreferences()
        setupButtonListeners()
    }

    private fun setupSpinners() {
        languageSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages.map { it.first }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        themeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Light", "Dark")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        fontStyleSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Normal", "Bold", "Italic")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        textSizeInput.setText(prefs.getFloat("textSize", 18f).toString())

        prefs.getString("language", Locale.getDefault().language)?.let { lang ->
            val index = languages.indexOfFirst { it.second == lang }
            if (index != -1) languageSpinner.setSelection(index)
        }

        themeSpinner.setSelection(
            (themeSpinner.adapter as ArrayAdapter<String>).getPosition(
                prefs.getString("theme", "Light")
            )
        )

        fontStyleSpinner.setSelection(
            (fontStyleSpinner.adapter as ArrayAdapter<String>).getPosition(
                prefs.getString("fontStyle", "Normal")
            )
        )
    }

    private fun setupButtonListeners() {
        saveButton.setOnClickListener {
            if (validateInputs()) {
                savePreferences()
                setResult(RESULT_OK, Intent())
                Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
            }
        }
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        return textSizeInput.text.toString().toFloatOrNull() != null &&
                languageSpinner.selectedItemPosition in languages.indices
    }

    private fun savePreferences() {
        getSharedPreferences("AppSettings", MODE_PRIVATE).edit().apply {
            putFloat("textSize", textSizeInput.text.toString().toFloat())
            putString("language", languages[languageSpinner.selectedItemPosition].second)
            putString("theme", themeSpinner.selectedItem.toString())
            putString("fontStyle", fontStyleSpinner.selectedItem.toString())
            apply()
        }
    }
}