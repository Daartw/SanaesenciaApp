package cl.sanaesencia.app.ui.agenda

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.presentation.AgendaViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.BotonPrimario
import cl.sanaesencia.app.ui.common.PantallaEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    vm: AgendaViewModel,
    onLogout: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val ficha by vm.ficha.collectAsState()

    var showFicha by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        vm.cargar()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Text("Agenda profesional")
            },
            actions = {
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Cerrar sesión")
                }
            },
        )

        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Solo muestra las citas asignadas a este profesional.",
                style = MaterialTheme.typography.bodySmall,
            )

            PantallaEstado(
                loading = state is UiState.Cargando,
                error = (state as? UiState.Error)?.mensaje,
                empty = state is UiState.Vacio,
                onRetry = vm::cargar,
            ) {
                val citas = (state as UiState.Exito).datos

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = citas,
                        key = { it.id },
                    ) { cita ->

                        val color by animateColorAsState(
                            targetValue = if (
                                cita.estado == EstadoCita.COMPLETADA
                            ) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            animationSpec = tween(300),
                            label = "estado-agenda",
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = color,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                            ) {
                                Text(
                                    text = "${cita.fecha} · ${cita.horaInicio}-${cita.horaFin}",
                                )

                                Text(
                                    text = "Paciente asignado · cita #${cita.id}",
                                )

                                TextButton(
                                    onClick = {
                                        vm.cargarFicha(cita.pacienteId)
                                        showFicha = true
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                ) {
                                    Text("Ver historial asignado")
                                }

                                Text(
                                    text = "Estado: ${cita.estado}",
                                )

                                if (
                                    cita.estado == EstadoCita.RESERVADA ||
                                    cita.estado == EstadoCita.CONFIRMADA
                                ) {
                                    BotonPrimario(
                                        texto = "Completar",
                                        onClick = {
                                            vm.estado(
                                                cita.id,
                                                EstadoCita.COMPLETADA,
                                                null,
                                            )
                                        },
                                    )

                                    BotonPrimario(
                                        texto = "No asistió",
                                        onClick = {
                                            vm.estado(
                                                cita.id,
                                                EstadoCita.NO_ASISTIO,
                                                null,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFicha) {
        AlertDialog(
            onDismissRequest = {
                showFicha = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFicha = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Cerrar")
                }
            },
            title = {
                Text("Historial asignado")
            },
            text = {
                Column {
                    if (ficha.isEmpty()) {
                        Text("Sin registros clínicos disponibles")
                    } else {
                        ficha.forEach { f ->
                            Text(
                                "${f.fecha} · ${f.estado} · ${
                                    f.notasAtencion ?: "Sin notas"
                                }",
                            )
                        }
                    }
                }
            },
        )
    }
}