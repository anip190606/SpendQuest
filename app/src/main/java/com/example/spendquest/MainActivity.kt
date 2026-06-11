package com.example.spendquest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        bottomNav = findViewById(R.id.bottomNav)

        findViewById<FloatingActionButton>(R.id.fabLogExpense).setOnClickListener {
            startActivity(Intent(this, LogExpenseActivity::class.java))
        }

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_achievements -> {
                    loadFragment(AchievementsFragment())
                    true
                }
                R.id.nav_report -> {
                    loadFragment(ReportFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_dashboard
        showMonthlyResetDialogIfNeeded()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showMonthlyResetDialogIfNeeded() {
        val budgetReset = intent.getBooleanExtra("budget_reset_happened", false)
        if (!budgetReset) return

        val names = intent.getStringArrayListExtra("monthly_achievement_names") ?: arrayListOf()

        val msg = if (names.isEmpty()) {
            "Your monthly budget has been reset for the new month."
        } else {
            "Your monthly budget has been reset for the new month.\n\nUnlocked:\n• " +
                    names.joinToString("\n• ")
        }

        AlertDialog.Builder(this)
            .setTitle("New Month Started")
            .setMessage(msg)
            .setPositiveButton("Nice") { dialog, _ -> dialog.dismiss() }
            .show()

        intent.removeExtra("budget_reset_happened")
        intent.removeExtra("monthly_achievement_names")
    }
}