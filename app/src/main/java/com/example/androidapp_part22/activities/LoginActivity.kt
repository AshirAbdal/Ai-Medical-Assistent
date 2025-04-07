package com.example.androidapp_part22.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.androidapp_part22.R
import com.google.android.material.textfield.TextInputEditText

 public final class LoginActivity : AppCompatActivity() {

    // Default credentials for testing
    private val DEFAULT_USERNAME = "testUser"
    private val DEFAULT_PASSWORD = "test123"
    private val DEFAULT_ORG_CODE = "ORG456"

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        // Add this line for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (!it.value) {
                    Toast.makeText(
                        this,
                        "Permission ${it.key} denied. Some features may not work properly.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        requestAppPermissions()

        val usernameEditText = findViewById<TextInputEditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val orgCodeEditText = findViewById<TextInputEditText>(R.id.orgCodeEditText)
        val signInButton = findViewById<Button>(R.id.signInButton)

        // Auto-fill default credentials
        usernameEditText.setText(DEFAULT_USERNAME)
        passwordEditText.setText(DEFAULT_PASSWORD)
        orgCodeEditText.setText(DEFAULT_ORG_CODE)

        signInButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val orgCode = orgCodeEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty() && orgCode.isNotEmpty()) {
                if (username == DEFAULT_USERNAME &&
                    password == DEFAULT_PASSWORD &&
                    orgCode == DEFAULT_ORG_CODE
                ) {

                    Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                    // Redirect to DashboardActivity
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestAppPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}