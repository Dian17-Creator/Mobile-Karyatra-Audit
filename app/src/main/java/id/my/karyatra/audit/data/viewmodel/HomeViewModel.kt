package id.my.karyatra.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.ApiResult
import id.my.karyatra.audit.data.DashboardSummaryResponse
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
    val error: String? = null
)

class HomeViewModel(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchDashboardSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getDashboardSummary()) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            totalKategori = data?.totalKategori?.toString() ?: "0",
                            totalPertanyaan = data?.totalPertanyaan?.toString() ?: "0"
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = result.message,
                            totalKategori = if (it.totalKategori == "--") "-" else it.totalKategori,
                            totalPertanyaan = if (it.totalPertanyaan == "--") "-" else it.totalPertanyaan
                        ) 
                    }
                }
            }
        }
    }
}
