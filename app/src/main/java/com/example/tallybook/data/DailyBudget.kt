package com.example.tallybook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "daily_budgets")
data class DailyBudget(
    @PrimaryKey
    val date: LocalDate,
    val budget: Double,
    val spent: Double = 0.0
) {
    val remaining: Double get() = budget - spent
    val isOverBudget: Boolean get() = spent > budget
    val overAmount: Double get() = if (isOverBudget) spent - budget else 0.0
    val savedAmount: Double get() = if (!isOverBudget) budget - spent else 0.0
}