package com.example.tallybook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date = :date ORDER BY timestamp DESC")
    fun getTransactionsByDate(date: LocalDate): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE date = :date AND type = 'EXPENSE'")
    fun getTotalExpenseByDate(date: LocalDate): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE date = :date AND type = 'INCOME'")
    fun getTotalIncomeByDate(date: LocalDate): Flow<Double?>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'EXPENSE'")
    suspend fun getTotalExpenseByDateRange(startDate: LocalDate, endDate: LocalDate): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'EXPENSE' AND category = :category")
    suspend fun getTotalExpenseByDateRangeAndCategory(startDate: LocalDate, endDate: LocalDate, category: String): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE date = :date AND type = 'EXPENSE' AND category IN ('FOOD', 'TRANSPORT', 'SHOPPING', 'MEDICAL', 'EDUCATION')")
    fun getDailyExpenseByDate(date: LocalDate): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'EXPENSE' AND category IN ('ENTERTAINMENT', 'OTHER')")
    fun getMonthlyOtherSpent(startDate: LocalDate, endDate: LocalDate): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'EXPENSE' AND category IN ('FOOD', 'TRANSPORT', 'SHOPPING', 'MEDICAL', 'EDUCATION')")
    fun getMonthlyDailySpent(startDate: LocalDate, endDate: LocalDate): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'INCOME'")
    fun getMonthlyIncome(startDate: LocalDate, endDate: LocalDate): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}