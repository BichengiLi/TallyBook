package com.example.tallybook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallybook.data.MonthlyBudget
import com.example.tallybook.ui.theme.*
import com.example.tallybook.viewmodel.TallyBookViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: TallyBookViewModel = viewModel()
) {
    val allMonthlyBudgets by viewModel.allMonthlyBudgets.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "月度记录",
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
        if (allMonthlyBudgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(AnimeBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无月度记录",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = AnimeOnSurface.copy(alpha = 0.5f)
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(AnimeBackground),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allMonthlyBudgets) { budget ->
                    MonthlyBudgetItem(
                        monthlyBudget = budget,
                        currencyFormat = currencyFormat,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyBudgetItem(
    monthlyBudget: MonthlyBudget,
    currencyFormat: NumberFormat,
    viewModel: TallyBookViewModel
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val currentMonth = "${today.year}-${today.monthNumber.toString().padStart(2, '0')}"
    val isCurrentMonth = monthlyBudget.month == currentMonth

    // 计算该月实际支出
    val monthParts = monthlyBudget.month.split("-")
    val year = monthParts[0].toInt()
    val monthNum = monthParts[1].toInt()
    val firstDay = LocalDate(year, monthNum, 1)
    val lastDay = if (isCurrentMonth) today else {
        LocalDate(year, monthNum, when (monthNum) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> 30
        })
    }

    // 获取该月支出和收入数据
    var otherSpent by remember { mutableStateOf(0.0) }
    var dailySpent by remember { mutableStateOf(0.0) }
    var totalIncome by remember { mutableStateOf(0.0) }

    LaunchedEffect(monthlyBudget.month) {
        viewModel.getTransactionsByDateRange(firstDay, lastDay).collect { transactions ->
            otherSpent = transactions
                .filter { it.type == com.example.tallybook.data.TransactionType.EXPENSE && (it.category == "ENTERTAINMENT" || it.category == "OTHER") }
                .sumOf { it.amount }
            dailySpent = transactions
                .filter { it.type == com.example.tallybook.data.TransactionType.EXPENSE && it.category !in listOf("ENTERTAINMENT", "OTHER") }
                .sumOf { it.amount }
            totalIncome = transactions
                .filter { it.type == com.example.tallybook.data.TransactionType.INCOME }
                .sumOf { it.amount }
        }
    }

    val totalSpent = dailySpent + otherSpent
    val netExpense = (totalSpent - totalIncome).coerceAtLeast(0.0)
    val totalSaved = monthlyBudget.monthlyTotalBudget - netExpense

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 月份标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = if (totalSaved >= 0) AnimeGreen else AnimeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monthlyBudget.month.replace("-", "年") + "月",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AnimeOnSurface
                        )
                    )
                }
                if (isCurrentMonth) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AnimePink.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "本月",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AnimePink,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = AnimePink.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // 预算详情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("月预算", style = MaterialTheme.typography.bodySmall.copy(color = AnimeOnSurface.copy(alpha = 0.6f)))
                Text(currencyFormat.format(monthlyBudget.monthlyTotalBudget), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("日常支出", style = MaterialTheme.typography.bodySmall.copy(color = AnimeOnSurface.copy(alpha = 0.6f)))
                Text("${currencyFormat.format(dailySpent)} / ${currencyFormat.format(monthlyBudget.dailyBudget)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = AnimePink, fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("娱乐/其他", style = MaterialTheme.typography.bodySmall.copy(color = AnimeOnSurface.copy(alpha = 0.6f)))
                Text("${currencyFormat.format(otherSpent)} / ${currencyFormat.format(monthlyBudget.otherBudget)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = AnimePurple, fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = AnimePink.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // 节省/超支
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (totalSaved >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (totalSaved >= 0) AnimeGreen else AnimeRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (totalSaved >= 0) "本月结余" else "本月超支",
                        style = MaterialTheme.typography.bodySmall.copy(color = AnimeOnSurface.copy(alpha = 0.6f))
                    )
                }
                Text(
                    text = currencyFormat.format(totalSaved),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (totalSaved >= 0) AnimeGreen else AnimeRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
