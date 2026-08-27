package id.my.karyatra.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.karyatra.audit.data.ApiResult
import id.my.karyatra.audit.data.SessionManager
import id.my.karyatra.audit.data.model.subscription.SubscriptionPlan
import id.my.karyatra.audit.data.model.subscription.SubscriptionStateResponse
import id.my.karyatra.audit.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val isPlansLoading: Boolean = false,
    val isUploading: Boolean = false,
    val subscriptionState: SubscriptionStateResponse? = null,
    val plans: List<SubscriptionPlan> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SubscriptionRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    fun fetchSubscriptionState() {
        val user = sessionManager.getUser() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getSubscriptionState(user.id)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, subscriptionState = result.data.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun fetchPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPlansLoading = true, error = null) }
            when (val result = repository.getSubscriptionPlans()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isPlansLoading = false, plans = result.data.data ?: emptyList()) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isPlansLoading = false, error = result.message) }
                }
            }
        }
    }

    fun requestSubscription(plan: SubscriptionPlan, proofFile: File, paymentRef: String?) {
        val user = sessionManager.getUser() ?: return
        if (user.is_owner != true) {
            _uiState.update { it.copy(error = "Hanya Owner Perusahaan yang dapat melakukan upgrade paket.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null, successMessage = null) }
            
            val userIdBody = user.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val planIdBody = plan.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val amountBody = plan.price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val paymentRefBody = paymentRef?.toRequestBody("text/plain".toMediaTypeOrNull())

            val proofPart = MultipartBody.Part.createFormData(
                "payment_proof",
                proofFile.name,
                proofFile.asRequestBody("image/*".toMediaTypeOrNull())
            )

            when (val result = repository.requestSubscription(userIdBody, planIdBody, amountBody, proofPart, paymentRefBody)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false, successMessage = result.data.message) }
                    fetchSubscriptionState() // Refresh state
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploading = false, error = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
