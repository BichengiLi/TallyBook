package com.example.tallybook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallybook.data.ExpenseCategory
import com.example.tallybook.data.IncomeCategory
import com.example.tallybook.data.TransactionType
import com.example.tallybook.ui.theme.*
import com.example.tallybook.viewmodel.TallyBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: TallyBookViewModel = viewModel()
) {
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val expenseCategories = ExpenseCategory.values().toList()
    val incomeCategories = IncomeCategory.values().toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "添加记录",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 类型选择
            TransactionTypeSelector(
                selectedType = transactionType,
                onTypeSelected = {
                    transactionType = it
                    selectedCategory = ""
                }
            )

            // 金额输入
            AmountInput(
                amount = amount,
                onAmountChange = { amount = it }
            )

            // 分类选择
            CategorySelector(
                categories = if (transactionType == TransactionType.EXPENSE) {
                    expenseCategories.map { it.name }
                } else {
                    incomeCategories.map { it.name }
                },
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                isExpense = transactionType == TransactionType.EXPENSE
            )

            // 备注输入
            NoteInput(
                note = note,
                onNoteChange = { note = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            SaveButton(
                enabled = amount.isNotEmpty() && selectedCategory.isNotEmpty(),
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue > 0) {
                        if (transactionType == TransactionType.EXPENSE) {
                            viewModel.addExpense(amountValue, selectedCategory, note)
                        } else {
                            viewModel.addIncome(amountValue, selectedCategory, note)
                        }
                        onNavigateBack()
                    }
                }
            )
        }
    }
}

@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TypeButton(
                text = "支出",
                icon = Icons.Default.ArrowUpward,
                isSelected = selectedType == TransactionType.EXPENSE,
                selectedColor = AnimeRed,
                onClick = { onTypeSelected(TransactionType.EXPENSE) }
            )
            TypeButton(
                text = "收入",
                icon = Icons.Default.ArrowDownward,
                isSelected = selectedType == TransactionType.INCOME,
                selectedColor = AnimeGreen,
                onClick = { onTypeSelected(TransactionType.INCOME) }
            )
        }
    }
}

@Composable
fun TypeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) selectedColor else Color.Transparent
    val contentColor = if (isSelected) Color.White else AnimeOnSurface

    Button(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = if (isSelected) {
            ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        } else {
            ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "金额",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AnimeOnSurface.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = AnimePink,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onAmountChange(newValue)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "0.00",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = AnimeOnSurface.copy(alpha = 0.3f)
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = AnimeOnSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AnimePink,
                        unfocusedBorderColor = AnimePink.copy(alpha = 0.3f),
                        cursorColor = AnimePink
                    ),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    isExpense: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "分类",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AnimeOnSurface.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = category == selectedCategory,
                        isExpense = isExpense,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    isExpense: Boolean,
    onClick: () -> Unit
) {
    val selectedColor = if (isExpense) AnimeRed else AnimeGreen
    val backgroundColor = if (isSelected) selectedColor else selectedColor.copy(alpha = 0.1f)
    val contentColor = if (isSelected) Color.White else selectedColor

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = getCategoryName(category),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}

@Composable
fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "备注",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AnimeOnSurface.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "添加备注...",
                        color = AnimeOnSurface.copy(alpha = 0.3f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AnimePink,
                    unfocusedBorderColor = AnimePink.copy(alpha = 0.3f),
                    cursorColor = AnimePink
                ),
                maxLines = 3
            )
        }
    }
}

@Composable
fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AnimePink,
            contentColor = Color.White,
            disabledContainerColor = AnimePink.copy(alpha = 0.3f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "保存",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}