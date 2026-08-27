package id.my.karyatra.audit.data.repository

import id.my.karyatra.audit.data.ApiErrorParser
import id.my.karyatra.audit.data.ApiResult
import id.my.karyatra.audit.data.RetrofitClientLaravel
import id.my.karyatra.audit.data.api.SubscriptionApiService
import id.my.karyatra.audit.data.model.subscription.ApiResponse
import id.my.karyatra.audit.data.model.subscription.SubscriptionPlan
import id.my.karyatra.audit.data.model.subscription.SubscriptionStateResponse
import id.my.karyatra.audit.data.model.subscription.SubscriptionTransaction
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException

class SubscriptionRepository {
    private val api: SubscriptionApiService = RetrofitClientLaravel.subscriptionInstance

    suspend fun getSubscriptionState(userId: Int): ApiResult<ApiResponse<SubscriptionStateResponse>> {
        return try {
            val response = api.getSubscriptionState(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Masalah koneksi internet. Silakan coba lagi.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun getSubscriptionPlans(): ApiResult<ApiResponse<List<SubscriptionPlan>>> {
        return try {
            val response = api.getSubscriptionPlans()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Masalah koneksi internet. Silakan coba lagi.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun requestSubscription(
        userId: RequestBody,
        planId: RequestBody,
        amount: RequestBody,
        paymentProof: MultipartBody.Part,
        paymentRef: RequestBody? = null
    ): ApiResult<ApiResponse<SubscriptionTransaction>> {
        return try {
            val response = api.requestSubscription(userId, planId, amount, paymentProof, paymentRef)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Masalah koneksi internet. Silakan coba lagi.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }
}
