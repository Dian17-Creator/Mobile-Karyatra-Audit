package id.my.karyatra.audit.data.repository

import id.my.karyatra.audit.data.*
import java.io.IOException

class AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): ApiResult<LoginResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.login(
                LoginRequest(email, password)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Terjadi kesalahan. Silakan coba lagi.")
                }
            } else {
                val errorMessage = ApiErrorParser.parseError(response.errorBody())
                ApiResult.Error(errorMessage)
            }
        } catch (e: IOException) {
            ApiResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan. Silakan coba lagi.")
        }
    }

    suspend fun register(request: RegisterRequest): ApiResult<RegisterResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.register(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal mendaftar")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun resendVerification(request: ResendVerificationRequest): ApiResult<GenericResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.resendVerification(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal mengirim ulang verifikasi")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun getCurrentUser(userId: Int): ApiResult<LoginResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.me(userId = userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal mengambil data user")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }
}
