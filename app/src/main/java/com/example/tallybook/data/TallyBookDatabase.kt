package com.example.tallybook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Transaction::class, DailyBudget::class, MonthlyBudget::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TallyBookDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun monthlyBudgetDao(): MonthlyBudgetDao

    companion object {
        @Volatile
        private var INSTANCE: TallyBookDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `monthly_budgets` (" +
                            "`month` TEXT NOT NULL, " +
                            "`totalBudget` REAL NOT NULL, " +
                            "`otherBudget` REAL NOT NULL, " +
                            "`dailyBudget` REAL NOT NULL, " +
                            "PRIMARY KEY(`month`))"
                )
            }
        }

        fun getDatabase(context: Context): TallyBookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TallyBookDatabase::class.java,
                    "tallybook_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}