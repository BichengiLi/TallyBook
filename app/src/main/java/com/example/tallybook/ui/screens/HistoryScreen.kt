package com.example.tallybook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallybook.data.Transaction
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

    // Group transactions by date (本月记录)
    val groupedTransactions = remember(monthlyTransactions) {
        monthlyTransactions.groupBy { it.date }
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
        if (groupedTransactions.isEmpty()) {
            EmptyHistoryContent(paddingValues)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(AnimeBackground),
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

@Composable
fun EmptyHistoryContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
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