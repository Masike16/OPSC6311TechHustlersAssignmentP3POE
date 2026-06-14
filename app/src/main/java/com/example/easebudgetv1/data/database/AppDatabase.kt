package com.example.easebudgetv1.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.easebudgetv1.data.database.dao.AppDao
import com.example.easebudgetv1.data.database.entities.Achievement
import com.example.easebudgetv1.data.database.entities.AdminActionLog
import com.example.easebudgetv1.data.database.entities.BudgetGoal
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.database.entities.Notification
import com.example.easebudgetv1.data.database.entities.SharedAccount
import com.example.easebudgetv1.data.database.entities.SharedAccountRequest
import com.example.easebudgetv1.data.database.entities.Streak
import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.User

@Database(
    entities = [
        User::class,
        Category::class,
        Transaction::class,
        BudgetGoal::class,
        Achievement::class,
        Streak::class,
        SharedAccount::class,
        SharedAccountRequest::class,
        Notification::class,
        AdminActionLog::class
    ],
    version = 6, // Optimization: Bumped version to 6 to resolve Room schema integrity issues
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
}
