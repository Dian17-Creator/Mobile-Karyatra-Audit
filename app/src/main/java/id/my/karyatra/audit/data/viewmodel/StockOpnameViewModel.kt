package id.my.karyatra.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.repository.AuditDepartmentRepository
import id.my.karyatra.audit.data.repository.DashboardRepository
import id.my.karyatra.audit.data.repository.StockOpnameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class StockOpnameUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val isSubmitting: Boolean = false,
    val departments: List<DepartmentData> = emptyList(),
    val selectedDepartment: DepartmentData? = null,
    val existingDraftId: Int? = null,
    val opnameDetail: StockOpnameDetailData? = null,
    val expandedCategoryIds: Set<Int> = emptySet(),
    val highlightedItemId: Int? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class StockOpnameViewModel(application: Application) : AndroidViewModel(application) {

    private val opnameRepository: StockOpnameRepository = StockOpnameRepository(application)
    private val departmentRepository: AuditDepartmentRepository = AuditDepartmentRepository(application)
    private val dashboardRepository: DashboardRepository = DashboardRepository(application)
    private val subscriptionRepository: id.my.karyatra.audit.data.repository.SubscriptionRepository = id.my.karyatra.audit.data.repository.SubscriptionRepository()
    private val sessionManager: SessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(StockOpnameUiState())
    val uiState: StateFlow<StockOpnameUiState> = _uiState.asStateFlow()

    private val autosaveJobs = mutableMapOf<Int, Job>()

    fun initialize(auditId: Int, auditorId: Int) {
        if (auditId != -1) {
            fetchDetail(auditId, auditorId)
        }
        fetchInitialData()
    }

    private fun fetchInitialData() {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            departmentRepository.getDepartments(user.id).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, departments = result.data.data ?: emptyList()) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    fun selectDepartment(department: DepartmentData) {
        _uiState.update { it.copy(selectedDepartment = department, existingDraftId = null) }
    }

    fun startOpname(auditorId: Int) {
        val user = sessionManager.getUser() ?: return
        val deptId = _uiState.value.selectedDepartment?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = opnameRepository.createStockOpname(user.id, deptId, auditorId)) {
                is ApiResult.Success -> {
                    val id = result.data.data?.id
                    if (id != null) {
                        dashboardRepository.invalidateCache()
                        fetchDetail(id, auditorId)
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "ID tidak ditemukan") }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun fetchDetail(id: Int, auditorId: Int) {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            // Instant load
            if (_uiState.value.opnameDetail == null) {
                opnameRepository.getCachedDetail(id)?.data?.let { detail ->
                    _uiState.update {
                        it.copy(
                            opnameDetail = detail,
                            expandedCategoryIds = detail.categories.map { it.id }.toSet()
                        )
                    }
                }
            }

            if (_uiState.value.opnameDetail == null) {
                _uiState.update { it.copy(isLoading = true) }
            }

            opnameRepository.getStockOpnameDetail(user.id, id, auditorId).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val detail = result.data.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                opnameDetail = detail,
                                expandedCategoryIds = detail?.categories?.map { it.id }?.toSet() ?: emptySet()
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    fun toggleCategory(categoryId: Int) {
        _uiState.update { state ->
            val newIds = if (state.expandedCategoryIds.contains(categoryId)) {
                state.expandedCategoryIds - categoryId
            } else {
                state.expandedCategoryIds + categoryId
            }
            state.copy(expandedCategoryIds = newIds)
        }
    }

    fun onItemChanged(itemId: Int, qtyStock: String?, qtyReal: String?, notes: String?) {
        val currentDetail = _uiState.value.opnameDetail ?: return
        
        // Update local state immediately
        val updatedCategories = currentDetail.categories.map { category ->
            category.copy(items = category.items.map { item ->
                if (item.id == itemId) {
                    item.copy(
                        response = item.response?.copy(
                            qtyStock = qtyStock?.toDoubleOrNull(),
                            qtyReal = qtyReal?.toDoubleOrNull(),
                            remark = notes
                        ) ?: StockOpnameItemResponse(
                            id = 0,
                            qtyStock = qtyStock?.toDoubleOrNull(),
                            qtyReal = qtyReal?.toDoubleOrNull(),
                            diff = null,
                            diffUnder = null,
                            diffOver = null,
                            isNa = false,
                            remark = notes
                        )
                    )
                } else item
            })
        }
        _uiState.update { it.copy(opnameDetail = currentDetail.copy(categories = updatedCategories)) }

        // Debounced autosave
        autosaveJobs[itemId]?.cancel()
        autosaveJobs[itemId] = viewModelScope.launch {
            delay(600)
            performAutosave(itemId, qtyStock, qtyReal, notes)
        }
    }

    private suspend fun performAutosave(itemId: Int, qtyStock: String?, qtyReal: String?, notes: String?) {
        val user = sessionManager.getUser() ?: return
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        
        val request = StockOpnameUpdateRequest(
            auditId = auditId,
            itemId = itemId,
            qtyStock = qtyStock?.toDoubleOrNull(),
            qtyReal = qtyReal?.toDoubleOrNull(),
            remark = notes
        )

        _uiState.update { it.copy(isSaving = true) }
        when (val result = opnameRepository.updateStockOpname(user.id, request)) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(isSaving = false) }
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    fun uploadPhoto(responseId: Int, photoFile: File, remark: String?, auditorId: Int) {
        val user = sessionManager.getUser() ?: return
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }

            // Check Subscription Limit
            val subState = when (val res = subscriptionRepository.getSubscriptionState(user.id)) {
                is ApiResult.Success -> res.data.data
                else -> null
            }
            
            val currentPhotoCount = _uiState.value.opnameDetail?.categories
                ?.flatMap { it.items }
                ?.find { it.response?.id == responseId }
                ?.photos?.size ?: 0
                
            val maxPhotos = subState?.getMaxPhotos(isOpname = true) ?: 0
            
            if (maxPhotos == 0) {
                _uiState.update { it.copy(isUploading = false, errorMessage = "Paket FREE tidak diizinkan mengunggah foto bukti. Silakan upgrade ke paket PRO.") }
                return@launch
            }

            if (currentPhotoCount >= maxPhotos) {
                _uiState.update { it.copy(isUploading = false, errorMessage = "Limit foto tercapai ($maxPhotos foto). Silakan upgrade paket.") }
                return@launch
            }

            when (val result = opnameRepository.uploadPhoto(user.id, auditId, responseId, photoFile, remark)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false) }
                    fetchDetail(auditId, auditorId)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun updatePhotoRemark(photoId: Int, remark: String?, auditorId: Int) {
        val user = sessionManager.getUser() ?: return
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = opnameRepository.updatePhotoRemark(user.id, auditId, photoId, remark)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    fetchDetail(auditId, auditorId)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun deletePhoto(photoId: Int, auditorId: Int) {
        val user = sessionManager.getUser() ?: return
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = opnameRepository.deletePhoto(user.id, auditId, photoId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    fetchDetail(auditId, auditorId)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun submitOpname(auditeeName: String, verificationPhoto: File, auditorId: Int) {
        val user = sessionManager.getUser() ?: return
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, highlightedItemId = null) }
            when (val result = opnameRepository.submitStockOpname(user.id, auditId, auditeeName, verificationPhoto)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        dashboardRepository.invalidateCache()
                        _uiState.update { it.copy(isSubmitting = false, successMessage = result.data.message) }
                        fetchDetail(auditId, auditorId)
                    } else {
                        val firstIncomplete = result.data.incompleteItems?.firstOrNull()
                        _uiState.update { 
                            it.copy(
                                isSubmitting = false, 
                                errorMessage = result.data.message,
                                highlightedItemId = firstIncomplete
                            ) 
                        }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun sendEmail(auditId: Int, recipient: String, message: String?) {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = opnameRepository.sendEmail(user.id, auditId, recipient, message)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, successMessage = result.data.message) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
