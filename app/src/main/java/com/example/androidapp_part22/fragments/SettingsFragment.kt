package com.example.androidapp_part22.fragments

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.androidapp_part22.R
import java.util.Locale

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var prefs: SharedPreferences
    private lateinit var textSizeInput: EditText
    private lateinit var languageSpinner: Spinner
    private lateinit var themeSpinner: Spinner
    private lateinit var fontStyleSpinner: Spinner

    private val languages = listOf(
        "English" to "en",
        "Spanish" to "es",
        "French" to "fr",
        "German" to "de",
        "Japanese" to "ja"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        initializeViews(view)
        setupSpinners()
        loadPreferences()
        setupButtonListeners(view)
    }

    private fun initializeViews(view: View) {
        textSizeInput = view.findViewById(R.id.textSizeInput)
        languageSpinner = view.findViewById(R.id.languageSpinner)
        themeSpinner = view.findViewById(R.id.themeSpinner)
        fontStyleSpinner = view.findViewById(R.id.fontStyleSpinner)
    }

    private fun setupSpinners() {
        // Language Spinner
        languageSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages.map { it.first }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Theme Spinner
        themeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Light", "Dark", "System Default")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Font Style Spinner
        fontStyleSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Normal", "Bold", "Italic")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun loadPreferences() {
        // Load text size
        textSizeInput.setText(prefs.getFloat("textSize", 18f).toString())

        // Load language
        prefs.getString("language", Locale.getDefault().language)?.let { lang ->
            val index = languages.indexOfFirst { it.second == lang }
            if (index != -1) languageSpinner.setSelection(index)
        }

        // Load theme
        themeSpinner.setSelection(
            (themeSpinner.adapter as ArrayAdapter<String>).getPosition(
                prefs.getString("theme", "System Default")
            )
        )

        // Load font style
        fontStyleSpinner.setSelection(
            (fontStyleSpinner.adapter as ArrayAdapter<String>).getPosition(
                prefs.getString("fontStyle", "Normal")
            )
        )
    }



    private fun setupButtonListeners(view: View) {
        view.findViewById<Button>(R.id.saveButton).setOnClickListener {
            if (validateInputs()) {
                savePreferences()
                applySettings()
                Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()

                // Navigate back programmatically after saving
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun validateInputs(): Boolean {
        return textSizeInput.text.toString().toFloatOrNull() != null &&
                languageSpinner.selectedItemPosition in languages.indices
    }

    private fun savePreferences() {
        prefs.edit().apply {
            putFloat("textSize", textSizeInput.text.toString().toFloat())
            putString("language", languages[languageSpinner.selectedItemPosition].second)
            putString("fontStyle", fontStyleSpinner.selectedItem.toString())
            putString("theme", themeSpinner.selectedItem.toString())
            apply()
        }
    }

    private fun applySettings() {
        applyTheme()
        applyLanguage()
        requireActivity().recreate()
    }

    private fun applyTheme() {
        val themeMode = when (themeSpinner.selectedItem.toString()) {
            "Light" -> AppCompatDelegate.MODE_NIGHT_NO
            "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(themeMode)

        // Add this to ensure theme persists
        prefs.edit().putString("theme", themeSpinner.selectedItem.toString()).apply()
    }

    private fun applyLanguage() {
        val langCode = languages[languageSpinner.selectedItemPosition].second
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}