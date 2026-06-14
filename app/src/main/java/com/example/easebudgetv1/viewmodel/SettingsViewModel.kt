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

    fun loadUserData(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = repository.getUserById(userId)
            val helpDisabled = sessionManager.isHelpDisabled(userId)
            _uiState.update { it.copy(
                currentUser = user,
                showGuideOnStartup = !helpDisabled, // Sync with SessionManager for UI consistency
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

    fun toggleGuidePreference(show: Boolean) {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch {
            // Requirement 2: Persist preference in both database and SessionManager
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

    fun exportTransactionsToCsv() {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val transactions = repository.getTransactionsByUserId(userId).first()
            val csvBuilder = StringBuilder()
            csvBuilder.append("Date,Description,Amount,Type,Category,Start Time,End Time\n")
            
            transactions.forEach { tx ->
                val date = DateUtils.formatDate(tx.date)
                csvBuilder.append("\"$date\",\"${tx.description}\",${tx.amount},${tx.type},\"${tx.categoryId}\",\"${tx.startTime}\",\"${tx.endTime}\"\n")
            }
            
            _uiState.update { it.copy(exportData = csvBuilder.toString()) }
        }
    }

    fun clearExportData() {
        _uiState.update { it.copy(exportData = null) }
    }

    fun deleteAccount() {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch {
            repository.deleteUserById(userId)
        }
    }
}
