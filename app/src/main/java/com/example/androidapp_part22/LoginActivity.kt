package com.example.androidapp_part22

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    // Default credentials for testing
    private val DEFAULT_USERNAME = "testUser"
    private val DEFAULT_PASSWORD = "test123"
    private val DEFAULT_ORG_CODE = "ORG456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

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
                    orgCode == DEFAULT_ORG_CODE) {

                    Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
