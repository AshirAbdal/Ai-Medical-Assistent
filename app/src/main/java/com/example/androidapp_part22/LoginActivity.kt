package com.example.androidapp_part22

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout defined in login.xml
        setContentView(R.layout.login)

        // Get references to the input fields and the sign in button
        val usernameEditText = findViewById<TextInputEditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val orgCodeEditText = findViewById<TextInputEditText>(R.id.orgCodeEditText)
        val signInButton = findViewById<Button>(R.id.signInButton)

        signInButton.setOnClickListener {
            // Retrieve and trim user inputs
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val orgCode = orgCodeEditText.text.toString().trim()

            // Basic validation: Check that no field is empty
            if (username.isNotEmpty() && password.isNotEmpty() && orgCode.isNotEmpty()) {
                // Here you can add any authentication logic (e.g., API call to validate credentials)
                // For demonstration, we just show a Toast and navigate to MainActivity.
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                // Navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                // Optionally finish the login activity so the user cannot return to it
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
