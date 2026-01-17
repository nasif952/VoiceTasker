package com.voicetasker.ui.navigation

sealed class NavRoute(val route: String) {
    data object Auth : NavRoute("auth")
    data object AuthLogin : NavRoute("auth/login")
    data object AuthRegister : NavRoute("auth/register")
    data object AuthForgotPassword : NavRoute("auth/forgot_password")

    data object Home : NavRoute("home")
    data object TaskCreate : NavRoute("home/task_create")
    data object TaskDetail : NavRoute("home/task/{taskId}") {
        fun createRoute(taskId: String) = "home/task/$taskId"
    }

    data object Reminder : NavRoute("reminder")
    data object Settings : NavRoute("settings")
}
