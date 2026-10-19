package cl.sanaesencia.app.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import cl.sanaesencia.app.AppContainer
import cl.sanaesencia.app.domain.model.RolUsuario
import cl.sanaesencia.app.ui.admin.AdminDashboardScreen
import cl.sanaesencia.app.ui.auth.LoginScreen
import cl.sanaesencia.app.ui.carrito.CartScreen
import cl.sanaesencia.app.ui.citas.AgendarScreen
import cl.sanaesencia.app.ui.citas.CitasScreen
import cl.sanaesencia.app.ui.components.SanaesenciaBottomBar
import cl.sanaesencia.app.ui.home.HomeScreen
import cl.sanaesencia.app.ui.home.ProfessionalHomeScreen
import cl.sanaesencia.app.ui.perfil.PerfilScreen
import cl.sanaesencia.app.ui.profesionales.ProfesionalesScreen
import cl.sanaesencia.app.ui.tienda.TiendaScreen

import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFESIONALES = "profesionales"
    const val TIENDA = "tienda"
    const val PERFIL = "perfil"
    const val ADMIN = "admin"
    const val AGENDA = "agenda"
    const val CITAS = "citas"
    const val CARRITO = "carrito"
    const val AGENDAR = "agendar/{profesionalId}"
    const val PROF = PROFESIONALES
    const val PROFESSIONAL_HOME = AGENDA
    const val AGENDA_DETALLE = "agenda_detalle"
}

@Composable
fun SanaesenciaNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    fun logout() {
        scope.launch {
            Log.d("SanaesenciaLogout", "Inicio del cierre de sesión")

            // 1. Resetear el estado del LoginViewModel para evitar la redirección automática al volver al Login.
            container.loginViewModel.resetState()

            // 2. Eliminar la sesión guardada en DataStore.
            try {
                container.sessionManager.clear()
                Log.d(
                    "SanaesenciaLogout",
                    "Sesión eliminada correctamente"
                )
            } catch (error: Exception) {
                Log.e(
                    "SanaesenciaLogout",
                    "Error al eliminar la sesión",
                    error
                )
                return@launch
            }

            // 3. Volver al Login y limpiar el historial de navegación.
            try {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }

                Log.d(
                    "SanaesenciaLogout",
                    "Navegación al Login solicitada"
                )
            } catch (error: Exception) {
                Log.e(
                    "SanaesenciaLogout",
                    "Error al navegar al Login",
                    error
                )
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // LOGIN
        composable(Routes.LOGIN) {
            LoginScreen(
                vm = container.loginViewModel,
                onLogged = { response ->
                    val destination = when (response.rol) {
                        RolUsuario.ADMIN -> Routes.ADMIN
                        RolUsuario.PROFESIONAL -> Routes.AGENDA
                        RolUsuario.PACIENTE -> Routes.HOME
                    }

                    container.loginViewModel.resetState()

                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // INICIO DEL PACIENTE
        composable(Routes.HOME) {
            ScreenWithBottomBar(
                navController = navController,
                role = RolUsuario.PACIENTE
            ) {
                HomeScreen(
                    onProf = {
                        navController.navigate(Routes.PROFESIONALES)
                    },
                    onShop = {
                        navController.navigate(Routes.TIENDA)
                    },
                    onProfile = {
                        navController.navigate(Routes.PERFIL)
                    },
                    onAppointments = {
                        navController.navigate(Routes.CITAS)
                    }
                )
            }
        }

        // PROFESIONALES
        composable(Routes.PROFESIONALES) {
            ScreenWithBottomBar(
                navController = navController,
                role = RolUsuario.PACIENTE
            ) {
                ProfesionalesScreen(
                    vm = container.catalogViewModel,
                    onAgendar = { id ->
                        navController.navigate("agendar/$id")
                    }
                )
            }
        }

        // TIENDA
        composable(Routes.TIENDA) {
            ScreenWithBottomBar(
                navController = navController,
                role = RolUsuario.PACIENTE
            ) {
                TiendaScreen(
                    vm = container.catalogViewModel,
                    cartVm = container.cartViewModel,
                    onCart = {
                        navController.navigate(Routes.CARRITO)
                    },
                    isStockBajo = { stock ->
                        container.verificarStockBajo(stock)
                    }
                )
            }
        }

        // CARRITO
        composable(Routes.CARRITO) {
            CartScreen(
                vm = container.cartViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // CITAS
        composable(Routes.CITAS) {
            ScreenWithBottomBar(
                navController = navController,
                role = RolUsuario.PACIENTE
            ) {
                CitasScreen(
                    vm = container.citasViewModel,
                    onNew = {
                        navController.navigate(Routes.PROFESIONALES)
                    }
                )
            }
        }

        // AGENDAR UNA CITA
        composable(
            route = Routes.AGENDAR,
            arguments = listOf(
                navArgument("profesionalId") {
                    type = NavType.LongType
                }
            )
        ) { entry ->
            val profesionalId =
                entry.arguments?.getLong("profesionalId")
                    ?: return@composable

            AgendarScreen(
                profesionalId = profesionalId,
                vm = container.citasViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // PERFIL
        composable(Routes.PERFIL) {
            ScreenWithBottomBar(
                navController = navController,
                role = RolUsuario.PACIENTE
            ) {
                PerfilScreen(
                    vm = container.perfilViewModel,
                    onLogout = {
                        logout()
                    }
                )
            }
        }

        // ADMINISTRADOR
        composable(Routes.ADMIN) {
            AdminDashboardScreen(
                vm = container.adminViewModel,
                onLogout = {
                    Log.d(
                        "SanaesenciaLogout",
                        "AdminDashboard llamó a onLogout"
                    )
                    logout()
                }
            )
        }

        // INICIO DEL PROFESIONAL
        composable(Routes.AGENDA) {
            ScreenWithBottomBar(
                navController = navController,
                role = RolUsuario.PROFESIONAL
            ) {
                ProfessionalHomeScreen(
                    vm = container.agendaViewModel,
                    onAgenda = {
                        navController.navigate(
                            Routes.AGENDA_DETALLE
                        )
                    },
                    onProfile = {
                        navController.navigate(Routes.PERFIL)
                    }
                )
            }
        }

        // AGENDA DEL PROFESIONAL
        composable(Routes.AGENDA_DETALLE) {
            cl.sanaesencia.app.ui.agenda.AgendaScreen(
                vm = container.agendaViewModel,
                onLogout = {
                    logout()
                }
            )
        }
    }
}

@Composable
private fun ScreenWithBottomBar(
    navController: NavHostController,
    role: RolUsuario,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            SanaesenciaBottomBar(
                nav = navController,
                role = role
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            content()
        }
    }
}