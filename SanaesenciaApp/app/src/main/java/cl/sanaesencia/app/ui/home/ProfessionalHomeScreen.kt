package cl.sanaesencia.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.presentation.AgendaViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.BotonPrimario

@Composable
fun ProfessionalHomeScreen(
    vm: AgendaViewModel,
    onAgenda: () -> Unit,
    onProfile: () -> Unit,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.cargar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "Hola, profesional",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text("Gestiona tus horas y tu atención.")
            }
            TextButton(
                onClick = onProfile,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Text("Perfil", modifier = Modifier.padding(start = 4.dp))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Text(
                        "  Mis horas",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                val count = (state as? UiState.Exito)?.datos?.size
                Text(
                    when (state) {
                        is UiState.Cargando -> "Cargando tus próximas atenciones..."
                        is UiState.Exito -> "$count cita(s) asignada(s)"
                        is UiState.Error -> "No se pudo cargar la agenda"
                        UiState.Vacio -> "No tienes citas asignadas"
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )

                BotonPrimario(
                    texto = "Ver mis horas",
                    onClick = onAgenda,
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null)
                    Text(
                        "  Tu espacio profesional",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    "Desde Mis horas puedes revisar tus citas asignadas y acceder al historial autorizado de tus pacientes.",
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        AnimatedVisibility(visible = state is UiState.Error) {
            Text(
                text = (state as? UiState.Error)?.mensaje.orEmpty(),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
