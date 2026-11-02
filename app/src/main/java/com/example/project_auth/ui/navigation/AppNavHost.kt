package com.example.project_auth.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project_auth.data.AuthRepository
import com.example.project_auth.ui.auth.LoginScreen
import com.example.project_auth.ui.auth.RegisterScreen
import com.example.project_auth.ui.home.HomeScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
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
            HomeScreen()
        }
    }
}

/** True when [this] route already belongs to the group [target] navigates to. */
private fun String?.isAuthGroupOf(target: String): Boolean = when (target) {
    Routes.HOME -> this == Routes.HOME
    else -> this == Routes.LOGIN || this == Routes.REGISTER
}
