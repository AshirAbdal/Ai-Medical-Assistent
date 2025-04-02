package com.example.androidapp_part22.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.androidapp_part22.helpers.ProfileImageHelper
import com.example.androidapp_part22.R
import com.mikhaellopez.circularimageview.CircularImageView

class PatientProfileActivity : AppCompatActivity() {

    private lateinit var profileImageHelper: ProfileImageHelper
    private lateinit var profileImage: CircularImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        // Initialize the helper
        profileImageHelper = ProfileImageHelper(this)

        // Back button setup
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finishWithAnimation()
        }

        // Profile picture setup
        profileImage = findViewById(R.id.patientProfileImage)
        profileImage.setOnClickListener {
            profileImageHelper.showImageSourceDialog()
        }
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