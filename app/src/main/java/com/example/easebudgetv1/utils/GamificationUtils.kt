package com.example.easebudgetv1.utils

import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.TransactionType

object GamificationUtils {
    
    /**
     * Optimization: Single-pass calculation of Age of Money to reduce CPU and allocations.
     */
    fun calculateAgeOfMoney(transactions: List<Transaction>): Int {
        if (transactions.isEmpty()) return 0
        
        var totalIncomeDate: Double = 0.0
        var incomeCount = 0
        var totalExpenseDate: Double = 0.0
        var expenseCount = 0
        
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
        
        val diffInDays = (avgExpenseDate - avgIncomeDate) / (1000 * 60 * 60 * 24)
        return diffInDays.toInt().coerceAtLeast(0)
    }
    
    fun calculateBudgetHealth(currentSpending: Double, minGoal: Double, maxGoal: Double): BudgetHealth {
        return when {
            currentSpending <= maxGoal && currentSpending >= minGoal -> BudgetHealth.ON_TRACK
            currentSpending > maxGoal -> BudgetHealth.OVER_BUDGET
            currentSpending < minGoal -> BudgetHealth.UNDER_BUDGET
            else -> BudgetHealth.ON_TRACK
        }
    }
    
    fun calculatePointsForTransaction(amount: Double): Int {
        return (amount / 10).toInt().coerceAtMost(10)
    }
    
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
