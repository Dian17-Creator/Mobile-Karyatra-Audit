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

    @POST("api/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/resend-verification")
    suspend fun resendVerification(
        @Body request: ResendVerificationRequest
    ): Response<GenericResponse>

    @GET("api/me")
    suspend fun me(
        @retrofit2.http.Query("user_id") userId: Int? = null,
        @retrofit2.http.Query("email") email: String? = null
    ): Response<LoginResponse>

    // Dashboard
    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<DashboardSummaryResponse>

    // Audit Categories
    @GET("api/audit/categories")
    suspend fun getCategories(
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<CategoryListResponse>

    @GET("api/audit/categories/{id}")
    suspend fun getCategory(
        @Path("id") id: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<CategoryResponse>

    @POST("api/audit/categories")
    suspend fun createCategory(
        @Body request: CategoryRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<CategoryResponse>

    @POST("api/audit/categories/{id}/update")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<CategoryResponse>

    @POST("api/audit/categories/{id}/delete")
    suspend fun deleteCategory(
        @Path("id") id: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<CategoryResponse>

    // Audit Questions
    @GET("api/audit/categories/{categoryId}/questions")
    suspend fun getQuestions(
        @Path("categoryId") categoryId: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<QuestionListResponse>

    @POST("api/audit/questions")
    suspend fun createQuestion(
        @Body request: QuestionRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<QuestionResponse>

    @POST("api/audit/questions/{id}")
    suspend fun updateQuestion(
        @Path("id") id: Int,
        @Body request: QuestionRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<QuestionResponse>

    @POST("api/audit/questions/{id}/delete")
    suspend fun deleteQuestion(
        @Path("id") id: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<QuestionResponse>

    @POST("api/audit/questions/reorder")
    suspend fun reorderQuestions(
        @Body request: ReorderRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<QuestionResponse>

    // Audit Departments & Mapping
    @GET("api/audit/departments")
    suspend fun getDepartments(
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<DepartmentListResponse>

    @POST("api/departments")
    suspend fun addDepartment(
        @Body request: DepartmentRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<DepartmentActionResponse>

    @POST("api/departments/{id}/update")
    suspend fun updateDepartment(
        @Path("id") id: Int,
        @Body request: DepartmentRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<DepartmentActionResponse>

    @POST("api/departments/{id}/delete")
    suspend fun deleteDepartment(
        @Path("id") id: Int,
        @Body request: DeleteDepartmentRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    @GET("api/audit/departments/{id}/mapping")
    suspend fun getDepartmentMapping(
        @Path("id") id: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<DepartmentMappingResponse>

    @POST("api/audit/departments/mapping")
    suspend fun saveDepartmentMapping(
        @Body request: SaveMappingRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    // Audit Execution
    @POST("api/audits/create")
    suspend fun createAudit(
        @Body request: AuditCreateRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<AuditCreateResponse>

    @GET("api/audits/detail")
    suspend fun getAuditDetail(
        @retrofit2.http.Query("id") auditId: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<AuditDetailResponse>

    @POST("api/audits/update")
    suspend fun updateAudit(
        @Body request: AuditUpdateRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<AuditUpdateResponse>

    @retrofit2.http.Multipart
    @POST("api/audits/upload-photo")
    suspend fun uploadAuditPhoto(
        @retrofit2.http.Part("audit_id") auditId: okhttp3.RequestBody,
        @retrofit2.http.Part("response_id") responseId: okhttp3.RequestBody,
        @retrofit2.http.Part photo: okhttp3.MultipartBody.Part,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<AuditUpdateResponse>

    @POST("api/audits/update-photo")
    suspend fun updateAuditPhoto(
        @Body request: AuditPhotoUpdateData,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<AuditUpdateResponse>

    @POST("api/audits/delete-photo")
    suspend fun deleteAuditPhoto(
        @Body request: AuditDeletePhotoRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<AuditUpdateResponse>

    @retrofit2.http.Multipart
    @POST("api/audits/submit")
    suspend fun submitAudit(
        @retrofit2.http.Part("audit_id") auditId: okhttp3.RequestBody,
        @retrofit2.http.Part("auditee_name") auditeeName: okhttp3.RequestBody,
        @retrofit2.http.Part verificationPhoto: okhttp3.MultipartBody.Part,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<AuditSubmitResponse>

    @GET("api/audits")
    suspend fun getAudits(
        @retrofit2.http.Query("user_id") userId: Int,
        @retrofit2.http.Query("department_id") departmentId: Int?,
        @retrofit2.http.Query("date_from") dateFrom: String?,
        @retrofit2.http.Query("date_to") dateTo: String?
    ): Response<AuditHistoryResponse>

    @POST("api/audits/send-email")
    suspend fun sendAuditEmail(
        @Body request: SendEmailRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    @POST("api/audits/delete")
    suspend fun deleteAudit(
        @Body request: GenericIdRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<AuditUpdateResponse>

    // User Management
    @POST("api/user/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<UserActionResponse>

    @POST("api/user/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    @GET("api/users")
    suspend fun getUsers(
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<UserListResponse>

    @POST("api/users")
    suspend fun addUser(
        @Body request: AddUserRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<UserActionResponse>

    @POST("api/users/{id}/level")
    suspend fun updateLevel(
        @Path("id") id: Int,
        @Body request: UpdateLevelRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<UserActionResponse>

    @POST("api/users/{id}/delete")
    suspend fun deleteUser(
        @Path("id") id: Int,
        @Body request: DeleteUserRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    // Stock Management
    @GET("api/stock/categories")
    suspend fun getStockCategories(
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockCategoryListResponse>

    @POST("api/stock/categories")
    suspend fun createStockCategory(
        @Body request: StockCategoryRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockCategoryResponse>

    @POST("api/stock/categories/{id}")
    suspend fun updateStockCategory(
        @Path("id") id: Int,
        @Body request: StockCategoryRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockCategoryResponse>

    @POST("api/stock/categories/{id}")
    suspend fun deleteStockCategory(
        @Path("id") id: Int,
        @Body request: StockDeleteRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    @POST("api/stock/items")
    suspend fun createStockItem(
        @Body request: StockItemRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockItemResponse>

    @POST("api/stock/items/{id}")
    suspend fun deleteStockItem(
        @Path("id") id: Int,
        @Body request: StockDeleteRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    @POST("api/stock/items/reorder")
    suspend fun reorderStockItems(
        @Body request: StockReorderRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    // Stock Detail Items
    @GET("api/stock/categories/{categoryId}/items")
    suspend fun getStockItems(
        @Path("categoryId") categoryId: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockCategoryResponse>

    // Stock Department Mapping
    @GET("api/stock/departments")
    suspend fun getStockDepartments(
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<DepartmentListResponse>

    @GET("api/stock/departments/{id}/mapping")
    suspend fun getStockDepartmentMapping(
        @Path("id") id: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockDepartmentMappingResponse>

    @POST("api/stock/departments/mapping")
    suspend fun saveStockDepartmentMapping(
        @Body request: SaveStockMappingRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    // Stock Opname
    @POST("api/stock/opname/create")
    suspend fun createStockOpname(
        @Body request: StockOpnameCreateRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockOpnameCreateResponse>

    @GET("api/stock/opname/detail/{id}")
    suspend fun getStockOpnameDetail(
        @Path("id") id: Int,
        @retrofit2.http.Query("auditor_id") auditorId: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockOpnameDetailResponse>

    @POST("api/stock/opname/update")
    suspend fun updateStockOpname(
        @Body request: StockOpnameUpdateRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockOpnameUpdateResponse>

    @retrofit2.http.Multipart
    @POST("api/stock/opname/upload-photo")
    suspend fun uploadStockOpnamePhoto(
        @retrofit2.http.Part("response_id") responseId: okhttp3.RequestBody,
        @retrofit2.http.Part photo: okhttp3.MultipartBody.Part,
        @retrofit2.http.Part("remark") remark: okhttp3.RequestBody? = null,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockOpnameUpdateResponse>

    @POST("api/stock/opname/update-photo")
    suspend fun updateStockOpnamePhoto(
        @Body request: StockOpnamePhotoUpdateRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockOpnameUpdateResponse>

    @POST("api/stock/opname/delete-photo")
    suspend fun deleteStockOpnamePhoto(
        @Body request: StockOpnamePhotoDeleteRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockOpnameUpdateResponse>

    @retrofit2.http.Multipart
    @POST("api/stock/opname/submit")
    suspend fun submitStockOpname(
        @retrofit2.http.Part("audit_id") auditId: okhttp3.RequestBody,
        @retrofit2.http.Part("auditee_name") auditeeName: okhttp3.RequestBody,
        @retrofit2.http.Part verificationPhoto: okhttp3.MultipartBody.Part,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<StockOpnameUpdateResponse>

    @GET("api/stock/opname")
    suspend fun getStockOpnameHistories(
        @retrofit2.http.Query("user_id") userId: Int,
        @retrofit2.http.Query("auditor_id") auditorId: Int,
        @retrofit2.http.Query("department_id") departmentId: Int?,
        @retrofit2.http.Query("date_from") dateFrom: String?,
        @retrofit2.http.Query("date_to") dateTo: String?,
        @retrofit2.http.Query("page") page: Int?
    ): Response<StockOpnameHistoryResponse>

    @retrofit2.http.Streaming
    @GET("api/stock/opname/{id}/export-pdf")
    suspend fun exportStockOpnamePdf(
        @Path("id") id: Int,
        @retrofit2.http.Query("auditor_id") auditorId: Int,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<okhttp3.ResponseBody>

    @POST("api/stock/opname/send-email")
    suspend fun sendStockOpnameEmail(
        @Body request: SendEmailRequest,
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<GenericResponse>

    // Company Lifecycle Management
    @GET("api/company/status")
    suspend fun getCompanyStatus(
        @retrofit2.http.Query("user_id") userId: Int
    ): Response<CompanyLifecycleStateResponse>

    @POST("api/company/deactivate")
    suspend fun deactivateCompany(
        @Body request: DeactivateCompanyRequest
    ): Response<CompanyLifecycleStateResponse>

    @POST("api/company/reactivate")
    suspend fun reactivateCompany(
        @Body request: ReactivateCompanyRequest
    ): Response<CompanyLifecycleStateResponse>

    @POST("api/company/delete-request")
    suspend fun requestCompanyDeletion(
        @Body request: RequestCompanyDeletionRequest
    ): Response<CompanyLifecycleStateResponse>

    @POST("api/company/cancel-deletion")
    suspend fun cancelCompanyDeletion(
        @Body request: CancelCompanyDeletionRequest
    ): Response<CompanyLifecycleStateResponse>

}
