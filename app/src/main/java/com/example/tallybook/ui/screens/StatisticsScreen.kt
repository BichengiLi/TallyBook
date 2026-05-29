package com.example.tallybook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.tallybook.data.TransactionType
import com.example.tallybook.ui.theme.*
import com.example.tallybook.viewmodel.TallyBookViewModel
import kotlinx.datetime.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TallyBookViewModel = viewModel()
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    val today = Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
    val startOfMonth = LocalDate(today.year, today.month, 1)
    val endOfMonth = today

    // Calculate statistics
    val monthlyTransactions = remember(allTransactions) {
        allTransactions.filter { it.date in startOfMonth..endOfMonth }
    }

    val totalExpense = monthlyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    val totalIncome = monthlyTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val categoryExpenses = remember(monthlyTransactions) {
        monthlyTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "统计分析",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AnimeBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 本月概览
            item {
                MonthlyOverviewCard(
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    monthlyBudget = monthlyBudget,
                    currencyFormat = currencyFormat
                )
            }

            // 支出分类统计
            item {
                Text(
                    text = "支出分类",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AnimeOnBackground
                    )
                )
            }

            if (categoryExpenses.isEmpty()) {
                item {
                    EmptyStatisticsCard()
                }
            } else {
                items(categoryExpenses.size) { index ->
                    val (category, amount) = categoryExpenses[index]
                    CategoryExpenseItem(
                        category = category,
                        amount = amount,
                        totalExpense = totalExpense,
                        currencyFormat = currencyFormat
                    )
                }
            }

        }
    }
}

@Composable
fun MonthlyOverviewCard(
    totalExpense: Double,
    totalIncome: Double,
    monthlyBudget: MonthlyBudget?,
    currencyFormat: NumberFormat
) {
    val monthlyTotal = monthlyBudget?.monthlyTotalBudget ?: 2000.0
    val netExpense = (totalExpense - totalIncome).coerceAtLeast(0.0)
    val balance = monthlyTotal - netExpense

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "本月概览",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = AnimeOnSurface.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 支出
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = AnimeRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "总支出",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = AnimeOnSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                    Text(
                        text = currencyFormat.format(totalExpense),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = AnimeRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // 收入
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "总收入",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = AnimeOnSurface.copy(alpha = 0.6f)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = AnimeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = currencyFormat.format(totalIncome),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = AnimeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = AnimePink.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // 结余
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月结余",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = AnimeOnSurface
                    )
                )
                Text(
                    text = currencyFormat.format(balance),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = if (balance >= 0) AnimeGreen else AnimeRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun CategoryExpenseItem(
    category: String,
    amount: Double,
    totalExpense: Double,
    currencyFormat: NumberFormat
) {
    val percentage = if (totalExpense > 0) (amount / totalExpense * 100) else 0.0

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AnimeRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = null,
                    tint = AnimeRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 分类信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getCategoryName(category),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 进度条
                LinearProgressIndicator(
                    progress = (percentage / 100).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = AnimePink,
                    trackColor = AnimePink.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 金额和百分比
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currencyFormat.format(amount),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = AnimeRed,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AnimeOnSurface.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyStatisticsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "本月暂无支出记录",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = AnimeOnSurface.copy(alpha = 0.5f)
                )
            )
        }
    }
}