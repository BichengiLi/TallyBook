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
    version = 4,
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // A historical v2 build already included this column (commit 9284a82).
                db.query("PRAGMA table_info(`monthly_budgets`)").use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        if (cursor.getString(nameIndex) == "monthlyTotalBudget") return
                    }
                }
                db.execSQL(
                    "ALTER TABLE `monthly_budgets` ADD COLUMN `monthlyTotalBudget` REAL NOT NULL DEFAULT 2000.0"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `daily_budgets` ADD COLUMN `rewardClaimed` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): TallyBookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TallyBookDatabase::class.java,
                    "tallybook_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
