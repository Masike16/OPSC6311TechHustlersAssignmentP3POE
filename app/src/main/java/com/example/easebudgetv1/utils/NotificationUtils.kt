package com.example.easebudgetv1.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.easebudgetv1.MainActivity
import com.example.easebudgetv1.R

object NotificationUtils {
    private const val CHANNEL_ID = "easebudget_channel"
    private const val CHANNEL_NAME = "EasEBudget Notifications"
    private const val OVERSPENDING_NOTIFICATION_ID = 1001
    private const val WEEKLY_SUMMARY_NOTIFICATION_ID = 1002
    private const val BADGE_NOTIFICATION_ID = 1003
    private const val BUDGET_ALERT_NOTIFICATION_ID = 1004
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for budget alerts and achievements"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showOverspendingAlert(context: Context, categoryName: String, spent: Double, limit: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Budget Alert: Overspending!")
            .setContentText("You've spent ${CurrencyFormatter.format(spent)} on $categoryName, exceeding your limit of ${CurrencyFormatter.format(limit)}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(OVERSPENDING_NOTIFICATION_ID, notification)
    }
    
    fun showWeeklySummary(context: Context, totalSpent: Double, budget: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Weekly Summary")
            .setContentText("You spent ${CurrencyFormatter.format(totalSpent)} this week. Budget: ${CurrencyFormatter.format(budget)}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WEEKLY_SUMMARY_NOTIFICATION_ID, notification)
    }
    
    fun showBadgeEarned(context: Context, badgeTitle: String, badgeDescription: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Achievement Unlocked!")
            .setContentText("$badgeTitle: $badgeDescription")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(BADGE_NOTIFICATION_ID, notification)
    }
    
    fun showBudgetAlert(context: Context, categoryName: String, percentage: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Budget Alert")
            .setContentText("You've used $percentage% of your budget for $categoryName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(BUDGET_ALERT_NOTIFICATION_ID, notification)
    }
}
