package com.example.easebudgetv1

import android.app.Application
import com.example.easebudgetv1.data.repository.AppRepository
import com.example.easebudgetv1.utils.NotificationUtils
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class EasEBudgetApplication : Application() {

    @Inject
    lateinit var repository: Lazy<AppRepository>

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        
        // Optimization: Initialize notification channels early
        NotificationUtils.createNotificationChannel(this)

        // Optimization: Pre-warm the Hilt dependency graph and Database.
        // By "touching" the repository in a background thread during startup,
        // we trigger Room initialization while the splash screen is visible.
        applicationScope.launch {
            repository.get()
        }
    }
}
