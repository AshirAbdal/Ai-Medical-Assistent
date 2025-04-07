package com.example.androidapp_part22.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.androidapp_part22.R
import com.example.androidapp_part22.fragments.HistoryFragment
import com.example.androidapp_part22.fragments.SettingsFragment
import com.example.androidapp_part22.fragments.SpeechToTextFragment
import com.google.android.material.tabs.TabLayout

class VoiceActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var backButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var toolbarTitle: TextView
    lateinit var tabLayout: TabLayout
    private var speechToTextFragment: SpeechToTextFragment? = null
    private lateinit var prefs: SharedPreferences

    // Tab indices
    private val TAB_TEXT = 0
    private val TAB_HISTORY = 1

    companion object {
        private const val API_ENDPOINT = "https://voicetotext.free.beeceptor.com"
        private const val API_HISTORY_PATH = "/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize preferences and register listener
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

        // Apply theme
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)

        // Initialize views
        initializeViews()

        // Setup toolbar actions
        setupToolbarActions()

        // Setup tab layout
        setupTabLayout()

        // Load speech-to-text fragment by default
        loadSpeechToTextFragment()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        shareButton = findViewById(R.id.shareButton)
        menuButton = findViewById(R.id.menuButton)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupToolbarActions() {
        // Setup back button
        backButton.setOnClickListener {
            showExitConfirmationDialog()
        }

        // Setup share button
        shareButton.setOnClickListener {
            shareVoiceText()
        }

        // Setup menu button
        menuButton.setOnClickListener {
            showOptionsMenu()
        }
    }

    private fun shareVoiceText() {
        // Get the current text from the Speech to Text fragment if it's the active fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)

        if (currentFragment is SpeechToTextFragment) {
            // Get the voiceInput EditText directly from the fragment
            val voiceInputEditText = currentFragment.view?.findViewById<EditText>(R.id.voiceInput)
            val speechText = voiceInputEditText?.text?.toString() ?: ""

            if (speechText.isNotEmpty()) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, speechText)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } else {
                Toast.makeText(this, "No text to share", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Go to Text tab to share content", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOptionsMenu() {
        val options = arrayOf("Settings", "Help", "Rate App", "About", "Clear All")

        AlertDialog.Builder(this)
            .setTitle("Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSettingsFragment()
                    1 -> Toast.makeText(this, "Help feature coming soon", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this, "Rate App feature coming soon", Toast.LENGTH_SHORT).show()
                    3 -> showAboutDialog()
                    4 -> {
                        // Clear all text if in the speech-to-text tab
                        val currentFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
                        if (currentFragment is SpeechToTextFragment) {
                            // Access EditText directly and clear it
                            val voiceInputEditText = currentFragment.view?.findViewById<EditText>(R.id.voiceInput)
                            voiceInputEditText?.text?.clear()
                            Toast.makeText(this, "Text cleared", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .show()
    }

    private fun showSettingsFragment() {
        // Load the settings fragment
        loadFragment(SettingsFragment(), true)

        // Hide the tabs when in settings
        tabLayout.visibility = android.view.View.GONE
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Voice to Text")
            .setMessage("Version 1.0\n\nThis feature allows you to convert speech to text and save your transcriptions.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showExitConfirmationDialog() {
        // If we have fragments in backstack, just pop the backstack
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            tabLayout.visibility = android.view.View.VISIBLE
            tabLayout.getTabAt(TAB_TEXT)?.select()
            return
        }

        // Check if we're in the speech-to-text fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)

        if (currentFragment is SpeechToTextFragment) {
            AlertDialog.Builder(this)
                .setTitle("Exit Voice to Text?")
                .setMessage("Do you want to exit the Voice to Text feature?")
                .setPositiveButton("Yes") { _, _ -> navigateToDashboard() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setupTabLayout() {
        // Clear existing tabs
        tabLayout.removeAllTabs()

        // Add only Text and History tabs (removing Settings tab)
        tabLayout.addTab(tabLayout.newTab().setText("Text"))
        tabLayout.addTab(tabLayout.newTab().setText("History"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    TAB_TEXT -> {
                        loadSpeechToTextFragment()
                    }
                    TAB_HISTORY -> {
                        loadHistoryFragment()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Nothing needed here
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Re-run the same action
                onTabSelected(tab)
            }
        })
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.contentFrame, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    private fun loadSpeechToTextFragment() {
        if (speechToTextFragment == null) {
            speechToTextFragment = SpeechToTextFragment()
        }

        speechToTextFragment?.let {
            loadFragment(it)
        }
    }

    private fun loadHistoryFragment() {
        val historyFragment = HistoryFragment.newInstance()
        loadFragment(historyFragment)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()

            // Restore tab layout visibility when returning from settings
            tabLayout.visibility = android.view.View.VISIBLE
            tabLayout.getTabAt(TAB_TEXT)?.select()
        } else {
            super.onBackPressed() // Call the superclass method
        }
    }

    // Apply theme based on settings
    private fun applyTheme() {
        when (prefs.getString("theme", "System Default")) {
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    // Listen for setting changes
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "theme" -> applyTheme()
            "textSize", "fontStyle", "language" -> {
                // Refresh the current fragment if it's the SpeechToTextFragment
                speechToTextFragment?.refreshSettings()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

}

