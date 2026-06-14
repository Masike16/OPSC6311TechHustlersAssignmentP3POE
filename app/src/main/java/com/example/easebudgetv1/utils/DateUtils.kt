package com.example.easebudgetv1.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DATE_FORMAT = "dd MMM yyyy"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATE_TIME_FORMAT = "dd MMM yyyy HH:mm"
    
    // Optimization: Cache formatters to avoid expensive re-instantiation in list scrolls
    private val dateDataFormatter by lazy { SimpleDateFormat(DATE_FORMAT, Locale.getDefault()) }
    private val timeFormatter by lazy { SimpleDateFormat(TIME_FORMAT, Locale.getDefault()) }
    private val dateTimeFormatter by lazy { SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()) }

    fun formatDate(timestamp: Long): String {
        return synchronized(dateDataFormatter) {
            dateDataFormatter.format(Date(timestamp))
        }
    }
    
    fun formatTime(timestamp: Long): String {
        return synchronized(timeFormatter) {
            timeFormatter.format(Date(timestamp))
        }
    }
    
    fun formatDateTime(timestamp: Long): String {
        return synchronized(dateTimeFormatter) {
            dateTimeFormatter.format(Date(timestamp))
        }
    }
    
    fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    fun getEndOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    
    fun getStartOfWeek(): Long {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    fun getEndOfWeek(): Long {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    
    fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    fun getEndOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    
    fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }
    
    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }
    
    fun daysBetween(start: Long, end: Long): Int {
        val diff = end - start
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}
