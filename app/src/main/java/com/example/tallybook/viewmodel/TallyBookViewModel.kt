package com.example.tallybook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallybook.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class TallyBookViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TallyBookDatabase.getDatabase(application)
    private val repository = TallyBookRepository(database.transactionDao(), database.budgetDao(), database.monthlyBudgetDao())

    private val _currentDate = MutableStateFlow(
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    )
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    val todayTransactions: StateFlow<List<Transaction>> = _currentDate.flatMapLatest { date ->
        repository.getTransactionsByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayBudget: StateFlow<DailyBudget?> = _currentDate.flatMapLatest { date ->
        repository.getBudgetByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayExpense: StateFlow<Double> = _currentDate.flatMapLatest { date ->
        repository.getTotalExpenseByDate(date).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // 今日日常支出（不含娱乐和其他）
    val todayDailyExpense: StateFlow<Double> = _currentDate.flatMapLatest { date ->
        repository.getDailyExpenseByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayIncome: StateFlow<Double> = _currentDate.flatMapLatest { date ->
        repository.getTotalIncomeByDate(date).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 月度预算
    val monthlyBudget: StateFlow<MonthlyBudget?> = _currentDate.flatMapLatest { date ->
        val month = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}"
        repository.getMonthlyBudgetFlow(month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 月度娱乐和其他支出
    val monthlyOtherSpent: StateFlow<Double> = _currentDate.flatMapLatest { date ->
        repository.getMonthlyOtherSpent(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // 月度日常支出（餐饮、交通、购物、医疗、教育）
    val monthlyDailySpent: StateFlow<Double> = _currentDate.flatMapLatest { date ->
        repository.getMonthlyDailySpent(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // 月度日常净支出 = 日常支出 - 收入（保底0）
    val monthlyDailyNetExpense: StateFlow<Double> = _currentDate.flatMapLatest { date ->
        repository.getMonthlyDailyNetExpense(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // 所有月度预算记录
    val allMonthlyBudgets: StateFlow<List<MonthlyBudget>> = repository.getAllMonthlyBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 本月交易记录
    val monthlyTransactions: StateFlow<List<Transaction>> = _currentDate.flatMapLatest { date ->
        val firstDayOfMonth = LocalDate(date.year, date.month, 1)
        repository.getTransactionsByDateRange(firstDayOfMonth, date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 撤回功能相关
    private val _lastAddedTransaction = MutableStateFlow<Transaction?>(null)
    val lastAddedTransaction: StateFlow<Transaction?> = _lastAddedTransaction.asStateFlow()

    private val _showUndoSnackbar = MutableStateFlow(false)
    val showUndoSnackbar: StateFlow<Boolean> = _showUndoSnackbar.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeBudgetForDate(_currentDate.value)
        }
    }

    fun addExpense(amount: Double, category: String, note: String = "") {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                type = TransactionType.EXPENSE,
                category = category,
                note = note,
                date = _currentDate.value
            )
            repository.insertTransaction(transaction)
            _lastAddedTransaction.value = transaction
            _showUndoSnackbar.value = true
        }
    }

    fun addIncome(amount: Double, category: String, note: String = "") {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                type = TransactionType.INCOME,
                category = category,
                note = note,
                date = _currentDate.value
            )
            repository.insertTransaction(transaction)
            _lastAddedTransaction.value = transaction
            _showUndoSnackbar.value = true
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun undoLastTransaction() {
        viewModelScope.launch {
            _lastAddedTransaction.value?.let { transaction ->
                repository.deleteTransaction(transaction)
                _lastAddedTransaction.value = null
                _showUndoSnackbar.value = false
            }
        }
    }

    fun dismissUndoSnackbar() {
        _showUndoSnackbar.value = false
        _lastAddedTransaction.value = null
    }

    fun setCurrentDate(date: LocalDate) {
        _currentDate.value = date
        viewModelScope.launch {
            repository.initializeBudgetForDate(date)
        }
    }

    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        return repository.getTransactionsByDateRange(startDate, endDate)
    }

    fun getBudgetsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyBudget>> {
        return repository.getBudgetsByDateRange(startDate, endDate)
    }

    fun updateMonthlyRatio(otherBudget: Double, dailyBudget: Double) {
        viewModelScope.launch {
            repository.updateMonthlyRatio(otherBudget, dailyBudget)
        }
    }

    fun updateMonthlyTotalBudget(newTotal: Double) {
        viewModelScope.launch {
            repository.updateMonthlyTotalBudget(newTotal)
        }
    }
}