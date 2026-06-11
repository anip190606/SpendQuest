package com.example.spendquest

import java.util.Calendar

object DateManager {

    fun todayString(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH) + 1}-${c.get(Calendar.DAY_OF_MONTH)}"
    }

    fun currentYearMonth(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH) + 1}"
    }

    fun todayDayOfMonth(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun currentYearAndMonth(): Pair<Int, Int> {
        val c = Calendar.getInstance()
        return c.get(Calendar.YEAR) to (c.get(Calendar.MONTH) + 1)
    }

    fun daysInMonth(year: Int, month: Int): Int {
        val c = Calendar.getInstance()
        c.set(year, month - 1, 1)
        return c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun isNewDay(savedDate: String): Boolean {
        if (savedDate.startsWith("demo-")) return false
        if (savedDate.isEmpty()) return true
        return savedDate != todayString()
    }

    fun isBudgetResetDay(budgetResetDay: Int, lastResetYearMonth: String): Boolean {
        val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val todayYM = currentYearMonth()
        return todayDay == budgetResetDay && lastResetYearMonth != todayYM
    }

    fun monthLabel(): String {
        val c = Calendar.getInstance()
        val months = listOf(
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        )
        return "${months[c.get(Calendar.MONTH)]} ${c.get(Calendar.YEAR)}"
    }

    fun monthNameOnly(): String {
        val c = Calendar.getInstance()
        val months = listOf(
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        )
        return months[c.get(Calendar.MONTH)]
    }

    fun demoMonthLabel(year: Int, month: Int): String {
        val months = listOf(
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        )
        return "${months[month - 1]} $year"
    }

    fun demoMonthName(year: Int, month: Int): String {
        val months = listOf(
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        )
        return months[month - 1]
    }

    fun todayDisplayDate(): String {
        val c = Calendar.getInstance()
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month = c.get(Calendar.MONTH) + 1
        val year = c.get(Calendar.YEAR)
        return String.format("%02d/%02d/%04d", day, month, year)
    }

    fun demoDisplayDate(day: Int, month: Int, year: Int): String {
        return String.format("%02d/%02d/%04d", day, month, year)
    }

    fun isStreakBroken(lastRealDate: String): Boolean {
        if (lastRealDate.isEmpty() || lastRealDate.startsWith("demo-")) return false
        val parts = lastRealDate.split("-")
        if (parts.size != 3) return false
        val last = Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffDays = (today.timeInMillis - last.timeInMillis) / (1000 * 60 * 60 * 24)
        return diffDays > 1
    }
}