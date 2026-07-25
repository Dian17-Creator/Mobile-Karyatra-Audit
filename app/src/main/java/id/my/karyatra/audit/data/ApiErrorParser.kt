package id.my.karyatra.audit.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

object ApiErrorParser {

    fun parseError(errorBody: ResponseBody?): String {
        return try {
            val jsonString = errorBody?.string()
            if (jsonString.isNullOrEmpty()) {
                return "Terjadi kesalahan. Silakan coba lagi."
            }

            val type = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = Gson().fromJson(jsonString, type)

            errorMap["message"]?.toString() ?: "Terjadi kesalahan. Silakan coba lagi."
        } catch (e: Exception) {
            "Terjadi kesalahan. Silakan coba lagi."
        }
    }
}
