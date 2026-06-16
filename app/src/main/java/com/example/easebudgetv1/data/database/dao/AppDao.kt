/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.data.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.easebudgetv1.data.database.entities.*
import com.example.easebudgetv1.data.database.entities.Transaction as TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: Long): Flow<User?>

    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsersPaged(): PagingSource<Int, User>

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCountFlow(): Flow<Int>

    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET password = :password WHERE id = :userId")
    suspend fun updateUserPassword(userId: Long, password: String)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :userId")
    suspend fun deactivateUser(userId: Long, isActive: Boolean)

    @Transaction
    suspend fun deleteUserAndAllData(userId: Long) {
        deleteUserById(userId)
        deleteAllTransactions(userId)
        deleteAllCategories(userId)
    }

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)

    @Query("UPDATE users SET showGuideOnStartup = :showAgain WHERE id = :userId")
    suspend fun updateUserGuidePreference(userId: Long, showAgain: Boolean)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategoriesByUserId(userId: Long): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

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
        ORDER BY date DESC, id DESC
    """)
    fun getTransactionsPaged(userId: Long, categoryId: Long?, query: String?, startDate: Long?, endDate: Long?): PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(userId: Long, limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactions(userId: Long)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions")
    fun getAllTransactionsSumFlow(): Flow<Double?>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE'")
    fun getAllExpensesSumFlow(): Flow<Double?>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getBudgetGoalFlow(userId: Long, month: Int, year: Int): Flow<BudgetGoal?>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetGoal(userId: Long, month: Int, year: Int): BudgetGoal?

    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getBudgetGoalsByUserId(userId: Long): Flow<List<BudgetGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long

    @Update
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal)

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getAchievementsByUserId(userId: Long): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND badgeType = :badgeType LIMIT 1")
    suspend fun getAchievementByType(userId: Long, badgeType: String): Achievement?

    @Insert
    suspend fun insertAchievement(achievement: Achievement): Long

    @Query("SELECT COUNT(*) FROM achievements")
    fun getTotalBadgesEarnedFlow(): Flow<Int>

    @Query("SELECT * FROM streaks WHERE userId = :userId AND streakType = :streakType LIMIT 1")
    suspend fun getStreak(userId: Long, streakType: String): Streak?

    @Query("SELECT * FROM streaks WHERE userId = :userId")
    fun getStreaksByUserId(userId: Long): Flow<List<Streak>>

    @Update
    suspend fun updateStreak(streak: Streak)

    @Insert
    suspend fun insertStreak(streak: Streak): Long

    @Query("SELECT * FROM shared_accounts WHERE primaryUserId = :userId")
    fun getSharedAccountsByUserId(userId: Long): Flow<List<SharedAccount>>

    @Query("SELECT * FROM shared_accounts WHERE id = :id LIMIT 1")
    suspend fun getSharedAccountById(id: Long): SharedAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedAccount(sharedAccount: SharedAccount): Long

    @Query("DELETE FROM shared_accounts WHERE id = :id")
    suspend fun deleteSharedAccount(id: Long)

    @Query("UPDATE shared_accounts SET spendingLimit = :limit WHERE id = :id")
    suspend fun updateSharedAccountLimit(id: Long, limit: Double)

    @Query("SELECT * FROM shared_account_requests WHERE toUserEmail = :email AND status = 'PENDING'")
    fun getPendingRequestsByEmail(email: String): Flow<List<SharedAccountRequest>>

    @Query("SELECT * FROM shared_account_requests WHERE id = :id LIMIT 1")
    suspend fun getSharedAccountRequest(id: Long): SharedAccountRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedAccountRequest(request: SharedAccountRequest): Long

    @Query("UPDATE shared_account_requests SET status = :status, respondedAt = :respondedAt WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Long, status: String, respondedAt: Long)

    @Query("SELECT * FROM admin_action_log ORDER BY timestamp DESC")
    fun getAdminActionLogPaged(): PagingSource<Int, AdminActionLog>

    @Insert
    suspend fun insertAdminActionLog(log: AdminActionLog)

    // Aggregate queries fixed with COALESCE and Flow<Double>
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'INCOME' AND date BETWEEN :start AND :end")
    fun getMonthlyIncomeFlow(userId: Long, start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'EXPENSE' AND date BETWEEN :start AND :end")
    fun getMonthlySpendingFlow(userId: Long, start: Long, end: Long): Flow<Double>

    @Query("""
        SELECT c.name as categoryName, COALESCE(SUM(t.amount), 0.0) as totalAmount 
        FROM transactions t 
        INNER JOIN categories c ON t.categoryId = c.id 
        WHERE t.userId = :userId AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate 
        GROUP BY t.categoryId
    """)
    fun getCategorySpendingFlow(userId: Long, startDate: Long, endDate: Long): Flow<List<CategorySpendingSummary>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    fun getTotalIncomeFlow(userId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    fun getTotalExpensesFlow(userId: Long): Flow<Double>

    @Query("""
        SELECT strftime('%Y-%m', date / 1000, 'unixepoch') as monthLabel, SUM(amount) as totalAmount, 'ON_TRACK' as health
        FROM transactions 
        WHERE userId = :userId AND type = 'EXPENSE'
        GROUP BY monthLabel
        ORDER BY monthLabel DESC
    """)
    fun getMonthlyExpenseHistoryFlow(userId: Long): Flow<List<MonthlyExpenseSummary>>

    @Query("""
        SELECT date as date, SUM(amount) as totalAmount
        FROM transactions
        WHERE userId = :userId AND type = 'EXPENSE' AND date >= :startDate AND date <= :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailySpendingFlow(userId: Long, startDate: Long, endDate: Long): Flow<List<DailySpendingSummary>>
}

data class CategorySpendingSummary(val categoryName: String, val totalAmount: Double)
data class MonthlyExpenseSummary(val monthLabel: String, val totalAmount: Double, val health: String)
data class DailySpendingSummary(val date: Long, val totalAmount: Double)
