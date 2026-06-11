package com.example.spendquest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@postDelayed
            }

            FirestoreManager.loadProfile { profile ->
                if (profile == null) {
                    runOnUiThread {
                        startActivity(Intent(this, SetupActivity::class.java))
                        finish()
                    }
                    return@loadProfile
                }

                var changed = false
                var budgetResetHappened = false
                val monthlyAchievementNames = ArrayList<String>()

                val currentCalendarMonth = DateManager.currentYearMonth()

                // First-time init
                if (profile.currentYearMonth.isEmpty()) {
                    profile.currentYearMonth = currentCalendarMonth
                    changed = true
                }

                if (profile.currentCalendarYearMonth.isEmpty()) {
                    profile.currentCalendarYearMonth = currentCalendarMonth
                    changed = true
                }

                if (profile.currentCycleIndex <= 0) {
                    profile.currentCycleIndex = 1
                    changed = true
                }

                if (profile.currentCycleLabel.isEmpty()) {
                    profile.currentCycleLabel = DateManager.monthNameOnly()
                    changed = true
                }

                if (profile.lastRealDate.isEmpty()) {
                    profile.lastRealDate = DateManager.todayString()
                    changed = true
                }

                // Track actual calendar month separately
                if (profile.currentCalendarYearMonth != currentCalendarMonth) {
                    profile.currentCalendarYearMonth = currentCalendarMonth
                    changed = true
                }

                // New real day
                if (DateManager.isNewDay(profile.lastRealDate)) {
                    if (DateManager.isStreakBroken(profile.lastRealDate)) {
                        profile.consecutiveDaysLogged = 0
                    }
                    profile.dailyEXPEarned = 0
                    profile.todayExpenseCount = 0
                    profile.lastRealDate = DateManager.todayString()
                    changed = true
                }

                // Allowance / budget-cycle reset
                if (DateManager.isBudgetResetDay(profile.budgetResetDay, profile.currentYearMonth)) {
                    budgetResetHappened = true

                    val effectiveBudget = profile.effectiveBudget
                    profile.currentSavings = effectiveBudget - profile.currentSpending

                    if (profile.currentSpending <= effectiveBudget && profile.currentSpending > 0) {
                        profile.monthsUnderBudget++
                    }

                    val savePct = if (effectiveBudget > 0)
                        (effectiveBudget - profile.currentSpending) / effectiveBudget * 100 else 0.0

                    if (savePct >= 40.0) profile.monthsWithSavings40++

                    if (profile.currentCycleHadActivity) {
                        profile.monthsWithEXP++
                        profile.consecutiveMonthsLogged++
                    }

                    val monthlyResults = AchievementManager.checkMonthly(profile)
                    monthlyResults.forEach { monthlyAchievementNames.add(it.unlockedAchievement) }

                    val newLevel = RPGEngine.levelFromExp(profile.currentEXP)
                    if (newLevel > profile.currentLevel) {
                        profile.currentLevel = newLevel
                    }
                    profile.levelTitle = RPGEngine.getLevelTitle(profile.currentLevel)

                    // Reset cycle values
                    profile.currentSpending = 0.0
                    profile.extraIncome = 0.0
                    profile.currentSavings = profile.monthlyBudget
                    profile.currentYearMonth = currentCalendarMonth
                    profile.currentCycleHadActivity = false

                    // Move to next cycle for report tabs/history
                    profile.currentCycleIndex += 1
                    profile.currentCycleLabel = DateManager.monthNameOnly()

                    changed = true
                }

                if (changed) FirestoreManager.saveProfile(profile)

                runOnUiThread {
                    val intent = if (!profile.budgetSetDone)
                        Intent(this, SetupActivity::class.java)
                    else
                        Intent(this, MainActivity::class.java)

                    if (budgetResetHappened) {
                        intent.putExtra("budget_reset_happened", true)
                        intent.putStringArrayListExtra(
                            "monthly_achievement_names",
                            monthlyAchievementNames
                        )
                    }

                    startActivity(intent)
                    finish()
                }
            }
        }, 2000)
    }
}