package com.example.androidapp_part22.activities

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.androidapp_part22.fragments.AllPatientsFragment
import com.example.androidapp_part22.fragments.MyPatientsFragment
import com.example.androidapp_part22.fragments.PatientListFragment
import com.example.androidapp_part22.R
import com.example.androidapp_part22.fragments.SettingsFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatDelegate

class DashboardActivity : AppCompatActivity() {
    private lateinit var searchInput: TextInputEditText
    private lateinit var myPatientButton: MaterialButton
    private lateinit var allPatientButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private val selectedColor = "#4CAF50"
    private val defaultColor = "#2E7D32"
    private lateinit var prefs: SharedPreferences
    private var currentSearchListener: SearchListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        initViews()
        setupSearchView()
        setupMenuButtons()
        loadInitialFragment()
    }

    private fun applySavedTheme() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        when (prefs.getString("theme", "System Default")) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun loadInitialFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentFrame, MyPatientsFragment())
            .commit()
        currentSearchListener = supportFragmentManager.findFragmentById(R.id.contentFrame) as? SearchListener
    }

    private fun initViews() {
        myPatientButton = findViewById(R.id.myPatientButton)
        allPatientButton = findViewById(R.id.allPatientButton)
        settingsButton = findViewById(R.id.settingsButton)
        searchInput = findViewById(R.id.searchInput)
    }

    private fun setupSearchView() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchListener?.onSearch(s?.toString()?.trim() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupMenuButtons() {
        setSelectedButton(myPatientButton)


        myPatientButton.setOnClickListener {
            setSelectedButton(it as MaterialButton)
            val fragment = MyPatientsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commitNow() // Use commitNow to execute immediately
            currentSearchListener = fragment
        }

        allPatientButton.setOnClickListener {
            setSelectedButton(it as MaterialButton)
            val fragment = AllPatientsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commitNow() // Use commitNow to execute immediately
            currentSearchListener = fragment
        }
        settingsButton.setOnClickListener {
            setSelectedButton(it as MaterialButton)
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentFrame, SettingsFragment())
                .addToBackStack("settings")
                .commit()
            currentSearchListener = null
        }
    }

    private fun setSelectedButton(selectedButton: MaterialButton) {
        listOf(myPatientButton, allPatientButton, settingsButton).forEach {
            it.setTextColor(Color.parseColor(defaultColor))
            it.iconTint = ColorStateList.valueOf(Color.parseColor(defaultColor))
        }
        selectedButton.setTextColor(Color.parseColor(selectedColor))
        selectedButton.iconTint = ColorStateList.valueOf(Color.parseColor(selectedColor))
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.fragments.forEach {
            if (it is PatientListFragment && it.isAdded) { // Check if fragment is added
                it.applyFontSettings()
            }
        }
    }
}

enum class PatientType {
    MY_PATIENTS, ALL_PATIENTS
}

interface SearchListener {
    fun onSearch(query: String)
}