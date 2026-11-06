package com.example.project_auth.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project_auth.data.AuthRepository
import com.example.project_auth.ui.auth.LoginScreen
import com.example.project_auth.ui.auth.RegisterScreen
import com.example.project_auth.ui.history.HistoryScreen
import com.example.project_auth.ui.home.HomeScreen
import com.example.project_auth.ui.stats.StatsScreen
import com.example.project_auth.ui.watchlist.WatchlistScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val WATCHLIST = "watchlist"
    const val STATS = "stats"
    const val HISTORY = "history/{symbol}"

    fun history(symbol: String) = "history/$symbol"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val startDestination = if (authRepository.currentUser != null) Routes.HOME else Routes.LOGIN

    LaunchedEffect(Unit) {
        authRepository.userFlow.collect { user ->
            val target = if (user != null) Routes.HOME else Routes.LOGIN
            if (navController.currentDestination?.route.isAuthGroupOf(target)) return@collect
            navController.navigate(target) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.LOGIN) {
            LoginScreen(onGoToRegister = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onGoToLogin = { navController.popBackStack() })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenWatchlist = { navController.navigate(Routes.WATCHLIST) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenHistory = { navController.navigate(Routes.history(it)) },
            )
        }
        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.WATCHLIST) {
            WatchlistScreen(
                onBack = { navController.popBackStack() },
                onOpenHistory = { navController.navigate(Routes.history(it)) },
            )
        }
        composable(
            route = Routes.HISTORY,
            arguments = listOf(navArgument("symbol") { type = NavType.StringType }),
        ) { entry ->
            HistoryScreen(
                symbol = entry.arguments?.getString("symbol").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/** True when [this] route already belongs to the group [target] navigates to. */
private fun String?.isAuthGroupOf(target: String): Boolean = when (target) {
    Routes.HOME -> this == Routes.HOME
    else -> this == Routes.LOGIN || this == Routes.REGISTER
}
