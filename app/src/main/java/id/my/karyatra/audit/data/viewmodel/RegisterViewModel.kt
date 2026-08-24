package id.my.karyatra.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null, isSuccess = false) }
            
            when (val result = repository.register(request)) {
                is ApiResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isSuccess = result.data.success, 
                            message = result.data.message,
                            error = if (!result.data.success) result.data.message else null
                        ) 
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun resendVerification(email: String, userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, error = null, message = null) }
            
            val request = ResendVerificationRequest(email, userId)
            when (val result = repository.resendVerification(request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isResending = false, message = result.data.message) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isResending = false, error = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, message = null) }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
