package id.my.karyatra.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.repository.DashboardRepository
import id.my.karyatra.audit.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockCategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<StockCategoryData> = emptyList(),
    val selectedCategory: StockCategoryData? = null,
    val isAddDialogOpen: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class StockCategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StockRepository = StockRepository(application)
    private val dashboardRepository: DashboardRepository = DashboardRepository(application)
    private val sessionManager: SessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(StockCategoryUiState())
    val uiState: StateFlow<StockCategoryUiState> = _uiState.asStateFlow()

    init {
        // Instant load
        repository.getCachedCategories()?.data?.let { data ->
            _uiState.update { it.copy(categories = data) }
        }
        fetchCategories()
    }

    fun fetchCategories() {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            if (_uiState.value.categories.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            repository.getCategories(user.id).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, categories = result.data.data ?: emptyList()) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = true) }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false) }
    }

    fun openEditDialog(category: StockCategoryData) {
        _uiState.update { it.copy(isEditDialogOpen = true, selectedCategory = category) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(isEditDialogOpen = false, selectedCategory = null) }
    }

    fun openDeleteDialog(category: StockCategoryData) {
        _uiState.update { it.copy(isDeleteDialogOpen = true, selectedCategory = category) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogOpen = false, selectedCategory = null) }
    }

    fun addCategory(name: String, description: String?) {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = StockCategoryRequest(name = name, description = description)
            when (val result = repository.createCategory(user.id, request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        dashboardRepository.invalidateCache()
                        _uiState.update { it.copy(isLoading = false, isAddDialogOpen = false, successMessage = result.data.message) }
                        fetchCategories()
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun updateCategory(id: Int, name: String, description: String?) {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = StockCategoryRequest(name = name, description = description)
            when (val result = repository.updateCategory(user.id, id, request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        dashboardRepository.invalidateCache()
                        _uiState.update { it.copy(isLoading = false, isEditDialogOpen = false, successMessage = result.data.message) }
                        fetchCategories()
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun deleteCategory(id: Int) {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteCategory(user.id, id)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        dashboardRepository.invalidateCache()
                        _uiState.update { it.copy(isLoading = false, isDeleteDialogOpen = false, successMessage = result.data.message) }
                        fetchCategories()
                    } else {
                        _uiState.update { it.copy(isLoading = false, isDeleteDialogOpen = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, isDeleteDialogOpen = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
