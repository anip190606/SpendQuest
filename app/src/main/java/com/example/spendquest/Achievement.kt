package com.example.spendquest

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val expReward: Int,
    val isUnlocked: Boolean,
    val category: String
)