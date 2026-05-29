package com.example.tallybook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

enum class TransactionType {
    EXPENSE, INCOME
}

enum class ExpenseCategory {
    FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT, MEDICAL, EDUCATION, OTHER
}

enum class IncomeCategory {
    SALARY, BONUS, INVESTMENT, GIFT, OTHER
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val note: String = "",
    val date: LocalDate,
    val timestamp: Long = System.currentTimeMillis()
)