package id.my.karyatra.audit.data.api

import id.my.karyatra.audit.data.model.subscription.ApiResponse
import id.my.karyatra.audit.data.model.subscription.SubscriptionPlan
import id.my.karyatra.audit.data.model.subscription.SubscriptionStateResponse
import id.my.karyatra.audit.data.model.subscription.SubscriptionTransaction
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface SubscriptionApiService {
    // Get State Berlangganan Perusahaan
    @GET("api/subscription/state")
    suspend fun getSubscriptionState(
        @Query("user_id") userId: Int
    ): Response<ApiResponse<SubscriptionStateResponse>>

    // Get List Paket Aktif
    @GET("api/subscription/plans")
    suspend fun getSubscriptionPlans(): Response<ApiResponse<List<SubscriptionPlan>>>

    // Pengajuan Berlangganan (Multipart Upload Proof)
    @Multipart
    @POST("api/subscription/request")
    suspend fun requestSubscription(
        @Part("user_id") userId: RequestBody,
        @Part("plan_id") planId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part paymentProof: MultipartBody.Part,
        @Part("payment_ref") paymentRef: RequestBody? = null
    ): Response<ApiResponse<SubscriptionTransaction>>
}
