package cl.sanaesencia.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.sanaesencia.app.AppContainer
import cl.sanaesencia.app.domain.model.RolUsuario
import cl.sanaesencia.app.domain.model.Usuario
import cl.sanaesencia.app.presentation.AdminFactory
import cl.sanaesencia.app.presentation.AgendaFactory
import cl.sanaesencia.app.presentation.AgendaViewModel
import cl.sanaesencia.app.presentation.CartFactory
import cl.sanaesencia.app.presentation.CartViewModel
import cl.sanaesencia.app.presentation.CatalogFactory
import cl.sanaesencia.app.presentation.CatalogViewModel
import cl.sanaesencia.app.presentation.CitasFactory
import cl.sanaesencia.app.presentation.CitasViewModel
import cl.sanaesencia.app.presentation.LoginFactory
import cl.sanaesencia.app.presentation.LoginViewModel
import cl.sanaesencia.app.presentation.PerfilFactory
import cl.sanaesencia.app.presentation.PerfilViewModel
import cl.sanaesencia.app.ui.admin.AdminDashboardScreen
import cl.sanaesencia.app.ui.admin.AdminViewModel
import cl.sanaesencia.app.ui.agenda.AgendaScreen
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
    const val PROFESSIONAL_HOME = "professional_home"
    const val PROF = "profesionales"
    const val TIENDA = "tienda"
    const val PERFIL = "perfil"
    const val ADMIN = "admin"
    const val AGENDA = "agenda"
    const val CITAS = "citas"
    const val AGENDAR = "agendar/{profesionalId}"
    const val CARRITO = "carrito"
}

@Composable
fun SanaesenciaNavGraph(
    container: AppContainer,
) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    val cartVm: CartViewModel = viewModel(
        factory = CartFactory(
            container.cart,
            container.pedidos,
        ),
    )

    val backStackEntry =
        nav.currentBackStackEntryAsState().value

    val route = backStackEntry?.destination?.route
    val session by container.sessionManager.session.collectAsState(initial = null)

    val showBottomBar =
        route != Routes.LOGIN &&
                route != Routes.AGENDAR &&
                route != Routes.CARRITO &&
                route != Routes.ADMIN

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                SanaesenciaBottomBar(
                    nav = nav,
                    role = session?.role,
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.LOGIN) {
                val vm: LoginViewModel = viewModel(
                    factory = LoginFactory(container.auth),
                )

                LoginScreen(vm) { response ->
                    navigateForRole(
                        nav = nav,
                        role = response.rol,
                        clearBackStack = true,
                    )
                }
            }

            composable(Routes.PROFESSIONAL_HOME) {
                val vm: AgendaViewModel = viewModel(
                    factory = AgendaFactory(container.citas),
                )

                ProfessionalHomeScreen(
                    vm = vm,
                    onAgenda = { nav.navigate(Routes.AGENDA) },
                    onProfile = { nav.navigate(Routes.PERFIL) },
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onProf = {
                        nav.navigate(Routes.PROF)
                    },
                    onShop = {
                        nav.navigate(Routes.TIENDA)
                    },
                    onProfile = {
                        nav.navigate(Routes.PERFIL)
                    },
                    onAppointments = {
                        nav.navigate(Routes.CITAS)
                    },
                )
            }

            composable(Routes.PROF) {
                val vm: CatalogViewModel = viewModel(
                    factory = CatalogFactory(container.catalog),
                )

                ProfesionalesScreen(vm) { id ->
                    nav.navigate("agendar/$id")
                }
            }

            composable(Routes.TIENDA) {
                val vm: CatalogViewModel = viewModel(
                    factory = CatalogFactory(container.catalog),
                )

                TiendaScreen(
                    vm = vm,
                    cartVm = cartVm,
                    onCart = {
                        nav.navigate(Routes.CARRITO)
                    },
                    isStockBajo = container.verificarStockBajo::invoke,
                )
            }

            composable(Routes.CARRITO) {
                CartScreen(cartVm) {
                    nav.popBackStack()
                }
            }

            composable(Routes.CITAS) {
                val vm: CitasViewModel = viewModel(
                    factory = CitasFactory(
                        container.citas,
                        container.agendarCita,
                    ),
                )

                CitasScreen(
                    vm = vm,
                    onNew = {
                        nav.navigate(Routes.PROF)
                    },
                )
            }

            composable(Routes.AGENDAR) { entry ->
                val id = entry.arguments
                    ?.getString("profesionalId")
                    ?.toLongOrNull()
                    ?: 0L

                val vm: CitasViewModel = viewModel(
                    factory = CitasFactory(
                        container.citas,
                        container.agendarCita,
                    ),
                )

                AgendarScreen(
                    profesionalId = id,
                    vm = vm,
                ) {
                    nav.popBackStack()
                }
            }

            composable(Routes.PERFIL) {
                val vm: PerfilViewModel = viewModel(
                    factory = PerfilFactory(container.auth),
                )

                PerfilScreen(
                    vm = vm,
                    onLogout = {
                        scope.launch {
                            container.auth.logout()
                            nav.navigate(Routes.LOGIN) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }

            composable(Routes.AGENDA) {
                val vm: AgendaViewModel = viewModel(
                    factory = AgendaFactory(container.citas),
                )

                AgendaScreen(vm) {
                    scope.launch {
                        container.auth.logout()

                        nav.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            }

            composable(Routes.ADMIN) {
                val vm: AdminViewModel = viewModel(
                    factory = AdminFactory(
                        container.api,
                        container.verificarStockBajo,
                    ),
                )

                AdminDashboardScreen(
                    vm = vm,
                    onProfile = { nav.navigate(Routes.PERFIL) },
                    onLogout = {
                        scope.launch {
                            container.auth.logout()
                            nav.navigate(Routes.LOGIN) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }
        }
    }
}

private fun navigateForRole(
    nav: NavHostController,
    role: RolUsuario,
    clearBackStack: Boolean,
) {
    val target = when (
        Usuario.desdeRol(role).pantallaInicial()
    ) {
        "home" -> Routes.HOME
        "agenda" -> Routes.PROFESSIONAL_HOME
        "admin" -> Routes.ADMIN
        else -> Routes.LOGIN
    }

    nav.navigate(target) {
        if (clearBackStack) {
            popUpTo(Routes.LOGIN) {
                inclusive = true
            }
        }

        launchSingleTop = true
    }
}