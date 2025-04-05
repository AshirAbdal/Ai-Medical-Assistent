package com.example.androidapp_part22

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class MyApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

        applyTheme()
    }

    override fun onTerminate() {
        super.onTerminate()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun applyTheme() {
        when (prefs.getString("theme", "System Default")) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val langCode = prefs.getString("language", Locale.getDefault().language) ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Create new configuration for Android N and above
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            // For older Android versions
            val resources = context.resources
            val config = Configuration(resources.configuration)
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
            context
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "theme" -> applyTheme()
            // Language changes require an Activity restart to fully take effect
        }
    }
}