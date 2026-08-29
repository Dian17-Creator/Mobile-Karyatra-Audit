package id.my.karyatra.audit.data

import com.google.gson.annotations.SerializedName

data class CompanyLifecycleStateResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: CompanyLifecycleData? = null
)

data class CompanyLifecycleData(
    @SerializedName("is_owner") val isOwner: Boolean = false,
    @SerializedName("is_company_inactive") val isCompanyInactive: Boolean = false,
    @SerializedName("is_deletion_pending") val isDeletionPending: Boolean = false,
    @SerializedName("dcompanynonactive") val dcompanynonactive: String? = null,
    @SerializedName("ddeletionrequested") val ddeletionrequested: String? = null,
    @SerializedName("ddeleteafter") val ddeleteafter: String? = null,
    @SerializedName("fdeletionwasinactive") val fdeletionwasinactive: Boolean = false
)

data class DeactivateCompanyRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("confirm") val confirm: Boolean = true
)

data class ReactivateCompanyRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("current_password") val currentPassword: String
)

data class RequestCompanyDeletionRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("confirm_deletion") val confirmDeletion: Boolean = true,
    @SerializedName("confirm_finance_retention") val confirmFinanceRetention: Boolean = true,
    @SerializedName("confirm_pro_no_refund") val confirmProNoRefund: Boolean = true
)

data class CancelCompanyDeletionRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("current_password") val currentPassword: String
)
