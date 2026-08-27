package id.my.karyatra.audit.data.model.subscription

import com.google.gson.annotations.SerializedName

// Generic API Response Wrapper
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T? = null
)

// 1. Status Entitlement Perusahaan
data class SubscriptionStateResponse(
    @SerializedName("plan") val plan: String, // "trial", "free", "pro"
    @SerializedName("pro_start") val proStart: String?,
    @SerializedName("pro_current_until") val proCurrentUntil: String?,
    @SerializedName("pro_until") val proUntil: String?,
    @SerializedName("upgrade_pending") val isUpgradePending: Boolean,
    @SerializedName("rejection_visible") val isRejectionVisible: Boolean,
    @SerializedName("owner_verified") val isOwnerVerified: Boolean
) {
    fun isPro(): Boolean = plan.lowercase() == "pro"
    fun isTrial(): Boolean = plan.lowercase() == "trial"
    fun isFree(): Boolean = plan.lowercase() == "free"

    fun getMaxPhotos(isOpname: Boolean): Int {
        return when {
            isFree() -> 1
            isTrial() -> 999 // Effectively unlimited
            isPro() -> if (isOpname) 5 else 10
            else -> 1
        }
    }

    fun canCreateDocument(currentCount: Int): Boolean {
        if (isFree()) return currentCount < 1
        return true // Trial & Pro unlimited documents
    }

    fun canExport(): Boolean {
        if (isTrial()) return false
        return true // Free and Pro can export
    }
}

// 2. Katalog Paket Berlangganan
data class SubscriptionPlan(
    @SerializedName("nid") val id: Int,
    @SerializedName("ccode") val code: String,
    @SerializedName("cnama") val name: String,
    @SerializedName("nduration_months") val durationMonths: Int,
    @SerializedName("nprice") val price: Double,
    @SerializedName("nreference_price") val referencePrice: Double?,
    @SerializedName("cdescription") val description: String?,
    @SerializedName("cbadge") val badge: String?,
    @SerializedName("fenabled") val isEnabled: Boolean,
    @SerializedName("nsort") val sort: Int
)

// 3. Respon Transaksi Pengajuan
data class SubscriptionTransaction(
    @SerializedName("nid") val id: Int,
    @SerializedName("nid_owner") val ownerId: Int,
    @SerializedName("nid_plan") val planId: Int,
    @SerializedName("cplan_name") val planName: String,
    @SerializedName("namount") val amount: Double,
    @SerializedName("cstatus") val status: String, // "pending", "approved", "rejected"
    @SerializedName("cpayment_proof") val paymentProofPath: String?
)
