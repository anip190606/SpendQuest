package com.example.spendquest

import kotlin.math.abs

object AchievementManager {

    private fun unlock(
        profile: UserProfile,
        expReward: Int,
        name: String,
        desc: String,
        icon: String,
        achievementTitle: String = ""
    ): RPGResult {
        profile.currentEXP += expReward
        if (achievementTitle.isNotEmpty()) profile.achievementTitle = achievementTitle
        return RPGResult(
            expGained = expReward,
            unlockedAchievement = name,
            achievementDesc = desc,
            achievementIcon = icon
        )
    }

    private fun isExactPercent(value: Double, target: Int): Boolean {
        return abs(value - target.toDouble()) < 0.0001
    }

    // Called every expense log
    fun checkAll(profile: UserProfile, expenses: List<Expense>): List<RPGResult> {
        val r = mutableListOf<RPGResult>()

        // A. Getting Started
        if (!profile.achFirstCoin && profile.totalExpenseCount >= 1)
            r.add(
                unlock(
                    profile, 25, "First Coin",
                    "Welcome to adulthood. Don't spend it all at once.", "🪙"
                ).also { profile.achFirstCoin = true }
            )

        if (!profile.achWelcomeLedger && profile.budgetSetDone)
            r.add(
                unlock(
                    profile, 25, "Welcome to the Ledger",
                    "Big brain energy. Planning > panic.", "📒"
                ).also { profile.achWelcomeLedger = true }
            )

        if (!profile.achSideQuest && profile.savingsGoalSetDone)
            r.add(
                unlock(
                    profile, 25, "Side Quest Accepted",
                    "You have a goal. Not bad for a beginner.", "🎯"
                ).also { profile.achSideQuest = true }
            )

        if (!profile.achPaperTrail && profile.totalExpenseCount >= 10)
            r.add(
                unlock(
                    profile, 50, "Paper Trail",
                    "Proof that money leaves faster than you thought.", "🧾"
                ).also { profile.achPaperTrail = true }
            )

        if (!profile.achItAddsUp && profile.totalExpenseCount >= 50)
            r.add(
                unlock(
                    profile, 100, "It Adds Up",
                    "At this point, your wallet has a fan club.", "💸"
                ).also { profile.achItAddsUp = true }
            )

        // B. Consistency — days
        if (!profile.achSevenDays && profile.consecutiveDaysLogged >= 7)
            r.add(
                unlock(
                    profile, 75, "Seven Days Logged",
                    "Consistency is key. Even Gandalf would nod.", "📅"
                ).also { profile.achSevenDays = true }
            )

        if (!profile.achThirtyDays && profile.consecutiveDaysLogged >= 30)
            r.add(
                unlock(
                    profile, 150, "Thirty Days Logged",
                    "You've been faithful to your wallet. Very proud.", "🗓️"
                ).also { profile.achThirtyDays = true }
            )

        // C. Budget — immediate only
        val effectiveBudget = profile.effectiveBudget
        val spendPct = if (effectiveBudget > 0)
            profile.currentSpending / effectiveBudget * 100 else 0.0

        if (!profile.achNotBatman && profile.currentSpending > effectiveBudget
            && effectiveBudget > 0
        )
            r.add(
                unlock(
                    profile, 25, "I'm Not BATMAN",
                    "Spent money like Bruce Wayne. You're not him tho.", "🦇"
                ).also { profile.achNotBatman = true }
            )

        if (!profile.achTonyStark && profile.currentSpending > effectiveBudget * 1.5
            && effectiveBudget > 0 && profile.savingsGoal > 0
        )
            r.add(
                unlock(
                    profile, 25, "Living Like Tony Stark",
                    "Jarvis not included. Overdraft real.", "🤖"
                ).also { profile.achTonyStark = true }
            )

        if (!profile.achGatsby && profile.todayExpenseCount >= 5 && spendPct > 80.0)
            r.add(
                unlock(
                    profile, 25, "Luxurious Like Gatsby",
                    "Throwing money like roaring 20s, bank not impressed.", "🥂"
                ).also { profile.achGatsby = true }
            )

        // D. Level
        if (!profile.achLevel10 && profile.currentLevel >= 10)
            r.add(
                unlock(
                    profile, 150, "Rising Through the Ranks",
                    "You're leveling up. Baby steps to Tycoon.", "⭐"
                ).also { profile.achLevel10 = true }
            )

        if (!profile.achLevel30 && profile.currentLevel >= 30)
            r.add(
                unlock(
                    profile, 250, "Veteran of the System",
                    "Experience shows. Wallet gains XP too.", "🎖️"
                ).also { profile.achLevel30 = true }
            )

        if (!profile.achLevel100 && profile.currentLevel >= 100)
            r.add(
                unlock(
                    profile, 500, "The Endgame",
                    "You did it. The financial apex. Respect earned.", "👑", "Tycoon"
                ).also { profile.achLevel100 = true }
            )

        // E. Engagement
        if (!profile.achMonthlyReflection && profile.hasViewedReport)
            r.add(
                unlock(
                    profile, 50, "Monthly Reflection",
                    "You checked in. Awareness is power.", "🔍"
                ).also { profile.achMonthlyReflection = true }
            )

        if (!profile.achStillWatching && profile.dashboardOpenCount >= 30)
            r.add(
                unlock(
                    profile, 100, "Still Watching",
                    "Your obsession is documented. Proudly.", "👀"
                ).also { profile.achStillWatching = true }
            )

        // F. Pop Culture — expense-based only
        if (!profile.achKaChing && !profile.largeExpenseLogged
            && expenses.any { it.amount > 100.0 }
        ) {
            profile.largeExpenseLogged = true
            profile.achKaChing = true
            r.add(
                unlock(
                    profile, 40, "Ka-Ching!",
                    "The register sings. Wallet still exists.", "🎰"
                )
            )
        }

        if (!profile.achBuyBuyBaby && profile.todayExpenseCount >= 10)
            r.add(
                unlock(
                    profile, 50, "Buy Buy Baby",
                    "Shopping spree remix. Registers fear you.", "🛒"
                ).also { profile.achBuyBuyBaby = true }
            )

        if (!profile.achChaChing && profile.totalExpenseCount >= 100)
            r.add(
                unlock(
                    profile, 150, "Cha-Ching, Cha-Cha-Cha",
                    "Spent more than a Bollywood musical extra.", "🎭"
                ).also { profile.achChaChing = true }
            )

        return r
    }

