package id.my.karyatra.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val userList: List<UserData> = emptyList(),
    val isActionSuccess: Boolean = false
)

class UserViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null, isActionSuccess = false)
    }

    fun updateProfile(userId: Int, fullName: String, email: String, sessionManager: SessionManager) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.updateProfile(userId, UpdateProfileRequest(userId, fullName, email))
            when (result) {
                is ApiResult.Success -> {
                    result.data.data?.let { sessionManager.saveSession(it, sessionManager.isRememberMe()) }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                        isActionSuccess = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun changePassword(userId: Int, current: String, new: String, confirm: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.changePassword(userId, ChangePasswordRequest(userId, current, new, confirm))
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                        isActionSuccess = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun fetchUsers(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getUsers(userId)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userList = result.data.data
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun addUser(ownerId: Int, name: String, email: String, pass: String, level: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.addUser(ownerId, AddUserRequest(ownerId, name, email, pass, level))
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                        isActionSuccess = true
                    )
                    fetchUsers(ownerId)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun updateUserLevel(ownerId: Int, targetId: Int, level: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Updated parameter names for clarity and to trigger re-compilation
            val request = UpdateLevelRequest(ownerId, level)
            val result = repository.updateLevel(ownerId, targetId, request)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                        isActionSuccess = true
                    )
                    fetchUsers(ownerId)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun deleteUser(ownerId: Int, targetId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteUser(ownerId, targetId, DeleteUserRequest(ownerId))
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                        isActionSuccess = true
                    )
                    fetchUsers(ownerId)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
