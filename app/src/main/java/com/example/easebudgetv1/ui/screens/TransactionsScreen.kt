package com.example.easebudgetv1.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.TransactionType
import com.example.easebudgetv1.utils.CurrencyFormatter
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.utils.ImageUtils
import com.example.easebudgetv1.utils.ValidationUtils
import com.example.easebudgetv1.viewmodel.DateFilter
import com.example.easebudgetv1.viewmodel.TransactionsViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    userId: Long,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagedTransactions = viewModel.transactionsPaged.collectAsLazyPagingItems()
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDatePickerRange by remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        viewModel.loadTransactions(userId)
        viewModel.loadCategories(userId)
    }

    if (showDatePickerRange) {
        DateRangePickerDialog(
            onDismiss = { showDatePickerRange = false },
            onDateRangeSelected = { start, end ->
                viewModel.setDateFilter(DateFilter.CUSTOM, start to end)
                showDatePickerRange = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DateFilter.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        if (filter == DateFilter.CUSTOM) {
                                            showDatePickerRange = true
                                        } else {
                                            viewModel.setDateFilter(filter)
                                        }
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.dateFilter == filter) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)) {
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    SearchAndCategoryBar(
                        searchQuery = uiState.searchQuery,
                        onSearchChange = { viewModel.onSearchQueryChange(it) },
                        categories = uiState.categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onCategorySelect = { viewModel.onCategoryFilterChange(it) }
                    )
                }

                items(
                    count = pagedTransactions.itemCount,
                    key = pagedTransactions.itemKey { it.id }
                ) { index ->
                    val transaction = pagedTransactions[index]
                    if (transaction != null) {
                        ModernTransactionCard(
                            transaction = transaction,
                            onEdit = { viewModel.showEditDialog(it) },
                            onDelete = { viewModel.deleteTransaction(it) }
                        )
                    }
                }

                if (pagedTransactions.loadState.append is LoadState.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            if (pagedTransactions.loadState.refresh is LoadState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            
            if (pagedTransactions.itemCount == 0 && pagedTransactions.loadState.refresh is LoadState.NotLoading) {
                EmptyStateView(onAddClick = { viewModel.showAddDialog() })
            }
        }
    }
    
    if (uiState.isAddEditDialogVisible) {
        ModernAddEditTransactionDialog(
            transaction = uiState.selectedTransaction,
            categories = uiState.categories,
            onDismiss = { viewModel.hideDialog() },
            onSave = { amount, type, categoryId, date, description, receiptPath, startTime, endTime ->
                if (uiState.selectedTransaction != null) {
                    viewModel.updateTransaction(
                        uiState.selectedTransaction!!.copy(
                            amount = amount,
                            type = type,
                            categoryId = categoryId,
                            date = date,
                            description = description,
                            receiptPath = receiptPath,
                            startTime = startTime,
                            endTime = endTime
                        )
                    )
                } else {
                    viewModel.addTransaction(userId, categoryId, amount, type, date, description, receiptPath, startTime, endTime)
                }
            }
        )
    }
}

@Composable
fun SearchAndCategoryBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelect: (Long?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search transactions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Default.Close, contentDescription = null) } }
            } else null,
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelect(null) },
                label = { Text("All") }
            )
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { onCategorySelect(category.id) },
                    label = { Text(category.name) }
                )
            }
        }
    }
}

@Composable
fun ModernTransactionCard(
    transaction: Transaction,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }
    val isIncome = transaction.type == TransactionType.INCOME

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showOptions = !showOptions },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = DateUtils.formatDate(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.startTime != null || transaction.endTime != null) {
                        Text(
                            text = "${transaction.startTime ?: ""} - ${transaction.endTime ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (if (isIncome) "+" else "-") + CurrencyFormatter.format(transaction.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    if (transaction.receiptPath != null) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Receipt",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (showOptions) {
                if (transaction.receiptPath != null) {
                    AsyncImage(
                        model = transaction.receiptPath,
                        contentDescription = "Receipt Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onEdit(transaction) }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit")
                    }
                    TextButton(
                        onClick = { onDelete(transaction) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No transactions found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Track your spending by adding your first entry",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add First Transaction")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAddEditTransactionDialog(
    transaction: Transaction?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Double, TransactionType, Long?, Long, String, String?, String?, String?) -> Unit
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var selectedCategoryId by remember { mutableStateOf(transaction?.categoryId) }
    var startTime by remember { mutableStateOf(transaction?.startTime) }
    var endTime by remember { mutableStateOf(transaction?.endTime) }
    var receiptPath by remember { mutableStateOf(transaction?.receiptPath) }
    
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    // Validation State (Requirement 5)
    val isValid = remember(amount, description) {
        ValidationUtils.isValidAmount(amount.toDoubleOrNull() ?: 0.0) && 
        ValidationUtils.isValidDescription(description)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { receiptPath = it.toString() }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val filename = "receipt_${System.currentTimeMillis()}.webp"
            val path = ImageUtils.saveImageToInternalStorage(context, it, filename)
            if (path != null) {
                receiptPath = path
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                if (transaction != null) "Edit Entry" else "New Entry",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEach { itemType ->
                        val selected = type == itemType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable { type = itemType },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                if (type == TransactionType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            } else Color.Transparent,
                            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = itemType.name,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("R ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amount.isNotEmpty() && !ValidationUtils.isValidAmount(amount.toDoubleOrNull() ?: 0.0)
                )

                // INNOVATION: Amount Seekbar (Requirement 5)
                val amountFloat = amount.toFloatOrNull() ?: 0f
                Slider(
                    value = amountFloat.coerceIn(0f, 10000f),
                    onValueChange = { amount = it.toInt().toString() },
                    valueRange = 0f..10000f,
                    steps = 100,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                if (type == TransactionType.EXPENSE) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "Select Category",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(category.name, fontWeight = FontWeight.Bold)
                                            category.budgetLimit?.let {
                                                Text("Limit: ${CurrencyFormatter.format(it)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = description.isNotEmpty() && !ValidationUtils.isValidDescription(description)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = {
                            val calendar = Calendar.getInstance().apply { timeInMillis = date }
                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                date = newCal.timeInMillis
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(DateUtils.formatDate(date), style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                startTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                            }, 12, 0, true).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(startTime ?: "Start Time", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                endTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                            }, 12, 0, true).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(endTime ?: "End Time", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Column {
                    Text("Receipt Attachment", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera")
                        }
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery")
                        }
                    }
                    if (receiptPath != null) {
                        Box(modifier = Modifier.padding(top = 8.dp).size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray)) {
                             AsyncImage(
                                model = receiptPath,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                             )
                             IconButton(
                                onClick = { receiptPath = null },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                             ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                             }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    onSave(amountValue, type, selectedCategoryId, date, description, receiptPath, startTime, endTime)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isValid
            ) {
                Text("Save Entry", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}
