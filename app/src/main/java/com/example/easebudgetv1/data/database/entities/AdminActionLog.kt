package com.example.easebudgetv1.data.database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "admin_action_log",
    indices = [
        Index(value = ["adminUserId"]),
        Index(value = ["targetUserId"]),
        Index(value = ["timestamp"])
    ]
)
data class AdminActionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adminUserId: Long,
    val actionType: String,
    val targetUserId: Long,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
