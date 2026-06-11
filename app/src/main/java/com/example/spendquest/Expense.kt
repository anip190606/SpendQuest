package com.example.spendquest

data class Expense(
    val amount: Double,
    val category: String,
    val day: Int,
    val type: String = "Expense",      // Expense / Income
    val dateText: String = "",         // 10/03/2026
    val cycleIndex: Int = 1,           // Month 1, Month 2, ...
    val cycleLabel: String = "",       // March, April, ...
    val baseBudget: Double = 0.0       // monthly budget snapshot for that cycle
)