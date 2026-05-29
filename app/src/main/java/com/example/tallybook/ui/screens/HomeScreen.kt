package com.example.tallybook.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallybook.data.DailyBudget
import com.example.tallybook.data.MonthlyBudget
import com.example.tallybook.data.Transaction
import com.example.tallybook.data.TransactionType
import com.example.tallybook.ui.theme.*
import com.example.tallybook.viewmodel.TallyBookViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToMonthlyHistory: () -> Unit,
    viewModel: TallyBookViewModel = viewModel()
) {
    val todayBudget by viewModel.todayBudget.collectAsState()
    val todayExpense by viewModel.todayExpense.collectAsState()
    val todayDailyExpense by viewModel.todayDailyExpense.collectAsState()
    val todayTransactions by viewModel.todayTransactions.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val showUndoSnackbar by viewModel.showUndoSnackbar.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val monthlyOtherSpent by viewModel.monthlyOtherSpent.collectAsState()
    val monthlyDailyNetExpense by viewModel.monthlyDailyNetExpense.collectAsState()

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    val snackbarHostState = remember { SnackbarHostState() }

    // 监听撤回Snackbar状态
    LaunchedEffect(showUndoSnackbar) {
        if (showUndoSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = "记录已添加",
                actionLabel = "撤回",
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    viewModel.undoLastTransaction()
                }
                SnackbarResult.Dismissed -> {
                    viewModel.dismissUndoSnackbar()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "记账本",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AnimePink
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = AnimePink,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加记录",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AnimeBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 预算卡片
            item {
                BudgetCard(
                    budget = todayBudget,
                    todayExpense = todayDailyExpense,
                    todayIncome = todayIncome,
                    monthlyBudget = monthlyBudget,
                    monthlyOtherSpent = monthlyOtherSpent,
                    monthlyDailySpent = monthlyDailyNetExpense,
                    currencyFormat = currencyFormat,
                    onUpdateRatio = { other, daily -> viewModel.updateMonthlyRatio(other, daily) }
                )
            }

            // 今日收支概览
            item {
                TodaySummaryCard(
                    todayExpense = todayDailyExpense,
                    todayIncome = todayIncome,
                    currencyFormat = currencyFormat
                )
            }

            // 快捷操作
            item {
                QuickActionsRow(
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToStatistics = onNavigateToStatistics,
                    onNavigateToMonthlyHistory = onNavigateToMonthlyHistory
                )
            }

            // 今日交易记录
            item {
                Text(
                    text = "今日记录",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AnimeOnBackground
                    )
                )
            }

            if (todayTransactions.isEmpty()) {
                item {
                    EmptyTransactionCard()
                }
            } else {
                items(todayTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { viewModel.deleteTransaction(transaction) },
                        currencyFormat = currencyFormat
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetCard(
    budget: DailyBudget?,
    todayExpense: Double,
    todayIncome: Double,
    monthlyBudget: MonthlyBudget?,
    monthlyOtherSpent: Double,
    monthlyDailySpent: Double,
    currencyFormat: NumberFormat,
    onUpdateRatio: (Double, Double) -> Unit
) {
    val budgetAmount = budget?.budget ?: 0.0
    val netExpense = todayExpense - todayIncome
    val remaining = budget?.remaining ?: (budgetAmount - netExpense).coerceAtLeast(0.0)
    val progress = if (budgetAmount > 0) {
        (netExpense / budgetAmount).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "budget_progress"
    )

    val progressColor = when {
        progress > 1f -> AnimeRed
        progress > 0.8f -> AnimeOrange
        progress > 0.5f -> AnimeYellow
        else -> AnimeGreen
    }

    var showRatioDialog by remember { mutableStateOf(false) }

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AnimePink, AnimePurple)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "每日日常预算",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (budget?.isOverBudget == true) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AnimeRed.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "超支",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        // 调整预算按钮
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { showRatioDialog = true }
                        ) {
                            Text(
                                text = "调整",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currencyFormat.format(budgetAmount),
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 进度条
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "净支出: ${currencyFormat.format(netExpense)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                    Text(
                        text = "剩余: ${currencyFormat.format(remaining.coerceAtLeast(0.0))}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }

                // 节省/超支金额显示器
                Spacer(modifier = Modifier.height(12.dp))
                SavingsDisplay(
                    budget = budget,
                    currencyFormat = currencyFormat
                )

                if (budget?.isOverBudget == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "超支 ${currencyFormat.format(budget.overAmount)}，明日预算将减少",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AnimeYellow
                        )
                    )
                }

                // 月度预算摘要
                if (monthlyBudget != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    MonthlyBudgetSummary(
                        monthlyBudget = monthlyBudget,
                        monthlyOtherSpent = monthlyOtherSpent,
                        monthlyDailySpent = monthlyDailySpent,
                        currencyFormat = currencyFormat
                    )
                }
            }
        }
    }

    // 调整预算比例对话框
    if (showRatioDialog && monthlyBudget != null) {
        BudgetRatioDialog(
            currentOther = monthlyBudget.otherBudget,
            currentDaily = monthlyBudget.dailyBudget,
            currentMonthlyTotal = monthlyBudget.monthlyTotalBudget,
            onConfirm = { monthlyTotal, other, daily ->
                viewModel.updateMonthlyTotalBudget(monthlyTotal)
                onUpdateRatio(other, daily)
                showRatioDialog = false
            },
            onDismiss = { showRatioDialog = false },
            currencyFormat = currencyFormat
        )
    }
}

