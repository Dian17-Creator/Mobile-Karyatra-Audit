package id.my.karyatra.audit.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/audit/categories")
    suspend fun getCategories(): Response<CategoryListResponse>

    @GET("api/audit/categories/{id}")
    suspend fun getCategory(
        @Path("id") id: Int
    ): Response<CategoryResponse>

    @POST("api/audit/categories")
    suspend fun createCategory(
        @Body request: CategoryRequest
    ): Response<CategoryResponse>

    @POST("api/audit/categories/{id}/update")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): Response<CategoryResponse>

    @POST("api/audit/categories/{id}/delete")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): Response<CategoryResponse>

}
