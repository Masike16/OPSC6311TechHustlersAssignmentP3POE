/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.easebudgetv1.data.database.entities.BudgetGoal
import com.example.easebudgetv1.utils.CurrencyFormatter
import com.example.easebudgetv1.utils.GamificationUtils
import com.example.easebudgetv1.viewmodel.CategorySpending
import com.example.easebudgetv1.viewmodel.MonthlyHealth
import com.example.easebudgetv1.viewmodel.ReportsViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

/*
 * the reports screen is where we show the user all the pretty charts.
 * it helps them see where their money is actually going.
 * 
 * References:
 * Vico (2024) 'Vico: A powerful charting library for Android', GitHub. Available at: https://github.com/patrykandpatrick/vico (Accessed: 22 May 2024)
 * Google (2024) 'Canvas in Compose', Android Developers. Available at: https://developer.android.com/develop/ui/compose/graphics/draw/canvas (Accessed: 25 May 2024)
 * 
 * we use the Vico library for the line and bar charts cause its really good for compose.
 * I also made a custom pie chart using the canvas api just to show how the spending is split.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    userId: Long,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePickerRange by remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        viewModel.loadReportsData(userId)
    }

    if (showDatePickerRange) {
        DateRangePickerDialog(
            onDismiss = { showDatePickerRange = false },
            onDateRangeSelected = { start, end ->
                viewModel.selectPeriod("Custom Range", start to end)
                showDatePickerRange = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Analytics", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                    // period selector lets users swap between today, this week, month etc
                    ModernPeriodSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { 
                            if (it == "Custom Range") {
                                showDatePickerRange = true
                            } else {
                                viewModel.selectPeriod(it) 
                            }
                        }
                    )
                }
                
                item {
                    ModernSummaryCards(uiState.totalIncome, uiState.totalExpenses, uiState.currentBalance)
                }

                item {
                    BudgetHealthVisualization(uiState.budgetGoal, uiState.totalExpenses)
                }

                if (uiState.healthHistory.isNotEmpty()) {
                    item {
                        BudgetHealthHistoryChart(uiState.healthHistory)
                    }
                }

                if (uiState.categorySpendingItems.isNotEmpty()) {
                    item {
                        SpendingVsGoalsChart(uiState.categorySpendingItems)
                    }

                    if (uiState.dailySpending.isNotEmpty()) {
                        item {
                            DailySpendingTrendChart(uiState.dailySpending)
                        }
                    }

                    item {
                        SpendingPieChart(uiState.categorySpendingItems)
                    }

                    item {
                        TopCategoriesSection(uiState.categorySpendingItems.take(5))
                    }
                }

                item {
                    ModernPredictionCard(uiState.predictedSpending)
                }

                item {
                    Text(
                        text = "Detailed Category Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.categorySpendingItems.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "No spending data for this period",
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = uiState.categorySpendingItems,
                        key = { "item_${it.categoryName}" }
                    ) { item ->
                        ModernCategorySpendingItem(item)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// this chart shows how they performed over the last few months
@Composable
fun BudgetHealthHistoryChart(history: List<MonthlyHealth>) {
    val entries = history.mapIndexed { index, health -> 
        entryOf(index.toFloat(), health.spent.toFloat()) 
    }
    val chartEntryModel = entryModelOf(entries)

    val horizontalAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        history.getOrNull(value.toInt())?.monthLabel ?: ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Budget Performance History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = lineChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = horizontalAxisValueFormatter),
                modifier = Modifier.height(180.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                history.takeLast(3).forEach { health ->
                    val color = when (health.health) {
                        GamificationUtils.BudgetHealth.ON_TRACK -> Color(0xFF4CAF50)
                        GamificationUtils.BudgetHealth.OVER_BUDGET -> Color(0xFFF44336)
                        GamificationUtils.BudgetHealth.UNDER_BUDGET -> Color(0xFFFF9800)
                    }
                    Surface(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(health.monthLabel, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = when (health.health) {
                                    GamificationUtils.BudgetHealth.ON_TRACK -> "Good"
                                    GamificationUtils.BudgetHealth.OVER_BUDGET -> "Over"
                                    GamificationUtils.BudgetHealth.UNDER_BUDGET -> "Under"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopCategoriesSection(topItems: List<CategorySpending>) {
    Column {
        Text(
            text = "Top Spending Categories",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            topItems.take(3).forEach { item ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(item.categoryName, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        Text(
                            CurrencyFormatter.format(item.amount), 
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailySpendingTrendChart(dailyData: List<com.example.easebudgetv1.data.database.dao.DailySpendingSummary>) {
    val entries = dailyData.mapIndexed { index, summary -> 
        entryOf(index.toFloat(), summary.totalAmount.toFloat()) 
    }
    val chartEntryModel = entryModelOf(entries)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Spending Trend",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = lineChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.height(180.dp)
            )
        }
    }
}

// visual indicator for how well the user is sticking to their budget
@Composable
fun BudgetHealthVisualization(budgetGoal: BudgetGoal?, currentSpending: Double) {
    if (budgetGoal == null) return

    val health = GamificationUtils.calculateBudgetHealth(
        currentSpending, 
        budgetGoal.minSpendingGoal, 
        budgetGoal.maxSpendingGoal
    )

    val (statusText, statusColor) = when (health) {
        GamificationUtils.BudgetHealth.ON_TRACK -> "On Track" to Color(0xFF4CAF50)
        GamificationUtils.BudgetHealth.OVER_BUDGET -> "Over Budget" to Color(0xFFF44336)
        GamificationUtils.BudgetHealth.UNDER_BUDGET -> "Under Budget" to Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(statusColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (health == GamificationUtils.BudgetHealth.ON_TRACK) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Budget Health: $statusText",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
                Text(
                    text = if (health == GamificationUtils.BudgetHealth.OVER_BUDGET) 
                        "You've exceeded your spending limit!" 
                        else "You're managing your budget effectively.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// shows how much they spent vs what they planned for each category
@Composable
fun SpendingVsGoalsChart(items: List<CategorySpending>) {
    val spentEntries = items.mapIndexed { index, item -> entryOf(index.toFloat(), item.amount.toFloat()) }
    val goalEntries = items.mapIndexed { index, item -> entryOf(index.toFloat(), item.budgetLimit.toFloat()) }
    
    val chartEntryModel = entryModelOf(spentEntries, goalEntries)

    val horizontalAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        items.getOrNull(value.toInt())?.categoryName?.take(8) ?: ""
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f).toArgb()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Category Spending vs. Goals",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = columnChart(
                    columns = listOf(primaryColor, secondaryColor).map { argb ->
                        com.patrykandpatrick.vico.core.component.shape.LineComponent(
                            color = argb,
                            thicknessDp = 8f,
                            shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(allPercent = 40)
                        )
                    }
                ),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = horizontalAxisValueFormatter),
                modifier = Modifier.height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                LegendItem(MaterialTheme.colorScheme.primary, "Spent")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), "Goal")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

// custom donut chart using canvas. looks much cleaner than a regular pie chart
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpendingPieChart(items: List<CategorySpending>) {
    if (items.isEmpty()) return

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF81C784),
        Color(0xFFE57373),
        Color(0xFF64B5F6),
        Color(0xFFFFB74D),
        Color(0xFF9575CD)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Expense Distribution",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    items.forEachIndexed { index, item ->
                        val sweepAngle = item.percentage * 360f
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelSmall)
                    Text(
                        CurrencyFormatter.format(items.sumOf { it.amount }),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(colors[index % colors.size], CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item.categoryName, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernPeriodSelector(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    val periods = listOf("Today", "This Week", "This Month", "All Time", "Custom Range")
    val selectedIndex = periods.indexOf(selectedPeriod).coerceAtLeast(0)
    
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp,
        divider = {},
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        periods.forEach { period ->
            Tab(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                text = {
                    Text(
                        text = period,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun ModernSummaryCards(income: Double, expenses: Double, balance: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Net Balance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text(
                        CurrencyFormatter.format(balance),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModernReportStatCard(
                title = "Income",
                amount = income,
                color = Color(0xFF2E7D32),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                modifier = Modifier.weight(1f)
            )
            ModernReportStatCard(
                title = "Expenses",
                amount = expenses,
                color = Color(0xFFC62828),
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ModernReportStatCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// prediction logic to show the user what to expect. helps them plan ahead.
@Composable
fun ModernPredictionCard(predictedSpending: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("AI Smart Forecast", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                Text(
                    "Expected spending: ${CurrencyFormatter.format(predictedSpending)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text("Based on your last 3 months", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ModernCategorySpendingItem(item: CategorySpending) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.categoryName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text(CurrencyFormatter.format(item.amount), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { item.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${(item.percentage * 100).toInt()}% of total expenses",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
