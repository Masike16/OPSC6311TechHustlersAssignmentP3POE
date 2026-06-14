package com.example.easebudgetv1.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.database.entities.User
import com.example.easebudgetv1.data.repository.AppRepository
import com.example.easebudgetv1.utils.HashUtils
import com.example.easebudgetv1.utils.SessionManager
import com.example.easebudgetv1.utils.ValidationUtils
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class AuthUiState(
    val currentUser: User? = null,
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val showGuideOnStartup: Boolean = true
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repositoryLazy: Lazy<AppRepository>,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val repository get() = repositoryLazy.get()
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            val userId = sessionManager.getUserId()
            viewModelScope.launch {
                val user = repository.getUserById(userId)
                if (user != null && user.isActive) {
                    // Sync session manager with DB preference
                    sessionManager.setHelpDisabled(userId, !user.showGuideOnStartup)
                    _uiState.update { it.copy(
                        currentUser = user,
                        isSuccess = true,
                        showGuideOnStartup = user.showGuideOnStartup
                    ) }
                } else {
                    sessionManager.logout()
                }
            }
        }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, error = null) }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            if (!ValidationUtils.isValidEmail(email)) {
                _uiState.update { it.copy(isLoading = false, error = "Invalid email format") }
                return@launch
            }
            
            val user = repository.getUserByEmail(email)
            val passwordHash = HashUtils.sha256(password)
            
            if (user != null && user.password == passwordHash && user.isActive) {
                sessionManager.saveLoginSession(user.id, user.username)
                sessionManager.setHelpDisabled(user.id, !user.showGuideOnStartup)
                _uiState.update { it.copy(
                    isLoading = false, 
                    currentUser = user, 
                    isSuccess = true,
                    showGuideOnStartup = user.showGuideOnStartup
                ) }
            } else if (user != null && !user.isActive) {
                _uiState.update { it.copy(isLoading = false, error = "Account is deactivated") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Invalid email or password") }
            }
        }
    }
    
    fun register(email: String, username: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            if (!ValidationUtils.isValidEmail(email)) {
                _uiState.update { it.copy(isLoading = false, error = "Invalid email format") }
                return@launch
            }
            
            if (!ValidationUtils.isValidUsername(username)) {
                _uiState.update { it.copy(isLoading = false, error = "Username must be 3-20 characters") }
                return@launch
            }
            
            if (!ValidationUtils.isValidPassword(password)) {
                _uiState.update { it.copy(isLoading = false, error = "Password must be 8+ characters with 1 uppercase and 1 number") }
                return@launch
            }
            
            if (password != confirmPassword) {
                _uiState.update { it.copy(isLoading = false, error = "Passwords do not match") }
                return@launch
            }
            
            val existingUser = repository.getUserByEmail(email)
            if (existingUser != null) {
                _uiState.update { it.copy(isLoading = false, error = "Email already registered") }
                return@launch
            }
            
            val newUser = User(
                email = email,
                password = HashUtils.sha256(password),
                username = username,
                isAdmin = false,
                isActive = true,
                showGuideOnStartup = true
            )
            
            val userId = repository.insertUser(newUser)
            createDefaultCategories(userId)
            
            sessionManager.saveLoginSession(userId, username)
            sessionManager.setHelpDisabled(userId, false)
            
            _uiState.update { it.copy(
                isLoading = false, 
                currentUser = newUser.copy(id = userId), 
                isSuccess = true,
                showGuideOnStartup = true
            ) }
        }
    }

    fun updateGuidePreference(userId: Long, show: Boolean) {
        viewModelScope.launch {
            repository.updateUserGuidePreference(userId, show)
            sessionManager.setHelpDisabled(userId, !show)
        }
    }

    private suspend fun createDefaultCategories(userId: Long) {
        val defaultCategories = listOf(
            Category(userId = userId, name = "Food & Dining", color = "#FF9800", group = "Daily", isDefault = true),
            Category(userId = userId, name = "Transportation", color = "#2196F3", group = "Daily", isDefault = true),
            Category(userId = userId, name = "Shopping", color = "#E91E63", group = "Lifestyle", isDefault = true),
            Category(userId = userId, name = "Entertainment", color = "#9C27B0", group = "Lifestyle", isDefault = true),
            Category(userId = userId, name = "Health", color = "#4CAF50", group = "Daily", isDefault = true),
            Category(userId = userId, name = "Utilities", color = "#9E9E9E", group = "Fixed", isDefault = true),
            Category(userId = userId, name = "Income", color = "#009688", group = "Income", isDefault = true)
        )
        repository.insertCategories(defaultCategories)
    }
    
    fun logout() {
        sessionManager.logout()
        _uiState.value = AuthUiState()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
