package id.my.karyatra.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.repository.AuthRepository
import id.my.karyatra.audit.data.repository.DashboardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val totalKategori: String = "--",
    val totalPertanyaan: String = "--",
    val totalAudit: String = "--",
    val recentActivities: List<RecentActivityData> = emptyList(),
    val currentUser: UserData? = null,
    val resendMessage: String? = null,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DashboardRepository = DashboardRepository(application)
    private val authRepository: AuthRepository = AuthRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(HomeUiState(currentUser = sessionManager.getUser()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    fun startVerificationCheck(userId: Int) {
        // Only poll if email is not verified yet
        if (_uiState.value.currentUser?.is_email_verified == true) return
        
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                val result = authRepository.getCurrentUser(userId)
                if (result is ApiResult.Success) {
                    val user = result.data.data
                    if (user != null) {
                        _uiState.update { it.copy(currentUser = user) }
                        // Update session if status changed
                        sessionManager.saveSession(user, sessionManager.isRememberMe())
                        
                        // Stop polling if verified
                        if (user.is_email_verified == true) {
                            break
                        }
                    }
                }
                delay(10000) // Poll every 10 seconds
            }
        }
    }

    fun resendVerification() {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, resendMessage = null) }
            val request = ResendVerificationRequest(user.email, user.id)
            when (val result = authRepository.resendVerification(request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isResending = false, resendMessage = result.data.message) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isResending = false, error = result.message) }
                }
            }
        }
    }

    fun stopVerificationCheck() {
        pollingJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopVerificationCheck()
    }

    fun fetchDashboardSummary() {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            repository.getDashboardSummary(user.id).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val response = result.data
                        if (response.success) {
                            val data = response.data
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    totalKategori = data?.totalKategori?.toString() ?: "0",
                                    totalPertanyaan = data?.totalPertanyaan?.toString() ?: "0",
                                    totalAudit = data?.totalAudit?.toString() ?: "0",
                                    recentActivities = data?.recentActivity ?: emptyList()
                                )
                            }
                        } else {
                            android.util.Log.e("HomeViewModel", "API Error: ${response.message}")
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = response.message,
                                    totalKategori = if (it.totalKategori == "--") "0" else it.totalKategori,
                                    totalPertanyaan = if (it.totalPertanyaan == "--") "0" else it.totalPertanyaan,
                                    totalAudit = if (it.totalAudit == "--") "0" else it.totalAudit
                                )
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        android.util.Log.e("HomeViewModel", "Error fetching dashboard: ${result.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.message,
                                totalKategori = if (it.totalKategori == "--") "0" else it.totalKategori,
                                totalPertanyaan = if (it.totalPertanyaan == "--") "0" else it.totalPertanyaan,
                                totalAudit = if (it.totalAudit == "--") "0" else it.totalAudit
                            ) 
                        }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null) }
    }
}
