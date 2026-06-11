package com.example.spendquest

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LogExpenseActivity : AppCompatActivity() {

    private val popupQueue = mutableListOf<() -> Unit>()
    private var finishAfterPopups = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_expense)
        supportActionBar?.apply {
            title = "Log Expense"
            setDisplayHomeAsUpEnabled(true)
        }

        loadHeaderAndDemoControls()

        val spinner = findViewById<Spinner>(R.id.spinnerCategory)
        ArrayAdapter.createFromResource(
            this, R.array.expense_categories, android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it
        }

        val btnTypeExpense = findViewById<Button>(R.id.btnTypeExpense)
        val btnTypeIncome = findViewById<Button>(R.id.btnTypeIncome)
        var isIncome = false

        btnTypeExpense.setOnClickListener {
            isIncome = false
            btnTypeExpense.backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFFF5C518.toInt())
            btnTypeExpense.setTextColor(0xFF0D1117.toInt())
            btnTypeIncome.backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF161B22.toInt())
            btnTypeIncome.setTextColor(0xFF8B949E.toInt())
            spinner.visibility = View.VISIBLE
        }

        btnTypeIncome.setOnClickListener {
            isIncome = true
            btnTypeIncome.backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF56D364.toInt())
            btnTypeIncome.setTextColor(0xFF0D1117.toInt())
            btnTypeExpense.backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF161B22.toInt())
            btnTypeExpense.setTextColor(0xFF8B949E.toInt())
            spinner.visibility = View.GONE
        }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val amtText = findViewById<EditText>(R.id.etAmount).text.toString()
            val amount = amtText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Enter a valid amount!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            finishAfterPopups = true
            if (isIncome) {
                handleIncome(amount)
            } else {
                handleExpense(amount, spinner.selectedItem.toString())
            }
        }

        findViewById<Button>(R.id.btnAddDemoExp).setOnClickListener {
            val expText = findViewById<EditText>(R.id.etDemoExpAmount).text.toString()
            val expAmount = expText.toIntOrNull()
            if (expAmount == null || expAmount <= 0) {
                Toast.makeText(this, "Enter a valid EXP amount!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            finishAfterPopups = false
            handleDemoExpAdd(expAmount)
        }
    }

    private fun loadHeaderAndDemoControls() {
        FirestoreManager.loadActiveProfile { profile ->
            if (profile == null) {
                finish()
                return@loadActiveProfile
            }

            val (expIn, expMax) = RPGEngine.getEXPProgress(profile)
            val displayTitle = if (profile.achievementTitle.isNotEmpty())
                profile.achievementTitle else profile.levelTitle

            runOnUiThread {
                findViewById<TextView>(R.id.tvLevelBadge).text = "LVL ${profile.currentLevel}"
                findViewById<TextView>(R.id.tvCharName).text = displayTitle
                findViewById<TextView>(R.id.tvExpBadge).text = "$expIn / $expMax EXP"

                findViewById<LinearLayout>(R.id.layoutDemoExpControls).visibility =
                    if (profile.isDemoMode) View.VISIBLE else View.GONE
            }
        }
    }

    private fun handleIncome(amount: Double) {
        FirestoreManager.loadActiveProfile { profile ->
            if (profile == null) return@loadActiveProfile

            FirestoreManager.loadActiveExpenses(profile.isDemoMode) { expenses ->
                val dateText = if (profile.isDemoMode)
                    DateManager.demoDisplayDate(profile.demoDay, profile.demoMonth, profile.demoYear)
                else
                    DateManager.todayDisplayDate()

                expenses.add(
                    Expense(
                        amount = amount,
                        category = "Income",
                        day = if (profile.isDemoMode) profile.demoDay else DateManager.todayDayOfMonth(),
                        type = "Income",
                        dateText = dateText,
                        cycleIndex = profile.currentCycleIndex,
                        cycleLabel = profile.currentCycleLabel,
                        baseBudget = profile.monthlyBudget
                    )
                )

                profile.extraIncome += amount
                profile.currentSavings = profile.effectiveBudget - profile.currentSpending
                profile.currentCycleHadActivity = true

                FirestoreManager.saveActiveProfile(profile)
                FirestoreManager.saveActiveExpenses(expenses, profile.isDemoMode)

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "+RM%.2f added to this month's budget! New budget: RM%.2f"
                            .format(amount, profile.effectiveBudget),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun handleExpense(amount: Double, category: String) {
        FirestoreManager.loadActiveProfile { profile ->
            if (profile == null) return@loadActiveProfile

            FirestoreManager.loadActiveExpenses(profile.isDemoMode) { expenses ->
                val usedList = profile.usedCategories
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toMutableList()

                if (category !in usedList) {
                    usedList.add(category)
                    profile.usedCategories = usedList.joinToString(",")
                }

                val expenseDay = if (profile.isDemoMode) profile.demoDay else profile.currentDay
                val dateText = if (profile.isDemoMode)
                    DateManager.demoDisplayDate(profile.demoDay, profile.demoMonth, profile.demoYear)
                else
                    DateManager.todayDisplayDate()

                expenses.add(
                    Expense(
                        amount = amount,
                        category = category,
                        day = expenseDay,
                        type = "Expense",
                        dateText = dateText,
                        cycleIndex = profile.currentCycleIndex,
                        cycleLabel = profile.currentCycleLabel,
                        baseBudget = profile.monthlyBudget
                    )
                )

                profile.currentSpending += amount
                profile.currentSavings = profile.effectiveBudget - profile.currentSpending
                profile.currentCycleHadActivity = true

                val result = RPGEngine.onExpenseLogged(profile, expenses)

                FirestoreManager.saveActiveProfile(profile)
                FirestoreManager.saveActiveExpenses(expenses, profile.isDemoMode)

                runOnUiThread {
                    if (result.leveledUp) {
                        popupQueue.add { showLevelUpPopup(result.newLevel) }
                    }
                    if (result.titleChanged) {
                        popupQueue.add { showTitleChangePopup(result.newTitle) }
                    }
                    result.newAchievements.forEach { ach ->
                        popupQueue.add { showAchievementPopup(ach) }
                    }

                    if (popupQueue.isEmpty()) {
                        val msg = when {
                            result.dailyCapReached -> "Daily EXP cap reached! Come back tomorrow."
                            else -> "+${result.expGained} EXP gained!"
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        showNextPopup()
                    }
                }
            }
        }
    }

    private fun handleDemoExpAdd(expAmount: Int) {
        FirestoreManager.loadActiveProfile { profile ->
            if (profile == null) return@loadActiveProfile
            if (!profile.isDemoMode) return@loadActiveProfile

            val oldLevel = profile.currentLevel
            val oldTitle = profile.levelTitle

            profile.currentEXP += expAmount

            val newLevel = RPGEngine.levelFromExp(profile.currentEXP)
            if (newLevel > oldLevel) {
                profile.currentLevel = newLevel
            }

            val newTitle = RPGEngine.getLevelTitle(profile.currentLevel)
            if (newTitle != oldTitle) {
                profile.levelTitle = newTitle
            }

            FirestoreManager.saveActiveProfile(profile)

            runOnUiThread {
                findViewById<EditText>(R.id.etDemoExpAmount).setText("")

                if (profile.currentLevel > oldLevel) {
                    popupQueue.add { showLevelUpPopup(profile.currentLevel) }
                }
                if (profile.levelTitle != oldTitle) {
                    popupQueue.add { showTitleChangePopup(profile.levelTitle) }
                }

                if (popupQueue.isEmpty()) {
                    Toast.makeText(this, "+$expAmount demo EXP added!", Toast.LENGTH_SHORT).show()
                    loadHeaderAndDemoControls()
                } else {
                    showNextPopup()
                }
            }
        }
    }

    private fun showNextPopup() {
        if (popupQueue.isEmpty()) {
            loadHeaderAndDemoControls()
            if (finishAfterPopups) finish()
            return
        }
        val next = popupQueue.removeAt(0)
        next()
    }

    private fun showLevelUpPopup(newLevel: Int) {
        val title = RPGEngine.getLevelTitle(newLevel)
        showFullscreenPopup(
            emoji = "⬆️",
            label = "LEVEL UP!",
            labelColor = 0xFF56D364.toInt(),
            mainText = "Level $newLevel",
            subText = "You are now a $title",
            btnText = "Got it!"
        )
    }

    private fun showTitleChangePopup(newTitle: String) {
        showFullscreenPopup(
            emoji = "✨",
            label = "TITLE UNLOCKED",
            labelColor = 0xFFF5C518.toInt(),
            mainText = newTitle,
            subText = "You are now known as this title",
            btnText = "Claim Title"
        )
    }

    private fun showAchievementPopup(result: RPGResult) {
        showFullscreenPopup(
            emoji = result.achievementIcon,
            label = "ACHIEVEMENT UNLOCKED",
            labelColor = 0xFFF5C518.toInt(),
            mainText = result.unlockedAchievement,
            subText = "${result.achievementDesc}\n\n+${result.expGained} XP",
            btnText = "Awesome!"
        )
    }

    private fun showFullscreenPopup(
        emoji: String,
        label: String,
        labelColor: Int,
        mainText: String,
        subText: String,
        btnText: String
    ) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(0xDD000000.toInt())
            setPadding(48, 0, 48, 0)
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val card = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(0xFF161B22.toInt())
            setPadding(48, 56, 48, 48)
        }

        card.addView(android.widget.TextView(this).apply {
            text = emoji
            textSize = 52f
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 16 }
        })

        card.addView(android.widget.TextView(this).apply {
            text = label
            textSize = 11f
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(labelColor)
            letterSpacing = 0.15f
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 12 }
        })

        card.addView(android.widget.TextView(this).apply {
            text = mainText
            textSize = 26f
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 16 }
        })

        card.addView(android.widget.TextView(this).apply {
            text = subText
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setTextColor(0xFF8B949E.toInt())
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 28 }
        })

        card.addView(android.widget.Button(this).apply {
            text = btnText
            setBackgroundColor(0xFFF5C518.toInt())
            setTextColor(0xFF0D1117.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setOnClickListener {
                dialog.dismiss()
                showNextPopup()
            }
        })

        root.addView(card)
        dialog.setContentView(root)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}