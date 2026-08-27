package id.my.karyatra.audit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.my.karyatra.audit.component.BottomBar
import id.my.karyatra.audit.data.SessionManager
import id.my.karyatra.audit.navigation.MainNavigation
import id.my.karyatra.audit.navigation.Screen
import id.my.karyatra.audit.ui.theme.Karyatra_AuditTheme
import id.my.karyatra.audit.component.Header
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

class AuditHome : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        if (user == null) {
            startActivity(Intent(this, AuditLogin::class.java))
            finish()
            return
        }

        setContent {
            Karyatra_AuditTheme {
                MainContainer(
                    username = user.name,
                    onLogout = {
                        sessionManager.clearSession()
                        startActivity(Intent(this, AuditLogin::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun MainContainer(
    username: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val headerTitle = when (currentRoute) {
        Screen.Home.route -> "Audit Karyatra"
        Screen.Stock.route -> "Stok Opname"
        Screen.Profile.route -> "Profil Pengguna"
        Screen.ManageUsers.route -> "Kelola User"
        Screen.ManageDepartments.route -> "Kelola Departemen"
        Screen.AuditPertanyaan.route -> "Kategori & Pertanyaan"
        Screen.AuditDepartemen.route -> "Pemetaan Departemen"
        Screen.AuditProses.route + "?audit_id={audit_id}" -> "Audit Proses"
        Screen.AuditHasil.route -> "Hasil Audit"
        Screen.Subscription.route -> "Paket Berlangganan"
        else -> "Audit Karyatra"
    }

    val isTopLevelScreen = currentRoute == Screen.Home.route || 
                           currentRoute == Screen.Stock.route || 
                           currentRoute == Screen.Profile.route

    val isChildScreen = !isTopLevelScreen && currentRoute != null

    Scaffold(
        topBar = {
            Header(
                title = headerTitle,
                onBack = if (isChildScreen) {
                    { navController.popBackStack() }
                } else null,
                centerTitle = isChildScreen
            )
        },
        bottomBar = {
            if (isTopLevelScreen) {
                BottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        MainNavigation(
            navController = navController,
            username = username,
            onLogout = onLogout,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
