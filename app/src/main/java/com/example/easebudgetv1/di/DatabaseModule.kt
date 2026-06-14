package com.example.easebudgetv1.di

import android.content.Context
import androidx.room.Room
import com.example.easebudgetv1.data.database.AppDatabase
import com.example.easebudgetv1.data.database.dao.AppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "easebudget_database_v3" // Forced fresh start to resolve schema integrity issues
        )
        .fallbackToDestructiveMigration()
        .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()
    }

    @Provides
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.dao()
    }
}
