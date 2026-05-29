package com.example.tallybook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface BudgetDao {
    @Query("SELECT * FROM daily_budgets WHERE date = :date")
    fun getBudgetByDate(date: LocalDate): Flow<DailyBudget?>

    @Query("SELECT * FROM daily_budgets WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getBudgetsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: DailyBudget)

    @Update
    suspend fun updateBudget(budget: DailyBudget)

    @Query("UPDATE daily_budgets SET spent = :spent WHERE date = :date")
    suspend fun updateSpentAmount(date: LocalDate, spent: Double)

    @Query("DELETE FROM daily_budgets WHERE date = :date")
    suspend fun deleteBudgetByDate(date: LocalDate)
}