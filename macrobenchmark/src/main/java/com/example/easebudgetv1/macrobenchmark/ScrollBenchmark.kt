package com.example.easebudgetv1.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Optimization: Benchmark to measure scroll performance and frame stability.
 * 
 * This test measures:
 * - Transactions list scrolling with Paging 3
 * - Dashboard content scrolling
 * - Frame drops and jank during scrolling
 * 
 * Metrics tracked:
 * - Frame timing (p50, p90, p95, p99)
 * - Frame duration
 * - Jank percentage
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollTransactionsList() = benchmarkRule.measureRepeated(
        packageName = "com.example.easebudgetv1",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        iterations = 5,
        startupMode = StartupMode.WARM,
        measureBlock = {
            pressHome()
            startActivityAndWait()
            
            // Navigate to Transactions screen
            device.wait(Until.hasObject(By.text("Transactions")), 5000)
            device.findObject(By.text("Transactions")).click()
            device.waitForIdle()
            
            // Scroll through transactions list
            val transactionsList = device.findObject(By.res("com.example.easebudgetv1:id/transactions_list"))
            if (transactionsList.exists()) {
                // Scroll down multiple times to trigger Paging 3 loading
                repeat(10) {
                    transactionsList.scroll(Direction.DOWN, 0.5f)
                    device.waitForIdle(200)
                }
                // Scroll back up
                repeat(10) {
                    transactionsList.scroll(Direction.UP, 0.5f)
                    device.waitForIdle(200)
                }
            }
        }
    )

    @Test
    fun scrollDashboard() = benchmarkRule.measureRepeated(
        packageName = "com.example.easebudgetv1",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        iterations = 5,
        startupMode = StartupMode.WARM,
        measureBlock = {
            pressHome()
            startActivityAndWait()
            
            device.waitForIdle()
            
            // Scroll through dashboard content
            val dashboard = device.findObject(By.res("com.example.easebudgetv1:id/dashboard_content"))
            if (dashboard.exists()) {
                repeat(5) {
                    dashboard.fling(Direction.DOWN)
                    device.waitForIdle(200)
                }
                repeat(5) {
                    dashboard.fling(Direction.UP)
                    device.waitForIdle(200)
                }
            }
        }
    )

    @Test
    fun scrollBudgetCategories() = benchmarkRule.measureRepeated(
        packageName = "com.example.easebudgetv1",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        iterations = 5,
        startupMode = StartupMode.WARM,
        measureBlock = {
            pressHome()
            startActivityAndWait()
            
            // Navigate to Budget screen
            device.wait(Until.hasObject(By.text("Budget")), 5000)
            device.findObject(By.text("Budget")).click()
            device.waitForIdle()
            
            // Scroll through categories
            val budgetList = device.findObject(By.res("com.example.easebudgetv1:id/budget_list"))
            if (budgetList.exists()) {
                repeat(5) {
                    budgetList.fling(Direction.DOWN)
                    device.waitForIdle(200)
                }
                repeat(5) {
                    budgetList.fling(Direction.UP)
                    device.waitForIdle(200)
                }
            }
        }
    )
}
