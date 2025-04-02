package com.example.androidapp_part22

import android.app.Application
import android.content.Context

import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applyLanguage()
        applyTheme()
    }

    private fun applyTheme() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        when (prefs.getString("theme", "System Default")) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.applyLanguage())
    }

    private fun Context.applyLanguage(): Context {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val langCode = prefs.getString("language", Locale.getDefault().language) ?: "en"
        val locale = Locale(langCode)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val config = resources.configuration
            config.setLocale(locale)
            createConfigurationContext(config)
        } else {
            val resources = resources
            val config = resources.configuration
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
            this
        }
    }
}