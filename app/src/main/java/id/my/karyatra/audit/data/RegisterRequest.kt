package id.my.karyatra.audit.data

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("namalengkap") val name: String,
    @SerializedName("perusahaan") val company: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
