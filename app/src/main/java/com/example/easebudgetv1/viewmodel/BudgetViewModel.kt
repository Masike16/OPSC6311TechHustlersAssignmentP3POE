package com.example.easebudgetv1.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easebudgetv1.data.database.entities.Achievement
import com.example.easebudgetv1.data.database.entities.BudgetGoal
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.repository.AppRepository
import com.example.easebudgetv1.utils.DateUtils
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class BudgetUiState(
    val categoryGroups: Map<String, List<Category>> = emptyMap(),
    val categorySpending: Map<Long, Double> = emptyMap(),
    val budgetGoal: BudgetGoal? = null,
    val selectedCategory: Category? = null,
    val readyToAssign: Double = 0.0,
    val totalCategoryLimits: Double = 0.0,
    val isAddCategoryDialogVisible: Boolean = false,
    val isBudgetGoalDialogVisible: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repositoryLazy: Lazy<AppRepository>
) : ViewModel() {
    
    private val repository get() = repositoryLazy.get()
    private val _userId = MutableStateFlow<Long?>(null)
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    private val _isAddCategoryDialogVisible = MutableStateFlow(false)
    private val _isBudgetGoalDialogVisible = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BudgetUiState> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            val start = DateUtils.getStartOfMonth()
            val end = DateUtils.getEndOfMonth()
            
            // Using a helper class to bundle filter states and avoid combine overloading issues
            val dialogStateFlow = combine(
                _selectedCategory,
                _isAddCategoryDialogVisible,
                _isBudgetGoalDialogVisible
            ) { selected, isAddVisible, isGoalVisible ->
                Triple(selected, isAddVisible, isGoalVisible)
            }

            combine(
                repository.getCategoriesByUserId(userId),
                repository.getBudgetGoalFlow(userId, DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()),
                repository.getCategorySpendingFlow(userId, start, end),
                dialogStateFlow
            ) { cats, budgetGoal, spendingSummaries, dialogStates ->
                val grouped = cats.groupBy { it.group }
                val totalLimits = cats.sumOf { it.budgetLimit ?: 0.0 }
                val totalBudget = budgetGoal?.monthlyTotalBudget ?: 0.0
                
                val spendingMap = cats.associate { cat ->
                    cat.id to (spendingSummaries.find { it.categoryName == cat.name }?.totalAmount ?: 0.0)
                }
                
                BudgetUiState(
                    categoryGroups = grouped,
                    categorySpending = spendingMap,
                    budgetGoal = budgetGoal,
                    selectedCategory = dialogStates.first,
                    readyToAssign = totalBudget - totalLimits,
                    totalCategoryLimits = totalLimits,
                    isAddCategoryDialogVisible = dialogStates.second,
                    isBudgetGoalDialogVisible = dialogStates.third,
                    isLoading = false
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetUiState(isLoading = true)
        )
    
    fun loadData(userId: Long) {
        if (_userId.value == userId) return
        _userId.value = userId
        
        viewModelScope.launch(Dispatchers.IO) {
            val categories = repository.getCategoriesByUserId(userId).first()
            if (categories.isEmpty()) {
                initializeDefaultCategories(userId)
            }
        }
    }
    
    fun showAddCategoryDialog() { 
        _selectedCategory.value = null
        _isAddCategoryDialogVisible.value = true 
    }
    
    fun showEditCategoryDialog(category: Category) {
        _selectedCategory.value = category
        _isAddCategoryDialogVisible.value = true
    }

    fun hideAddCategoryDialog() { 
        _isAddCategoryDialogVisible.value = false 
        _selectedCategory.value = null
    }
    
    fun showBudgetGoalDialog() { _isBudgetGoalDialogVisible.value = true }
    fun hideBudgetGoalDialog() { _isBudgetGoalDialogVisible.value = false }
    
    fun saveCategory(userId: Long, name: String, color: String, group: String, budgetLimit: Double?) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _selectedCategory.value
            if (current != null) {
                repository.updateCategory(current.copy(
                    name = name,
                    color = color,
                    group = group,
                    budgetLimit = budgetLimit
                ))
            } else {
                val category = Category(
                    userId = userId,
                    name = name,
                    color = color,
                    group = group,
                    budgetLimit = budgetLimit,
                    isDefault = false
                )
                repository.insertCategory(category)
            }
            _isAddCategoryDialogVisible.value = false
            _selectedCategory.value = null
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
        }
    }
    
    fun setBudgetGoal(userId: Long, monthlyTotalBudget: Double, minSpendingGoal: Double, maxSpendingGoal: Double, savingsGoal: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingGoal = repository.getBudgetGoal(userId, DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())

            if (existingGoal != null) {
                repository.updateBudgetGoal(existingGoal.copy(
                    monthlyTotalBudget = monthlyTotalBudget,
                    minSpendingGoal = minSpendingGoal,
                    maxSpendingGoal = maxSpendingGoal,
                    savingsGoal = savingsGoal
                ))
            } else {
                val newGoal = BudgetGoal(
                    userId = userId,
                    month = DateUtils.getCurrentMonth(),
                    year = DateUtils.getCurrentYear(),
                    monthlyTotalBudget = monthlyTotalBudget,
                    minSpendingGoal = minSpendingGoal,
                    maxSpendingGoal = maxSpendingGoal,
                    savingsGoal = savingsGoal
                )
                repository.insertBudgetGoal(newGoal)
            }
            
            // Gamification: Goal Setter Badge
            if (repository.getAchievementByType(userId, "GOAL_SETTER") == null) {
                repository.insertAchievement(Achievement(
                    userId = userId,
                    badgeType = "GOAL_SETTER",
                    title = "Planner",
                    description = "Set your first monthly budget goal!",
                    icon = "🎯"
                ))
            }

            _isBudgetGoalDialogVisible.value = false
        }
    }
    
    private suspend fun initializeDefaultCategories(userId: Long) {
        val defaultCategories = listOf(
            Category(userId = userId, name = "Food & Dining", color = "#FF9800", group = "Daily", isDefault = true),
            Category(userId = userId, name = "Transportation", color = "#2196F3", group = "Daily", isDefault = true),
            Category(userId = userId, name = "Shopping", color = "#E91E63", group = "Lifestyle", isDefault = true),
            Category(userId = userId, name = "Entertainment", color = "#9C27B0", group = "Lifestyle", isDefault = true),
            Category(userId = userId, name = "Health", color = "#4CAF50", group = "Daily", isDefault = true),
            Category(userId = userId, name = "Utilities", color = "#9E9E9E", group = "Fixed", isDefault = true),
            Category(userId = userId, name = "Income", color = "#009688", group = "Income", isDefault = true)
        )
        repository.insertCategories(defaultCategories)
    }
}
