package id.my.karyatra.audit.data

import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

class CompanyStatusInterceptor(
    private val onCompanyDeactivated: (message: String) -> Unit = { CompanyDeactivatedEventBus.emitDeactivated(it) }
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 403) {
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            try {
                val json = JSONObject(responseBody)
                val message = json.optString("message", "")
                if (message.contains("dinonaktifkan", ignoreCase = true)) {
                    onCompanyDeactivated(message)
                }
            } catch (e: Exception) {
                // Ignore json parse exception
            }
        }
        return response
    }
}
