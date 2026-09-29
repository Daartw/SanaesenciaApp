package cl.sanaesencia.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import cl.sanaesencia.app.ui.navigation.Routes

@Composable
fun SanaesenciaBottomBar(
    navController: NavController
) {

    val items = listOf(
        BottomItem(
            route = Routes.HOME,
            label = "Inicio",
            icon = Icons.Default.Home
        ),
        BottomItem(
            route = Routes.PROF,
            label = "Profesionales",
            icon = Icons.Default.People
        ),
        BottomItem(
            route = Routes.TIENDA,
            label = "Tienda",
            icon = Icons.Default.ShoppingBag
        ),
        BottomItem(
            route = Routes.PERFIL,
            label = "Perfil",
            icon = Icons.Default.Person
        )
    )

    val currentRoute =
        navController.currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White
    ) {

        items.forEach { item ->

            NavigationBarItem(
                selected = currentRoute == item.route,

                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Routes.HOME) {
                            saveState = true
                        }

                        launchSingleTop = true
                        restoreState = true
                    }
                },

                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },

                label = {
                    Text(item.label)
                },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF6A7B66),
                    selectedTextColor = Color(0xFF6A7B66),
                    indicatorColor = Color(0xFFE8EEE5),
                    unselectedIconColor = Color(0xFF777777),
                    unselectedTextColor = Color(0xFF777777)
                )
            )
        }
    }
}

data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)