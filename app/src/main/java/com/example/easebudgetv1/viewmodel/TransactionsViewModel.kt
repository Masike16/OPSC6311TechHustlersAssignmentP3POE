package com.example.easebudgetv1.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.easebudgetv1.data.database.entities.Achievement
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.database.entities.Streak
import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.TransactionType
import com.example.easebudgetv1.data.repository.AppRepository
import com.example.easebudgetv1.utils.DateUtils
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DateFilter {
    TODAY, WEEK, MONTH, ALL, CUSTOM
}

@Immutable
data class TransactionsUiState(
    val categories: List<Category> = emptyList(),
    val categorySpending: Map<Long, Double> = emptyMap(),
    val selectedTransaction: Transaction? = null,
    val selectedCategoryId: Long? = null,
    val dateFilter: DateFilter = DateFilter.MONTH,
    val customDateRange: Pair<Long, Long>? = null,
    val searchQuery: String = "",
    val isAddEditDialogVisible: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repositoryLazy: Lazy<AppRepository>
) : ViewModel() {
    
    private val repository get() = repositoryLazy.get()
    private val _userId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _dateFilter = MutableStateFlow(DateFilter.MONTH)
    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    private val _isAddEditDialogVisible = MutableStateFlow(false)
    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactionsPaged: Flow<PagingData<Transaction>> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                _selectedCategoryId,
                _searchQuery.debounce(300),
                _dateFilter,
                _customDateRange
            ) { categoryId, query, filter, customRange ->
                val range = when (filter) {
                    DateFilter.TODAY -> DateUtils.getStartOfDay() to DateUtils.getEndOfDay()
                    DateFilter.WEEK -> DateUtils.getStartOfWeek() to DateUtils.getEndOfWeek()
                    DateFilter.MONTH -> DateUtils.getStartOfMonth() to DateUtils.getEndOfMonth()
                    DateFilter.CUSTOM -> customRange ?: (0L to Long.MAX_VALUE)
                    DateFilter.ALL -> null
                }
                repository.getTransactionsPaged(
                    userId,
                    categoryId,
                    query.takeIf { it.isNotBlank() },
                    range?.first,
                    range?.second
                )
            }.flatMapLatest { it }
        }
        .cachedIn(viewModelScope)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TransactionsUiState> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            val categoriesFlow = repository.getCategoriesByUserId(userId)
            val spendingFlow = repository.getCategorySpendingFlow(userId, DateUtils.getStartOfMonth(), DateUtils.getEndOfMonth())
            
            combine(
                categoriesFlow,
                spendingFlow,
                _selectedTransaction,
                combine(
                    _selectedCategoryId,
                    _searchQuery,
                    _dateFilter,
                    _customDateRange,
                    _isAddEditDialogVisible
                ) { categoryId, query, filter, customRange, isVisible ->
                    FilterParams(categoryId, query, filter, customRange, isVisible)
                }
            ) { cats, spendingSummaries, selected, filters ->
                TransactionsUiState(
                    categories = cats,
                    categorySpending = cats.associate { cat ->
                        cat.id to (spendingSummaries.find { it.categoryName == cat.name }?.totalAmount ?: 0.0)
                    },
                    selectedTransaction = selected,
                    selectedCategoryId = filters.categoryId,
                    searchQuery = filters.query,
                    dateFilter = filters.filter,
                    customDateRange = filters.customRange,
                    isAddEditDialogVisible = filters.isVisible,
                    isLoading = false
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TransactionsUiState(isLoading = true)
        )
    
    fun loadTransactions(userId: Long) {
        _userId.value = userId
    }
    
    fun setDateFilter(filter: DateFilter, customRange: Pair<Long, Long>? = null) {
        _dateFilter.value = filter
        _customDateRange.value = customRange
    }

    fun loadCategories(userId: Long) {
        _userId.value = userId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryFilterChange(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }
    
    fun showAddDialog() {
        _selectedTransaction.value = null
        _isAddEditDialogVisible.value = true
    }
    
    fun showEditDialog(transaction: Transaction) {
        _selectedTransaction.value = transaction
        _isAddEditDialogVisible.value = true
    }
    
    fun hideDialog() {
        _isAddEditDialogVisible.value = false
        _selectedTransaction.value = null
    }
    
    fun addTransaction(
        userId: Long,
        categoryId: Long?,
        amount: Double,
        type: TransactionType,
        date: Long,
        description: String,
        receiptPath: String?,
        startTime: String? = null,
        endTime: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = Transaction(
                userId = userId,
                categoryId = categoryId,
                amount = amount,
                type = type,
                date = date,
                description = description,
                receiptPath = receiptPath,
                createdBy = userId,
                startTime = startTime,
                endTime = endTime
            )
            repository.insertTransaction(transaction)
            
            // Gamification: Update Streak
            updateLoggingStreak(userId)
            
            // Gamification: Check Badges
            checkBadges(userId, receiptPath != null)
            
            _isAddEditDialogVisible.value = false
        }
    }
    
    private suspend fun updateLoggingStreak(userId: Long) {
        val streakType = "LOGGING"
        val existingStreak = repository.getStreak(userId, streakType)
        val now = System.currentTimeMillis()
        
        if (existingStreak == null) {
            repository.insertStreak(Streak(userId = userId, streakType = streakType, currentStreak = 1, longestStreak = 1, lastActivityDate = now))
        } else {
            val lastActivity = existingStreak.lastActivityDate
            val diff = now - lastActivity
            val oneDayMs = 24 * 60 * 60 * 1000L
            
            if (diff in oneDayMs..(2 * oneDayMs)) {
                // Consecutive day
                val newCurrent = existingStreak.currentStreak + 1
                repository.updateStreak(existingStreak.copy(
                    currentStreak = newCurrent,
                    longestStreak = maxOf(existingStreak.longestStreak, newCurrent),
                    lastActivityDate = now
                ))
            } else if (diff > 2 * oneDayMs) {
                // Streak broken
                repository.updateStreak(existingStreak.copy(currentStreak = 1, lastActivityDate = now))
            }
        }
    }

    private suspend fun checkBadges(userId: Long, hasReceipt: Boolean) {
        // First Transaction Badge
        if (repository.getAchievementByType(userId, "FIRST_TRANSACTION") == null) {
            repository.insertAchievement(Achievement(
                userId = userId,
                badgeType = "FIRST_TRANSACTION",
                title = "Kickstarter",
                description = "Added your first financial record!",
                icon = "🚀"
            ))
        }
        
        // Receipt Keeper Badge
        if (hasReceipt && repository.getAchievementByType(userId, "RECEIPT_KEEPER") == null) {
            repository.insertAchievement(Achievement(
                userId = userId,
                badgeType = "RECEIPT_KEEPER",
                title = "Organized",
                description = "Saved your first receipt photo.",
                icon = "📸"
            ))
        }
        
        // Check for 10 transactions
        repository.getTransactionsByUserId(userId).first().let { transactions ->
            if (transactions.size >= 10 && repository.getAchievementByType(userId, "CONSISTENT_LOGGER") == null) {
                repository.insertAchievement(Achievement(
                    userId = userId,
                    badgeType = "CONSISTENT_LOGGER",
                    title = "Tracker",
                    description = "Logged 10 transactions. Keep going!",
                    icon = "📊"
                ))
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTransaction(transaction)
            _isAddEditDialogVisible.value = false
        }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
        }
    }
}

private data class FilterParams(
    val categoryId: Long?,
    val query: String,
    val filter: DateFilter,
    val customRange: Pair<Long, Long>?,
    val isVisible: Boolean
)
