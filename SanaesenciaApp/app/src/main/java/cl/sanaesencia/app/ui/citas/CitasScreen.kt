package cl.sanaesencia.app.ui.citas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.presentation.CitasViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.BotonPrimario
import cl.sanaesencia.app.ui.common.PantallaEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(
    vm: CitasViewModel,
    onNew: () -> Unit,
) {
    val state by vm.citas.collectAsState()
    val message by vm.message.collectAsState()

    LaunchedEffect(Unit) {
        vm.cargar()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Text("Mis citas")
            },
        )

        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            BotonPrimario(
                texto = "Agendar nueva cita",
                onClick = onNew,
            )

            PantallaEstado(
                loading = state is UiState.Cargando,
                error = (state as? UiState.Error)?.mensaje,
                empty = state is UiState.Vacio,
                onRetry = vm::cargar,
            ) {
                val citas = (state as? UiState.Exito<List<CitaResponse>>)?.datos ?: emptyList()

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = citas,
                        key = { it.id },
                    ) { cita ->
                        CitaCard(
                            cita = cita,
                            vm = vm,
                            modifier = Modifier.animateContentSize(),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = message != null,
            ) {
                Text(
                    text = message.orEmpty(),
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun CitaCard(
    cita: cl.sanaesencia.app.data.remote.CitaResponse,
    vm: CitasViewModel,
    modifier: Modifier = Modifier,
) {
    val targetColor = when (cita.estado) {
        EstadoCita.CONFIRMADA,
        EstadoCita.COMPLETADA -> {
            MaterialTheme.colorScheme.primaryContainer
        }

        EstadoCita.CANCELADA,
        EstadoCita.NO_ASISTIO -> {
            MaterialTheme.colorScheme.errorContainer
        }

        else -> {
            MaterialTheme.colorScheme.surfaceVariant
        }
    }

    val color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "estado-cita",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                "${cita.fecha} · ${cita.horaInicio}-${cita.horaFin}",
            )

            Text(
                "Modalidad: ${cita.modalidad}",
            )

            Text(
                text = "Estado: ${cita.estado}",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (cita.estado != EstadoCita.CANCELADA) {
                Button(
                    onClick = {
                        vm.cancelar(cita.id)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}