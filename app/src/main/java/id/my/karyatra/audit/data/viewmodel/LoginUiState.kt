package id.my.karyatra.audit.data.viewmodel

import id.my.karyatra.audit.data.UserData

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val userData: UserData? = null
)
