/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.utils

import java.text.SimpleDateFormat
import java.util.*

/*
 * basic date helper object for the app. 
 * it helps with formatting dates and getting the start and end of time ranges 
 * for the transactions in the app. 
 * 
 * References:
 * Oracle (2024) 'Class Calendar', Java Platform SE 17. Available at: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Calendar.html (Accessed: 24 May 2024)
 * Google (2024) 'SimpleDateFormat', Android Developers. Available at: https://developer.android.com/reference/java/text/SimpleDateFormat (Accessed: 24 May 2024)
 * 
 * we cached the formatters here so that we don't have to keep creating new ones 
 * in loops which is better for performance.
 */
object DateUtils {
    private const val DATE_FORMAT = "dd MMM yyyy"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATE_TIME_FORMAT = "dd MMM yyyy HH:mm"
    
    // using lazy for the formatters so they only get made when actually needed
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
    
    // gets the very start of the current month. good for monthly budget resets
    fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    // gets the end of the month for budget calculations. makes sure we include the last day
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
    
    // simple math to find out how many days between two timestamps.
    fun daysBetween(start: Long, end: Long): Int {
        val diff = end - start
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}
