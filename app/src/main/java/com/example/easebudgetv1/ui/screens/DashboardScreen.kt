/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.TransactionType
import com.example.easebudgetv1.utils.CurrencyFormatter
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.utils.GamificationUtils
import com.example.easebudgetv1.viewmodel.CategorySpending
import com.example.easebudgetv1.viewmodel.DashboardViewModel

/*
 * the main landing page of the app. shows a summary of everything.
 * we used material 3 cards and headers to make it look modern and clean
 * 
 * References:
 * Material Design (2024) 'Material 3', Google. Available at: https://m3.material.io/ (Accessed: 24 May 2024)
 * Android Developers (2024) 'Compose Layouts', Google. Available at: https://developer.android.com/develop/ui/compose/layouts (Accessed: 25 May 2024)
 * Kotlin (2024) 'Flow', JetBrains. Available at: https://kotlinlang.org/docs/flow.html (Accessed: 25 May 2024)
 * 
 * it uses a lazycolumn so it doesnt lag when scrolling through transactions.
 * the FAB at the bottom right is for quick entry since thats the main thing people do
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userId: Long,
    onAddTransactionClick: () -> Unit,
    onSharedAccountsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSeeAllTransactionsClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showHelpDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        viewModel.loadDashboardData(userId)
    }

    if (showHelpDialog) {
        HelpOverlayDialog(onDismiss = { showHelpDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = if (uiState.userName.isNotEmpty()) "Hello, ${uiState.userName}" else "My Budget",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            DateUtils.formatDate(System.currentTimeMillis()), 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        BalanceHeader(uiState.currentBalance, uiState.totalIncome, uiState.totalExpenses, uiState.readyToAssign)
                    }

                    // Requirement 3: Savings Goal Progress
                    if (uiState.savingsGoal > 0) {
                        item {
                            SavingsGoalCard(uiState.savingsGoal, uiState.savingsProgress)
                        }
                    }

                    // INNOVATION: Gamification Quick Stats
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoCard(
                                title = "Age of Money",
                                value = "${uiState.ageOfMoney} Days",
                                icon = Icons.Default.Timer,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            val health = GamificationUtils.calculateBudgetHealth(uiState.monthlySpending, uiState.budgetLimit * 0.8, uiState.budgetLimit)
                            InfoCard(
                                title = "Budget Status",
                                value = when (health) {
                                    GamificationUtils.BudgetHealth.ON_TRACK -> "Healthy"
                                    GamificationUtils.BudgetHealth.OVER_BUDGET -> "Critical"
                                    GamificationUtils.BudgetHealth.UNDER_BUDGET -> "Under"
                                },
                                icon = Icons.Default.HealthAndSafety,
                                modifier = Modifier.weight(1f),
                                color = when (health) {
                                    GamificationUtils.BudgetHealth.ON_TRACK -> Color(0xFF2E7D32)
                                    GamificationUtils.BudgetHealth.OVER_BUDGET -> Color(0xFFC62828)
                                    GamificationUtils.BudgetHealth.UNDER_BUDGET -> Color(0xFFFF9800)
                                }
                            )
                        }
                    }
                    
                    if (uiState.topCategories.isNotEmpty()) {
                        item {
                            SectionHeader("Category Progress")
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    uiState.topCategories.forEach { category ->
                                        CategoryProgressItem(category)
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        SectionHeader("Monthly Limit Progress")
                        Spacer(modifier = Modifier.height(8.dp))
                        ModernSpendingCard(uiState.monthlySpending, uiState.budgetLimit)
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader("Recent Transactions")
                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                ),
                                modifier = Modifier.clickable { onSeeAllTransactionsClick() }
                            )
                        }
                    }
                    
                    if (uiState.transactions.isEmpty()) {
                        item {
                            EmptyStateDashboard()
                        }
                    } else {
                        items(
                            items = uiState.transactions,
                            key = { it.id }
                        ) { transaction ->
                            ModernTransactionItem(transaction)
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            // Floating Buttons Column
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Help FAB (Requirement 2)
                SmallFloatingActionButton(
                    onClick = { showHelpDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Help")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shared Accounts FAB
                    FloatingActionButton(
                        onClick = onSharedAccountsClick,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = "Family Sharing")
                    }

                    // Add Transaction FAB
                    ExtendedFloatingActionButton(
                        onClick = onAddTransactionClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Log Entry") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// shows how much the user has saved vs their goal. uses a progress bar
@Composable
fun SavingsGoalCard(goal: Double, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Savings Goal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Text(CurrencyFormatter.format(goal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% towards your monthly target",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategoryProgressItem(category: CategorySpending) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category.categoryName, style = MaterialTheme.typography.bodySmall)
            Text(CurrencyFormatter.format(category.amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { category.percentage.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = if (category.percentage > 1f) Color.Red else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun EmptyStateDashboard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No transactions yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HelpOverlayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
        title = { Text("EasEBudget Guide") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HelpItem(Icons.Default.Dashboard, "Dashboard", "Overview of your finances and unallocated budget.")
                HelpItem(Icons.Default.AccountBalanceWallet, "Budget", "Set your monthly goals and category limits.")
                HelpItem(Icons.Default.Add, "Add Entry", "Log expenses with optional receipt photos and time tracking.")
                HelpItem(Icons.Default.BarChart, "Reports", "Visualize your spending patterns.")
            }
        }
    )
}

@Composable
fun HelpItem(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// this is the main header showing balance. its the first thing users see
@Composable
fun BalanceHeader(balance: Double, income: Double, expenses: Double, readyToAssign: Double) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyFormatter.format(balance),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderStat(
                        label = "Income",
                        amount = income,
                        icon = Icons.Default.ArrowDownward,
                        color = Color(0xFF81C784)
                    )
                    VerticalDivider(
                        modifier = Modifier.height(40.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    )
                    HeaderStat(
                        label = "Expenses",
                        amount = expenses,
                        icon = Icons.Default.ArrowUpward,
                        color = Color(0xFFE57373)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Ready to Assign",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Text(
                    CurrencyFormatter.format(readyToAssign),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (readyToAssign >= 0) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun HeaderStat(label: String, amount: Double, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
            Text(CurrencyFormatter.format(amount), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

// this card shows monthly limit progress. turns red if you go over
@Composable
fun ModernSpendingCard(monthlySpending: Double, budgetLimit: Double) {
    val progress = if (budgetLimit > 0) (monthlySpending / budgetLimit).coerceIn(0.0, 1.0).toFloat() else 0f
    val percentValue = (progress * 100).toInt()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Spending Limit", style = MaterialTheme.typography.titleSmall)
                Text("$percentValue%", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (progress > 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${CurrencyFormatter.format(monthlySpending)} spent of ${CurrencyFormatter.format(budgetLimit)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// individual transaction item for the list. shows income in green and expense in red
@Composable
fun ModernTransactionItem(transaction: Transaction) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isIncome = transaction.type == TransactionType.INCOME
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
                Text(
                    text = DateUtils.formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = (if (isIncome) "+" else "-") + CurrencyFormatter.format(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}
