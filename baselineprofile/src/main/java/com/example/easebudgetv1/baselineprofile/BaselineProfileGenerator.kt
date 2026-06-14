package com.example.easebudgetv1.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Optimization: This class generates a Baseline Profile to improve app startup 
 * and scroll performance by pre-compiling critical code paths.
 * 
 * Critical user journeys included:
 * - App startup and initial navigation
 * - Dashboard scrolling and metric loading
 * - Transactions list scrolling with Paging 3
 * - Navigation between major screens
 * - Reports screen with chart rendering
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.example.easebudgetv1",
        includeInvolvedProcesses = true
    ) {
        pressHome()
        startActivityAndWait()
        
        // Critical Journey 1: Dashboard interaction
        // Wait for dashboard to load and scroll through recent transactions
        device.wait(Until.hasObject(By.text("Dashboard")), 5000)
        device.findObject(By.text("Dashboard")).click()
        device.waitForIdle()
        
        // Scroll through dashboard content to trigger lazy loading
        val dashboard = device.findObject(By.res("com.example.easebudgetv1:id/dashboard_content"))
        if (dashboard.exists()) {
            dashboard.fling(Direction.DOWN)
            device.waitForIdle()
            dashboard.fling(Direction.UP)
            device.waitForIdle()
        }
        
        // Critical Journey 2: Navigate to Transactions screen
        // This tests Paging 3 integration and list scrolling
        device.findObject(By.text("Transactions")).click()
        device.waitForIdle()
        
        // Scroll through transactions list multiple times to profile Paging 3
        val transactionsList = device.findObject(By.res("com.example.easebudgetv1:id/transactions_list"))
        if (transactionsList.exists()) {
            repeat(3) {
                transactionsList.scroll(Direction.DOWN, 0.5f)
                device.waitForIdle(500)
            }
            repeat(3) {
                transactionsList.scroll(Direction.UP, 0.5f)
                device.waitForIdle(500)
            }
        }
        
        // Critical Journey 3: Navigate to Reports screen
        // This tests chart rendering and aggregation computations
        device.findObject(By.text("Reports")).click()
        device.waitForIdle()
        
        // Wait for charts to render
        device.wait(Until.hasObject(By.text("This Month")), 3000)
        
        // Critical Journey 4: Navigate to Budget screen
        // This tests category grouping and budget goal calculations
        device.findObject(By.text("Budget")).click()
        device.waitForIdle()
        
        // Scroll through categories
        val budgetList = device.findObject(By.res("com.example.easebudgetv1:id/budget_list"))
        if (budgetList.exists()) {
            budgetList.fling(Direction.DOWN)
            device.waitForIdle()
        }
        
        // Critical Journey 5: Navigate to Goals screen
        // This tests gamification calculations
        device.findObject(By.text("Goals")).click()
        device.waitForIdle()
        
        // Critical Journey 6: Navigate to Settings
        // This tests preference loading and biometric setup
        device.findObject(By.text("Settings")).click()
        device.waitForIdle()
        
        // Navigate back to dashboard
        device.pressBack()
        device.pressBack()
        device.findObject(By.text("Dashboard")).click()
        device.waitForIdle()
    }
}
