package com.example.easebudgetv1.data.database

import androidx.room.TypeConverter
import com.example.easebudgetv1.data.database.entities.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}
