package com.example.easebudgetv1.data.database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "shared_account_requests",
    indices = [
        Index(value = ["fromUserId"]),
        Index(value = ["toUserEmail"])
    ]
)
data class SharedAccountRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromUserId: Long,
    val toUserEmail: String,
    val toUserPhone: String? = null,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null
)
