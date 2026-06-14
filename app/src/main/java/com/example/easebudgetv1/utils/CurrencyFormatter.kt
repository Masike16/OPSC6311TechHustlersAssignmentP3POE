package com.example.easebudgetv1.utils

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object CurrencyFormatter {
    private const val DEFAULT_CURRENCY = "ZAR"
    
    // Optimization: Cache formatters to avoid expensive re-instantiation in lists
    private val formatters = ConcurrentHashMap<String, NumberFormat>()
    
    fun format(amount: Double, currencyCode: String = DEFAULT_CURRENCY): String {
        val formatter = formatters.getOrPut(currencyCode) {
            NumberFormat.getCurrencyInstance(getLocaleForCurrency(currencyCode))
        }
        return synchronized(formatter) {
            formatter.format(amount)
        }
    }
    
    fun formatWithoutSymbol(amount: Double): String {
        return String.format("%.2f", amount)
    }
    
    private fun getLocaleForCurrency(currencyCode: String): Locale {
        return when (currencyCode.uppercase()) {
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "GBP" -> Locale.UK
            "ZAR" -> Locale("en", "ZA")
            else -> Locale.getDefault()
        }
    }
}
