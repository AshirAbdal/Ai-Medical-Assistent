package com.example.androidapp_part22.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.androidapp_part22.R
import com.example.androidapp_part22.fragments.SettingsFragment
import com.example.androidapp_part22.fragments.SpeechToTextFragment
import com.google.android.material.tabs.TabLayout
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class VoiceActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var backButton: ImageButton
    private lateinit var tabLayout: TabLayout
    private var speechToTextFragment: SpeechToTextFragment? = null
    private lateinit var prefs: SharedPreferences

    // Tab indices
    private val TAB_TEXT = 0
    private val TAB_SETTINGS = 1
    private val TAB_HISTORY = 2

    companion object {
        private const val API_ENDPOINT = "https://voicetotext.free.beeceptor.com"
        private const val API_HISTORY_PATH = "/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)

        initializeViews()
        setupBackButton()
        setupTabLayout()

        // Load speech-to-text fragment by default
        loadSpeechToTextFragment()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        // If we have fragments in backstack, just pop the backstack
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
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
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    TAB_TEXT -> {
                        loadSpeechToTextFragment()
                    }
                    TAB_SETTINGS -> {
                        loadSettingsFragment()
                    }
                    TAB_HISTORY -> {
                        fetchHistoryFromApi()
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

    private fun loadSettingsFragment() {
        val settingsFragment = SettingsFragment()
        loadFragment(settingsFragment, true)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            tabLayout.getTabAt(TAB_TEXT)?.select()
        } else {
            showExitConfirmationDialog()
        }
    }

    // Apply theme based on settings
    private fun applyTheme() {
        when (prefs.getString("theme", "Light")) {
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

    private fun fetchHistoryFromApi() {
        // Show the text fragment while loading history
        tabLayout.getTabAt(TAB_TEXT)?.select()

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
                        this@VoiceActivity,
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
                            this@VoiceActivity,
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
                                this@VoiceActivity,
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
                                this@VoiceActivity,
                                "History is empty",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@runOnUiThread
                        }

                        showHistoryDialog(historyList)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@VoiceActivity,
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
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}