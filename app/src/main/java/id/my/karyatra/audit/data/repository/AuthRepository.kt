package id.my.karyatra.audit.data.repository

import id.my.karyatra.audit.data.LoginRequest
import id.my.karyatra.audit.data.LoginResponse
import id.my.karyatra.audit.data.RetrofitClientLaravel

class AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse? {

        val response = RetrofitClientLaravel.instance.login(
            LoginRequest(email, password)
        )

        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}