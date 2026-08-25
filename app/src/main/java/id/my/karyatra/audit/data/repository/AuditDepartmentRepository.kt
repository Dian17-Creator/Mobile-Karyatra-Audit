package id.my.karyatra.audit.data.repository

import android.content.Context
import id.my.karyatra.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class AuditDepartmentRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_DEPARTMENTS = "audit_departments_list"
    }

    fun getDepartments(userId: Int): Flow<ApiResult<DepartmentListResponse>> = flow {
        val cached = getCachedDepartments()
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getDepartments(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(CACHE_KEY_DEPARTMENTS, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data tidak ditemukan"))
                }
            } else if (cached == null) {
                emit(ApiResult.Error(ApiErrorParser.parseError(response.errorBody())))
            }
        } catch (e: IOException) {
            if (cached == null) emit(ApiResult.Error("Kesalahan Koneksi: ${e.message}"))
        } catch (e: Exception) {
            if (cached == null) emit(ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }

    fun getCachedDepartments(): DepartmentListResponse? {
        return cache.get(CACHE_KEY_DEPARTMENTS, DepartmentListResponse::class.java)
    }

    suspend fun getDepartmentMapping(userId: Int, id: Int): ApiResult<DepartmentMappingResponse> {
        return try {
            val response = api.getDepartmentMapping(id, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Data tidak ditemukan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun saveDepartmentMapping(userId: Int, request: SaveMappingRequest): ApiResult<GenericResponse> {
        return try {
            val response = api.saveDepartmentMapping(request, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal menyimpan data")
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
