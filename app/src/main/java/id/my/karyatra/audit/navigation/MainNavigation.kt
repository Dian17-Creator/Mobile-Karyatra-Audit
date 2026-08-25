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
import androidx.compose.material.icons.filled.People
import androidx.navigation.compose.composable
import id.my.karyatra.audit.ui.home.AuditHomeScreen
import id.my.karyatra.audit.ui.profile.ProfileScreen
import id.my.karyatra.audit.ui.stock.StockScreen
import id.my.karyatra.audit.ui.stock.StockScreen

import id.my.karyatra.audit.ui.profile.ManageUsersScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Stock : Screen("stock", "Stock", Icons.Default.Inventory2)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object ManageUsers : Screen("manage_users", "Manage Users", Icons.Default.People)
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
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            AuditHomeScreen(
                username = username,
                onManageUsers = { navController.navigate(Screen.ManageUsers.route) }
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
    }
}
