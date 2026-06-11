package com.example.spendquest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        // If already logged in, skip to main
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val progress   = findViewById<ProgressBar>(R.id.progressLogin)

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            btnLogin.isEnabled  = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progress.visibility = View.GONE
                    btnLogin.isEnabled  = true
                    if (task.isSuccessful) {
                        goToMain()
                    } else {
                        Toast.makeText(this,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMain() {
        FirestoreManager.loadProfile { profile ->
            val intent = if (profile == null) {
                Intent(this, SetupActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}