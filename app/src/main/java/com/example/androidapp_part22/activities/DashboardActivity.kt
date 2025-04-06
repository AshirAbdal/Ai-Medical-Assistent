package com.example.androidapp_part22.activities

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.androidapp_part22.R
import com.example.androidapp_part22.fragments.AllPatientsFragment
import com.example.androidapp_part22.fragments.MyPatientsFragment
import com.example.androidapp_part22.fragments.PatientListFragment
import com.example.androidapp_part22.fragments.ScheduleFragment
import com.example.androidapp_part22.fragments.SettingsFragment
import com.example.androidapp_part22.fragments.UpcomingAppointmentsFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class DashboardActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var searchInput: TextInputEditText
    private lateinit var searchLayout: TextInputLayout
    private lateinit var searchButton: ImageButton
    private lateinit var notificationsButton: ImageButton
    private lateinit var toolbarTitle: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var prefs: SharedPreferences
    private var currentSearchListener: SearchListener? = null
    private var isSearchVisible = false

    // Tab indices
    private val TAB_MY_PATIENTS = 0
    private val TAB_ALL_PATIENTS = 1
    private val TAB_SCHEDULE = 2  // New Schedule tab
    private val TAB_SETTINGS = 3  // Moved Settings to tab 4

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
        if (event.action == MotionEvent.ACTION_DOWN && isSearchVisible) {
            val searchInput = findViewById<TextInputEditText>(R.id.searchInput)
            val rect = android.graphics.Rect().apply { searchInput.getGlobalVisibleRect(this) }

            // Convert touch coordinates correctly
            val touchX = event.rawX.toInt()
            val touchY = event.rawY.toInt()

            if (!rect.contains(touchX, touchY)) {
                toggleSearchVisibility(false)
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
        // Load My Patients fragment with Upcoming Appointments above it
        val myPatientsFragment = MyPatientsFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.contentFrame, myPatientsFragment)
            .commit()

        // Add upcoming appointments widget at the top of the content frame
        supportFragmentManager.beginTransaction()
            .add(R.id.dashboardWidgetsContainer, UpcomingAppointmentsFragment.newInstance())
            .commit()

        currentSearchListener = myPatientsFragment
    }

    private fun initViews() {
        searchButton = findViewById(R.id.searchButton)
        searchLayout = findViewById(R.id.searchLayout)
        searchInput = findViewById(R.id.searchInput)
        notificationsButton = findViewById(R.id.notificationsButton)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        tabLayout = findViewById(R.id.tabLayout)

        // Set click listeners for the toolbar buttons
        searchButton.setOnClickListener {
            toggleSearchVisibility(!isSearchVisible)
        }

        notificationsButton.setOnClickListener {
            Toast.makeText(this, "Notifications feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSearchVisibility(show: Boolean) {
        isSearchVisible = show

        if (show) {
            searchLayout.visibility = View.VISIBLE
            searchButton.visibility = View.GONE
            toolbarTitle.visibility = View.GONE  // Hide title when search is visible
            searchInput.requestFocus()

            // Show keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
        } else {
            searchLayout.visibility = View.GONE
            searchButton.visibility = View.VISIBLE
            toolbarTitle.visibility = View.VISIBLE  // Show title when search is hidden
            searchInput.text?.clear()
            hideKeyboard()

            // Clear search results
            currentSearchListener?.onSearch("")
        }
    }

    private fun setupSearchView() {
        searchInput.setOnEditorActionListener { textView, _, _ ->
            currentSearchListener?.onSearch(textView.text.toString().trim())
            true
        }
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Hide search when changing tabs
                if (isSearchVisible) {
                    toggleSearchVisibility(false)
                }

                // Clear the dashboard widgets container when switching tabs
                supportFragmentManager.findFragmentById(R.id.dashboardWidgetsContainer)?.let {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }

                when (tab.position) {
                    TAB_MY_PATIENTS -> {
                        val fragment = MyPatientsFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contentFrame, fragment)
                            .commitNow()

                        // Add upcoming appointments widget for My Patients tab
                        supportFragmentManager.beginTransaction()
                            .add(R.id.dashboardWidgetsContainer, UpcomingAppointmentsFragment.newInstance())
                            .commit()

                        currentSearchListener = fragment
                        toolbarTitle.text = "My Patients"
                    }
                    TAB_ALL_PATIENTS -> {
                        val fragment = AllPatientsFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contentFrame, fragment)
                            .commitNow()
                        currentSearchListener = fragment
                        toolbarTitle.text = "All Patients"
                    }
                    TAB_SCHEDULE -> {
                        val fragment = ScheduleFragment.newInstance()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contentFrame, fragment)
                            .commitNow()
                        currentSearchListener = null
                        toolbarTitle.text = "Schedule"
                    }
                    TAB_SETTINGS -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contentFrame, SettingsFragment())
                            .addToBackStack("settings")
                            .commit()
                        currentSearchListener = null
                        toolbarTitle.text = "Settings"
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onBackPressed() {
        if (isSearchVisible) {
            toggleSearchVisibility(false)
            return
        }

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

// Define PatientType enum
enum class PatientType {
    MY_PATIENTS, ALL_PATIENTS
}

// Define SearchListener interface
interface SearchListener {
    fun onSearch(query: String)
}