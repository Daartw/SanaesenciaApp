package com.example.sanaesencia_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sanaesencia_app.ui.agenda.AgendaScreen
import com.example.sanaesencia_app.ui.catalogo.CatalogoScreen
import com.example.sanaesencia_app.ui.historial.HistorialScreen
import com.example.sanaesencia_app.ui.login.LoginScreen

@Composable
fun SanaesenciaNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Catalogo.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Catalogo.route) {
            CatalogoScreen(
                onIrAAgenda = { navController.navigate(Screen.Agenda.route) },
                onIrAHistorial = { navController.navigate(Screen.Historial.route) }
            )
        }

        composable(Screen.Agenda.route) {
            AgendaScreen(onVolver = { navController.popBackStack() })
        }

        composable(Screen.Historial.route) {
            HistorialScreen(onVolver = { navController.popBackStack() })
        }
    }
}
