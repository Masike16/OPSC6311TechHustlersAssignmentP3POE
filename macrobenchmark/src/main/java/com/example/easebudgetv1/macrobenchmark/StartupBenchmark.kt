package com.example.easebudgetv1.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Optimization: Benchmark to measure app startup performance.
 * 
 * This test measures:
 * - Cold startup: App started from scratch (no process running)
 * - Warm startup: App started with process in background
 * - Hot startup: App started with process in foreground
 * 
 * Metrics tracked:
 * - Time to first frame
 * - Time to full display
 * - Time to initial draw
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupColdCompilationNone() = benchmarkRule.measureRepeated(
        packageName = "com.example.easebudgetv1",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.None(),
        startupMode = StartupMode.COLD,
        iterations = 10,
        measureBlock = {
            pressHome()
            startActivityAndWait()
        }
    )

    @Test
    fun startupColdCompilationPartial() = benchmarkRule.measureRepeated(
        packageName = "com.example.easebudgetv1",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.COLD,
        iterations = 10,
        measureBlock = {
            pressHome()
            startActivityAndWait()
        }
    )

    @Test
    fun startupColdCompilationFull() = benchmarkRule.measureRepeated(
        packageName = "com.example.easebudgetv1",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.COLD,
        iterations = 10,
        measureBlock = {
            pressHome()
            startActivityAndWait()
        }
    )

    @Test
    fun startupWarm() = benchmarkRule.measureRepeated(
        packageName = "com.example.easebudgetv1",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.WARM,
        iterations = 10,
        measureBlock = {
            pressHome()
            startActivityAndWait()
        }
    )

    @Test
    fun startupHot() = benchmarkRule.measureRepeated(
        packageName = "com.example.easebudgetv1",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.HOT,
        iterations = 10,
        measureBlock = {
            pressHome()
            startActivityAndWait()
        }
    )
}
