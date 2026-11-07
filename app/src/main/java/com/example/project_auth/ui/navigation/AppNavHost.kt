package com.example.project_auth.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project_auth.data.AuthRepository
import com.example.project_auth.ui.auth.LoginScreen
import com.example.project_auth.ui.auth.RegisterScreen
import com.example.project_auth.ui.history.StockDetailModal
import com.example.project_auth.ui.home.HomeScreen
import com.example.project_auth.ui.settings.SettingsScreen
import com.example.project_auth.ui.stats.StatsScreen
import com.example.project_auth.ui.watchlist.WatchlistScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val WATCHLIST = "watchlist"
    const val SETTINGS = "settings"
    const val STATS = "stats"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val startDestination = if (authRepository.currentUser != null) Routes.HOME else Routes.LOGIN

    // The stock detail is a modal over whichever screen opened it, not a navigation destination.
    var detailSymbol by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authRepository.userFlow.collect { user ->
            val target = if (user != null) Routes.HOME else Routes.LOGIN
            if (navController.currentDestination?.route.isAuthGroupOf(target)) return@collect
            detailSymbol = null
            navController.navigate(target) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // The modal sits outside the Scaffold so its overlay also covers the system bar areas.
    Box(modifier.fillMaxSize()) {
        Scaffold(Modifier.fillMaxSize()) { innerPadding ->
            NavHost(navController, startDestination = startDestination, modifier = Modifier.padding(innerPadding)) {
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
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onOpenDetail = { detailSymbol = it },
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.STATS) {
                    StatsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.WATCHLIST) {
                    WatchlistScreen(
                        onBack = { navController.popBackStack() },
                        onOpenDetail = { detailSymbol = it },
                    )
                }
            }
        }
        StockDetailModal(symbol = detailSymbol, onDismiss = { detailSymbol = null })
    }
}

/** True when [this] route already belongs to the group [target] navigates to. */
private fun String?.isAuthGroupOf(target: String): Boolean = when (target) {
    Routes.HOME -> this == Routes.HOME
    else -> this == Routes.LOGIN || this == Routes.REGISTER
}
