package com.example.easebudgetv1.data.database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "shared_accounts",
    indices = [
        Index(value = ["primaryUserId"]),
        Index(value = ["linkedUserId"])
    ]
)
data class SharedAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val primaryUserId: Long,
    val linkedUserId: Long,
    val status: String,
    val role: String,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val spendingLimit: Double? = null
)
