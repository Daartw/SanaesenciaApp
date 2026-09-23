package com.example.sanaesencia_app.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Patient : Screen("patient")
    data object Therapist : Screen("therapist")
    data object Admin : Screen("admin")
    data object Profile : Screen("profile")
    data object Agenda : Screen("agenda")
    data object Historial : Screen("historial")
}
