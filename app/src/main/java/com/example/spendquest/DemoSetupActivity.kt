package com.example.spendquest

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class DemoSetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_setup)
        supportActionBar?.hide()

        val etBudget     = findViewById<EditText>(R.id.etDemoBudget)
        val etGoal       = findViewById<EditText>(R.id.etDemoGoal)
        val spinnerDay   = findViewById<Spinner>(R.id.spinnerDemoStartDay)
        val spinnerMonth = findViewById<Spinner>(R.id.spinnerDemoStartMonth)
        val spinnerYear  = findViewById<Spinner>(R.id.spinnerDemoStartYear)
        val btnStart     = findViewById<Button>(R.id.btnStartDemo)

        // Day spinner 1-28 (safe for all months)
        spinnerDay.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, (1..28).map { it.toString() }).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Month spinner
        val months = listOf("January","February","March","April","May","June",
            "July","August","September","October","November","December")
        spinnerMonth.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, months).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Year spinner — current year + next 2
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear..currentYear + 2).map { it.toString() }
        spinnerYear.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, years).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Default to today
        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_MONTH).coerceAtMost(28)
        spinnerDay.setSelection(todayDay - 1)
        spinnerMonth.setSelection(today.get(Calendar.MONTH))

        btnStart.setOnClickListener {
            val budget = etBudget.text.toString().toDoubleOrNull()
            val goal   = etGoal.text.toString().toDoubleOrNull() ?: 0.0
            val day    = spinnerDay.selectedItemPosition + 1
            val month  = spinnerMonth.selectedItemPosition + 1
            val year   = years[spinnerYear.selectedItemPosition].toInt()

            if (budget == null || budget <= 0) {
                Toast.makeText(this, "Enter a valid budget!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid   = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

            val demoProfile = UserProfile(
                uid                = uid,
                email              = email,
                monthlyBudget      = budget,
                currentSavings     = budget,
                savingsGoal        = goal,
                levelTitle         = "Commoner",
                budgetSetDone      = true,
                savingsGoalSetDone = goal > 0,
                isDemoMode         = true,
                demoYear           = year,
                demoMonth          = month,
                demoDay            = day,
                demoBudgetResetDay = day,
                currentYearMonth   = "$year-$month",
                lastRealDate       = "$year-$month-$day"
            )

            FirestoreManager.saveDemoProfile(demoProfile) { success ->
                if (success) {
                    runOnUiThread {
                        Toast.makeText(this, "🎮 Demo started!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Failed. Check connection!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}