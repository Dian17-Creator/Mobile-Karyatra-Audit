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
    val demailverified: String?,
    val created_at: String? = null
)

data class UpdateProfileRequest(
    val user_id: Int,
    val full_name: String,
    val email: String
)

data class ChangePasswordRequest(
    val user_id: Int,
    val current_password: String,
    val new_password: String,
    val confirm_password: String
)

data class UserListResponse(
    val success: Boolean,
    val message: String,
    val data: List<UserData>
)

data class AddUserRequest(
    val owner_id: Int,
    val new_name: String,
    val new_email: String,
    val new_user_password: String,
    val new_level: String
)

data class UpdateLevelRequest(
    val owner_id: Int,
    val level: String
)

data class DeleteUserRequest(
    val owner_id: Int
)

data class UserActionResponse(
    val success: Boolean,
    val message: String,
    val data: UserData? = null
)
