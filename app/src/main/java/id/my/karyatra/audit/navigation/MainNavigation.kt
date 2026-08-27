package id.my.karyatra.audit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
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

import id.my.karyatra.audit.ui.profile.ManageUsersScreen
import id.my.karyatra.audit.ui.profile.ManageDepartmentsScreen
import id.my.karyatra.audit.ui.subscription.SubscriptionScreen
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
    object Subscription : Screen("subscription", "Berlangganan", Icons.Default.WorkspacePremium)
}

@Composable
fun MainNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    username: String,
    onLogout: () -> Unit
) {
    val mainTabs = listOf(Screen.Home.route, Screen.Stock.route, Screen.Profile.route)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            val fromIndex = mainTabs.indexOf(initialState.destination.route)
            val toIndex = mainTabs.indexOf(targetState.destination.route)
            
            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                if (toIndex > fromIndex) {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn()
                } else {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn()
                }
            } else {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn()
            }
        },
        exitTransition = {
            val fromIndex = mainTabs.indexOf(initialState.destination.route)
            val toIndex = mainTabs.indexOf(targetState.destination.route)

            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                if (toIndex > fromIndex) {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut()
                } else {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut()
                }
            } else {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut()
            }
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut()
        }
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
                onHasilAudit = { navController.navigate(Screen.AuditHasil.route) },
                onSubscription = { navController.navigate(Screen.Subscription.route) }
            )
        }
        composable(Screen.Stock.route) {
            StockScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = onLogout,
                onUpgrade = { navController.navigate(Screen.Subscription.route) }
            )
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
        composable(Screen.Subscription.route) {
            SubscriptionScreen(onBack = { navController.popBackStack() })
        }
    }
}
