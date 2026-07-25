package id.my.karyatra.audit.data.repository

import id.my.karyatra.audit.data.ApiErrorParser
import id.my.karyatra.audit.data.ApiResult
import id.my.karyatra.audit.data.CategoryListResponse
import id.my.karyatra.audit.data.CategoryRequest
import id.my.karyatra.audit.data.CategoryResponse
import id.my.karyatra.audit.data.RetrofitClientLaravel
import java.io.IOException

class AuditCategoryRepository {

    private val api = RetrofitClientLaravel.instance

    suspend fun getCategories(): ApiResult<CategoryListResponse> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Data tidak ditemukan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }

    suspend fun createCategory(request: CategoryRequest): ApiResult<CategoryResponse> {
        return try {
            val response = api.createCategory(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal menyimpan data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }

    suspend fun updateCategory(id: Int, request: CategoryRequest): ApiResult<CategoryResponse> {
        return try {
            val response = api.updateCategory(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal memperbarui data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }

    suspend fun deleteCategory(id: Int): ApiResult<CategoryResponse> {
        return try {
            val response = api.deleteCategory(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal menghapus data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }
}
