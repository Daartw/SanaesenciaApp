package cl.sanaesencia.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.sanaesencia.app.domain.model.RolUsuario
import cl.sanaesencia.app.ui.admin.AdminDashboardScreen
import cl.sanaesencia.app.ui.auth.LoginScreen
import cl.sanaesencia.app.ui.components.SanaesenciaBottomBar
import cl.sanaesencia.app.ui.home.HomeScreen
import cl.sanaesencia.app.ui.perfil.PerfilScreen
import cl.sanaesencia.app.ui.profesionales.ProfesionalesScreen
import cl.sanaesencia.app.ui.tienda.TiendaScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROF = "profesionales"
    const val TIENDA = "tienda"
    const val PERFIL = "perfil"
    const val ADMIN = "admin"
    const val AGENDA = "agenda"
}

@Composable
fun SanaesenciaNavGraph() {

    val nav = rememberNavController()

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar =
        currentRoute != Routes.LOGIN &&
                currentRoute != Routes.ADMIN

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                SanaesenciaBottomBar(nav)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = nav,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ─────────────────────────────
            // LOGIN
            // ─────────────────────────────
            composable(Routes.LOGIN) {

                LoginScreen { role ->

                    when (role) {

                        RolUsuario.PACIENTE -> {
                            nav.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) {
                                    inclusive = true
                                }
                            }
                        }

                        RolUsuario.PROFESIONAL -> {
                            nav.navigate(Routes.AGENDA) {
                                popUpTo(Routes.LOGIN) {
                                    inclusive = true
                                }
                            }
                        }

                        RolUsuario.ADMIN -> {
                            nav.navigate(Routes.ADMIN) {
                                popUpTo(Routes.LOGIN) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            }

            // ─────────────────────────────
            // INICIO
            // ─────────────────────────────
            composable(Routes.HOME) {

                HomeScreen(
                    {
                        nav.navigate(Routes.PROF)
                    },
                    {
                        nav.navigate(Routes.TIENDA)
                    },
                    {
                        nav.navigate(Routes.PERFIL)
                    }
                )
            }

            // ─────────────────────────────
            // PROFESIONALES
            // ─────────────────────────────
            composable(Routes.PROF) {

                ProfesionalesScreen {
                    nav.popBackStack()
                }
            }

            // ─────────────────────────────
            // TIENDA
            // ─────────────────────────────
            composable(Routes.TIENDA) {

                TiendaScreen {
                    nav.popBackStack()
                }
            }

            // ─────────────────────────────
            // PERFIL
            // ─────────────────────────────
            composable(Routes.PERFIL) {

                PerfilScreen {
                    nav.popBackStack()
                }
            }

            // ─────────────────────────────
            // AGENDA PROFESIONAL
            // ─────────────────────────────
            composable(Routes.AGENDA) {

                HomeScreen(
                    {},
                    {
                        nav.navigate(Routes.TIENDA)
                    },
                    {
                        nav.navigate(Routes.PERFIL)
                    }
                )
            }

            // ─────────────────────────────
            // ADMIN
            // ─────────────────────────────
            composable(Routes.ADMIN) {

                AdminDashboardScreen()
            }
        }
    }
}