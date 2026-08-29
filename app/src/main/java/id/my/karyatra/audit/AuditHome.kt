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
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import id.my.karyatra.audit.data.ApiResult
import id.my.karyatra.audit.data.CompanyDeactivatedEventBus
import id.my.karyatra.audit.data.repository.CompanyLifecycleRepository

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
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val user = sessionManager.getUser()
    val userId = remember { user?.id ?: -1 }
    val isOwner = remember { user?.is_owner ?: false }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var deactivationMessage by remember { mutableStateOf<String?>(null) }
    var isCompanyInactive by remember { mutableStateOf(false) }

    var isInitialStatusChecked by remember { mutableStateOf(false) }
    var initialStartDestination by remember { mutableStateOf(Screen.Home.route) }

    LaunchedEffect(userId) {
        if (userId != -1) {
            try {
                val repository = CompanyLifecycleRepository()
                val result = repository.getCompanyStatus(userId)
                if (result is ApiResult.Success) {
                    val isInactive = result.data.data?.isCompanyInactive ?: false
                    isCompanyInactive = isInactive
                    if (isInactive) {
                        if (isOwner) {
                            initialStartDestination = Screen.CompanyStatus.route
                        } else {
                            deactivationMessage = "Akun perusahaan Anda telah dinonaktifkan oleh Owner."
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore initial check error
            }
        }
        isInitialStatusChecked = true
    }

    LaunchedEffect(Unit) {
        CompanyDeactivatedEventBus.deactivatedEvent.collect { message ->
            if (isOwner) {
                isCompanyInactive = true
                if (currentRoute != Screen.CompanyStatus.route) {
                    navController.navigate(Screen.CompanyStatus.route) {
                        launchSingleTop = true
                    }
                }
            } else {
                deactivationMessage = message
            }
        }
    }

    if (!isInitialStatusChecked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFB63352))
        }
        return
    }

    val headerTitle = when (currentRoute) {
        Screen.Home.route -> "Auditra"
        Screen.Stock.route -> "Stok Opname"
        Screen.Profile.route -> "Profil Pengguna"
        Screen.ManageUsers.route -> "Kelola User"
        Screen.ManageDepartments.route -> "Kelola Departemen"
        Screen.ManageCompany.route -> "Kelola Perusahaan"
        Screen.CompanyStatus.route -> "Status Perusahaan"
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

    val isAuditProsesScreen = currentRoute?.startsWith(Screen.AuditProses.route) == true
    val isChildScreen = !isTopLevelScreen && currentRoute != null
    val shouldHideBack = currentRoute == Screen.CompanyStatus.route && isCompanyInactive

    Scaffold(
        topBar = {
            if (!isAuditProsesScreen) {
                Header(
                    title = headerTitle,
                    onBack = if (isChildScreen && !shouldHideBack) {
                        { navController.popBackStack() }
                    } else null,
                    centerTitle = isChildScreen
                )
            }
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
            onCompanyStatusLoaded = { isInactive -> isCompanyInactive = isInactive },
            startDestination = initialStartDestination,
            modifier = Modifier.padding(
                top = if (isAuditProsesScreen) 0.dp else innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
        )
    }

    if (deactivationMessage != null) {
        AlertDialog(
            onDismissRequest = { /* Modal locking access */ },
            title = {
                Text(
                    text = "Akun Perusahaan Dinonaktifkan",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Text(
                    text = deactivationMessage ?: "Akun perusahaan Anda telah dinonaktifkan oleh Owner. Silakan hubungi Owner perusahaan Anda untuk mengaktifkan kembali akses."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deactivationMessage = null
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
                ) {
                    Text("Keluar (Logout)")
                }
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        )
    }
}
