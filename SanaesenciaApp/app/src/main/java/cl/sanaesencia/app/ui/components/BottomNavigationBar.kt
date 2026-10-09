package cl.sanaesencia.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import cl.sanaesencia.app.domain.model.RolUsuario
import cl.sanaesencia.app.ui.navigation.Routes

@Composable
fun SanaesenciaBottomBar(
    nav: NavHostController,
    role: RolUsuario?,
) {
    val currentDestination =
        nav.currentBackStackEntryAsState().value?.destination

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            when (role) {

                // =====================================================
                // PROFESIONAL
                // =====================================================
                RolUsuario.PROFESIONAL -> {

                    BottomBarItem(
                        label = "Inicio",
                        icon = Icons.Default.Home,
                        route = Routes.PROFESSIONAL_HOME,
                        nav = nav,
                        currentDestination = currentDestination,
                    )

                    BottomBarItem(
                        label = "Perfil",
                        icon = Icons.Default.AccountCircle,
                        route = Routes.PERFIL,
                        nav = nav,
                        currentDestination = currentDestination,
                    )

                    BottomBarItem(
                        label = "Mis horas",
                        icon = Icons.Default.Event,
                        route = Routes.AGENDA,
                        nav = nav,
                        currentDestination = currentDestination,
                    )
                }

                // =====================================================
                // PACIENTE
                // =====================================================
                RolUsuario.PACIENTE,
                null -> {

                    BottomBarItem(
                        label = "Inicio",
                        icon = Icons.Default.Home,
                        route = Routes.HOME,
                        nav = nav,
                        currentDestination = currentDestination,
                    )

                    BottomBarItem(
                        label = "Profesionales",
                        icon = Icons.Default.Person,
                        route = Routes.PROF,
                        nav = nav,
                        currentDestination = currentDestination,
                    )

                    BottomBarItem(
                        label = "Mis horas",
                        icon = Icons.Default.Event,
                        route = Routes.CITAS,
                        nav = nav,
                        currentDestination = currentDestination,
                    )

                    BottomBarItem(
                        label = "Perfil",
                        icon = Icons.Default.AccountCircle,
                        route = Routes.PERFIL,
                        nav = nav,
                        currentDestination = currentDestination,
                    )
                }

                // =====================================================
                // ADMIN
                // =====================================================
                RolUsuario.ADMIN -> {
                    // El administrador utiliza su propio dashboard.
                }
            }
        }
    }
}

/*
 * Importante:
 * Esta función es una extensión de RowScope.
 *
 * Así Modifier.weight(1f) funciona correctamente y cada
 * elemento ocupa exactamente la misma proporción de la barra.
 */
@Composable
private fun RowScope.BottomBarItem(
    label: String,
    icon: ImageVector,
    route: String,
    nav: NavHostController,
    currentDestination: NavDestination?,
) {
    val selected =
        currentDestination
            ?.hierarchy
            ?.any { destination ->
                destination.route == route
            } == true

    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 56.dp)
            .clickable {
                nav.navigate(route) {
                    popUpTo(nav.graph.startDestinationId) {
                        saveState = true
                    }

                    launchSingleTop = true
                    restoreState = true
                }
            }
            .padding(
                horizontal = 4.dp,
                vertical = 6.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Text(
            text = label,
            maxLines = 1,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}