package id.my.karyatra.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class DepartmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DepartmentRepository = DepartmentRepository()
    private val sessionManager: SessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(DepartmentUiState())
    val uiState: StateFlow<DepartmentUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null, isActionSuccess = false)
    }

    fun fetchDepartments() {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getDepartments(user.id)
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
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.addDepartment(user.id, DepartmentRequest(ownerId, name))
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
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.updateDepartment(user.id, departmentId, DepartmentRequest(ownerId, name))
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
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteDepartment(user.id, departmentId, DeleteDepartmentRequest(ownerId))
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
