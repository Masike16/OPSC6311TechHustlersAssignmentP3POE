package com.example.easebudgetv1.data.database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "categories",
    indices = [Index(value = ["userId"])]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val color: String,
    val icon: String? = null,
    val budgetLimit: Double? = null,
    val group: String,
    val isDefault: Boolean = false
)
