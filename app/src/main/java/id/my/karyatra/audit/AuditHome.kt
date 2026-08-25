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
        Screen.ManageUsers.route -> "Manajemen Pengguna"
        Screen.ManageDepartments.route -> "Manajemen Departemen"
        else -> "Audit Karyatra"
    }

    val isManageScreen = currentRoute == Screen.ManageUsers.route || currentRoute == Screen.ManageDepartments.route

    Scaffold(
        topBar = {
            Header(
                title = headerTitle,
                onBack = if (isManageScreen) {
                    { navController.popBackStack() }
                } else null,
                centerTitle = isManageScreen
            )
        },
        bottomBar = {
            if (!isManageScreen) {
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

@Composable
fun Header(
    title: String,
    onBack: (() -> Unit)? = null,
    centerTitle: Boolean = false
) {
    Surface(
        color = Color(0xFFB63352),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = if (onBack != null && centerTitle) 48.dp else 0.dp),
                textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start
            )
        }
    }
}
