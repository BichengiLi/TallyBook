package com.example.tallybook.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.math.max
import kotlin.math.min

class TallyBookRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val monthlyBudgetDao: MonthlyBudgetDao
) {
    // Transaction operations
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByDate(date: LocalDate): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDate(date)

    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    fun getTotalExpenseByDate(date: LocalDate): Flow<Double?> = transactionDao.getTotalExpenseByDate(date)

    fun getTotalIncomeByDate(date: LocalDate): Flow<Double?> = transactionDao.getTotalIncomeByDate(date)

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
        updateBudgetSpent(transaction.date)

        // 如果是娱乐或其他支出，检查是否超支并自动调整比例
        if (transaction.type == TransactionType.EXPENSE &&
            (transaction.category == "ENTERTAINMENT" || transaction.category == "OTHER")) {
            checkAndAdjustOtherBudget(transaction.date)
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        updateBudgetSpent(transaction.date)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        updateBudgetSpent(transaction.date)
    }

    // Budget operations
    fun getBudgetByDate(date: LocalDate): Flow<DailyBudget?> = budgetDao.getBudgetByDate(date)

    fun getBudgetsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyBudget>> =
        budgetDao.getBudgetsByDateRange(startDate, endDate)

    fun getMonthlyBudgetFlow(month: String): Flow<MonthlyBudget?> =
        monthlyBudgetDao.getMonthlyBudget(month)

    // 获取月度娱乐和其他支出
    fun getMonthlyOtherSpent(date: LocalDate): Flow<Double> {
        val firstDayOfMonth = LocalDate(date.year, date.month, 1)
        return transactionDao.getMonthlyOtherSpent(firstDayOfMonth, date)
    }

    // 获取月度日常支出（餐饮、交通、购物、医疗、教育）
    fun getMonthlyDailySpent(date: LocalDate): Flow<Double> {
        val firstDayOfMonth = LocalDate(date.year, date.month, 1)
        return transactionDao.getMonthlyDailySpent(firstDayOfMonth, date)
    }

    // 获取月度日常净支出 = 日常支出 - 收入（保底0）
    fun getMonthlyDailyNetExpense(date: LocalDate): Flow<Double> {
        val firstDayOfMonth = LocalDate(date.year, date.month, 1)
        return flow {
            val dailySpent = transactionDao.getMonthlyDailySpent(firstDayOfMonth, date).first()
            val income = transactionDao.getMonthlyIncome(firstDayOfMonth, date).first()
            emit(max(0.0, dailySpent - income))
        }
    }

    // 获取所有月度预算记录
    fun getAllMonthlyBudgets(): Flow<List<MonthlyBudget>> {
        return monthlyBudgetDao.getAllMonthlyBudgets()
    }

    // 获取日常类支出（不含娱乐和其他）
    fun getDailyExpenseByDate(date: LocalDate): Flow<Double> {
        return transactionDao.getDailyExpenseByDate(date)
    }

    suspend fun getOrCreateMonthlyBudget(month: String): MonthlyBudget {
        val budget = monthlyBudgetDao.getMonthlyBudget(month).firstOrNull()
        return budget ?: run {
            val default = MonthlyBudget(month = month)
            monthlyBudgetDao.insertOrUpdate(default)
            default
        }
    }

    suspend fun updateMonthlyBudget(monthlyTotalBudget: Double, otherBudget: Double, dailyBudget: Double) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val month = formatMonth(today)
        monthlyBudgetDao.insertOrUpdate(
            MonthlyBudget(
                month = month,
                monthlyTotalBudget = monthlyTotalBudget,
                totalBudget = otherBudget + dailyBudget,
                otherBudget = otherBudget,
                dailyBudget = dailyBudget
            )
        )
        calculateDailyBudget(today)
    }

    suspend fun getOrCreateBudget(date: LocalDate): DailyBudget {
        val budget = budgetDao.getBudgetByDate(date).firstOrNull()
        return budget ?: calculateDailyBudget(date)
    }

    suspend fun initializeBudgetForDate(date: LocalDate) {
        calculateDailyBudget(date)
    }

    /**
     * 核心预算计算逻辑
     * 1. 获取当月 MonthlyBudget
     * 2. 计算本月1号到昨天的日常净支出 = 日常支出 - 收入（保底0）
     * 3. 每日预算 = (月度日常预算 - 日常净支出) / 剩余天数
     * 4. 娱乐和其他支出从月度 otherBudget 扣除，不影响日常预算
     */
    suspend fun calculateDailyBudget(date: LocalDate): DailyBudget {
        val month = formatMonth(date)
        val monthlyBudget = getOrCreateMonthlyBudget(month)

        // 本月1号
        val firstDayOfMonth = LocalDate(date.year, date.month, 1)
        // 昨天
        val yesterday = date.minus(1, DateTimeUnit.DAY)

        // 计算本月1号到昨天的日常净支出 = 日常支出 - 收入（保底0）
        val dailyCategories = listOf("FOOD", "TRANSPORT", "SHOPPING", "MEDICAL", "EDUCATION")
        val dailyNet = if (yesterday >= firstDayOfMonth) {
            val dailySpent = transactionDao.getTotalExpenseByCategories(
                firstDayOfMonth, yesterday, dailyCategories
            )
            val income = transactionDao.getMonthlyIncome(firstDayOfMonth, yesterday).first()
            max(dailySpent - income, 0.0)
        } else {
            0.0
        }

        // 剩余日常预算 = 月度日常预算 - 日常净支出
        val remainingDaily = max(monthlyBudget.dailyBudget - dailyNet, 0.0)

        // 本月天数
        val daysInMonth = daysInMonth(date.year, date.monthNumber)
        // 剩余天数（含今天）
        val remainingDays = daysInMonth - date.dayOfMonth + 1

        // 每日预算 = 剩余日常预算 / 剩余天数，上限为60
        val calculatedBudget = if (remainingDays > 0) remainingDaily / remainingDays else remainingDaily
        val todayBudget = min(calculatedBudget, 60.0)
        val newBudget = DailyBudget(date = date, budget = todayBudget)

        // 查是否已有当天预算记录
        val existing = budgetDao.getBudgetByDate(date).firstOrNull()
        if (existing != null) {
            // 更新 budget 但保留 spent
            budgetDao.insertBudget(newBudget.copy(spent = existing.spent))
        } else {
            budgetDao.insertBudget(newBudget)
        }

        return newBudget
    }

    /**
     * 更新当日支出后，重算明天预算
     */
    private suspend fun updateBudgetSpent(date: LocalDate) {
        // 只计算日常类支出（不含娱乐和其他）
        val dailyCategories = listOf("FOOD", "TRANSPORT", "SHOPPING", "MEDICAL", "EDUCATION")
        val dailyExpense = transactionDao.getTotalExpenseByCategories(date, date, dailyCategories)
        val income = transactionDao.getTotalIncomeByDate(date).firstOrNull() ?: 0.0
        budgetDao.updateSpentAmount(date, dailyExpense - income)
    }

    /**
     * 检查其他支出是否超支，如果超支则自动调整比例
     */
    private suspend fun checkAndAdjustOtherBudget(date: LocalDate) {
        val month = formatMonth(date)
        val monthlyBudget = getOrCreateMonthlyBudget(month)
        val firstDayOfMonth = LocalDate(date.year, date.month, 1)

        // 计算本月其他支出总额
        val entertainmentSpent = transactionDao.getTotalExpenseByDateRangeAndCategory(
            firstDayOfMonth, date, "ENTERTAINMENT"
        )
        val otherSpent = transactionDao.getTotalExpenseByDateRangeAndCategory(
            firstDayOfMonth, date, "OTHER"
        )
        val totalOtherSpent = entertainmentSpent + otherSpent

        // 如果其他支出超过预算，自动调整比例
        if (totalOtherSpent > monthlyBudget.otherBudget) {
            val newOtherBudget = totalOtherSpent
            val newDailyBudget = monthlyBudget.totalBudget - newOtherBudget

            // 确保日常预算不为负数
            if (newDailyBudget >= 0) {
                monthlyBudgetDao.insertOrUpdate(
                    MonthlyBudget(
                        month = month,
                        monthlyTotalBudget = monthlyBudget.monthlyTotalBudget,
                        totalBudget = monthlyBudget.totalBudget,
                        otherBudget = newOtherBudget,
                        dailyBudget = newDailyBudget
                    )
                )
                // 重新计算当天预算
                calculateDailyBudget(date)
            }
        }
    }

    private fun formatMonth(date: LocalDate): String {
        val m = date.monthNumber.toString().padStart(2, '0')
        return "${date.year}-$m"
    }

    private fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> 30
        }
    }
}
