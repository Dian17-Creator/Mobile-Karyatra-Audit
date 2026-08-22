package id.my.karyatra.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.ApiResult
import id.my.karyatra.audit.data.RecentActivityData
import id.my.karyatra.audit.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val totalKategori: String = "--",
    val totalPertanyaan: String = "--",
    val totalAudit: String = "--",
    val recentActivities: List<RecentActivityData> = emptyList(),
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DashboardRepository = DashboardRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchDashboardSummary() {
        viewModelScope.launch {
            repository.getDashboardSummary().collect { result ->
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
