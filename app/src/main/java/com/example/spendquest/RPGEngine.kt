package com.example.spendquest

object RPGEngine {

    private const val EXP_PER_LOG = 50

    private val LEVEL_TITLES = listOf(
        1 to "Commoner",
        5 to "Coin Holder",
        10 to "Budget Novice",
        15 to "Expense Scout",
        20 to "Frugal Adventurer",
        25 to "Budget Knight",
        30 to "Silver Planner",
        40 to "Gold Planner",
        50 to "Frugal Sage",
        75 to "Master of Coins",
        100 to "Tycoon"
    )

    fun getLevelTitle(level: Int): String =
        LEVEL_TITLES.filter { it.first <= level }.maxByOrNull { it.first }?.second ?: "Commoner"

    fun getTitleRoadmap(): List<Pair<Int, String>> = LEVEL_TITLES

    fun expRequiredForLevel(level: Int): Int = when {
        level <= 5 -> 500
        level <= 20 -> 1000
        level <= 50 -> 2000
        else -> 5000
    }

    fun totalExpForLevel(level: Int): Int {
        if (level <= 1) return 0
        var total = 0
        for (l in 1 until level) total += expRequiredForLevel(l)
        return total
    }

    fun levelFromExp(totalExp: Int): Int {
        var level = 1
        var spent = 0
        while (true) {
            val needed = expRequiredForLevel(level)
            if (spent + needed > totalExp) break
            spent += needed
            level++
            if (level >= 100) break
        }
        return level
    }

    fun getEXPProgress(profile: UserProfile): Triple<Int, Int, Int> {
        val level = profile.currentLevel
        val floorExp = totalExpForLevel(level)
        val needed = expRequiredForLevel(level)
        val progress = (profile.currentEXP - floorExp).coerceAtLeast(0)
        return Triple(progress, needed, level)
    }

    fun onExpenseLogged(profile: UserProfile, expenses: List<Expense>): RPGResult {
        val result = RPGResult()
        val todayStr = DateManager.todayString()

        if (!profile.isDemoMode) {
            if (DateManager.isNewDay(profile.lastRealDate)) {
                if (DateManager.isStreakBroken(profile.lastRealDate)) {
                    profile.consecutiveDaysLogged = 0
                }
                profile.dailyEXPEarned = 0
                profile.todayExpenseCount = 0
                profile.consecutiveDaysLogged++
                profile.lastRealDate = todayStr
            }

            profile.lastLoggedDay = DateManager.todayDayOfMonth()
            profile.currentDay = DateManager.todayDayOfMonth()
        } else {
            profile.lastLoggedDay = profile.demoDay
            profile.currentDay = profile.demoDay
        }

        val remaining = UserProfile.DAILY_EXP_CAP - profile.dailyEXPEarned
        if (remaining > 0) {
            val award = minOf(EXP_PER_LOG, remaining)
            profile.currentEXP += award
            profile.dailyEXPEarned += award
            result.expGained = award
        } else {
            result.dailyCapReached = true
        }

        profile.totalExpenseCount++
        profile.todayExpenseCount++

        val achResults = AchievementManager.checkAll(profile, expenses)
        result.newAchievements.addAll(achResults)
        result.expGained += achResults.sumOf { it.expGained }

        val newLevel = levelFromExp(profile.currentEXP)
        if (newLevel > profile.currentLevel) {
            profile.currentLevel = newLevel
            result.leveledUp = true
            result.newLevel = newLevel
        }

        val newLevelTitle = getLevelTitle(profile.currentLevel)
        if (newLevelTitle != profile.levelTitle) {
            profile.levelTitle = newLevelTitle
            result.titleChanged = true
            result.newTitle = newLevelTitle
        }

        return result
    }

    fun advanceDemoDay(profile: UserProfile): RPGResult {
        val result = RPGResult(dayAdvanced = true)

        val cal = java.util.Calendar.getInstance()
        cal.set(profile.demoYear, profile.demoMonth - 1, profile.demoDay)
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1)

        val newYear = cal.get(java.util.Calendar.YEAR)
        val newMonth = cal.get(java.util.Calendar.MONTH) + 1
        val newDay = cal.get(java.util.Calendar.DAY_OF_MONTH)

        profile.demoYear = newYear
        profile.demoMonth = newMonth
        profile.demoDay = newDay
        profile.currentDay = newDay
        profile.lastRealDate = "demo-$newYear-$newMonth-$newDay"

        profile.dailyEXPEarned = 0
        profile.todayExpenseCount = 0
        profile.consecutiveDaysLogged++

        result.newDay = newDay

        if (newDay == profile.demoBudgetResetDay) {
            val effectiveBudget = profile.effectiveBudget
            profile.currentSavings = effectiveBudget - profile.currentSpending

            if (profile.currentSpending <= effectiveBudget && profile.currentSpending > 0)
                profile.monthsUnderBudget++

            val savePct = if (effectiveBudget > 0)
                (effectiveBudget - profile.currentSpending) / effectiveBudget * 100 else 0.0

            if (savePct >= 40.0) profile.monthsWithSavings40++

            if (profile.currentCycleHadActivity) {
                profile.monthsWithEXP++
                profile.consecutiveMonthsLogged++
            }

            val monthlyResults = AchievementManager.checkMonthly(profile)
            result.newAchievements.addAll(monthlyResults)
            result.expGained += monthlyResults.sumOf { it.expGained }

            val newLevel = levelFromExp(profile.currentEXP)
            if (newLevel > profile.currentLevel) {
                profile.currentLevel = newLevel
                result.leveledUp = true
                result.newLevel = newLevel
            }

            val newLevelTitle = getLevelTitle(profile.currentLevel)
            if (newLevelTitle != profile.levelTitle) {
                profile.levelTitle = newLevelTitle
                result.titleChanged = true
                result.newTitle = newLevelTitle
            }

            profile.currentSpending = 0.0
            profile.extraIncome = 0.0
            profile.currentSavings = profile.monthlyBudget
            profile.currentYearMonth = "$newYear-$newMonth"
            profile.currentCycleHadActivity = false
            profile.currentCycleIndex += 1
            profile.currentCycleLabel = DateManager.demoMonthName(newYear, newMonth)

            result.monthReset = true
            result.newMonth = newMonth
            result.newMonthYear = newYear
        }

        return result
    }
}

data class RPGResult(
    var expGained: Int = 0,
    var dailyCapReached: Boolean = false,
    var leveledUp: Boolean = false,
    var newLevel: Int = 1,
    var titleChanged: Boolean = false,
    var newTitle: String = "",
    var unlockedAchievement: String = "",
    var achievementDesc: String = "",
    var achievementIcon: String = "",
    var dayAdvanced: Boolean = false,
    var newDay: Int = 1,
    var monthReset: Boolean = false,
    var newMonth: Int = 1,
    var newMonthYear: Int = 2026,
    var newAchievements: MutableList<RPGResult> = mutableListOf()
)