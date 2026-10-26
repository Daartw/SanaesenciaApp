package cl.sanaesencia.app.ui.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import cl.sanaesencia.app.AppContainer
import cl.sanaesencia.app.data.remote.SessionEvents
import cl.sanaesencia.app.data.session.Session
import cl.sanaesencia.app.domain.model.RolUsuario
import cl.sanaesencia.app.ui.admin.AdminDashboardScreen
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

import kotlinx.coroutines.flow.first
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

    /** Pantalla de inicio de cada rol. */
    fun inicioDe(rol: RolUsuario): String = when (rol) {
        RolUsuario.ADMIN -> ADMIN
        RolUsuario.PROFESIONAL -> AGENDA
        RolUsuario.PACIENTE -> HOME
    }
}

private val SOLO_PACIENTE = setOf(RolUsuario.PACIENTE)
private val SOLO_PROFESIONAL = setOf(RolUsuario.PROFESIONAL)
private val SOLO_ADMIN = setOf(RolUsuario.ADMIN)
private val PACIENTE_O_PROFESIONAL = setOf(RolUsuario.PACIENTE, RolUsuario.PROFESIONAL)

@Composable
fun SanaesenciaNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Sesión actual (null = nadie conectado). Es la única fuente de verdad para los permisos.
    // Se actualiza en el mismo momento del login/logout, así ninguna pantalla ve un estado a medias.
    var session by remember { mutableStateOf<Session?>(null) }

    // Pantalla inicial: se decide leyendo la sesión guardada, así no hay que iniciar sesión
    // cada vez que se abre la app.
    var startDestination by remember { mutableStateOf<String?>(null) }
    var avisoLogin by remember { mutableStateOf<String?>(null) }
    var cerrandoSesion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val guardada = container.sessionManager.session.first()
        session = guardada
        startDestination = guardada?.let { Routes.inicioDe(it.role) } ?: Routes.LOGIN
    }

    fun irAlLogin() {
        navController.navigate(Routes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun logout(aviso: String? = null) {
        if (cerrandoSesion) return
        cerrandoSesion = true
        scope.launch {
            try {
                container.cerrarSesion()
            } catch (error: Exception) {
                Log.e("SanaesenciaLogout", "Error al cerrar sesión", error)
            }
            session = null
            avisoLogin = aviso
            runCatching { irAlLogin() }
            cerrandoSesion = false
        }
    }

    // Si cualquier pantalla recibe 401 del backend, la sesión expiró: se vuelve al login.
    LaunchedEffect(Unit) {
        SessionEvents.expirada.collect {
            if (session != null) {
                logout("Tu sesión expiró. Vuelve a iniciar sesión.")
            }
        }
    }

    val start = startDestination
    if (start == null) {
        SplashCarga()
        return
    }

    NavHost(
        navController = navController,
        startDestination = start
    ) {
        // LOGIN
        composable(Routes.LOGIN) {
            LoginScreen(
                vm = container.loginViewModel,
                aviso = avisoLogin,
                onLogged = { response ->
                    avisoLogin = null
                    session = Session(response.token, response.usuarioId, response.rol, response.nombre)
                    container.loginViewModel.resetState()

                    navController.navigate(Routes.inicioDe(response.rol)) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // INICIO DEL PACIENTE
        composable(Routes.HOME) {
            ConRol(session, SOLO_PACIENTE, navController) { s ->
                ScreenWithBottomBar(navController, s.role) {
                    HomeScreen(
                        nombre = s.name,
                        onProf = { navController.navigate(Routes.PROFESIONALES) },
                        onShop = { navController.navigate(Routes.TIENDA) },
                        onProfile = { navController.navigate(Routes.PERFIL) },
                        onAppointments = { navController.navigate(Routes.CITAS) },
                    )
                }
            }
        }

        // PROFESIONALES
        composable(Routes.PROFESIONALES) {
            ConRol(session, SOLO_PACIENTE, navController) { s ->
                ScreenWithBottomBar(navController, s.role) {
                    ProfesionalesScreen(
                        vm = container.catalogViewModel,
                        onAgendar = { id -> navController.navigate("agendar/$id") },
                    )
                }
            }
        }

        // TIENDA
        composable(Routes.TIENDA) {
            ConRol(session, SOLO_PACIENTE, navController) { s ->
                ScreenWithBottomBar(navController, s.role) {
                    TiendaScreen(
                        vm = container.catalogViewModel,
                        cartVm = container.cartViewModel,
                        onCart = { navController.navigate(Routes.CARRITO) },
                        isStockBajo = { stock -> container.verificarStockBajo(stock) },
                    )
                }
            }
        }

        // CARRITO
        composable(Routes.CARRITO) {
            ConRol(session, SOLO_PACIENTE, navController) {
                CartScreen(
                    vm = container.cartViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        // CITAS
        composable(Routes.CITAS) {
            ConRol(session, SOLO_PACIENTE, navController) { s ->
                ScreenWithBottomBar(navController, s.role) {
                    CitasScreen(
                        vm = container.citasViewModel,
                        onNew = { navController.navigate(Routes.PROFESIONALES) },
                    )
                }
            }
        }

        // AGENDAR UNA CITA
        composable(
            route = Routes.AGENDAR,
            arguments = listOf(
                navArgument("profesionalId") { type = NavType.LongType }
            )
        ) { entry ->
            val profesionalId = entry.arguments?.getLong("profesionalId") ?: return@composable

            ConRol(session, SOLO_PACIENTE, navController) {
                AgendarScreen(
                    profesionalId = profesionalId,
                    vm = container.citasViewModel,
                    catalogVm = container.catalogViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        // PERFIL (paciente y profesional)
        composable(Routes.PERFIL) {
            ConRol(session, PACIENTE_O_PROFESIONAL, navController) { s ->
                ScreenWithBottomBar(navController, s.role) {
                    PerfilScreen(
                        vm = container.perfilViewModel,
                        onLogout = { logout() },
                    )
                }
            }
        }

        // ADMINISTRADOR
        composable(Routes.ADMIN) {
            ConRol(session, SOLO_ADMIN, navController) {
                AdminDashboardScreen(
                    vm = container.adminViewModel,
                    onLogout = { logout() },
                )
            }
        }

        // INICIO DEL PROFESIONAL
        composable(Routes.AGENDA) {
            ConRol(session, SOLO_PROFESIONAL, navController) { s ->
                ScreenWithBottomBar(navController, s.role) {
                    ProfessionalHomeScreen(
                        nombre = s.name,
                        vm = container.agendaViewModel,
                        onAgenda = { navController.navigate(Routes.AGENDA_DETALLE) },
                        onProfile = { navController.navigate(Routes.PERFIL) },
                    )
                }
            }
        }

        // AGENDA DEL PROFESIONAL
        composable(Routes.AGENDA_DETALLE) {
            ConRol(session, SOLO_PROFESIONAL, navController) { s ->
                ScreenWithBottomBar(navController, s.role) {
                    AgendaScreen(
                        vm = container.agendaViewModel,
                        onLogout = { logout() },
                    )
                }
            }
        }
    }
}

/**
 * Protege cada pantalla según el rol:
 * - Sin sesión → vuelve al login.
 * - Con sesión pero de otro rol → lo lleva a su propio inicio.
 * Así un paciente nunca ve el panel admin, y un admin no ve pantallas de paciente.
 */
@Composable
private fun ConRol(
    session: Session?,
    permitidos: Set<RolUsuario>,
    navController: NavHostController,
    content: @Composable (Session) -> Unit,
) {
    val actual = session
    val autorizado = actual != null && actual.role in permitidos

    LaunchedEffect(actual?.token, actual?.role) {
        if (actual == null) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (actual.role !in permitidos) {
            navController.navigate(Routes.inicioDe(actual.role)) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (autorizado) {
        content(actual!!)
    } else {
        SplashCarga()
    }
}

@Composable
private fun SplashCarga() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp),
                )
            }
            Text(
                text = "Sanaesencia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp),
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
        containerColor = Color.Transparent,
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
