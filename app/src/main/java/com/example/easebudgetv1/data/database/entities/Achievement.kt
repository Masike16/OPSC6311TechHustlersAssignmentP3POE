package com.example.easebudgetv1.data.database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "achievements",
    indices = [Index(value = ["userId"])]
)
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val badgeType: String,
    val earnedAt: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val icon: String
)
