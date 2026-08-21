package id.my.karyatra.audit.component

import android.app.DatePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object UiUtils {
    fun formatDateIndo(dateStr: String): String {
        if (dateStr.isEmpty()) return "-"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun showDatePicker(
        context: Context,
        currentValue: String,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        if (currentValue.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.parse(currentValue)?.let { calendar.time = it }
            } catch (_: Exception) {}
        }

        DatePickerDialog(
            context,
            { _, y, m, d ->
                val cal = Calendar.getInstance()
                cal.set(y, m, d)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onDateSelected(sdf.format(cal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
