/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easebudgetv1.data.database.entities.User
import com.example.easebudgetv1.data.repository.AppRepository
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.utils.SessionManager
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * this viewmodel is for the settings screen. it handles stuff like user profile
 * updates, theme toggles and exporting data to csv format.
 * 
 * References:
 * Google (2024) 'DataStore', Android Developers. Available at: https://developer.android.com/topic/libraries/architecture/datastore (Accessed: 24 May 2024)
 * Kotlin (2024) 'StringBuilder', Kotlin Documentation. Available at: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-string-builder/ (Accessed: 26 May 2024)
 * 
 * we use the session manager to keep the login state and the repo for the DB stuff. 
 * the export function basically just loops through transactions and builds a big string.
 */

data class SettingsUiState(
    val currentUser: User? = null,
    val themeMode: String = "system",
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val showGuideOnStartup: Boolean = true,
    val isLoading: Boolean = false,
    val exportData: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repositoryLazy: Lazy<AppRepository>,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val repository get() = repositoryLazy.get()
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // loads user info from the database and syncs with the session manager
    fun loadUserData(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = repository.getUserById(userId)
            val helpDisabled = sessionManager.isHelpDisabled(userId)
            _uiState.update { it.copy(
                currentUser = user,
                showGuideOnStartup = !helpDisabled, 
                isLoading = false
            ) }
        }
    }

    fun updateUsername(newName: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.updateUser(user.copy(username = newName))
            loadUserData(user.id)
        }
    }

    fun updateEmail(newEmail: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.updateUser(user.copy(email = newEmail))
            loadUserData(user.id)
        }
    }

    fun changePassword(newPasswordHash: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.updateUserPassword(user.id, newPasswordHash)
        }
    }

    // toggles if the user wants to see the onboarding guide again
    fun toggleGuidePreference(show: Boolean) {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch {
            sessionManager.setHelpDisabled(userId, !show)
            repository.updateUserGuidePreference(userId, show)
            _uiState.update { it.copy(showGuideOnStartup = show) }
        }
    }

    fun updateThemeMode(mode: String) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun toggleBiometric(enabled: Boolean) {
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }

    // pulls transactions and converts them to a csv string for the user to download
    fun exportTransactionsToCsv() {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val transactions = repository.getTransactionsByUserId(userId).first()
            val csvBuilder = StringBuilder()
            csvBuilder.append("Date,Description,Amount,Type,Category,Start Time,End Time\n")
            
            transactions.forEach { tx ->
                val date = DateUtils.formatDate(tx.date)
                // we wrap fields in quotes just in case they have commas in the description
                csvBuilder.append("\"$date\",\"${tx.description}\",${tx.amount},${tx.type},\"${tx.categoryId}\",\"${tx.startTime}\",\"${tx.endTime}\"\n")
            }
            
            _uiState.update { it.copy(exportData = csvBuilder.toString()) }
        }
    }

    fun clearExportData() {
        _uiState.update { it.copy(exportData = null) }
    }

    // sad to see them go but we have to delete all their data if they ask
    fun deleteAccount() {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch {
            repository.deleteUserById(userId)
        }
    }
}
