package id.my.karyatra.audit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.compose.composable
import id.my.karyatra.audit.ui.home.AuditHomeScreen
import id.my.karyatra.audit.ui.profile.ProfileScreen
import id.my.karyatra.audit.ui.stock.StockScreen
import id.my.karyatra.audit.ui.stock.StockScreen

import id.my.karyatra.audit.ui.profile.ManageUsersScreen
import id.my.karyatra.audit.ui.profile.ManageDepartmentsScreen
import id.my.karyatra.audit.AuditPertanyaanScreen
import id.my.karyatra.audit.AuditDepartemenScreen
import id.my.karyatra.audit.AuditExecutionScreen
import id.my.karyatra.audit.AuditHasilScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Stock : Screen("stock", "Stock", Icons.Default.Inventory2)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object ManageUsers : Screen("manage_users", "Kelola User", Icons.Default.People)
    object ManageDepartments : Screen("manage_departments", "Kelola Departemen", Icons.Default.Settings)
    object AuditPertanyaan : Screen("audit_pertanyaan", "Kategori & Pertanyaan", Icons.Default.Settings)
    object AuditDepartemen : Screen("audit_departemen", "Pemetaan Departemen", Icons.Default.Settings)
    object AuditProses : Screen("audit_proses", "Audit", Icons.Default.Settings)
    object AuditHasil : Screen("audit_hasil", "Hasil Audit", Icons.Default.Settings)
}

@Composable
fun MainNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    username: String,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn(animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn(animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Screen.Home.route) {
            AuditHomeScreen(
                username = username,
                onManageUsers = { navController.navigate(Screen.ManageUsers.route) },
                onManageDepartments = { navController.navigate(Screen.ManageDepartments.route) },
                onKategoriPertanyaan = { navController.navigate(Screen.AuditPertanyaan.route) },
                onPemetaanDepartemen = { navController.navigate(Screen.AuditDepartemen.route) },
                onAudit = { auditId -> 
                    navController.navigate(Screen.AuditProses.route + "?audit_id=$auditId") 
                },
                onHasilAudit = { navController.navigate(Screen.AuditHasil.route) }
            )
        }
        composable(Screen.Stock.route) {
            StockScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(onLogout = onLogout)
        }
        composable(Screen.ManageUsers.route) {
            ManageUsersScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ManageDepartments.route) {
            ManageDepartmentsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AuditPertanyaan.route) {
            AuditPertanyaanScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AuditDepartemen.route) {
            AuditDepartemenScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.AuditProses.route + "?audit_id={audit_id}",
            arguments = listOf(navArgument("audit_id") { 
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val auditId = backStackEntry.arguments?.getInt("audit_id") ?: -1
            AuditExecutionScreen(auditId = auditId, onBack = { navController.popBackStack() })
        }
        composable(Screen.AuditHasil.route) {
            AuditHasilScreen(onBack = { navController.popBackStack() })
        }
    }
}
