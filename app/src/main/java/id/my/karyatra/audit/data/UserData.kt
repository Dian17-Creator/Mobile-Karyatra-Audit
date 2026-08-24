package id.my.karyatra.audit.data

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val company: String?,
    val department_id: Int?,
    val department_name: String?,
    val role: UserRole,
    val is_owner: Boolean?,
    val is_email_verified: Boolean?,
    val is_trial: Boolean?,
    val demailverified: String?
)