package com.example.tallybook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_budgets")
data class MonthlyBudget(
    @PrimaryKey
    val month: String,           // "2026-04" 格式
    val monthlyTotalBudget: Double = 2000.0,  // 月度总预算（可调整）
    val totalBudget: Double = 60.0,
    val otherBudget: Double = 20.0,      // 其他支出(娱乐)
    val dailyBudget: Double = 40.0       // 日常支出
)
