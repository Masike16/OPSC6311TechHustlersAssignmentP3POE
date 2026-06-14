package com.example.easebudgetv1.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easebudgetv1.data.database.entities.Achievement
import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.User
import com.example.easebudgetv1.data.repository.AppRepository
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.utils.GamificationUtils
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class DashboardUiState(
    val userName: String = "",
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val currentBalance: Double = 0.0,
    val monthlySpending: Double = 0.0,
    val budgetLimit: Double = 0.0,
    val readyToAssign: Double = 0.0,
    val savingsGoal: Double = 0.0,
    val savingsProgress: Float = 0f,
    val ageOfMoney: Int = 0,
    val recentAchievements: List<Achievement> = emptyList(),
    val topCategories: List<CategorySpending> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repositoryLazy: Lazy<AppRepository>
) : ViewModel() {
    
    private val repository get() = repositoryLazy.get()
    private val _userId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            val start = DateUtils.getStartOfMonth()
            val end = DateUtils.getEndOfMonth()
            
            val userFlow = repository.getUserByIdFlow(userId)
            val categoriesFlow = repository.getCategoriesByUserId(userId)
            val spendingSummariesFlow = repository.getCategorySpendingFlow(userId, start, end)
            val allTransactionsFlow = repository.getTransactionsByUserId(userId)
            val achievementsFlow = repository.getAchievementsByUserId(userId)
            
            val monthlyDataFlow = combine(
                repository.getMonthlyIncomeFlow(userId, start, end),
                repository.getMonthlySpendingFlow(userId, start, end),
                repository.getBudgetGoalFlow(userId, DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())
            ) { monthlyInc, monthlyExp, goal ->
                Triple(monthlyInc ?: 0.0, monthlyExp ?: 0.0, goal)
            }

            combine(
                combine(
                    repository.getRecentTransactions(userId, 5),
                    categoriesFlow,
                    spendingSummariesFlow
                ) { recent, cats, summaries -> Triple(recent, cats, summaries) },
                monthlyDataFlow,
                userFlow,
                allTransactionsFlow,
                achievementsFlow
            ) { recentData, monthlyData, user, allTransactions, achievements ->
                val (recentTransactions, categories, spendingSummaries) = recentData
                val (monthlyInc, monthlyExp, goal) = monthlyData

                val budgetedIncome = goal?.monthlyTotalBudget ?: 0.0
                val totalCategoryLimits = categories.sumOf { it.budgetLimit ?: 0.0 }
                val savingsGoalValue = goal?.savingsGoal ?: 0.0
                
                val age = GamificationUtils.calculateAgeOfMoney(allTransactions)
                val netSavings = maxOf(0.0, monthlyInc - monthlyExp)
                val savingsProgressValue = if (savingsGoalValue > 0) (netSavings / savingsGoalValue).coerceIn(0.0, 1.0).toFloat() else 0f
                
                // Trigger badges asynchronously
                checkBadges(userId, age, monthlyExp, goal?.maxSpendingGoal ?: 0.0, netSavings, savingsGoalValue)

                val topCats = categories.map { cat ->
                    val spent = spendingSummaries.find { it.categoryName == cat.name }?.totalAmount ?: 0.0
                    CategorySpending(
                        categoryName = cat.name,
                        amount = spent,
                        budgetLimit = cat.budgetLimit ?: 0.0,
                        percentage = if (cat.budgetLimit != null && cat.budgetLimit > 0) (spent / cat.budgetLimit).toFloat() else 0f
                    )
                }.filter { it.amount > 0 }.sortedByDescending { it.amount }.take(4)
                
                DashboardUiState(
                    userName = user?.username ?: "",
                    transactions = recentTransactions,
                    totalIncome = monthlyInc,
                    totalExpenses = monthlyExp,
                    // INNOVATION: Balance = Budgeted Amount - Expenses (YNAB-style)
                    currentBalance = maxOf(budgetedIncome, monthlyInc) - monthlyExp,
                    monthlySpending = monthlyExp,
                    budgetLimit = goal?.maxSpendingGoal ?: 0.0,
                    readyToAssign = budgetedIncome - totalCategoryLimits - savingsGoalValue,
                    savingsGoal = savingsGoalValue,
                    savingsProgress = savingsProgressValue,
                    ageOfMoney = age,
                    recentAchievements = achievements.take(3),
                    topCategories = topCats,
                    isLoading = false
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
    
    fun loadDashboardData(userId: Long) {
        _userId.value = userId
    }

    private fun checkBadges(userId: Long, age: Int, spent: Double, limit: Double, netSavings: Double, savingsGoal: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            if (age >= 7 && repository.getAchievementByType(userId, "AGE_OF_MONEY_7") == null) {
                repository.insertAchievement(Achievement(
                    userId = userId,
                    badgeType = "AGE_OF_MONEY_7",
                    title = "Week Ahead",
                    description = "Your money is now at least 7 days old!",
                    icon = "⏳"
                ))
            }
            if (age >= 30 && repository.getAchievementByType(userId, "AGE_OF_MONEY_30") == null) {
                repository.insertAchievement(Achievement(
                    userId = userId,
                    badgeType = "AGE_OF_MONEY_30",
                    title = "Financial Fortress",
                    description = "Impressive! Your money is over 30 days old.",
                    icon = "🏰"
                ))
            }
            
            // Savings Hero badge
            if (savingsGoal > 0 && netSavings >= savingsGoal && repository.getAchievementByType(userId, "SAVINGS_HERO") == null) {
                repository.insertAchievement(Achievement(
                    userId = userId,
                    badgeType = "SAVINGS_HERO",
                    title = "Savings Hero",
                    description = "You've reached your savings goal for this month!",
                    icon = "🦸"
                ))
            }
            
            // Check for Budget Master badge at end of month
            if (limit > 0 && System.currentTimeMillis() > DateUtils.getEndOfMonth() - 86400000) {
                 if (spent <= limit && repository.getAchievementByType(userId, "BUDGET_MASTER") == null) {
                    repository.insertAchievement(Achievement(
                        userId = userId,
                        badgeType = "BUDGET_MASTER",
                        title = "Budget Master",
                        description = "Stayed within your monthly limit. Consistency is key!",
                        icon = "👑"
                    ))
                }
            }
        }
    }
}
