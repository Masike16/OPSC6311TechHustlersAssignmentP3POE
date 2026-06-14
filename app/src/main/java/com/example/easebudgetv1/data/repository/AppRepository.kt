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
    suspend fun getAllUsers(): List<User> = dao.getAllUsers()
    
    fun getUserCountFlow(): Flow<Int> = dao.getUserCountFlow()
    
    fun getAllUsersPaged(): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.getAllUsersPaged() }
        ).flow
    }

    suspend fun insertUser(user: User): Long = dao.insertUser(user)
    suspend fun updateUser(user: User) = dao.updateUser(user)
    suspend fun updateUserPassword(userId: Long, password: String) = dao.updateUserPassword(userId, password)
    suspend fun updateUserGuidePreference(userId: Long, showGuide: Boolean) = dao.updateUserGuidePreference(userId, showGuide)
    suspend fun deactivateUser(userId: Long, isActive: Boolean) = dao.deactivateUser(userId, isActive)
    suspend fun deleteUser(user: User) = dao.deleteUser(user)
    suspend fun deleteUserById(userId: Long) = dao.deleteUserAndAllData(userId)

    // Category operations
    fun getCategoriesByUserId(userId: Long): Flow<List<Category>> = dao.getCategoriesByUserId(userId)
    suspend fun getCategoryById(categoryId: Long): Category? = dao.getCategoryById(categoryId)
    suspend fun insertCategory(category: Category): Long = dao.insertCategory(category)
    suspend fun insertCategories(categories: List<Category>) = dao.insertCategories(categories)
    suspend fun updateCategory(category: Category) = dao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)
    suspend fun deleteAllCategories(userId: Long) = dao.deleteAllCategories(userId)

    // Transaction operations
    fun getTransactionsByUserId(userId: Long): Flow<List<Transaction>> = dao.getTransactionsByUserId(userId)
    
    fun getTransactionsPaged(userId: Long, categoryId: Long? = null, query: String? = null, startDate: Long? = null, endDate: Long? = null): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { dao.getTransactionsPaged(userId, categoryId, query, startDate, endDate) }
        ).flow
    }
    
    fun getRecentTransactions(userId: Long, limit: Int): Flow<List<Transaction>> = dao.getRecentTransactions(userId, limit)

    fun getTransactionsByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = 
        dao.getTransactionsByDateRange(userId, startDate, endDate)
    fun getTransactionsByCategory(userId: Long, categoryId: Long): Flow<List<Transaction>> = 
        dao.getTransactionsByCategory(userId, categoryId)
    suspend fun getTransactionById(transactionId: Long): Transaction? = dao.getTransactionById(transactionId)
    fun getTransactionsBySharedAccount(sharedAccountId: Long): Flow<List<Transaction>> = 
        dao.getTransactionsBySharedAccount(sharedAccountId)
    suspend fun insertTransaction(transaction: Transaction): Long = dao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = dao.deleteTransaction(transaction)
    suspend fun deleteAllTransactions(userId: Long) = dao.deleteAllTransactions(userId)

    // BudgetGoal operations
    suspend fun getBudgetGoal(userId: Long, month: Int, year: Int): BudgetGoal? = 
        dao.getBudgetGoal(userId, month, year)
    
    fun getBudgetGoalFlow(userId: Long, month: Int, year: Int): Flow<BudgetGoal?> = 
        dao.getBudgetGoalFlow(userId, month, year)

    fun getBudgetGoalsByUserId(userId: Long): Flow<List<BudgetGoal>> = dao.getBudgetGoalsByUserId(userId)
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long = dao.insertBudgetGoal(budgetGoal)
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal) = dao.updateBudgetGoal(budgetGoal)
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal) = dao.deleteBudgetGoal(budgetGoal)

    // Achievement operations
    fun getAchievementsByUserId(userId: Long): Flow<List<Achievement>> = dao.getAchievementsByUserId(userId)
    suspend fun getAchievementByType(userId: Long, badgeType: String): Achievement? = 
        dao.getAchievementByType(userId, badgeType)
    suspend fun insertAchievement(achievement: Achievement): Long = dao.insertAchievement(achievement)
    suspend fun deleteAchievement(achievement: Achievement) = dao.deleteAchievement(achievement)
    fun getTotalBadgesEarnedFlow(): Flow<Int> = dao.getTotalBadgesEarnedFlow()

    // Streak operations
    suspend fun getStreak(userId: Long, streakType: String): Streak? = dao.getStreak(userId, streakType)
    fun getStreaksByUserId(userId: Long): Flow<List<Streak>> = dao.getStreaksByUserId(userId)
    suspend fun insertStreak(streak: Streak): Long = dao.insertStreak(streak)
    suspend fun updateStreak(streak: Streak) = dao.updateStreak(streak)

    // SharedAccount operations
    fun getSharedAccountsByUserId(userId: Long): Flow<List<SharedAccount>> = 
        dao.getSharedAccountsByUserId(userId)
    suspend fun getSharedAccountById(accountId: Long): SharedAccount? = dao.getSharedAccountById(accountId)
    suspend fun insertSharedAccount(sharedAccount: SharedAccount): Long = dao.insertSharedAccount(sharedAccount)
    suspend fun updateSharedAccount(sharedAccount: SharedAccount) = dao.updateSharedAccount(sharedAccount)
    suspend fun updateSharedAccountLimit(accountId: Long, limit: Double) = 
        dao.updateSharedAccountLimit(accountId, limit)
    suspend fun deleteSharedAccount(sharedAccount: SharedAccount) = dao.deleteSharedAccount(sharedAccount)

    // SharedAccountRequest operations
    fun getPendingRequestsByEmail(email: String): Flow<List<SharedAccountRequest>> = 
        dao.getPendingRequestsByEmail(email)
    fun getRequestsByUserId(userId: Long): Flow<List<SharedAccountRequest>> = dao.getRequestsByUserId(userId)
    suspend fun getSharedAccountRequest(requestId: Long): SharedAccountRequest? = 
        dao.getSharedAccountRequest(requestId)
    suspend fun insertSharedAccountRequest(request: SharedAccountRequest): Long = 
        dao.insertSharedAccountRequest(request)
    suspend fun updateSharedAccountRequest(request: SharedAccountRequest) = 
        dao.updateSharedAccountRequest(request)
    suspend fun updateRequestStatus(requestId: Long, status: String, timestamp: Long) = 
        dao.updateRequestStatus(requestId, status, timestamp)

    // Notification operations
    fun getNotificationsByUserId(userId: Long): Flow<List<Notification>> = 
        dao.getNotificationsByUserId(userId)
    suspend fun insertNotification(notification: Notification): Long = dao.insertNotification(notification)
    suspend fun updateNotification(notification: Notification) = dao.updateNotification(notification)
    suspend fun markNotificationAsRead(notificationId: Long, isRead: Boolean) = 
        dao.markNotificationAsRead(notificationId, isRead)
    suspend fun deleteNotification(notification: Notification) = dao.deleteNotification(notification)

    // AdminActionLog operations
    fun getAdminActionLog(): Flow<List<AdminActionLog>> = dao.getAdminActionLog()
    
    fun getAdminActionLogPaged(): Flow<PagingData<AdminActionLog>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.getAdminActionLogPaged() }
        ).flow
    }

    suspend fun insertAdminActionLog(log: AdminActionLog): Long = dao.insertAdminActionLog(log)

    // Aggregate operations
    fun getTotalIncomeFlow(userId: Long): Flow<Double?> = dao.getTotalIncomeFlow(userId)
    fun getMonthlyIncomeFlow(userId: Long, start: Long, end: Long): Flow<Double?> = dao.getMonthlyIncomeFlow(userId, start, end)
    fun getTotalExpensesFlow(userId: Long): Flow<Double?> = dao.getTotalExpensesFlow(userId)
    fun getMonthlySpendingFlow(userId: Long, start: Long, end: Long): Flow<Double?> = dao.getMonthlySpendingFlow(userId, start, end)

    fun getCategorySpendingFlow(userId: Long, start: Long, end: Long): Flow<List<CategorySpendingSummary>> = 
        dao.getCategorySpendingFlow(userId, start, end)

    fun getMonthlyExpenseHistoryFlow(userId: Long): Flow<List<MonthlyExpenseSummary>> =
        dao.getMonthlyExpenseHistoryFlow(userId)

    fun getDailySpendingFlow(userId: Long, start: Long, end: Long): Flow<List<DailySpendingSummary>> =
        dao.getDailySpendingFlow(userId, start, end)

    fun getAllTransactionsSumFlow(): Flow<Double?> = dao.getAllTransactionsSumFlow()
    fun getAllExpensesSumFlow(): Flow<Double?> = dao.getAllExpensesSumFlow()
}
