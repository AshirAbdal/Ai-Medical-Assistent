package com.example.androidapp_part22.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.androidapp_part22.fragments.JournalFragment
import com.example.androidapp_part22.fragments.MedicationsFragment
import com.example.androidapp_part22.fragments.ReportsFragment
import com.example.androidapp_part22.helpers.ProfileImageHelper
import com.example.androidapp_part22.models.Patient
import com.example.androidapp_part22.R
import com.google.android.material.tabs.TabLayout
import com.mikhaellopez.circularimageview.CircularImageView

class PatientProfileActivity : AppCompatActivity() {

    private lateinit var profileImageHelper: ProfileImageHelper
    private lateinit var profileImage: CircularImageView
    private lateinit var patientNameTextView: TextView
    private lateinit var patientAgeGenderTextView: TextView
    private lateinit var patientIdTextView: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var patient: Patient

    // Tab indices - updated without Upload tab
    private val TAB_JOURNAL = 0
    private val TAB_REPORTS = 1
    private val TAB_MEDICATIONS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        // Get the patient data from intent
        patient = intent.getParcelableExtra("SELECTED_PATIENT") ?: run {
            finish() // Close activity if no patient data
            return
        }

        // Initialize the helper
        profileImageHelper = ProfileImageHelper(this)

        // Initialize views
        initViews()

        // Setup UI with patient data
        setupPatientInfo()

        // Setup tabs
        setupTabLayout()

        // Load the default fragment (Journal now that Upload is removed)
        loadFragment(JournalFragment.newInstance(patient))
    }

    private fun initViews() {
        // Back button setup
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finishWithAnimation()
        }

        // Patient info views
        profileImage = findViewById(R.id.patientProfileImage)
        patientNameTextView = findViewById(R.id.patientNameTextView)
        patientAgeGenderTextView = findViewById(R.id.patientAgeGenderTextView)
        patientIdTextView = findViewById(R.id.patientIdTextView)

        // Profile picture setup - still leaving this for patient profile picture
        profileImage.setOnClickListener {
            profileImageHelper.showImageSourceDialog()
        }

        // Tab layout
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupPatientInfo() {
        patientNameTextView.text = patient.name
        patientAgeGenderTextView.text = "Age: ${patient.age}, Gender: ${patient.gender}"
        patientIdTextView.text = "ID: ${patient.id}"
    }

    private fun setupTabLayout() {
        // Clear existing tabs if any
        tabLayout.removeAllTabs()

        // Add tabs - without Upload tab
        tabLayout.addTab(tabLayout.newTab().setText("Journal"))
        tabLayout.addTab(tabLayout.newTab().setText("Reports"))
        tabLayout.addTab(tabLayout.newTab().setText("Medications"))

        // Set tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    TAB_JOURNAL -> loadFragment(JournalFragment.newInstance(patient))
                    TAB_REPORTS -> loadFragment(ReportsFragment.newInstance(patient))
                    TAB_MEDICATIONS -> loadFragment(MedicationsFragment.newInstance(patient))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) { /* Not needed */ }
            override fun onTabReselected(tab: TabLayout.Tab?) { /* Not needed */ }
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        profileImageHelper.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        profileImageHelper.handleActivityResult(requestCode, resultCode, data, profileImage)
    }

    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishWithAnimation()
    }
}