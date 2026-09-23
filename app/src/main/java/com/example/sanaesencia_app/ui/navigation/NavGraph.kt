package com.example.sanaesencia_app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.sanaesencia_app.domain.model.Profesional
import com.example.sanaesencia_app.domain.model.RolUsuario
import com.example.sanaesencia_app.session.DemoSession
import com.example.sanaesencia_app.ui.admin.AdminDashboard
import com.example.sanaesencia_app.ui.agenda.AgendaScreen
import com.example.sanaesencia_app.ui.agendamiento.PerfilProfesionalScreen
import com.example.sanaesencia_app.ui.historial.HistorialScreen
import com.example.sanaesencia_app.ui.login.LoginScreen
import com.example.sanaesencia_app.ui.paciente.PacienteDashboard
import com.example.sanaesencia_app.ui.terapeuta.TerapeutaDashboard

@Composable
fun SanaesenciaNavGraph(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    var selectedProfessional by remember { mutableStateOf<Profesional?>(null) }

    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen { role ->
                DemoSession.rol = role
                DemoSession.usuarioId = if (role == RolUsuario.PACIENTE) "P001" else "P001"
                val destination = when (role) { RolUsuario.PACIENTE -> Screen.Patient.route; RolUsuario.TERAPEUTA -> Screen.Therapist.route; RolUsuario.ADMIN -> Screen.Admin.route }
                navController.navigate(destination) { popUpTo(Screen.Login.route) { inclusive = true } }
            }
        }
        composable(Screen.Patient.route) {
            PacienteDashboard(context, onProfesional = { selectedProfessional = it; navController.navigate(Screen.Profile.route) }, onAgenda = { navController.navigate(Screen.Agenda.route) }, onHistorial = { navController.navigate(Screen.Historial.route) })
        }
        composable(Screen.Therapist.route) { TerapeutaDashboard(context) { navController.navigate(Screen.Login.route) { popUpTo(navController.graph.startDestinationId) { inclusive = true } } } }
        composable(Screen.Admin.route) { AdminDashboard(context) { navController.navigate(Screen.Login.route) { popUpTo(navController.graph.startDestinationId) { inclusive = true } } } }
        composable(Screen.Profile.route) { selectedProfessional?.let { PerfilProfesionalScreen(it, context) { navController.popBackStack() } } }
        composable(Screen.Agenda.route) { AgendaScreen(context) { navController.popBackStack() } }
        composable(Screen.Historial.route) { HistorialScreen(context) { navController.popBackStack() } }
    }
}
