package id.my.karyatra.audit.data

import com.google.gson.annotations.SerializedName

data class DashboardSummaryData(
    @SerializedName("total_kategori") val totalKategori: Int,
    @SerializedName("total_pertanyaan") val totalPertanyaan: Int
)

data class DashboardSummaryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: DashboardSummaryData?
)
