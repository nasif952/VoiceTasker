package com.voicetasker.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.voicetasker.ui.navigation.NavRoute
import com.voicetasker.ui.screens.auth.LoginScreen
import com.voicetasker.ui.screens.auth.RegisterScreen
import com.voicetasker.ui.screens.home.HomeScreen
import com.voicetasker.ui.screens.task.TaskCreateScreen
import com.voicetasker.ui.screens.task.TaskEditScreen

/**
 * Main app-level Composable that sets up navigation.
 */
@Composable
fun VoiceTaskerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoute.Auth.route
    ) {
        navigation(
            route = NavRoute.Auth.route,
            startDestination = NavRoute.AuthLogin.route
        ) {
            composable(NavRoute.AuthLogin.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoute.Home.route) {
                            popUpTo(NavRoute.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoute.AuthRegister.route)
                    }
                )
            }

            composable(NavRoute.AuthRegister.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(NavRoute.AuthLogin.route) {
                            popUpTo(NavRoute.AuthRegister.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(NavRoute.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(NavRoute.TaskCreate.route) {
            TaskCreateScreen(navController = navController)
        }

        composable(
            route = NavRoute.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) {
            TaskEditScreen(navController = navController)
        }
    }
}
