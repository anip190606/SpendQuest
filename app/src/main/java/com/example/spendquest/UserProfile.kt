package com.example.spendquest

data class UserProfile(
    var uid: String = "",
    var email: String = "",

    // Financial
    var monthlyBudget: Double = 1500.0,
    var currentSpending: Double = 0.0,
    var currentSavings: Double = 1500.0,
    var savingsGoal: Double = 0.0,
    var extraIncome: Double = 0.0,
    var budgetResetDay: Int = 1,

    // RPG
    var currentEXP: Int = 0,
    var currentLevel: Int = 1,
    var levelTitle: String = "Commoner",
    var achievementTitle: String = "",

    // Day tracking
    var currentDay: Int = 1,
    var dailyEXPEarned: Int = 0,
    var lastExpenseDay: Int = 0,

    // Tracking
    var totalExpenseCount: Int = 0,
    var consecutiveDaysLogged: Int = 0,
    var lastLoggedDay: Int = 0,
    var lastRealDate: String = "",
    var currentYearMonth: String = "",              // budget cycle marker
    var currentCalendarYearMonth: String = "",      // actual calendar month marker
    var currentCycleHadActivity: Boolean = false,   // used to prevent fake 100% savings
    var currentCycleIndex: Int = 1,                 // Month 1, Month 2, ...
    var currentCycleLabel: String = "",             // March, April, ...
    var isDemoMode: Boolean = false,
    var demoDay: Int = 1,
    var demoMonth: Int = 1,
    var demoYear: Int = 2026,
    var demoBudgetResetDay: Int = 1,
    var usedCategories: String = "",
    var monthsUnderBudget: Int = 0,
    var monthsWithSavings40: Int = 0,
    var monthsWithEXP: Int = 0,
    var consecutiveMonthsLogged: Int = 0,
    var dashboardOpenCount: Int = 0,
    var hasViewedReport: Boolean = false,
    var hasComparedMonths: Boolean = false,
    var largeExpenseLogged: Boolean = false,
    var tenPurchasesOneDay: Boolean = false,
    var todayExpenseCount: Int = 0,
    var lastExpenseDayCount: Int = 0,
    var budgetSetDone: Boolean = false,
    var savingsGoalSetDone: Boolean = false,

    // Getting Started (5)
    var achFirstCoin: Boolean = false,
    var achWelcomeLedger: Boolean = false,
    var achSideQuest: Boolean = false,
    var achPaperTrail: Boolean = false,
    var achItAddsUp: Boolean = false,

    // Consistency (5)
    var achSevenDays: Boolean = false,
    var achThirtyDays: Boolean = false,
    var achQuarterMaster: Boolean = false,
    var achHalfYearHabit: Boolean = false,
    var achLegendLedger: Boolean = false,

    // Budget Discipline (5)
    var achUnderControl: Boolean = false,
    var achSteadyHand: Boolean = false,
    var achIronLedger: Boolean = false,
    var achCleanMonth: Boolean = false,
    var achPerfectBalance: Boolean = false,

    // Savings / exact-spend set (7)
    var achSave10: Boolean = false,
    var achSave25: Boolean = false,
    var achSave42: Boolean = false,
    var achSave50: Boolean = false,
    var achSave67: Boolean = false,
    var achSave70: Boolean = false,
    var achLockedVault: Boolean = false,

    // Level (3)
    var achLevel10: Boolean = false,
    var achLevel30: Boolean = false,
    var achLevel100: Boolean = false,

    // Engagement (5)
    var achMonthlyReflection: Boolean = false,
    var achLookingBack: Boolean = false,
    var achStillWatching: Boolean = false,
    var achLongGame: Boolean = false,
    var achConsistencyIntensity: Boolean = false,

    // Pop Culture (10)
    var achNotBatman: Boolean = false,
    var achMoneyMoneyMoney: Boolean = false,
    var achGatsby: Boolean = false,
    var achThriftShop: Boolean = false,
    var achKaChing: Boolean = false,
    var achTonyStark: Boolean = false,
    var achWolfBudget: Boolean = false,
    var achBuyBuyBaby: Boolean = false,
    var achChaChing: Boolean = false,
    var achWannaBeRich: Boolean = false
) {
    val effectiveBudget: Double get() = monthlyBudget + extraIncome

    companion object {
        const val DAILY_EXP_CAP = 200


        fun toMap(p: UserProfile): Map<String, Any> = mapOf(
            "uid" to p.uid,
            "email" to p.email,
            "monthlyBudget" to p.monthlyBudget,
            "currentSpending" to p.currentSpending,
            "currentSavings" to p.currentSavings,
            "savingsGoal" to p.savingsGoal,
            "extraIncome" to p.extraIncome,
            "budgetResetDay" to p.budgetResetDay,
            "currentEXP" to p.currentEXP,
            "currentLevel" to p.currentLevel,
            "levelTitle" to p.levelTitle,
            "achievementTitle" to p.achievementTitle,
            "currentDay" to p.currentDay,
            "dailyEXPEarned" to p.dailyEXPEarned,
            "lastExpenseDay" to p.lastExpenseDay,
            "totalExpenseCount" to p.totalExpenseCount,
            "consecutiveDaysLogged" to p.consecutiveDaysLogged,
            "lastLoggedDay" to p.lastLoggedDay,
            "lastRealDate" to p.lastRealDate,
            "currentYearMonth" to p.currentYearMonth,
            "currentCalendarYearMonth" to p.currentCalendarYearMonth,
            "currentCycleHadActivity" to p.currentCycleHadActivity,
            "currentCycleIndex" to p.currentCycleIndex,
            "currentCycleLabel" to p.currentCycleLabel,
            "isDemoMode" to p.isDemoMode,
            "demoDay" to p.demoDay,
            "demoMonth" to p.demoMonth,
            "demoYear" to p.demoYear,
            "demoBudgetResetDay" to p.demoBudgetResetDay,
            "usedCategories" to p.usedCategories,
            "monthsUnderBudget" to p.monthsUnderBudget,
            "monthsWithSavings40" to p.monthsWithSavings40,
            "monthsWithEXP" to p.monthsWithEXP,
            "consecutiveMonthsLogged" to p.consecutiveMonthsLogged,
            "dashboardOpenCount" to p.dashboardOpenCount,
            "hasViewedReport" to p.hasViewedReport,
            "hasComparedMonths" to p.hasComparedMonths,
            "largeExpenseLogged" to p.largeExpenseLogged,
            "tenPurchasesOneDay" to p.tenPurchasesOneDay,
            "todayExpenseCount" to p.todayExpenseCount,
            "lastExpenseDayCount" to p.lastExpenseDayCount,
            "budgetSetDone" to p.budgetSetDone,
            "savingsGoalSetDone" to p.savingsGoalSetDone,

            "achFirstCoin" to p.achFirstCoin,
            "achWelcomeLedger" to p.achWelcomeLedger,
            "achSideQuest" to p.achSideQuest,
            "achPaperTrail" to p.achPaperTrail,
            "achItAddsUp" to p.achItAddsUp,

            "achSevenDays" to p.achSevenDays,
            "achThirtyDays" to p.achThirtyDays,
            "achQuarterMaster" to p.achQuarterMaster,
            "achHalfYearHabit" to p.achHalfYearHabit,
            "achLegendLedger" to p.achLegendLedger,

            "achUnderControl" to p.achUnderControl,
            "achSteadyHand" to p.achSteadyHand,
            "achIronLedger" to p.achIronLedger,
            "achCleanMonth" to p.achCleanMonth,
            "achPerfectBalance" to p.achPerfectBalance,

            "achSave10" to p.achSave10,
            "achSave25" to p.achSave25,
            "achSave42" to p.achSave42,
            "achSave50" to p.achSave50,
            "achSave67" to p.achSave67,
            "achSave70" to p.achSave70,
            "achLockedVault" to p.achLockedVault,

            "achLevel10" to p.achLevel10,
            "achLevel30" to p.achLevel30,
            "achLevel100" to p.achLevel100,

            "achMonthlyReflection" to p.achMonthlyReflection,
            "achLookingBack" to p.achLookingBack,
            "achStillWatching" to p.achStillWatching,
            "achLongGame" to p.achLongGame,
            "achConsistencyIntensity" to p.achConsistencyIntensity,

            "achNotBatman" to p.achNotBatman,
            "achMoneyMoneyMoney" to p.achMoneyMoneyMoney,
            "achGatsby" to p.achGatsby,
            "achThriftShop" to p.achThriftShop,
            "achKaChing" to p.achKaChing,
            "achTonyStark" to p.achTonyStark,
            "achWolfBudget" to p.achWolfBudget,
            "achBuyBuyBaby" to p.achBuyBuyBaby,
            "achChaChing" to p.achChaChing,
            "achWannaBeRich" to p.achWannaBeRich
        )

        fun fromMap(m: Map<String, Any>): UserProfile = UserProfile(
            uid = m["uid"] as? String ?: "",
            email = m["email"] as? String ?: "",

            monthlyBudget = (m["monthlyBudget"] as? Number)?.toDouble() ?: 1500.0,
            currentSpending = (m["currentSpending"] as? Number)?.toDouble() ?: 0.0,
            currentSavings = (m["currentSavings"] as? Number)?.toDouble() ?: 1500.0,
            savingsGoal = (m["savingsGoal"] as? Number)?.toDouble() ?: 0.0,
            extraIncome = (m["extraIncome"] as? Number)?.toDouble() ?: 0.0,
            budgetResetDay = (m["budgetResetDay"] as? Number)?.toInt() ?: 1,

            currentEXP = (m["currentEXP"] as? Number)?.toInt() ?: 0,
            currentLevel = (m["currentLevel"] as? Number)?.toInt() ?: 1,
            levelTitle = m["levelTitle"] as? String ?: "Commoner",
            achievementTitle = m["achievementTitle"] as? String ?: "",

            currentDay = (m["currentDay"] as? Number)?.toInt() ?: 1,
            dailyEXPEarned = (m["dailyEXPEarned"] as? Number)?.toInt() ?: 0,
            lastExpenseDay = (m["lastExpenseDay"] as? Number)?.toInt() ?: 0,

            totalExpenseCount = (m["totalExpenseCount"] as? Number)?.toInt() ?: 0,
            consecutiveDaysLogged = (m["consecutiveDaysLogged"] as? Number)?.toInt() ?: 0,
            lastLoggedDay = (m["lastLoggedDay"] as? Number)?.toInt() ?: 0,
            lastRealDate = m["lastRealDate"] as? String ?: "",
            currentYearMonth = m["currentYearMonth"] as? String ?: "",
            currentCalendarYearMonth = m["currentCalendarYearMonth"] as? String ?: "",
            currentCycleHadActivity = m["currentCycleHadActivity"] as? Boolean ?: false,
            currentCycleIndex = (m["currentCycleIndex"] as? Number)?.toInt() ?: 1,
            currentCycleLabel = m["currentCycleLabel"] as? String ?: "",
            isDemoMode = m["isDemoMode"] as? Boolean ?: false,
            demoDay = (m["demoDay"] as? Number)?.toInt() ?: 1,
            demoMonth = (m["demoMonth"] as? Number)?.toInt() ?: 1,
            demoYear = (m["demoYear"] as? Number)?.toInt() ?: 2026,
            demoBudgetResetDay = (m["demoBudgetResetDay"] as? Number)?.toInt() ?: 1,
            usedCategories = m["usedCategories"] as? String ?: "",
            monthsUnderBudget = (m["monthsUnderBudget"] as? Number)?.toInt() ?: 0,
            monthsWithSavings40 = (m["monthsWithSavings40"] as? Number)?.toInt() ?: 0,
            monthsWithEXP = (m["monthsWithEXP"] as? Number)?.toInt() ?: 0,
            consecutiveMonthsLogged = (m["consecutiveMonthsLogged"] as? Number)?.toInt() ?: 0,
            dashboardOpenCount = (m["dashboardOpenCount"] as? Number)?.toInt() ?: 0,
            hasViewedReport = m["hasViewedReport"] as? Boolean ?: false,
            hasComparedMonths = m["hasComparedMonths"] as? Boolean ?: false,
            largeExpenseLogged = m["largeExpenseLogged"] as? Boolean ?: false,
            tenPurchasesOneDay = m["tenPurchasesOneDay"] as? Boolean ?: false,
            todayExpenseCount = (m["todayExpenseCount"] as? Number)?.toInt() ?: 0,
            lastExpenseDayCount = (m["lastExpenseDayCount"] as? Number)?.toInt() ?: 0,
            budgetSetDone = m["budgetSetDone"] as? Boolean ?: false,
            savingsGoalSetDone = m["savingsGoalSetDone"] as? Boolean ?: false,

            achFirstCoin = m["achFirstCoin"] as? Boolean ?: false,
            achWelcomeLedger = m["achWelcomeLedger"] as? Boolean ?: false,
            achSideQuest = m["achSideQuest"] as? Boolean ?: false,
            achPaperTrail = m["achPaperTrail"] as? Boolean ?: false,
            achItAddsUp = m["achItAddsUp"] as? Boolean ?: false,

            achSevenDays = m["achSevenDays"] as? Boolean ?: false,
            achThirtyDays = m["achThirtyDays"] as? Boolean ?: false,
            achQuarterMaster = m["achQuarterMaster"] as? Boolean ?: false,
            achHalfYearHabit = m["achHalfYearHabit"] as? Boolean ?: false,
            achLegendLedger = m["achLegendLedger"] as? Boolean ?: false,

            achUnderControl = m["achUnderControl"] as? Boolean ?: false,
            achSteadyHand = m["achSteadyHand"] as? Boolean ?: false,
            achIronLedger = m["achIronLedger"] as? Boolean ?: false,
            achCleanMonth = m["achCleanMonth"] as? Boolean ?: false,
            achPerfectBalance = m["achPerfectBalance"] as? Boolean ?: false,

            achSave10 = m["achSave10"] as? Boolean ?: false,
            achSave25 = m["achSave25"] as? Boolean ?: false,
            achSave42 = m["achSave42"] as? Boolean ?: false,
            achSave50 = m["achSave50"] as? Boolean ?: false,
            achSave67 = m["achSave67"] as? Boolean ?: false,
            achSave70 = m["achSave70"] as? Boolean ?: false,
            achLockedVault = m["achLockedVault"] as? Boolean ?: false,

            achLevel10 = m["achLevel10"] as? Boolean ?: false,
            achLevel30 = m["achLevel30"] as? Boolean ?: false,
            achLevel100 = m["achLevel100"] as? Boolean ?: false,

            achMonthlyReflection = m["achMonthlyReflection"] as? Boolean ?: false,
            achLookingBack = m["achLookingBack"] as? Boolean ?: false,
            achStillWatching = m["achStillWatching"] as? Boolean ?: false,
            achLongGame = m["achLongGame"] as? Boolean ?: false,
            achConsistencyIntensity = m["achConsistencyIntensity"] as? Boolean ?: false,

            achNotBatman = m["achNotBatman"] as? Boolean ?: false,
            achMoneyMoneyMoney = m["achMoneyMoneyMoney"] as? Boolean ?: false,
            achGatsby = m["achGatsby"] as? Boolean ?: false,
            achThriftShop = m["achThriftShop"] as? Boolean ?: false,
            achKaChing = m["achKaChing"] as? Boolean ?: false,
            achTonyStark = m["achTonyStark"] as? Boolean ?: false,
            achWolfBudget = m["achWolfBudget"] as? Boolean ?: false,
            achBuyBuyBaby = m["achBuyBuyBaby"] as? Boolean ?: false,
            achChaChing = m["achChaChing"] as? Boolean ?: false,
            achWannaBeRich = m["achWannaBeRich"] as? Boolean ?: false
        )
    }
}