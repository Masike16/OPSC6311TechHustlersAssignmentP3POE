package com.example.easebudgetv1.utils

object ValidationUtils {
    // Optimization: Pre-compile Regex to avoid expensive re-creation on every validation check
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        return emailRegex.matches(email)
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() }
    }
    
    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.length <= 20
    }
    
    fun isValidAmount(amount: Double): Boolean {
        return amount > 0
    }
    
    fun isValidDescription(description: String): Boolean {
        return description.isNotBlank() && description.length <= 200
    }
}