    // Called only on budget-cycle reset
    fun checkMonthly(profile: UserProfile): List<RPGResult> {
        val r = mutableListOf<RPGResult>()

        val effectiveBudget = profile.effectiveBudget
        val spendPct = if (effectiveBudget > 0)
            profile.currentSpending / effectiveBudget * 100 else 0.0
        val savePct = if (effectiveBudget > 0)
            (effectiveBudget - profile.currentSpending) / effectiveBudget * 100 else 0.0

        // Consistency — months
        if (!profile.achQuarterMaster && profile.consecutiveMonthsLogged >= 3)
            r.add(
                unlock(
                    profile, 200, "Quarter Master",
                    "Your bank statement doesn't lie. Good job.", "📊"
                ).also { profile.achQuarterMaster = true }
            )

        if (!profile.achHalfYearHabit && profile.consecutiveMonthsLogged >= 6)
            r.add(
                unlock(
                    profile, 300, "Half-Year Habit",
                    "Half a year! Your future self thanks you.", "⏳"
                ).also { profile.achHalfYearHabit = true }
            )

        if (!profile.achLegendLedger && profile.consecutiveMonthsLogged >= 12)
            r.add(
                unlock(
                    profile, 500, "Legend of the Ledger",
                    "You're officially a money wizard.", "🧙", "Ledger Legend"
                ).also { profile.achLegendLedger = true }
            )

        // Budget — cycle end only
        if (!profile.achUnderControl && profile.monthsUnderBudget >= 1)
            r.add(
                unlock(
                    profile, 50, "Under Control",
                    "You held the line. Budget army approved.", "🛡️"
                ).also { profile.achUnderControl = true }
            )

        if (!profile.achSteadyHand && profile.monthsUnderBudget >= 3)
            r.add(
                unlock(
                    profile, 150, "Steady Hand",
                    "Your budget didn't break. Your pride didn't either.", "🤝"
                ).also { profile.achSteadyHand = true }
            )

        if (!profile.achIronLedger && profile.monthsUnderBudget >= 6)
            r.add(
                unlock(
                    profile, 300, "Iron Ledger",
                    "You wielded your ledger like a sword.", "⚔️", "Budget Knight"
                ).also { profile.achIronLedger = true }
            )

        if (!profile.achCleanMonth && profile.currentSpending > 0 && spendPct <= 90.0)
            r.add(
                unlock(
                    profile, 75, "Clean Month",
                    "Not perfect, but your bank smiled.", "✨"
                ).also { profile.achCleanMonth = true }
            )

        if (!profile.achPerfectBalance && profile.currentSpending > 0 && spendPct <= 80.0)
            r.add(
                unlock(
                    profile, 100, "Perfect Balance",
                    "Wow. You actually saved some coins. Impressive.", "⚖️"
                ).also { profile.achPerfectBalance = true }
            )

        // Exact spend percentage achievements — cycle end only
        if (!profile.achSave10 && isExactPercent(spendPct, 10))
            r.add(
                unlock(
                    profile, 50, "10",
                    "You spent exactly 10% of your budget. Clean and precise.", "💰"
                ).also { profile.achSave10 = true }
            )

        if (!profile.achSave25 && isExactPercent(spendPct, 25))
            r.add(
                unlock(
                    profile, 75, "25",
                    "Quarter used. Dead accurate.", "🥈"
                ).also { profile.achSave25 = true }
            )

        if (!profile.achSave42 && isExactPercent(spendPct, 42))
            r.add(
                unlock(
                    profile, 100, "42",
                    "Hitchhiker approved. The answer is 42.", "🌌"
                ).also { profile.achSave42 = true }
            )

        if (!profile.achSave50 && isExactPercent(spendPct, 50))
            r.add(
                unlock(
                    profile, 125, "50/50",
                    "Perfect half used. Balanced like a blade.", "🏅"
                ).also { profile.achSave50 = true }
            )

        if (!profile.achSave67 && isExactPercent(spendPct, 67))
            r.add(
                unlock(
                    profile, 150, "67",
                    "You landed exactly 67% of your budget. Ridiculously precise.", "🎓"
                ).also { profile.achSave67 = true }
            )

        if (!profile.achSave70 && isExactPercent(spendPct, 70))
            r.add(
                unlock(
                    profile, 200, "No Cents Wasted",
                    "Exactly 70% used. Sharp control.", "🧘", "Frugal Sage"
                ).also { profile.achSave70 = true }
            )

        if (!profile.achLockedVault && profile.monthsUnderBudget >= 6 && savePct >= 50.0)
            r.add(
                unlock(
                    profile, 300, "Locked Vault",
                    "Even Fort Knox would be impressed.", "🔐"
                ).also { profile.achLockedVault = true }
            )

        // Engagement — months
        if (!profile.achLongGame && profile.monthsWithSavings40 >= 12)
            r.add(
                unlock(
                    profile, 400, "Long Game",
                    "Patience pays. Literally.", "🏆", "Master of Coin"
                ).also { profile.achLongGame = true }
            )

        if (!profile.achConsistencyIntensity && profile.monthsWithEXP >= 12)
            r.add(
                unlock(
                    profile, 250, "Consistency Over Intensity",
                    "Slow and steady… winning the financial marathon.", "🐢"
                ).also { profile.achConsistencyIntensity = true }
            )

        if (!profile.achLookingBack && profile.consecutiveMonthsLogged >= 2) {
            profile.hasComparedMonths = true
            r.add(
                unlock(
                    profile, 75, "Looking Back",
                    "Reflection: cheaper than therapy.", "🪞"
                ).also { profile.achLookingBack = true }
            )
        }

        // Pop culture — exact spend percentage
        if (!profile.achMoneyMoneyMoney && isExactPercent(spendPct, 10))
            r.add(
                unlock(
                    profile, 40, "Money, Money, Money",
                    "ABBA would be proud. Precision flex.", "🎵"
                ).also { profile.achMoneyMoneyMoney = true }
            )

        if (!profile.achThriftShop && isExactPercent(spendPct, 50))
            r.add(
                unlock(
                    profile, 100, "Thrift Shop",
                    "Macklemore would approve. Exactly half used.", "🛍️", "Thrift King"
                ).also { profile.achThriftShop = true }
            )

        if (!profile.achWolfBudget && isExactPercent(spendPct, 70))
            r.add(
                unlock(
                    profile, 125, "The Wolf of Budget Street",
                    "Belfort would approve… legally more responsible.", "🐺"
                ).also { profile.achWolfBudget = true }
            )

        // 100% save should require actual activity in this cycle
        if (!profile.achWannaBeRich &&
            effectiveBudget > 0 &&
            profile.currentSpending == 0.0 &&
            profile.currentCycleHadActivity
        ) {
            r.add(
                unlock(
                    profile, 250, "I Wanna Be Rich",
                    "You saved 100% of your allowance cycle and still stayed active. Wild discipline.", "💎"
                ).also { profile.achWannaBeRich = true }
            )
        }

        return r
    }

