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

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        val etEmail     = findViewById<EditText>(R.id.etRegEmail)
        val etPassword  = findViewById<EditText>(R.id.etRegPassword)
        val etConfirm   = findViewById<EditText>(R.id.etRegConfirm)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin     = findViewById<TextView>(R.id.tvLogin)
        val progress    = findViewById<ProgressBar>(R.id.progressRegister)

        btnRegister.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm  = etConfirm.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progress.visibility  = View.VISIBLE
            btnRegister.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progress.visibility   = View.GONE
                    btnRegister.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created! Set up your quest.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, SetupActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this,
                            "Register failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLogin.setOnClickListener { finish() }
    }
}