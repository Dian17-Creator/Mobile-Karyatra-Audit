package id.my.karyatra.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.repository.DepartmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DepartmentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val departmentList: List<DepartmentData> = emptyList(),
    val isActionSuccess: Boolean = false
)

class DepartmentViewModel(private val repository: DepartmentRepository = DepartmentRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(DepartmentUiState())
    val uiState: StateFlow<DepartmentUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null, isActionSuccess = false)
    }

    fun fetchDepartments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getDepartments()
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        departmentList = result.data.data ?: emptyList()
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun addDepartment(ownerId: Int, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.addDepartment(DepartmentRequest(ownerId, name))
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                        isActionSuccess = true
                    )
                    fetchDepartments()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun updateDepartment(ownerId: Int, departmentId: Int, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.updateDepartment(departmentId, DepartmentRequest(ownerId, name))
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                        isActionSuccess = true
                    )
                    fetchDepartments()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun deleteDepartment(ownerId: Int, departmentId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteDepartment(departmentId, DeleteDepartmentRequest(ownerId))
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data.message,
                        isActionSuccess = true
                    )
                    fetchDepartments()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
