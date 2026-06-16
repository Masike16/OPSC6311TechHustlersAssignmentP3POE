/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easebudgetv1.data.database.dao.CategorySpendingSummary
import com.example.easebudgetv1.data.database.dao.DailySpendingSummary
import com.example.easebudgetv1.data.database.dao.MonthlyExpenseSummary
import com.example.easebudgetv1.data.database.entities.BudgetGoal
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.repository.AppRepository
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.utils.GamificationUtils
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@Immutable
data class CategorySpending(
    val categoryName: String,
    val amount: Double,
    val budgetLimit: Double,
    val percentage: Float
)

@Immutable
data class MonthlyHealth(
    val monthLabel: String,
    val spent: Double,
    val minGoal: Double,
    val maxGoal: Double,
    val health: GamificationUtils.BudgetHealth
)

@Immutable
data class ReportsUiState(
    val budgetGoal: BudgetGoal? = null,
    val selectedPeriod: String = "This Month",
    val customDateRange: Pair<Long, Long>? = null,
    val categorySpendingItems: List<CategorySpending> = emptyList(),
    val expenseHistory: List<MonthlyExpenseSummary> = emptyList(),
    val healthHistory: List<MonthlyHealth> = emptyList(),
    val dailySpending: List<DailySpendingSummary> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val currentBalance: Double = 0.0,
    val predictedSpending: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repositoryLazy: Lazy<AppRepository>
) : ViewModel() {

    private val repository get() = repositoryLazy.get()
    private val _userId = MutableStateFlow<Long?>(null)
    private val _selectedPeriod = MutableStateFlow("This Month")
    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReportsUiState> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            val coreTotalsFlow = combine(
                repository.getTotalIncomeFlow(userId),
                repository.getTotalExpensesFlow(userId),
                repository.getMonthlyExpenseHistoryFlow(userId),
                repository.getBudgetGoalsByUserId(userId)
            ) { income, expenses, history, goals ->
                val healthHistory = history.map { summary ->
                    val parts = summary.monthLabel.split("-")
                    val year = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val month = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    
                    val goal = goals.find { it.month == month && it.year == year }
                    val minGoal = goal?.minSpendingGoal ?: 0.0
                    val maxGoal = goal?.maxSpendingGoal ?: 0.0
                    
                    MonthlyHealth(
                        monthLabel = summary.monthLabel,
                        spent = summary.totalAmount,
                        minGoal = minGoal,
                        maxGoal = maxGoal,
                        health = GamificationUtils.calculateBudgetHealth(summary.totalAmount, minGoal, maxGoal)
                    )
                }
                
                Quad(income, expenses, history, healthHistory)
            }

            val periodBoundsFlow = combine(_selectedPeriod, _customDateRange) { period, customRange ->
                if (period == "Custom Range" && customRange != null) {
                    customRange
                } else {
                    getPeriodBounds(period)
                }
            }

            val periodDataFlow = periodBoundsFlow.flatMapLatest { bounds ->
                combine(
                    repository.getCategorySpendingFlow(userId, bounds.first, bounds.second),
                    repository.getCategoriesByUserId(userId),
                    repository.getDailySpendingFlow(userId, bounds.first, bounds.second)
                ) { summaries, categories, daily ->
                    Triple(summaries, categories, daily)
                }
            }

            val goalFlow = repository.getBudgetGoalFlow(userId, DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())

            combine(
                coreTotalsFlow,
                periodDataFlow,
                goalFlow,
                _selectedPeriod,
                _customDateRange
            ) { coreData, periodData, budgetGoal, period, customRange ->
                val (income, expenses, history, healthHistory) = coreData
                val (summaries, categories, daily) = periodData
                
                val totalFilteredExpenses = summaries.sumOf { it.totalAmount }
                
                val spendingItems = categories.map { category ->
                    val spent = summaries.find { it.categoryName == category.name }?.totalAmount ?: 0.0
                    CategorySpending(
                        categoryName = category.name,
                        amount = spent,
                        budgetLimit = category.budgetLimit ?: 0.0,
                        percentage = if (totalFilteredExpenses > 0) (spent / totalFilteredExpenses).toFloat() else 0f
                    )
                }.filter { it.amount > 0 || it.budgetLimit > 0 }.sortedByDescending { it.amount }
                
                val predicted = if (history.isNotEmpty()) {
                    history.map { it.totalAmount }.sum() / history.size.toDouble()
                } else 0.0
                
                ReportsUiState(
                    budgetGoal = budgetGoal,
                    selectedPeriod = period,
                    customDateRange = customRange,
                    categorySpendingItems = spendingItems,
                    expenseHistory = history,
                    healthHistory = healthHistory,
                    dailySpending = daily,
                    totalIncome = income,
                    totalExpenses = expenses,
                    currentBalance = income - expenses,
                    predictedSpending = predicted,
                    isLoading = false
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReportsUiState(isLoading = true)
        )

    fun loadReportsData(userId: Long) {
        _userId.value = userId
    }

    fun selectPeriod(period: String, customRange: Pair<Long, Long>? = null) {
        _selectedPeriod.value = period
        if (customRange != null) {
            _customDateRange.value = customRange
        }
    }

    private fun getPeriodBounds(period: String): Pair<Long, Long> {
        return when (period) {
            "Today" -> DateUtils.getStartOfDay() to DateUtils.getEndOfDay()
            "This Week" -> DateUtils.getStartOfWeek() to DateUtils.getEndOfWeek()
            "This Month" -> DateUtils.getStartOfMonth() to DateUtils.getEndOfMonth()
            "All Time" -> 0L to System.currentTimeMillis()
            else -> 0L to Long.MAX_VALUE
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
