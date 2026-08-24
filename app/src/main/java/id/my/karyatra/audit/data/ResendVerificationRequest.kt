package id.my.karyatra.audit.data

import com.google.gson.annotations.SerializedName

data class ResendVerificationRequest(
    @SerializedName("email") val email: String,
    @SerializedName("user_id") val userId: Int
)
