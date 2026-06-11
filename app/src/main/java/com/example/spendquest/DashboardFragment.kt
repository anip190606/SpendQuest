package com.example.spendquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.util.Calendar

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnAdvanceDay).setOnClickListener {
            FirestoreManager.loadActiveProfile { profile ->
                if (profile == null) return@loadActiveProfile
                if (!profile.isDemoMode) return@loadActiveProfile

                val r = RPGEngine.advanceDemoDay(profile)
                FirestoreManager.saveActiveProfile(profile)

                activity?.runOnUiThread {
                    updateDashboard(view)

                    if (r.monthReset) {
                        val msg = if (r.newAchievements.isEmpty()) {
                            "Monthly allowance reset done."
                        } else {
                            "Monthly allowance reset done.\n\nUnlocked:\n• " +
                                    r.newAchievements.joinToString("\n• ") { it.unlockedAchievement }
                        }

                        AlertDialog.Builder(requireContext())
                            .setTitle("New Allowance Cycle Started")
                            .setMessage(msg)
                            .setPositiveButton("Nice", null)
                            .show()
                    } else {
                        Toast.makeText(context, "⏭ Demo Day ${r.newDay}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        view.findViewById<Button>(R.id.btnAdvanceMonth).setOnClickListener {
            FirestoreManager.loadActiveProfile { profile ->
                if (profile == null) return@loadActiveProfile
                if (!profile.isDemoMode) return@loadActiveProfile

                var finalResult = RPGResult()
                var safety = 0

                while (!finalResult.monthReset && safety < 40) {
                    finalResult = RPGEngine.advanceDemoDay(profile)
                    safety++
                }

                FirestoreManager.saveActiveProfile(profile)

                activity?.runOnUiThread {
                    updateDashboard(view)

                    val msg = if (finalResult.monthReset) {
                        if (finalResult.newAchievements.isEmpty()) {
                            "Jumped to the next month successfully."
                        } else {
                            "Jumped to the next month successfully.\n\nUnlocked:\n• " +
                                    finalResult.newAchievements.joinToString("\n• ") { it.unlockedAchievement }
                        }
                    } else {
                        "Could not reach the next month safely."
                    }

                    AlertDialog.Builder(requireContext())
                        .setTitle("Demo Month Advance")
                        .setMessage(msg)
                        .setPositiveButton("Nice", null)
                        .show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { updateDashboard(it) }
    }

    private fun updateDashboard(view: View) {
        FirestoreManager.loadActiveProfile { p ->
            if (p == null) return@loadActiveProfile

            if (p.currentYearMonth.isEmpty()) {
                p.currentYearMonth = if (p.isDemoMode) "${p.demoYear}-${p.demoMonth}"
                else DateManager.currentYearMonth()
                FirestoreManager.saveActiveProfile(p)
            }

            val (expIn, expMax, _) = RPGEngine.getEXPProgress(p)
            val savingsGoalPct = if (p.savingsGoal > 0)
                (p.currentSavings / p.savingsGoal * 100).toInt().coerceIn(0, 100) else 0
            val displayTitle = if (p.achievementTitle.isNotEmpty())
                p.achievementTitle else p.levelTitle

            activity?.runOnUiThread {
                val dayLabel = if (p.isDemoMode) {
                    val dateText = String.format("%02d/%02d/%04d", p.demoDay, p.demoMonth, p.demoYear)
                    "🎮 Demo — Day ${p.demoDay} ($dateText)"
                } else {
                    val cal = Calendar.getInstance()
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    val month = cal.get(Calendar.MONTH) + 1
                    val year = cal.get(Calendar.YEAR)
                    val dateText = String.format("%02d/%02d/%04d", day, month, year)
                    "Day $day ($dateText)"
                }

                view.findViewById<TextView>(R.id.tvDay).text = dayLabel
                view.findViewById<TextView>(R.id.tvLevel).text = "LVL ${p.currentLevel}"
                view.findViewById<TextView>(R.id.tvTitle).text = displayTitle
                view.findViewById<TextView>(R.id.tvSubtitle).text =
                    if (p.achievementTitle.isNotEmpty()) "Level: ${p.levelTitle}"
                    else "Earn achievements to unlock special titles"
                view.findViewById<TextView>(R.id.tvExpProgress).text = "$expIn / $expMax XP"
                view.findViewById<ProgressBar>(R.id.progressEXP).apply {
                    max = expMax
                    progress = expIn
                }

                val effectiveBudget = p.effectiveBudget
                val pct = if (effectiveBudget > 0)
                    (p.currentSpending / effectiveBudget * 100.0).toInt() else 0

                view.findViewById<TextView>(R.id.tvBudget).text =
                    "RM ${String.format("%,.2f", effectiveBudget)}"
                view.findViewById<TextView>(R.id.tvSpent).text =
                    "RM ${String.format("%,.2f", p.currentSpending)}"
                view.findViewById<TextView>(R.id.tvSpentPercent).text = "$pct% of budget"
                view.findViewById<TextView>(R.id.tvSavings).text =
                    "RM ${String.format("%,.2f", p.currentSavings)}"
                view.findViewById<TextView>(R.id.tvSavingsStatus).text =
                    if (p.currentSavings >= 0) "Safe zone" else "Over budget!"

                view.findViewById<TextView>(R.id.tvSavingsGoal).text =
                    "SAVINGS GOAL: RM ${String.format("%,.0f", p.savingsGoal)}"
                view.findViewById<ProgressBar>(R.id.progressSavingsGoal).apply {
                    max = 100
                    progress = savingsGoalPct
                }
                view.findViewById<TextView>(R.id.tvSavingsGoalPct).text = "$savingsGoalPct%"

                val bossMax = effectiveBudget.toInt().coerceAtLeast(1)
                view.findViewById<ProgressBar>(R.id.progressBoss).apply {
                    max = bossMax
                    progress = p.currentSpending.toInt().coerceAtMost(bossMax)
                }
                view.findViewById<TextView>(R.id.tvBossHealth).text =
                    "RM ${p.currentSpending.toInt()} / RM ${effectiveBudget.toInt()}"
                view.findViewById<TextView>(R.id.tvBossMessage).text =
                    if (p.currentSpending <= effectiveBudget)
                        "Within budget!"
                    else
                        "Budget exceeded! ⚠️"

                view.findViewById<Button>(R.id.btnAdvanceDay).visibility =
                    if (p.isDemoMode) View.VISIBLE else View.GONE

                view.findViewById<Button>(R.id.btnAdvanceMonth).visibility =
                    if (p.isDemoMode) View.VISIBLE else View.GONE
            }
        }
    }
}