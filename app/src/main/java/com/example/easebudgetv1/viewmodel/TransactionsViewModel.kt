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

/* 
 * viewmodel for transactions list. handles filterin by date and category.
 * 
 * References:
 * Google (2024) 'Paging Library overview', Android Developers. Available at: https://developer.android.com/topic/libraries/architecture/paging/v3-pms (Accessed: 24 May 2024)
 * Google (2024) 'Paging data caching', Android Developers. Available at: https://developer.android.com/topic/libraries/architecture/paging/v3-transform#cachedin (Accessed: 26 May 2024)
 * Kotlin (2024) 'Flow debounce', Kotlin Documentation. Available at: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/debounce.html (Accessed: 26 May 2024)
 * 
 * we used the paging lib to make sure even if there is thousands of logs it dont lag the phone.
 * it also tracks streaks for when the user adds stuff daily. basically keeping them engaged.
 */

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

    // this is the main flow for the paginated list. 
    // it updates automatically when any filter changes. debounce helps so we dont spam the DB
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
    
    // combining everything into one state flow for the screen.
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
    
    // logic to add a new transaction and trigger badge checks. 
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
            
            // update the logging streak if they loggin everyday.
            updateLoggingStreak(userId)
            
            // see if they earned any badges like for using a receipt photo
            checkBadges(userId, receiptPath != null)
            
            _isAddEditDialogVisible.value = false
        }
    }
    
    // basic logic to keep track of consecutive days of logging.
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
                // its a consecutive day. good job user
                val newCurrent = existingStreak.currentStreak + 1
                repository.updateStreak(existingStreak.copy(
                    currentStreak = newCurrent,
                    longestStreak = maxOf(existingStreak.longestStreak, newCurrent),
                    lastActivityDate = now
                ))
            } else if (diff > 2 * oneDayMs) {
                // streak is broken unfortunately. resets to 1
                repository.updateStreak(existingStreak.copy(currentStreak = 1, lastActivityDate = now))
            }
        }
    }

    // checks for achievements when a transaction is added to the system
    private suspend fun checkBadges(userId: Long, hasReceipt: Boolean) {
        // give them the first transaction badge so they feel welcome
        if (repository.getAchievementByType(userId, "FIRST_TRANSACTION") == null) {
            repository.insertAchievement(Achievement(
                userId = userId,
                badgeType = "FIRST_TRANSACTION",
                title = "Kickstarter",
                description = "Added your first financial record!",
                icon = "🚀"
            ))
        }
        
        // badge for saving a photo of the receipt. keeps them organized
        if (hasReceipt && repository.getAchievementByType(userId, "RECEIPT_KEEPER") == null) {
            repository.insertAchievement(Achievement(
                userId = userId,
                badgeType = "RECEIPT_KEEPER",
                title = "Organized",
                description = "Saved your first receipt photo.",
                icon = "📸"
            ))
        }
        
        // reward consistency after 10 logs
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
