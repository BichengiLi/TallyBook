package com.example.tallybook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallybook.data.Transaction
import com.example.tallybook.data.TransactionType
import com.example.tallybook.ui.theme.*
import com.example.tallybook.viewmodel.TallyBookViewModel
import kotlinx.datetime.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: TallyBookViewModel = viewModel()
) {
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    // 筛选状态: null 表示全部, 否则为具体分类
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    // 根据筛选条件过滤
    val filteredTransactions = remember(monthlyTransactions, selectedFilter) {
        when (selectedFilter) {
            null -> monthlyTransactions
            "TYPE_EXPENSE" -> monthlyTransactions.filter { it.type == TransactionType.EXPENSE }
            "TYPE_INCOME" -> monthlyTransactions.filter { it.type == TransactionType.INCOME }
            "OTHER_EXPENSE" -> monthlyTransactions.filter { it.type == TransactionType.EXPENSE && it.category == "OTHER" }
            "OTHER_INCOME" -> monthlyTransactions.filter { it.type == TransactionType.INCOME && it.category == "OTHER" }
            else -> monthlyTransactions.filter { it.category == selectedFilter }
        }
    }

    // Group transactions by date
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.date }
            .toSortedMap(compareByDescending { it })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "本月记录",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AnimePink
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AnimeBackground)
        ) {
            // 筛选下拉框
            FilterDropdown(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            if (groupedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyHistoryContent(Modifier)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedTransactions.forEach { (date, transactions) ->
                        item {
                            DateHeader(date = date)
                        }

                        items(transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onDelete = { viewModel.deleteTransaction(transaction) },
                                currencyFormat = currencyFormat
                            )
                        }

                        item {
                            DaySummary(
                                transactions = transactions,
                                currencyFormat = currencyFormat
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = AnimePink.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "本月还没有记录",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = AnimeOnSurface.copy(alpha = 0.5f)
                )
            )
            Text(
                text = "开始记账后这里会显示所有记录",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AnimeOnSurface.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun DateHeader(date: LocalDate) {
    val today = Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
    val yesterday = today.minus(DatePeriod(days = 1))

    val dateText = when (date) {
        today -> "今天"
        yesterday -> "昨天"
        else -> {
            val sdf = SimpleDateFormat("MM月dd日 EEEE", Locale.CHINA)
            sdf.format(Date(date.toEpochDays() * 24 * 60 * 60 * 1000L))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AnimePink.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = AnimePink,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = AnimePink,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun DaySummary(
    transactions: List<com.example.tallybook.data.Transaction>,
    currencyFormat: NumberFormat
) {
    val totalExpense = transactions
        .filter { it.type == com.example.tallybook.data.TransactionType.EXPENSE }
        .sumOf { it.amount }
    val totalIncome = transactions
        .filter { it.type == com.example.tallybook.data.TransactionType.INCOME }
        .sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "支出",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AnimeOnSurface.copy(alpha = 0.6f)
                    )
                )
                Text(
                    text = currencyFormat.format(totalExpense),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = AnimeRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "收入",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AnimeOnSurface.copy(alpha = 0.6f)
                    )
                )
                Text(
                    text = currencyFormat.format(totalIncome),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = AnimeGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun FilterDropdown(
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = when (selectedFilter) {
        null -> "全部"
        "TYPE_EXPENSE" -> "支出"
        "TYPE_INCOME" -> "收入"
        "FOOD" -> "餐饮"
        "TRANSPORT" -> "交通"
        "SHOPPING" -> "购物"
        "ENTERTAINMENT" -> "娱乐"
        "MEDICAL" -> "医疗"
        "EDUCATION" -> "教育"
        "OTHER_EXPENSE" -> "其他支出"
        "SALARY" -> "工资"
        "BONUS" -> "奖金"
        "INVESTMENT" -> "投资"
        "GIFT" -> "礼金"
        "OTHER_INCOME" -> "其他收入"
        else -> "全部"
    }

    val expenseItems = listOf(
        "FOOD" to "餐饮",
        "TRANSPORT" to "交通",
        "SHOPPING" to "购物",
        "ENTERTAINMENT" to "娱乐",
        "MEDICAL" to "医疗",
        "EDUCATION" to "教育",
        "OTHER_EXPENSE" to "其他支出"
    )
    val incomeItems = listOf(
        "SALARY" to "工资",
        "BONUS" to "奖金",
        "INVESTMENT" to "投资",
        "GIFT" to "礼金",
        "OTHER_INCOME" to "其他收入"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AnimeBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AnimePink
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, AnimePink.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(selectedLabel)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("全部", fontWeight = FontWeight.Bold) },
                    onClick = {
                        onFilterSelected(null)
                        expanded = false
                    }
                )
                Divider()
                // 支出
                DropdownMenuItem(
                    text = { Text("支出", fontWeight = FontWeight.Bold, color = AnimeRed) },
                    onClick = {
                        onFilterSelected("TYPE_EXPENSE")
                        expanded = false
                    }
                )
                expenseItems.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text("    $label") },
                        onClick = {
                            onFilterSelected(key)
                            expanded = false
                        }
                    )
                }
                Divider()
                // 收入
                DropdownMenuItem(
                    text = { Text("收入", fontWeight = FontWeight.Bold, color = AnimeGreen) },
                    onClick = {
                        onFilterSelected("TYPE_INCOME")
                        expanded = false
                    }
                )
                incomeItems.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text("    $label") },
                        onClick = {
                            onFilterSelected(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
