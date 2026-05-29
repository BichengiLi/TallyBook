package com.example.tallybook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE month = :month")
    fun getMonthlyBudget(month: String): Flow<MonthlyBudget?>

    @Query("SELECT * FROM monthly_budgets ORDER BY month DESC")
    fun getAllMonthlyBudgets(): Flow<List<MonthlyBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: MonthlyBudget)
}
