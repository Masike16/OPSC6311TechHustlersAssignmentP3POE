/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easebudgetv1.data.database.entities.SharedAccount
import com.example.easebudgetv1.data.database.entities.SharedAccountRequest
import com.example.easebudgetv1.data.repository.AppRepository
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SharedAccountsUiState(
    val linkedAccounts: List<SharedAccount> = emptyList(),
    val pendingRequests: List<SharedAccountRequest> = emptyList(),
    val isAddRequestDialogVisible: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SharedAccountsViewModel @Inject constructor(
    private val repositoryLazy: Lazy<AppRepository>
) : ViewModel() {
    
    private val repository get() = repositoryLazy.get()
    private val _userId = MutableStateFlow<Long?>(null)
    private val _userEmail = MutableStateFlow<String?>(null)
    private val _isAddRequestDialogVisible = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SharedAccountsUiState> = combine(_userId, _userEmail) { id, email ->
        id to email
    }.filter { (id, email) -> id != null && email != null }
    .flatMapLatest { (id, email) ->
        combine(
            repository.getSharedAccountsByUserId(id!!),
            repository.getPendingRequestsByEmail(email!!),
            _isAddRequestDialogVisible
        ) { accounts, requests, isDialogVisible ->
            SharedAccountsUiState(
                linkedAccounts = accounts,
                pendingRequests = requests,
                isAddRequestDialogVisible = isDialogVisible,
                isLoading = false
            )
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharedAccountsUiState(isLoading = true)
    )
    
    fun loadSharedAccounts(userId: Long, email: String) {
        _userId.value = userId
        _userEmail.value = email
    }
    
    fun showAddRequestDialog() {
        _isAddRequestDialogVisible.value = true
    }
    
    fun hideAddRequestDialog() {
        _isAddRequestDialogVisible.value = false
    }
    
    fun sendLinkRequest(userId: Long, email: String, phone: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = SharedAccountRequest(
                fromUserId = userId,
                toUserEmail = email,
                toUserPhone = phone,
                status = "PENDING"
            )
            repository.insertSharedAccountRequest(request)
            _isAddRequestDialogVisible.value = false
        }
    }
    
    fun approveRequest(requestId: Long, currentUserId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = repository.getSharedAccountRequest(requestId)
            request?.let {
                val sharedAccount = SharedAccount(
                    primaryUserId = currentUserId,
                    linkedUserId = it.fromUserId,
                    status = "APPROVED",
                    role = "LINKED",
                    approvedAt = System.currentTimeMillis()
                )
                repository.insertSharedAccount(sharedAccount)
                repository.updateRequestStatus(requestId, "APPROVED", System.currentTimeMillis())
            }
        }
    }
    
    fun denyRequest(requestId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRequestStatus(requestId, "DENIED", System.currentTimeMillis())
        }
    }
    
    fun removeLinkedAccount(accountId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSharedAccount(accountId)
        }
    }
    
    fun setSpendingLimit(accountId: Long, limit: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSharedAccountLimit(accountId, limit)
        }
    }
}
