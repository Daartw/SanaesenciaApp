package com.example.sanaesencia_app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Catalogo : Screen("catalogo")
    object Agenda : Screen("agenda")
    object Historial : Screen("historial")
}
