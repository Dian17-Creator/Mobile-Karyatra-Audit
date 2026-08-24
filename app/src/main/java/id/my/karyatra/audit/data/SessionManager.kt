package id.my.karyatra.audit.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "audit_session"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_COMPANY = "company"
        private const val KEY_DEPT_ID = "dept_id"
        private const val KEY_DEPT_NAME = "dept_name"
        private const val KEY_ADMIN = "admin"
        private const val KEY_SUPER_ADMIN = "super_admin"
        private const val KEY_HRD = "hrd"
        private const val KEY_AUDIT = "audit"
        private const val KEY_IS_OWNER = "is_owner"
        private const val KEY_IS_EMAIL_VERIFIED = "is_email_verified"
        private const val KEY_IS_TRIAL = "is_trial"
        private const val KEY_EMAIL_VERIFIED_DATE = "email_verified_date"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REMEMBER_ME = "remember_me"
    }

    fun saveSession(user: UserData, rememberMe: Boolean) {
        prefs.edit().apply {
            putInt(KEY_ID, user.id)
            putString(KEY_NAME, user.name)
            putString(KEY_EMAIL, user.email)
            putString(KEY_COMPANY, user.company)
            putInt(KEY_DEPT_ID, user.department_id ?: -1)
            putString(KEY_DEPT_NAME, user.department_name)
            putBoolean(KEY_ADMIN, user.role.admin)
            putBoolean(KEY_SUPER_ADMIN, user.role.superAdmin)
            putBoolean(KEY_HRD, user.role.hrd)
            putBoolean(KEY_AUDIT, user.role.audit)
            putBoolean(KEY_IS_OWNER, user.is_owner ?: false)
            putBoolean(KEY_IS_EMAIL_VERIFIED, user.is_email_verified ?: false)
            putBoolean(KEY_IS_TRIAL, user.is_trial ?: false)
            putString(KEY_EMAIL_VERIFIED_DATE, user.demailverified)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isRememberMe(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun getUser(): UserData? {
        if (!isLoggedIn()) return null

        val role = UserRole(
            admin = prefs.getBoolean(KEY_ADMIN, false),
            superAdmin = prefs.getBoolean(KEY_SUPER_ADMIN, false),
            hrd = prefs.getBoolean(KEY_HRD, false),
            audit = prefs.getBoolean(KEY_AUDIT, false)
        )

        val deptId = prefs.getInt(KEY_DEPT_ID, -1)

        return UserData(
            id = prefs.getInt(KEY_ID, -1),
            name = prefs.getString(KEY_NAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            company = prefs.getString(KEY_COMPANY, ""),
            department_id = if (deptId == -1) null else deptId,
            department_name = prefs.getString(KEY_DEPT_NAME, ""),
            role = role,
            is_owner = prefs.getBoolean(KEY_IS_OWNER, false),
            is_email_verified = prefs.getBoolean(KEY_IS_EMAIL_VERIFIED, false),
            is_trial = prefs.getBoolean(KEY_IS_TRIAL, false),
            demailverified = prefs.getString(KEY_EMAIL_VERIFIED_DATE, null)
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