    fun getAllAchievements(p: UserProfile): List<Achievement> = listOf(
        Achievement("first_coin", "🪙 First Coin", "Log your first expense", 25, p.achFirstCoin, "Getting Started"),
        Achievement("welcome_ledger", "📒 Welcome to the Ledger", "Set your first budget", 25, p.achWelcomeLedger, "Getting Started"),
        Achievement("side_quest", "🎯 Side Quest Accepted", "Set your first savings goal", 25, p.achSideQuest, "Getting Started"),
        Achievement("paper_trail", "🧾 Paper Trail", "Log 10 expenses", 50, p.achPaperTrail, "Getting Started"),
        Achievement("it_adds_up", "💸 It Adds Up", "Log 50 expenses", 100, p.achItAddsUp, "Getting Started"),

        Achievement("seven_days", "📅 Seven Days Logged", "Log expenses 7 days in a row", 75, p.achSevenDays, "Consistency"),
        Achievement("thirty_days", "🗓️ Thirty Days Logged", "Log expenses 30 days in a row", 150, p.achThirtyDays, "Consistency"),
        Achievement("quarter_master", "📊 Quarter Master", "Track expenses for 3 budget cycles straight", 200, p.achQuarterMaster, "Consistency"),
        Achievement("half_year", "⏳ Half-Year Habit", "Track expenses for 6 budget cycles straight", 300, p.achHalfYearHabit, "Consistency"),
        Achievement("legend_ledger", "🧙 Legend of the Ledger", "Track expenses for 12 budget cycles straight", 500, p.achLegendLedger, "Consistency"),

        Achievement("under_control", "🛡️ Under Control", "Stay within budget for 1 cycle", 50, p.achUnderControl, "Budget"),
        Achievement("steady_hand", "🤝 Steady Hand", "Stay within budget for 3 cycles", 150, p.achSteadyHand, "Budget"),
        Achievement("iron_ledger", "⚔️ Iron Ledger", "Stay within budget for 6 cycles", 300, p.achIronLedger, "Budget"),
        Achievement("clean_month", "✨ Clean Month", "Spend ≤90% of budget in a cycle", 75, p.achCleanMonth, "Budget"),
        Achievement("perfect_balance", "⚖️ Perfect Balance", "Spend ≤80% of budget in a cycle", 100, p.achPerfectBalance, "Budget"),

        Achievement("save_10", "💰 10", "Spend exactly 10% of your monthly budget", 50, p.achSave10, "Savings"),
        Achievement("save_25", "🥈 25", "Spend exactly 25% of your monthly budget", 75, p.achSave25, "Savings"),
        Achievement("save_42", "🌌 42", "Spend exactly 42% of your monthly budget", 100, p.achSave42, "Savings"),
        Achievement("save_50", "🏅 50/50", "Spend exactly 50% of your monthly budget", 125, p.achSave50, "Savings"),
        Achievement("save_67", "🎓 67", "Spend exactly 67% of your monthly budget", 150, p.achSave67, "Savings"),
        Achievement("save_70", "🧘 No Cents Wasted", "Spend exactly 70% of your monthly budget", 200, p.achSave70, "Savings"),
        Achievement("locked_vault", "🔐 Locked Vault", "Under budget & save ≥50% for 6 cycles", 300, p.achLockedVault, "Savings"),

        Achievement("level_10", "⭐ Rising Through the Ranks", "Reach Level 10", 150, p.achLevel10, "Level"),
        Achievement("level_30", "🎖️ Veteran of the System", "Reach Level 30", 250, p.achLevel30, "Level"),
        Achievement("level_100", "👑 The Endgame", "Reach Level 100", 500, p.achLevel100, "Level"),

        Achievement("monthly_ref", "🔍 Monthly Reflection", "View your monthly report", 50, p.achMonthlyReflection, "Engagement"),
        Achievement("looking_back", "🪞 Looking Back", "Track for 2 cycles", 75, p.achLookingBack, "Engagement"),
        Achievement("still_watching", "👀 Still Watching", "Open dashboard 30 times", 100, p.achStillWatching, "Engagement"),
        Achievement("long_game", "🏆 Long Game", "Save ≥40% every cycle for 12 cycles", 400, p.achLongGame, "Engagement"),
        Achievement("consistency", "🐢 Consistency Over Intensity", "Earn EXP every cycle for 12 cycles", 250, p.achConsistencyIntensity, "Engagement"),

        Achievement("not_batman", "🦇 I'm Not BATMAN", "Spend over your monthly budget", 25, p.achNotBatman, "Pop Culture"),
        Achievement("money3", "🎵 Money, Money, Money", "Spend exactly 10% of budget", 40, p.achMoneyMoneyMoney, "Pop Culture"),
        Achievement("gatsby", "🥂 Luxurious Like Gatsby", "5 expenses in one day & spent >80% budget", 25, p.achGatsby, "Pop Culture"),
        Achievement("thrift_shop", "🛍️ Thrift Shop", "Spend exactly 50% of budget", 100, p.achThriftShop, "Pop Culture"),
        Achievement("ka_ching", "🎰 Ka-Ching!", "Log a single expense above RM100", 40, p.achKaChing, "Pop Culture"),
        Achievement("tony_stark", "🤖 Living Like Tony Stark", "Spend >150% budget with a savings goal", 25, p.achTonyStark, "Pop Culture"),
        Achievement("wolf_budget", "🐺 The Wolf of Budget Street", "Spend exactly 70% of budget", 125, p.achWolfBudget, "Pop Culture"),
        Achievement("buy_buy_baby", "🛒 Buy Buy Baby", "Log 10 expenses in a single day", 50, p.achBuyBuyBaby, "Pop Culture"),
        Achievement("cha_ching", "🎭 Cha-Ching, Cha-Cha-Cha", "Log 100 total expenses", 150, p.achChaChing, "Pop Culture"),
        Achievement("wanna_be_rich", "💎 I Wanna Be Rich", "Stay active and save 100% of your monthly budget", 250, p.achWannaBeRich, "Pop Culture")
    )
}