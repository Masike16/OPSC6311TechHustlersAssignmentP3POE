package com.example.easebudgetv1.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "EaseBudgetSession"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_HELP_DISABLED_PREFIX = "help_disabled_"
    }

    fun saveLoginSession(userId: Long, username: String) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun logout() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    private fun helpKey(userId: Long) = "$KEY_HELP_DISABLED_PREFIX$userId"

    fun setHelpDisabled(userId: Long, disabled: Boolean) {
        if (userId <= 0) return
        prefs.edit().putBoolean(helpKey(userId), disabled).apply()
    }

    fun isHelpDisabled(userId: Long): Boolean {
        if (userId <= 0) return false
        return prefs.getBoolean(helpKey(userId), false)
    }
}
