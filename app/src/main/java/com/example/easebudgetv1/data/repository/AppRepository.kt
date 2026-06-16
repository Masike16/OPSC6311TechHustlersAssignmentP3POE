/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.easebudgetv1.data.database.dao.AppDao
import com.example.easebudgetv1.data.database.dao.CategorySpendingSummary
import com.example.easebudgetv1.data.database.dao.DailySpendingSummary
import com.example.easebudgetv1.data.database.dao.MonthlyExpenseSummary
import com.example.easebudgetv1.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val dao: AppDao
) {
    // User operations
    suspend fun getUserByEmail(email: String): User? = dao.getUserByEmail(email)
    suspend fun getUserById(userId: Long): User? = dao.getUserById(userId)
    fun getUserByIdFlow(userId: Long): Flow<User?> = dao.getUserByIdFlow(userId)
    fun getAllUsersPaged(): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.getAllUsersPaged() }
        ).flow
    }
    fun getUserCountFlow(): Flow<Int> = dao.getUserCountFlow()
    suspend fun insertUser(user: User): Long = dao.insertUser(user)
    suspend fun updateUser(user: User) = dao.updateUser(user)
    suspend fun updateUserPassword(userId: Long, passwordHash: String) = dao.updateUserPassword(userId, passwordHash)
    suspend fun deactivateUser(userId: Long, isActive: Boolean) = dao.deactivateUser(userId, isActive)
    suspend fun deleteUserById(userId: Long) = dao.deleteUserAndAllData(userId)
    suspend fun updateUserGuidePreference(userId: Long, showAgain: Boolean) = dao.updateUserGuidePreference(userId, showAgain)

    // Category operations
    fun getCategoriesByUserId(userId: Long): Flow<List<Category>> = dao.getCategoriesByUserId(userId)
    suspend fun insertCategory(category: Category): Long = dao.insertCategory(category)
    suspend fun insertCategories(categories: List<Category>) = dao.insertCategories(categories)
    suspend fun updateCategory(category: Category) = dao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)

    // Transaction operations
    fun getTransactionsByUserId(userId: Long): Flow<List<Transaction>> = dao.getTransactionsByUserId(userId)
    
    fun getTransactionsPaged(userId: Long, categoryId: Long? = null, query: String? = null, startDate: Long? = null, endDate: Long? = null): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { dao.getTransactionsPaged(userId, categoryId, query, startDate, endDate) }
        ).flow
    }
    
    fun getRecentTransactions(userId: Long, limit: Int): Flow<List<Transaction>> = dao.getRecentTransactions(userId, limit)
    suspend fun getTransactionById(transactionId: Long): Transaction? = dao.getTransactionById(transactionId)
    suspend fun insertTransaction(transaction: Transaction): Long = dao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = dao.deleteTransaction(transaction)
    fun getAllTransactionsSumFlow(): Flow<Double?> = dao.getAllTransactionsSumFlow()
    fun getAllExpensesSumFlow(): Flow<Double?> = dao.getAllExpensesSumFlow()

    // BudgetGoal operations
    fun getBudgetGoalFlow(userId: Long, month: Int, year: Int): Flow<BudgetGoal?> = dao.getBudgetGoalFlow(userId, month, year)
    suspend fun getBudgetGoal(userId: Long, month: Int, year: Int): BudgetGoal? = dao.getBudgetGoal(userId, month, year)
    fun getBudgetGoalsByUserId(userId: Long): Flow<List<BudgetGoal>> = dao.getBudgetGoalsByUserId(userId)
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long = dao.insertBudgetGoal(budgetGoal)
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal) = dao.updateBudgetGoal(budgetGoal)

    // Achievement operations
    fun getAchievementsByUserId(userId: Long): Flow<List<Achievement>> = dao.getAchievementsByUserId(userId)
    suspend fun getAchievementByType(userId: Long, badgeType: String): Achievement? = dao.getAchievementByType(userId, badgeType)
    suspend fun insertAchievement(achievement: Achievement): Long = dao.insertAchievement(achievement)
    fun getTotalBadgesEarnedFlow(): Flow<Int> = dao.getTotalBadgesEarnedFlow()

    // Streak operations
    suspend fun getStreak(userId: Long, streakType: String): Streak? = dao.getStreak(userId, streakType)
    fun getStreaksByUserId(userId: Long): Flow<List<Streak>> = dao.getStreaksByUserId(userId)
    suspend fun updateStreak(streak: Streak) = dao.updateStreak(streak)
    suspend fun insertStreak(streak: Streak): Long = dao.insertStreak(streak)

    // Shared Account operations
    fun getSharedAccountsByUserId(userId: Long): Flow<List<SharedAccount>> = dao.getSharedAccountsByUserId(userId)
    suspend fun getSharedAccountById(id: Long): SharedAccount? = dao.getSharedAccountById(id)
    suspend fun insertSharedAccount(sharedAccount: SharedAccount): Long = dao.insertSharedAccount(sharedAccount)
    suspend fun deleteSharedAccount(id: Long) = dao.deleteSharedAccount(id)
    suspend fun updateSharedAccountLimit(id: Long, limit: Double) = dao.updateSharedAccountLimit(id, limit)
    
    fun getPendingRequestsByEmail(email: String): Flow<List<SharedAccountRequest>> = dao.getPendingRequestsByEmail(email)
    suspend fun getSharedAccountRequest(id: Long): SharedAccountRequest? = dao.getSharedAccountRequest(id)
    suspend fun insertSharedAccountRequest(request: SharedAccountRequest): Long = dao.insertSharedAccountRequest(request)
    suspend fun updateRequestStatus(requestId: Long, status: String, respondedAt: Long) = dao.updateRequestStatus(requestId, status, respondedAt)

    // Admin operations
    fun getAdminActionLogPaged(): Flow<PagingData<AdminActionLog>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.getAdminActionLogPaged() }
        ).flow
    }
    suspend fun insertAdminActionLog(log: AdminActionLog) = dao.insertAdminActionLog(log)

    // Aggregate operations - Unified to non-nullable Double Flow
    fun getMonthlyIncomeFlow(userId: Long, start: Long, end: Long): Flow<Double> = dao.getMonthlyIncomeFlow(userId, start, end)
    fun getMonthlySpendingFlow(userId: Long, start: Long, end: Long): Flow<Double> = dao.getMonthlySpendingFlow(userId, start, end)
    fun getCategorySpendingFlow(userId: Long, start: Long, end: Long): Flow<List<CategorySpendingSummary>> = dao.getCategorySpendingFlow(userId, start, end)
    fun getTotalIncomeFlow(userId: Long): Flow<Double> = dao.getTotalIncomeFlow(userId)
    fun getTotalExpensesFlow(userId: Long): Flow<Double> = dao.getTotalExpensesFlow(userId)
    fun getMonthlyExpenseHistoryFlow(userId: Long): Flow<List<MonthlyExpenseSummary>> = dao.getMonthlyExpenseHistoryFlow(userId)
    fun getDailySpendingFlow(userId: Long, startDate: Long, endDate: Long): Flow<List<DailySpendingSummary>> = dao.getDailySpendingFlow(userId, startDate, endDate)
}
