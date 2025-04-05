package com.example.androidapp_part22.activities

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.example.androidapp_part22.fragments.AllPatientsFragment
import com.example.androidapp_part22.fragments.MyPatientsFragment
import com.example.androidapp_part22.fragments.PatientListFragment
import com.example.androidapp_part22.R
import com.example.androidapp_part22.fragments.SettingsFragment
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText

class DashboardActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var searchInput: TextInputEditText
    private lateinit var tabLayout: TabLayout
    private lateinit var prefs: SharedPreferences
    private var currentSearchListener: SearchListener? = null

    // Tab indices
    private val TAB_MY_PATIENTS = 0
    private val TAB_ALL_PATIENTS = 1
    private val TAB_SETTINGS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

        applySavedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setupSearchView()
        setupTabLayout()
        loadInitialFragment()
        setupTouchListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        findViewById<CoordinatorLayout>(R.id.rootLayout).setOnTouchListener { _, event ->
            handleTouchOutsideSearch(event)
            false
        }
    }

    private fun handleTouchOutsideSearch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val searchInput = findViewById<TextInputEditText>(R.id.searchInput)
            val rect = Rect().apply { searchInput.getGlobalVisibleRect(this) }

            // Convert touch coordinates correctly
            val touchX = event.rawX.toInt()
            val touchY = event.rawY.toInt()

            if (!rect.contains(touchX, touchY)) {
                searchInput.clearFocus()
                hideKeyboard()

                // Clear focus from all views
                window.decorView.clearFocus()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun applySavedTheme() {
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
        searchInput = findViewById(R.id.searchInput)
        tabLayout = findViewById(R.id.tabLayout)
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

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    TAB_MY_PATIENTS -> {
                        val fragment = MyPatientsFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contentFrame, fragment)
                            .commitNow()
                        currentSearchListener = fragment
                    }
                    TAB_ALL_PATIENTS -> {
                        val fragment = AllPatientsFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contentFrame, fragment)
                            .commitNow()
                        currentSearchListener = fragment
                    }
                    TAB_SETTINGS -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contentFrame, SettingsFragment())
                            .addToBackStack("settings")
                            .commit()
                        currentSearchListener = null
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            tabLayout.getTabAt(TAB_MY_PATIENTS)?.select()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // Apply potential font changes to fragments
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is PatientListFragment && fragment.isAdded) {
                fragment.applyFontSettings()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "theme" -> applySavedTheme()
            "textSize", "fontStyle" -> {
                // Refresh currently visible fragment
                supportFragmentManager.fragments.forEach { fragment ->
                    if (fragment is PatientListFragment && fragment.isAdded) {
                        fragment.applyFontSettings()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }
}

enum class PatientType {
    MY_PATIENTS, ALL_PATIENTS
}

interface SearchListener {
    fun onSearch(query: String)
}