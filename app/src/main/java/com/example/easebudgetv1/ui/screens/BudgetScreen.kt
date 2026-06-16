/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.database.entities.BudgetGoal
import com.example.easebudgetv1.utils.CurrencyFormatter
import com.example.easebudgetv1.viewmodel.BudgetViewModel

/*
 * this screen lets users plan their monthly budget. they can set goals and 
 * manage their categories here. its basically the brain of the app
 * 
 * References:
 * Google (2024) 'Compose layouts', Android Developers. Available at: https://developer.android.com/develop/ui/compose/layouts (Accessed: 24 May 2024)
 * Material Design (2024) 'Cards', Material 3. Available at: https://m3.material.io/components/cards/overview (Accessed: 25 May 2024)
 * 
 * we used the lazycolumn to keep the list fast even if there are many categories. 
 * the state is collected from the viewmodel so the ui always stays in sync.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    userId: Long,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Planner", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddCategoryDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Category, contentDescription = null) },
                text = { Text("New Category") }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // using lazy column for scrolling through categories. its efficient
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    ModernBudgetGoalCard(
                        budgetGoal = uiState.budgetGoal,
                        readyToAssign = uiState.readyToAssign,
                        totalCategoryLimits = uiState.totalCategoryLimits,
                        onSetGoal = { viewModel.showBudgetGoalDialog() }
                    )
                }
                
                // showing categories grouped by their group name like daily or lifestyle
                uiState.categoryGroups.forEach { (groupName, categories) ->
                    item {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                    
                    items(
                        items = categories,
                        key = { it.id }
                    ) { category ->
                        val spent = uiState.categorySpending[category.id] ?: 0.0
                        ModernCategoryCard(
                            category = category,
                            spent = spent,
                            onEdit = { viewModel.showEditCategoryDialog(category) },
                            onDelete = { viewModel.deleteCategory(it) }
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
    
    // dialogs for adding or editing. we show them based on state from viewmodel
    if (uiState.isAddCategoryDialogVisible) {
        ModernAddEditCategoryDialog(
            category = uiState.selectedCategory,
            onDismiss = { viewModel.hideAddCategoryDialog() },
            onSave = { name, color, group, limit ->
                viewModel.saveCategory(userId, name, color, group, limit)
            }
        )
    }
    
    if (uiState.isBudgetGoalDialogVisible) {
        ModernBudgetGoalDialog(
            budgetGoal = uiState.budgetGoal,
            onDismiss = { viewModel.hideBudgetGoalDialog() },
            onSave = { total, min, max, savings ->
                viewModel.setBudgetGoal(userId, total, min, max, savings)
            }
        )
    }
}

// this card shows the main goal info. its colorful so it stands out
@Composable
fun ModernBudgetGoalCard(
    budgetGoal: BudgetGoal?,
    readyToAssign: Double,
    totalCategoryLimits: Double,
    onSetGoal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Monthly Budget",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = budgetGoal?.let { CurrencyFormatter.format(it.monthlyTotalBudget) } ?: "Set Goal",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                IconButton(
                    onClick = onSetGoal,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ready to Assign", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
                    Text(
                        CurrencyFormatter.format(readyToAssign),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (readyToAssign >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Total Limits", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
                    Text(CurrencyFormatter.format(totalCategoryLimits), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// card for each category. it has a progress bar to show how much is spent
@Composable
fun ModernCategoryCard(
    category: Category,
    spent: Double,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    val limit = category.budgetLimit ?: 0.0
    val progress = if (limit > 0) (spent / limit).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOverBudget = limit > 0 && spent > limit

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            try { Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f) } 
                            catch (e: Exception) { MaterialTheme.colorScheme.primaryContainer },
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(category.icon ?: "📦", style = MaterialTheme.typography.titleLarge)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            CurrencyFormatter.format(spent),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverBudget) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            " / ${CurrencyFormatter.format(limit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = { onEdit(category) }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDelete(category) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            if (limit > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = if (isOverBudget) Color.Red else Color(android.graphics.Color.parseColor(category.color)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val remaining = limit - spent
                    Text(
                        text = if (remaining >= 0) "${CurrencyFormatter.format(remaining)} left" else "${CurrencyFormatter.format(-remaining)} over",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else Color.Red
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// dialog for category management. using vertical scroll so it doesnt break on small screens
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ModernAddEditCategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double?) -> Unit
) {
    var name by remember(category) { mutableStateOf(category?.name ?: "") }
    var group by remember(category) { mutableStateOf(category?.group ?: "Needs") }
    var budgetLimit by remember(category) { mutableStateOf(category?.budgetLimit?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(if (category == null) "New Category" else "Edit Category", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                
                OutlinedTextField(
                    value = budgetLimit,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) budgetLimit = it },
                    label = { Text("Monthly Limit") },
                    prefix = { Text("R ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Text("Type", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Needs", "Wants", "Bills", "Lifestyle").forEach { type ->
                        val selected = group == type
                        FilterChip(
                            selected = selected,
                            onClick = { group = type },
                            label = { Text(type, style = MaterialTheme.typography.labelLarge) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, category?.color ?: "#1A237E", group, budgetLimit.toDoubleOrNull()) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (category == null) "Add Category" else "Update Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}

// this dialog handles the monthly goal. we take total income and savings goal
@Composable
fun ModernBudgetGoalDialog(
    budgetGoal: BudgetGoal?,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double, Double) -> Unit
) {
    var total by remember(budgetGoal) { mutableStateOf(budgetGoal?.monthlyTotalBudget?.toString() ?: "") }
    var savings by remember(budgetGoal) { mutableStateOf(budgetGoal?.savingsGoal?.toString() ?: "") }
    var maxSpend by remember(budgetGoal) { mutableStateOf(budgetGoal?.maxSpendingGoal?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Monthly Goal", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = total,
                    onValueChange = { total = it },
                    label = { Text("Total Monthly Income") },
                    prefix = { Text("R ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = savings,
                    onValueChange = { savings = it },
                    label = { Text("Savings Goal") },
                    prefix = { Text("R ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = maxSpend,
                    onValueChange = { maxSpend = it },
                    label = { Text("Max Spending Limit") },
                    prefix = { Text("R ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onSave(
                        total.toDoubleOrNull() ?: 0.0, 
                        0.0, 
                        maxSpend.toDoubleOrNull() ?: 0.0, 
                        savings.toDoubleOrNull() ?: 0.0
                    ) 
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}
