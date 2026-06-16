package com.example.easebudgetv1.utils

import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.TransactionType

/* 
 * this the utility for gamification and checking financial health
 * 
 * Refrences:
 * YNAB (2024) 'The Four Rules', You Need A Budget. Available at: https://www.ynab.com/the-four-rules (Accessed: 22 May 2024)
 * Sailer, M. and Homner, L. (2020) 'The Gamification of Learning: a Meta-analysis', Educational Psychology Review, 32(1), pp. 77-112.
 * 
 * mostly used to work out the age of money and points for the badges we give users
 */
object GamificationUtils {
    
    /**
     * calculation the Age of Money here. its basically how long your money stays in
     * the account before you actually spend it on stuff.
     * 
     * Logic follows Rule 4 from YNAB (2024) which is about aging your money.
     * higher values mean more financial stability for the user
     */
    fun calculateAgeOfMoney(transactions: List<Transaction>): Int {
        if (transactions.isEmpty()) return 0
        
        var totalIncomeDate: Double = 0.0
        var incomeCount = 0
        var totalExpenseDate: Double = 0.0
        var expenseCount = 0
        
        // doing it in one loop to keep the app fast
        for (transaction in transactions) {
            if (transaction.type == TransactionType.INCOME) {
                totalIncomeDate += transaction.date
                incomeCount++
            } else {
                totalExpenseDate += transaction.date
                expenseCount++
            }
        }
        
        if (incomeCount == 0 || expenseCount == 0) return 0
        
        val avgIncomeDate = (totalIncomeDate / incomeCount).toLong()
        val avgExpenseDate = (totalExpenseDate / expenseCount).toLong()
        
        // work out the diffrence in days between income and spending
        val diffInDays = (avgExpenseDate - avgIncomeDate) / (1000 * 60 * 60 * 24)
        return diffInDays.toInt().coerceAtLeast(0)
    }
    
    /**
     * used to figure out if the user is spending too much or if they on track with their goals
     * based on standard budgeting practices.
     */
    fun calculateBudgetHealth(currentSpending: Double, minGoal: Double, maxGoal: Double): BudgetHealth {
        return when {
            currentSpending <= maxGoal && currentSpending >= minGoal -> BudgetHealth.ON_TRACK
            currentSpending > maxGoal -> BudgetHealth.OVER_BUDGET
            currentSpending < minGoal -> BudgetHealth.UNDER_BUDGET
            else -> BudgetHealth.ON_TRACK
        }
    }
    
    // gives small amount of points for every transaction logged to encourage use by the user.
    fun calculatePointsForTransaction(amount: Double): Int {
        return (amount / 10).toInt().coerceAtMost(10)
    }
    
    // points for earning badges
    fun calculatePointsForBadge(badgeType: String): Int {
        return when (badgeType) {
            "FIRST_TRANSACTION" -> 10
            "BUDGET_MASTER" -> 50
            "CONSISTENT_LOGGER" -> 30
            "SAVINGS_HERO" -> 40
            "CATEGORY_PRO" -> 25
            "AGE_OF_MONEY_7" -> 20
            "AGE_OF_MONEY_30" -> 35
            "STREAK_CHAMPION" -> 45
            "RECEIPT_KEEPER" -> 15
            "SHARED_SAVER" -> 30
            "GOAL_CRUSHER" -> 40
            else -> 10
        }
    }
    
    enum class BudgetHealth {
        ON_TRACK,
        OVER_BUDGET,
        UNDER_BUDGET
    }
}
