package id.my.karyatra.audit.data

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)