package com.example.spendquest

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        supportActionBar?.hide()

        val etBudget = findViewById<EditText>(R.id.etBudget)
        val etSavingsGoal = findViewById<EditText>(R.id.etSavingsGoal)
        val spinnerResetDay = findViewById<Spinner>(R.id.spinnerResetDay)
        val btnStart = findViewById<Button>(R.id.btnStart)

        // Populate reset day spinner 1–28
        val days = (1..28).map {
            when (it) {
                1 -> "1st"
                2 -> "2nd"
                3 -> "3rd"
                else -> "${it}th"
            }
        }

        spinnerResetDay.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            days
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        btnStart.setOnClickListener {
            val budget = etBudget.text.toString().toDoubleOrNull()
            val goal = etSavingsGoal.text.toString().toDoubleOrNull() ?: 0.0
            val resetDay = spinnerResetDay.selectedItemPosition + 1

            if (budget == null || budget <= 0) {
                Toast.makeText(this, "Enter a valid budget!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

            val profile = UserProfile(
                uid = uid,
                email = email,
                monthlyBudget = budget,
                currentSavings = budget,
                savingsGoal = goal,
                levelTitle = "Commoner",
                budgetSetDone = true,
                savingsGoalSetDone = goal > 0,
                budgetResetDay = resetDay,
                currentYearMonth = DateManager.currentYearMonth(),
                currentCalendarYearMonth = DateManager.currentYearMonth(),
                currentCycleIndex = 1,
                currentCycleLabel = DateManager.monthNameOnly(),
                lastRealDate = DateManager.todayString()
            )

            FirestoreManager.saveProfile(profile) { success ->
                runOnUiThread {
                    if (success) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to save. Check connection!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}