@Composable
fun MonthlyBudgetSummary(
    monthlyBudget: MonthlyBudget,
    monthlyOtherSpent: Double,
    monthlyDailySpent: Double,
    currencyFormat: NumberFormat
) {
    val otherRemaining = monthlyBudget.otherBudget - monthlyOtherSpent
    val dailyRemaining = monthlyBudget.dailyBudget - monthlyDailySpent

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "月预算",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
                Text(
                    text = currencyFormat.format(monthlyBudget.monthlyTotalBudget),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "其他支出(娱乐)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
                Text(
                    text = "${currencyFormat.format(monthlyOtherSpent)} / ${currencyFormat.format(monthlyBudget.otherBudget)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (otherRemaining >= 0) Color.White else AnimeRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "日常净支出",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
                Text(
                    text = "${currencyFormat.format(monthlyDailySpent)} / ${currencyFormat.format(monthlyBudget.dailyBudget)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (dailyRemaining >= 0) Color.White else AnimeRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun BudgetRatioDialog(
    currentOther: Double,
    currentDaily: Double,
    currentMonthlyTotal: Double,
    onConfirm: (Double, Double, Double) -> Unit,
    onDismiss: () -> Unit,
    currencyFormat: NumberFormat
) {
    var monthlyTotal by remember { mutableStateOf(currentMonthlyTotal) }
    var isEditingTotal by remember { mutableStateOf(false) }
    var totalText by remember { mutableStateOf(currentMonthlyTotal.toInt().toString()) }

    val total = monthlyTotal.toFloat()
    var dailyValue by remember { mutableStateOf(currentDaily.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "调整月度预算",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 月度总预算
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "月度总预算",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (isEditingTotal) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = totalText,
                                    onValueChange = { totalText = it },
                                    modifier = Modifier.width(120.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(onClick = {
                                    val parsed = totalText.toDoubleOrNull()
                                    if (parsed != null && parsed > 0) {
                                        monthlyTotal = parsed
                                    }
                                    if (dailyValue > monthlyTotal) dailyValue = monthlyTotal
                                    isEditingTotal = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "确认",
                                        tint = AnimeGreen
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currencyFormat.format(monthlyTotal),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AnimePink
                                    )
                                )
                                IconButton(onClick = { isEditingTotal = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "编辑",
                                        tint = AnimePink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Divider()

                // 日常支出滑块
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "日常支出",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = currencyFormat.format(dailyValue.toDouble()),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AnimePink
                            )
                        )
                    }
                    Slider(
                        value = dailyValue,
                        onValueChange = { dailyValue = it },
                        valueRange = 0f..total,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = AnimePink,
                            activeTrackColor = AnimePink
                        )
                    )
                }

                // 其他支出(娱乐)滑块（自动计算）
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "其他支出(娱乐)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = currencyFormat.format((total - dailyValue).toDouble()),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AnimePurple
                            )
                        )
                    }
                    LinearProgressIndicator(
                        progress = (total - dailyValue) / total,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AnimePurple,
                        trackColor = AnimePurple.copy(alpha = 0.2f)
                    )
                }

                // 总额显示
                Text(
                    text = "合计: ${currencyFormat.format(total.toDouble())}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AnimeGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        confirmButton = {
            val otherValue = total - dailyValue
            TextButton(
                onClick = { onConfirm(monthlyTotal, otherValue.toDouble(), dailyValue.toDouble()) }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun TodaySummaryCard(
    todayExpense: Double,
    todayIncome: Double,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 支出
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = AnimeRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "支出",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AnimeOnSurface.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = currencyFormat.format(todayExpense),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = AnimeRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // 分隔线
            Divider(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp),
                color = AnimePink.copy(alpha = 0.2f)
            )

            // 收入
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = AnimeGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "收入",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AnimeOnSurface.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = currencyFormat.format(todayIncome),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = AnimeGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onNavigateToHistory: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToMonthlyHistory: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.History,
                text = "本月记录",
                onClick = onNavigateToHistory,
                color = AnimeBlue
            )
            QuickActionButton(
                icon = Icons.Default.BarChart,
                text = "统计分析",
                onClick = onNavigateToStatistics,
                color = AnimePurple
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.Savings,
                text = "月度记录",
                onClick = onNavigateToMonthlyHistory,
                color = AnimeGreen
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun EmptyTransactionCard() {
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
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = AnimePink.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "今天还没有记录哦~",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = AnimeOnSurface.copy(alpha = 0.5f)
                )
            )
            Text(
                text = "点击右下角按钮添加记录",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AnimeOnSurface.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit,
    currencyFormat: NumberFormat
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) AnimeRed else AnimeGreen
    val amountPrefix = if (isExpense) "-" else "+"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.category),
                    contentDescription = transaction.category,
                    tint = amountColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getCategoryName(transaction.category),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                if (transaction.note.isNotEmpty()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AnimeOnSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            // 金额
            Text(
                text = "$amountPrefix${currencyFormat.format(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
            )

            // 删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = AnimeOnSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "FOOD" -> Icons.Default.Restaurant
        "TRANSPORT" -> Icons.Default.DirectionsCar
        "SHOPPING" -> Icons.Default.ShoppingCart
        "ENTERTAINMENT" -> Icons.Default.Movie
        "MEDICAL" -> Icons.Default.LocalHospital
        "EDUCATION" -> Icons.Default.School
        "SALARY" -> Icons.Default.AttachMoney
        "BONUS" -> Icons.Default.CardGiftcard
        "INVESTMENT" -> Icons.Default.TrendingUp
        "GIFT" -> Icons.Default.Redeem
        else -> Icons.Default.MoreHoriz
    }
}

fun getCategoryName(category: String): String {
    return when (category) {
        "FOOD" -> "餐饮"
        "TRANSPORT" -> "交通"
        "SHOPPING" -> "购物"
        "ENTERTAINMENT" -> "娱乐"
        "MEDICAL" -> "医疗"
        "EDUCATION" -> "教育"
        "SALARY" -> "工资"
        "BONUS" -> "奖金"
        "INVESTMENT" -> "投资"
        "GIFT" -> "礼金"
        else -> "其他"
    }
}

@Composable
fun SavingsDisplay(
    budget: DailyBudget?,
    currencyFormat: NumberFormat
) {
    val isOverBudget = budget?.isOverBudget ?: false
    val amount = if (isOverBudget) {
        budget?.overAmount ?: 0.0
    } else {
        budget?.savedAmount ?: 0.0
    }

    val displayColor = if (isOverBudget) AnimeRed else AnimeGreen
    val displayText = if (isOverBudget) {
        "-${currencyFormat.format(amount)}"
    } else {
        "+${currencyFormat.format(amount)}"
    }

    val labelText = if (isOverBudget) "超支" else "节省"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = displayColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isOverBudget) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = displayColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = displayColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = displayColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}