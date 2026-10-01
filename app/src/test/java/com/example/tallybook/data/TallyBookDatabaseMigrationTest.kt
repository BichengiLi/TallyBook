package com.example.tallybook.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class TallyBookDatabaseMigrationTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val date = LocalDate(2026, 9, 15)
    private var database: TallyBookDatabase? = null

    @After
    fun tearDown() {
        closeDatabase()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun version1PreservesTransactionsAndDailyBudgets() = runBlocking(Dispatchers.IO) {
        createHistoricalDatabase(version = 1)
        verifyMigratedData(expectedMonthlyTotal = 2000.0, expectedRewardClaimed = false)
    }

    @Test
    fun originalVersion2AddsMonthlyTotalWithoutLosingData() = runBlocking(Dispatchers.IO) {
        createHistoricalDatabase(version = 2)
        verifyMigratedData(expectedMonthlyTotal = 2000.0, expectedRewardClaimed = false)
    }

    @Test
    fun releasedVersion2WithMonthlyTotalPreservesExistingValue() = runBlocking(Dispatchers.IO) {
        // Commit 9284a82 added this column while the database version was still 2.
        createHistoricalDatabase(version = 2, hasMonthlyTotal = true)
        verifyMigratedData(expectedMonthlyTotal = 3200.0, expectedRewardClaimed = false)
    }

    @Test
    fun version3AddsRewardFlagWithoutLosingData() = runBlocking(Dispatchers.IO) {
        createHistoricalDatabase(version = 3, hasMonthlyTotal = true)
        verifyMigratedData(expectedMonthlyTotal = 3200.0, expectedRewardClaimed = false)
    }

    @Test
    fun version4PreservesRewardFlagAcrossProcessRestart() = runBlocking(Dispatchers.IO) {
        createHistoricalDatabase(version = 4, hasMonthlyTotal = true)
        verifyMigratedData(expectedMonthlyTotal = 3200.0, expectedRewardClaimed = true)
        closeDatabase()
        verifyMigratedData(expectedMonthlyTotal = 3200.0, expectedRewardClaimed = true)
    }

    @Test
    fun freshDatabaseCanInitializeAndReopen() = runBlocking(Dispatchers.IO) {
        val db = openDatabase()
        val repository = TallyBookRepository(db.transactionDao(), db.budgetDao(), db.monthlyBudgetDao())
        repository.initializeBudgetForDate(date)
        assertFalse(db.budgetDao().getBudgetByDate(date).first()!!.rewardClaimed)
        db.budgetDao().setRewardClaimed(date, true)
        closeDatabase()
        assertTrue(openDatabase().budgetDao().getBudgetByDate(date).first()!!.rewardClaimed)
    }

    private fun createHistoricalDatabase(version: Int, hasMonthlyTotal: Boolean = false) {
        context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null).use { db ->
            db.execSQL(
                "CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "amount REAL NOT NULL, type TEXT NOT NULL, category TEXT NOT NULL, " +
                    "note TEXT NOT NULL, date TEXT NOT NULL, timestamp INTEGER NOT NULL)"
            )
            val rewardColumn = if (version >= 4) ", rewardClaimed INTEGER NOT NULL" else ""
            db.execSQL(
                "CREATE TABLE daily_budgets (date TEXT NOT NULL PRIMARY KEY, " +
                    "budget REAL NOT NULL, spent REAL NOT NULL$rewardColumn)"
            )
            db.execSQL(
                "INSERT INTO transactions VALUES (7, 12.5, 'EXPENSE', 'FOOD', 'retained', '2026-09-15', 123)"
            )
            val rewardValue = if (version >= 4) ", 1" else ""
            db.execSQL("INSERT INTO daily_budgets VALUES ('2026-09-15', 40.0, 12.5$rewardValue)")
            if (version >= 2) {
                val totalColumn = if (hasMonthlyTotal) ", monthlyTotalBudget REAL NOT NULL" else ""
                db.execSQL(
                    "CREATE TABLE monthly_budgets (month TEXT NOT NULL PRIMARY KEY, " +
                        "totalBudget REAL NOT NULL, otherBudget REAL NOT NULL, " +
                        "dailyBudget REAL NOT NULL$totalColumn)"
                )
                val totalValue = if (hasMonthlyTotal) ", 3200.0" else ""
                db.execSQL("INSERT INTO monthly_budgets VALUES ('2026-09', 60.0, 20.0, 40.0$totalValue)")
            }
            db.version = version
        }
    }

    private suspend fun verifyMigratedData(expectedMonthlyTotal: Double, expectedRewardClaimed: Boolean) {
        val db = openDatabase()
        // Exercise Room's schema validation, generated DAOs, and the actual startup operation.
        val repository = TallyBookRepository(db.transactionDao(), db.budgetDao(), db.monthlyBudgetDao())
        repository.initializeBudgetForDate(date)
        val transaction = db.transactionDao().getTransactionsByDate(date).first().single()
        assertEquals(7L, transaction.id)
        assertEquals(12.5, transaction.amount, 0.0)
        assertEquals("retained", transaction.note)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        val budget = db.budgetDao().getBudgetByDate(date).first()!!
        assertEquals(12.5, budget.spent, 0.0)
        assertEquals(expectedRewardClaimed, budget.rewardClaimed)
        assertEquals(expectedMonthlyTotal, db.monthlyBudgetDao().getMonthlyBudget("2026-09").first()!!.monthlyTotalBudget, 0.0)
        assertEquals(4, db.openHelper.writableDatabase.version)
    }

    private fun openDatabase(): TallyBookDatabase =
        TallyBookDatabase.getDatabase(context).also { database = it }

    private fun closeDatabase() {
        database?.close()
        database = null
        // Reset the process singleton to model a cold start with the same database file.
        TallyBookDatabase::class.java.getDeclaredField("INSTANCE").apply {
            isAccessible = true
            set(null, null)
        }
    }

    companion object {
        private const val DATABASE_NAME = "tallybook_database"
    }
}
