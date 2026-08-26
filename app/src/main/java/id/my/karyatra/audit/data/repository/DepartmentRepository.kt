package id.my.karyatra.audit.data.repository

import id.my.karyatra.audit.data.*
import java.io.IOException

class DepartmentRepository {

    suspend fun getDepartments(userId: Int): ApiResult<DepartmentListResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.getDepartments(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal mengambil daftar departemen")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun addDepartment(userId: Int, request: DepartmentRequest): ApiResult<DepartmentActionResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.addDepartment(request, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal menambah departemen")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun updateDepartment(userId: Int, departmentId: Int, request: DepartmentRequest): ApiResult<DepartmentActionResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.updateDepartment(departmentId, request, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal memperbarui departemen")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deleteDepartment(userId: Int, departmentId: Int, request: DeleteDepartmentRequest): ApiResult<GenericResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.deleteDepartment(departmentId, request, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal menghapus departemen")
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
