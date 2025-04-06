package com.example.androidapp_part22.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var backButton: ImageButton
    private lateinit var searchPatientButton: ImageButton
    private lateinit var editPatientButton: ImageButton
    private lateinit var toolbarTitle: TextView
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

        // Setup toolbar actions
        setupToolbarActions()

        // Setup UI with patient data
        setupPatientInfo()

        // Setup tabs
        setupTabLayout()

        // Load the default fragment (Journal now that Upload is removed)
        loadFragment(JournalFragment.newInstance(patient))
    }

    private fun initViews() {
        // Toolbar views
        backButton = findViewById(R.id.backButton)
        searchPatientButton = findViewById(R.id.searchPatientButton)
        editPatientButton = findViewById(R.id.editPatientButton)
        toolbarTitle = findViewById(R.id.toolbarTitle)

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

    private fun setupToolbarActions() {
        // Set the patient name in the toolbar title


        // Back button setup
        backButton.setOnClickListener {
            finishWithAnimation()
        }

        // Search button setup
        searchPatientButton.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Edit button setup
        editPatientButton.setOnClickListener {
            showEditOptions()
        }
    }

    private fun showEditOptions() {
        val options = arrayOf("Edit Profile", "Add Note", "Schedule Appointment", "Archive Patient")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Patient Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show()
                    1 -> {
                        // Switch to Journal tab and focus on new entry
                        tabLayout.getTabAt(TAB_JOURNAL)?.select()
                        // Would need a method in JournalFragment to focus on new entry input
                    }
                    2 -> Toast.makeText(this, "Schedule Appointment feature coming soon", Toast.LENGTH_SHORT).show()
                    3 -> Toast.makeText(this, "Archive Patient feature coming soon", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
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