package id.my.karyatra.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.repository.CompanyLifecycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CompanyUiState {
    object Loading : CompanyUiState()
    data class Success(val lifecycleData: CompanyLifecycleData, val message: String? = null) : CompanyUiState()
    data class Error(val errorMessage: String) : CompanyUiState()
}

class CompanyLifecycleViewModel(
    private val repository: CompanyLifecycleRepository = CompanyLifecycleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CompanyUiState>(CompanyUiState.Loading)
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    fun loadStatus(userId: Int) {
        viewModelScope.launch {
            _uiState.value = CompanyUiState.Loading
            when (val result = repository.getCompanyStatus(userId)) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    if (data != null) {
                        _uiState.value = CompanyUiState.Success(data)
                    } else {
                        _uiState.value = CompanyUiState.Error(result.data.message.ifEmpty { "Gagal memuat status." })
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = CompanyUiState.Error(result.message)
                }
            }
        }
    }

    fun deactivateCompany(userId: Int, password: String) {
        viewModelScope.launch {
            _uiState.value = CompanyUiState.Loading
            val request = DeactivateCompanyRequest(userId, password, confirm = true)
            when (val result = repository.deactivateCompany(request)) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    if (data != null) {
                        _uiState.value = CompanyUiState.Success(data, "Perusahaan berhasil dinonaktifkan sementara.")
                    } else {
                        _uiState.value = CompanyUiState.Error("Respon tidak valid.")
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = CompanyUiState.Error(result.message)
                }
            }
        }
    }

    fun reactivateCompany(userId: Int, password: String) {
        viewModelScope.launch {
            _uiState.value = CompanyUiState.Loading
            val request = ReactivateCompanyRequest(userId, password)
            when (val result = repository.reactivateCompany(request)) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    if (data != null) {
                        _uiState.value = CompanyUiState.Success(data, "Perusahaan berhasil diaktifkan kembali.")
                    } else {
                        _uiState.value = CompanyUiState.Error("Respon tidak valid.")
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = CompanyUiState.Error(result.message)
                }
            }
        }
    }
}
