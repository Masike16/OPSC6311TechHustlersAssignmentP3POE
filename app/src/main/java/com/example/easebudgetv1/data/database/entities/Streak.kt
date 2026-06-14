package com.example.easebudgetv1.data.database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "streaks",
    indices = [Index(value = ["userId", "streakType"])]
)
data class Streak(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val streakType: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: Long = System.currentTimeMillis()
)
