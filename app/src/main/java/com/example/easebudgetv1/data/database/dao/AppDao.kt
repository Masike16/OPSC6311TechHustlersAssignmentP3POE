package com.example.easebudgetv1.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.easebudgetv1.data.database.entities.Achievement
import com.example.easebudgetv1.data.database.entities.AdminActionLog
import com.example.easebudgetv1.data.database.entities.BudgetGoal
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.database.entities.Notification
import com.example.easebudgetv1.data.database.entities.SharedAccount
import com.example.easebudgetv1.data.database.entities.SharedAccountRequest
import com.example.easebudgetv1.data.database.entities.Streak
import com.example.easebudgetv1.data.database.entities.Transaction as TransactionEntity
import com.example.easebudgetv1.data.database.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: Long): Flow<User?>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCountFlow(): Flow<Int>

    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsersPaged(): PagingSource<Int, User>

    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET password = :password WHERE id = :userId")
    suspend fun updateUserPassword(userId: Long, password: String)

    @Query("UPDATE users SET showGuideOnStartup = :showGuide WHERE id = :userId")
    suspend fun updateUserGuidePreference(userId: Long, showGuide: Boolean)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :userId")
    suspend fun deactivateUser(userId: Long, isActive: Boolean)

    @Delete
    suspend fun deleteUser(user: User)

    @Transaction
    suspend fun deleteUserAndAllData(userId: Long) {
        deleteUserById(userId)
        deleteAllTransactions(userId)
        deleteAllCategories(userId)
        deleteBudgetGoalsInternal(userId)
        deleteAchievementsInternal(userId)
        deleteStreaksInternal(userId)
    }

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategoriesByUserId(userId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllCategories(userId: Long)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUserId(userId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:query IS NULL OR description LIKE '%' || :query || '%')
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC
    """)
    fun getTransactionsPaged(userId: Long, categoryId: Long?, query: String?, startDate: Long?, endDate: Long?): PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(userId: Long, limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(userId: Long, categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE sharedAccountId = :sharedAccountId ORDER BY date DESC")
    fun getTransactionsBySharedAccount(sharedAccountId: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactions(userId: Long)

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetGoal(userId: Long, month: Int, year: Int): BudgetGoal?

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getBudgetGoalFlow(userId: Long, month: Int, year: Int): Flow<BudgetGoal?>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getBudgetGoalsByUserId(userId: Long): Flow<List<BudgetGoal>>

    @Query("DELETE FROM budget_goals WHERE userId = :userId")
    suspend fun deleteBudgetGoalsInternal(userId: Long)

    @Insert
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long

    @Update
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal)

    @Delete
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal)

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getAchievementsByUserId(userId: Long): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND badgeType = :badgeType LIMIT 1")
    suspend fun getAchievementByType(userId: Long, badgeType: String): Achievement?

    @Query("DELETE FROM achievements WHERE userId = :userId")
    suspend fun deleteAchievementsInternal(userId: Long)

    @Insert
    suspend fun insertAchievement(achievement: Achievement): Long

    @Delete
    suspend fun deleteAchievement(achievement: Achievement)

    @Query("SELECT COUNT(*) FROM achievements")
    fun getTotalBadgesEarnedFlow(): Flow<Int>

    @Query("SELECT * FROM streaks WHERE userId = :userId AND streakType = :streakType LIMIT 1")
    suspend fun getStreak(userId: Long, streakType: String): Streak?

    @Query("SELECT * FROM streaks WHERE userId = :userId")
    fun getStreaksByUserId(userId: Long): Flow<List<Streak>>

    @Query("DELETE FROM streaks WHERE userId = :userId")
    suspend fun deleteStreaksInternal(userId: Long)

    @Insert
    suspend fun insertStreak(streak: Streak): Long

    @Update
    suspend fun updateStreak(streak: Streak)

    @Query("SELECT * FROM shared_accounts WHERE primaryUserId = :userId OR linkedUserId = :userId")
    fun getSharedAccountsByUserId(userId: Long): Flow<List<SharedAccount>>

    @Query("SELECT * FROM shared_accounts WHERE id = :accountId LIMIT 1")
    suspend fun getSharedAccountById(accountId: Long): SharedAccount?

    @Insert
    suspend fun insertSharedAccount(sharedAccount: SharedAccount): Long

    @Update
    fun updateSharedAccount(sharedAccount: SharedAccount)

    @Query("UPDATE shared_accounts SET spendingLimit = :limit WHERE id = :accountId")
    suspend fun updateSharedAccountLimit(accountId: Long, limit: Double)

    @Delete
    suspend fun deleteSharedAccount(sharedAccount: SharedAccount)

    @Query("SELECT * FROM shared_account_requests WHERE toUserEmail = :email AND status = 'PENDING'")
    fun getPendingRequestsByEmail(email: String): Flow<List<SharedAccountRequest>>

    @Query("SELECT * FROM shared_account_requests WHERE fromUserId = :userId")
    fun getRequestsByUserId(userId: Long): Flow<List<SharedAccountRequest>>

    @Query("SELECT * FROM shared_account_requests WHERE id = :requestId LIMIT 1")
    suspend fun getSharedAccountRequest(requestId: Long): SharedAccountRequest?

    @Insert
    suspend fun insertSharedAccountRequest(request: SharedAccountRequest): Long

    @Update
    suspend fun updateSharedAccountRequest(request: SharedAccountRequest)

    @Query("UPDATE shared_account_requests SET status = :status, respondedAt = :timestamp WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Long, status: String, timestamp: Long)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsByUserId(userId: Long): Flow<List<Notification>>

    @Insert
    suspend fun insertNotification(notification: Notification): Long

    @Update
    suspend fun updateNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Long, isRead: Boolean)

    @Delete
    suspend fun deleteNotification(notification: Notification)

    @Query("SELECT * FROM admin_action_log ORDER BY timestamp DESC")
    fun getAdminActionLog(): Flow<List<AdminActionLog>>

    @Query("SELECT * FROM admin_action_log ORDER BY timestamp DESC")
    fun getAdminActionLogPaged(): PagingSource<Int, AdminActionLog>

    @Insert
    suspend fun insertAdminActionLog(log: AdminActionLog): Long

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    fun getTotalIncomeFlow(userId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    fun getMonthlyIncomeFlow(userId: Long, startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    fun getTotalExpensesFlow(userId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    fun getMonthlySpendingFlow(userId: Long, startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT c.name as categoryName, SUM(t.amount) as totalAmount 
        FROM transactions t 
        INNER JOIN categories c ON t.categoryId = c.id 
        WHERE t.userId = :userId AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate 
        GROUP BY t.categoryId
    """)
    fun getCategorySpendingFlow(userId: Long, startDate: Long, endDate: Long): Flow<List<CategorySpendingSummary>>

    @Query("""
        SELECT strftime('%Y-%m', date/1000, 'unixepoch') as monthLabel, SUM(amount) as totalAmount 
        FROM transactions 
        WHERE userId = :userId AND type = 'EXPENSE' 
        GROUP BY monthLabel
        ORDER BY monthLabel ASC
    """)
    fun getMonthlyExpenseHistoryFlow(userId: Long): Flow<List<MonthlyExpenseSummary>>

    @Query("""
        SELECT date(date/1000, 'unixepoch') as dayLabel, SUM(amount) as totalAmount 
        FROM transactions 
        WHERE userId = :userId AND type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate 
        GROUP BY dayLabel
        ORDER BY dayLabel ASC
    """)
    fun getDailySpendingFlow(userId: Long, startDate: Long, endDate: Long): Flow<List<DailySpendingSummary>>

    @Query("SELECT SUM(amount) FROM transactions")
    fun getAllTransactionsSumFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getAllExpensesSumFlow(): Flow<Double?>
}

data class CategorySpendingSummary(
    val categoryName: String,
    val totalAmount: Double
)

data class MonthlyExpenseSummary(
    val monthLabel: String,
    val totalAmount: Double
)

data class DailySpendingSummary(
    val dayLabel: String,
    val totalAmount: Double
)
