package com.example.easebudgetv1.data.database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["date"]),
        Index(value = ["categoryId"]),
        Index(value = ["sharedAccountId"])
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryId: Long?,
    val amount: Double,
    val type: TransactionType,
    val date: Long,
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String,
    val receiptPath: String? = null,
    val sharedAccountId: Long? = null,
    val createdBy: Long,
    val createdAt: Long = System.currentTimeMillis()
)
