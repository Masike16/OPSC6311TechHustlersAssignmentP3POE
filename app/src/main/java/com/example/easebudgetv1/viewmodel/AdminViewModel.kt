package com.example.easebudgetv1.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.easebudgetv1.data.database.entities.Achievement
import com.example.easebudgetv1.data.database.entities.AdminActionLog
import com.example.easebudgetv1.data.database.entities.BudgetGoal
import com.example.easebudgetv1.data.database.entities.SharedAccount
import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.TransactionType
import com.example.easebudgetv1.data.database.entities.User
import com.example.easebudgetv1.data.repository.AppRepository
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class AdminUiState(
    val selectedUser: User? = null,
    val userDetails: AdminUserDetails? = null,
    val platformStats: PlatformStats = PlatformStats(),
    val authState: AdminAuthState = AdminAuthState.Idle,
    val isLoading: Boolean = false
)

@Immutable
data class AdminUserDetails(
    val user: User?,
    val transactions: List<Transaction>,
    val budgetGoals: List<BudgetGoal>,
    val achievements: List<Achievement>,
    val sharedAccounts: List<SharedAccount>,
    val currentBalance: Double,
    val monthlySpending: Double,
    val isOverspending: Boolean
)

@Immutable
data class PlatformStats(
    val totalUsers: Int = 0,
    val totalMoneyTracked: Double = 0.0,
    val avgSpendingPerUser: Double = 0.0,
    val totalBadgesEarned: Int = 0
)

sealed class AdminAuthState {
    object Idle : AdminAuthState()
    object Loading : AdminAuthState()
    object Success : AdminAuthState()
    data class Error(val message: String) : AdminAuthState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    // Optimization: Lazy injection defers repository heavy lifting
    private val repositoryLazy: Lazy<AppRepository>
) : ViewModel() {
    
    private val repository get() = repositoryLazy.get()
    
    private val _selectedUserId = MutableStateFlow<Long?>(null)
    private val _authState = MutableStateFlow<AdminAuthState>(AdminAuthState.Idle)
    private val _isLoading = MutableStateFlow(false)

    // Optimization: Paging flows handle their own resource management
    val usersPaged: Flow<PagingData<User>> = repository.getAllUsersPaged().cachedIn(viewModelScope)
    val adminLogsPaged: Flow<PagingData<AdminActionLog>> = repository.getAdminActionLogPaged().cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AdminUiState> = combine(
        combine(_selectedUserId, _authState, _isLoading) { id, auth, loading ->
            AdminBaseState(id, auth, loading)
        },
        combine(
            repository.getUserCountFlow(),
            repository.getAllTransactionsSumFlow(),
            repository.getAllExpensesSumFlow(),
            repository.getTotalBadgesEarnedFlow()
        ) { userCount, totalSum, expensesSum, totalBadges ->
            PlatformStats(
                totalUsers = userCount,
                totalMoneyTracked = totalSum ?: 0.0,
                avgSpendingPerUser = if (userCount > 0) (expensesSum ?: 0.0) / userCount else 0.0,
                totalBadgesEarned = totalBadges
            )
        }
    ) { base, stats ->
        base to stats
    }.flatMapLatest { (base, stats) ->
        val userId = base.userId
        if (userId != null) {
            combine(
                repository.getUserByIdFlow(userId),
                repository.getTransactionsByUserId(userId),
                repository.getBudgetGoalsByUserId(userId),
                repository.getAchievementsByUserId(userId),
                repository.getSharedAccountsByUserId(userId)
            ) { user, transactions, budgetGoals, achievements, sharedAccounts ->
                
                // One-pass calculation for specific user metrics
                var income = 0.0
                var expenses = 0.0
                transactions.forEach {
                    if (it.type == TransactionType.INCOME) income += it.amount
                    else expenses += it.amount
                }
                
                AdminUiState(
                    selectedUser = user,
                    userDetails = AdminUserDetails(
                        user = user,
                        transactions = transactions,
                        budgetGoals = budgetGoals,
                        achievements = achievements,
                        sharedAccounts = sharedAccounts,
                        currentBalance = income - expenses,
                        monthlySpending = expenses,
                        isOverspending = false 
                    ),
                    platformStats = stats,
                    authState = base.authState,
                    isLoading = base.isLoading
                )
            }
        } else {
            flowOf(AdminUiState(platformStats = stats, authState = base.authState, isLoading = base.isLoading))
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminUiState(isLoading = true)
    )

    private data class AdminBaseState(
        val userId: Long?,
        val authState: AdminAuthState,
        val isLoading: Boolean
    )
    
    fun adminLogin(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AdminAuthState.Loading
            if (username == "admin" && password == "admin123") {
                _authState.value = AdminAuthState.Success
            } else {
                _authState.value = AdminAuthState.Error("Invalid admin credentials")
            }
        }
    }
    
    fun selectUser(userId: Long) {
        _selectedUserId.value = userId
    }

    fun deselectUser() {
        _selectedUserId.value = null
    }
    
    fun changeUserPassword(userId: Long, newPassword: String) {
        viewModelScope.launch {
            repository.updateUserPassword(userId, newPassword)
            logAdminAction("PASSWORD_CHANGE", userId, "Changed password")
        }
    }
    
    fun deactivateAccount(userId: Long) {
        viewModelScope.launch {
            repository.deactivateUser(userId, false)
            logAdminAction("ACCOUNT_DEACTIVATE", userId, "Deactivated account")
        }
    }
    
    fun activateAccount(userId: Long) {
        viewModelScope.launch {
            repository.deactivateUser(userId, true)
            logAdminAction("ACCOUNT_ACTIVATE", userId, "Activated account")
        }
    }
    
    fun deleteAccount(userId: Long) {
        viewModelScope.launch {
            repository.deleteUserById(userId)
            logAdminAction("ACCOUNT_DELETE", userId, "Deleted account")
        }
    }
    
    private fun logAdminAction(actionType: String, targetUserId: Long, description: String) {
        viewModelScope.launch {
            repository.insertAdminActionLog(
                AdminActionLog(
                    adminUserId = 0,
                    actionType = actionType,
                    targetUserId = targetUserId,
                    description = description
                )
            )
        }
    }
    
    fun clearError() {
        _authState.value = AdminAuthState.Idle
    }
}
