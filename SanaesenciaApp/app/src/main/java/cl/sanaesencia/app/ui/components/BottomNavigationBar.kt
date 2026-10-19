package cl.sanaesencia.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import cl.sanaesencia.app.domain.model.RolUsuario
import cl.sanaesencia.app.ui.navigation.Routes

private data class NavItemData(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String,
)

@Composable
fun SanaesenciaBottomBar(
    nav: NavHostController,
    role: RolUsuario?,
) {
    if (role == RolUsuario.ADMIN) return

    val currentDestination = nav.currentBackStackEntryAsState().value?.destination

    val items = when (role) {
        RolUsuario.PROFESIONAL -> listOf(
            NavItemData("Inicio", Icons.Filled.Home, Icons.Outlined.Home, Routes.AGENDA),
            NavItemData("Mis Horas", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, Routes.AGENDA_DETALLE),
            NavItemData("Perfil", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, Routes.PERFIL),
        )
        else -> listOf(
            NavItemData("Inicio", Icons.Filled.Home, Icons.Outlined.Home, Routes.HOME),
            NavItemData("Profesionales", Icons.Filled.People, Icons.Outlined.People, Routes.PROFESIONALES),
            NavItemData("Mis Citas", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, Routes.CITAS),
            NavItemData("Perfil", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, Routes.PERFIL),
        )
    }

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        nav.navigate(item.route) {
                            popUpTo(nav.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
