package id.my.karyatra.audit.data.repository

import id.my.karyatra.audit.data.*
import java.io.IOException

class CompanyLifecycleRepository(
    private val apiService: ApiService = RetrofitClientLaravel.instance
) {

    suspend fun getCompanyStatus(userId: Int): ApiResult<CompanyLifecycleStateResponse> {
        return try {
            val response = apiService.getCompanyStatus(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal memuat status perusahaan.")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deactivateCompany(request: DeactivateCompanyRequest): ApiResult<CompanyLifecycleStateResponse> {
        return try {
            val response = apiService.deactivateCompany(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal menonaktifkan perusahaan.")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun reactivateCompany(request: ReactivateCompanyRequest): ApiResult<CompanyLifecycleStateResponse> {
        return try {
            val response = apiService.reactivateCompany(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal mengaktifkan kembali perusahaan.")
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
