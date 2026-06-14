package com.example.easebudgetv1.data.database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "budget_goals",
    indices = [Index(value = ["userId", "month", "year"])]
)
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val month: Int,
    val year: Int,
    val monthlyTotalBudget: Double,
    val minSpendingGoal: Double,
    val maxSpendingGoal: Double,
    val savingsGoal: Double,
    val savingsTargetAmount: Double? = null,
    val savingsDeadline: Long? = null
)
