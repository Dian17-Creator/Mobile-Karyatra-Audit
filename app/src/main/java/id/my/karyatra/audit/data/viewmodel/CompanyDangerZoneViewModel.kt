package id.my.karyatra.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.repository.CompanyLifecycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompanyDangerZoneViewModel(
    private val repository: CompanyLifecycleRepository = CompanyLifecycleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CompanyUiState>(CompanyUiState.Loading)
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        val current = _uiState.value
        if (current is CompanyUiState.Success && current.message != null) {
            _uiState.value = CompanyUiState.Success(current.lifecycleData, null)
        }
    }

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

    fun requestDeletion(
        userId: Int,
        companyName: String,
        password: String,
        isPro: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = CompanyUiState.Loading
            val req = RequestCompanyDeletionRequest(
                userId = userId,
                companyName = companyName,
                currentPassword = password,
                confirmDeletion = true,
                confirmFinanceRetention = true,
                confirmProNoRefund = isPro
            )
            when (val result = repository.requestCompanyDeletion(req)) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    if (data != null) {
                        _uiState.value = CompanyUiState.Success(
                            data,
                            "Penghapusan perusahaan berhasil dijadwalkan."
                        )
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

    fun cancelDeletion(userId: Int, password: String) {
        viewModelScope.launch {
            _uiState.value = CompanyUiState.Loading
            val req = CancelCompanyDeletionRequest(userId, password)
            when (val result = repository.cancelCompanyDeletion(req)) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    if (data != null) {
                        _uiState.value = CompanyUiState.Success(
                            data,
                            "Penghapusan perusahaan berhasil dibatalkan."
                        )
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
