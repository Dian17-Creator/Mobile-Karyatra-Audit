package id.my.karyatra.audit.data.repository

import android.content.Context
import id.my.karyatra.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class StockRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_STOCK_CATEGORIES = "stock_categories"
        private const val CACHE_KEY_STOCK_ITEMS_PREFIX = "stock_items_"
        private const val CACHE_KEY_STOCK_DEPARTMENTS = "stock_departments_list"
        private const val CACHE_KEY_STOCK_MAPPING_PREFIX = "stock_department_mapping_"
    }

    fun getCategories(userId: Int): Flow<ApiResult<StockCategoryListResponse>> = flow {
        val cached = getCachedCategories()
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockCategories(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(CACHE_KEY_STOCK_CATEGORIES, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data tidak ditemukan"))
                }
            } else if (cached == null) {
                emit(ApiResult.Error(ApiErrorParser.parseError(response.errorBody())))
            }
        } catch (e: IOException) {
            if (cached == null) {
                emit(ApiResult.Error("Kesalahan Koneksi: ${e.message}"))
            }
        } catch (e: Exception) {
            if (cached == null) {
                emit(ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}"))
            }
        }
    }

    fun getCategory(userId: Int, id: Int): Flow<ApiResult<StockCategoryResponse>> = flow {
        val cacheKey = CACHE_KEY_STOCK_ITEMS_PREFIX + id
        val cached = getCachedItems(id)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockItems(id, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(cacheKey, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data tidak ditemukan"))
                }
            } else if (cached == null) {
                emit(ApiResult.Error(ApiErrorParser.parseError(response.errorBody())))
            }
        } catch (e: IOException) {
            if (cached == null) {
                emit(ApiResult.Error("Kesalahan Koneksi: ${e.message}"))
            }
        } catch (e: Exception) {
            if (cached == null) {
                emit(ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}"))
            }
        }
    }

    suspend fun createCategory(userId: Int, request: StockCategoryRequest): ApiResult<StockCategoryResponse> {
        return try {
            val response = api.createStockCategory(request, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCategoriesCache()
                    ApiResult.Success(body)
                }
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

    suspend fun updateCategory(userId: Int, id: Int, request: StockCategoryRequest): ApiResult<StockCategoryResponse> {
        return try {
            val spoofedRequest = request.copy(method = "PUT")
            val response = api.updateStockCategory(id, spoofedRequest, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCategoriesCache()
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal memperbarui data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deleteCategory(userId: Int, id: Int): ApiResult<GenericResponse> {
        return try {
            val response = api.deleteStockCategory(id, StockDeleteRequest(), userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCategoriesCache()
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menghapus data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun createItem(userId: Int, request: StockItemRequest): ApiResult<StockItemResponse> {
        return try {
            val response = api.createStockItem(request, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateItemsCache(request.categoryId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menambah barang")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deleteItem(userId: Int, categoryId: Int, id: Int): ApiResult<GenericResponse> {
        return try {
            val response = api.deleteStockItem(id, StockDeleteRequest(), userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateItemsCache(categoryId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menghapus barang")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun reorderItems(userId: Int, request: StockReorderRequest): ApiResult<GenericResponse> {
        return try {
            val response = api.reorderStockItems(request, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateItemsCache(request.categoryId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal mengurutkan barang")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun invalidateCategoriesCache() {
        cache.delete(CACHE_KEY_STOCK_CATEGORIES)
    }

    fun invalidateItemsCache(categoryId: Int) {
        cache.delete(CACHE_KEY_STOCK_ITEMS_PREFIX + categoryId)
    }

    fun getCachedCategories(): StockCategoryListResponse? {
        return cache.get(CACHE_KEY_STOCK_CATEGORIES, StockCategoryListResponse::class.java)
    }

    fun getCachedItems(categoryId: Int): StockCategoryResponse? {
        return cache.get(CACHE_KEY_STOCK_ITEMS_PREFIX + categoryId, StockCategoryResponse::class.java)
    }

    fun getCachedDepartments(): DepartmentListResponse? {
        return cache.get(CACHE_KEY_STOCK_DEPARTMENTS, DepartmentListResponse::class.java)
    }

    fun getCachedMapping(departmentId: Int): StockDepartmentMappingResponse? {
        return cache.get(CACHE_KEY_STOCK_MAPPING_PREFIX + departmentId, StockDepartmentMappingResponse::class.java)
    }

    // Mapping Methods
    fun getDepartments(userId: Int): Flow<ApiResult<DepartmentListResponse>> = flow {
        val cached = cache.get(CACHE_KEY_STOCK_DEPARTMENTS, DepartmentListResponse::class.java)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockDepartments(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(CACHE_KEY_STOCK_DEPARTMENTS, body)
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

    fun getDepartmentMapping(userId: Int, id: Int): Flow<ApiResult<StockDepartmentMappingResponse>> = flow {
        val cacheKey = CACHE_KEY_STOCK_MAPPING_PREFIX + id
        val cached = cache.get(cacheKey, StockDepartmentMappingResponse::class.java)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockDepartmentMapping(id, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(cacheKey, body)
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

    suspend fun saveMapping(userId: Int, request: SaveStockMappingRequest): ApiResult<GenericResponse> {
        return try {
            val response = api.saveStockDepartmentMapping(request, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateMappingCache(request.departmentId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menyimpan pemetaan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun invalidateMappingCache(departmentId: Int) {
        cache.delete(CACHE_KEY_STOCK_MAPPING_PREFIX + departmentId)
    }
}
