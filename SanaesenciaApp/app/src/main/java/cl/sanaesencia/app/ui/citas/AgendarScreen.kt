package cl.sanaesencia.app.ui.citas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import cl.sanaesencia.app.data.remote.CrearCitaRequest
import cl.sanaesencia.app.data.remote.DisponibilidadResponse
import cl.sanaesencia.app.domain.model.ModalidadCita
import cl.sanaesencia.app.presentation.CitasViewModel
import java.time.LocalDate
import java.time.LocalTime

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun AgendarScreen(
    profesionalId: Long,
    vm: CitasViewModel,
    onBack: () -> Unit,
) {
    var date by remember {
        mutableStateOf(
            LocalDate.now().plusDays(1)
        )
    }

    var selected by remember {
        mutableStateOf<DisponibilidadResponse?>(null)
    }

    var modalidad by remember {
        mutableStateOf(
            ModalidadCita.ONLINE
        )
    }

    val slots by vm.disponibilidad.collectAsState()
    val message by vm.message.collectAsState()
    val creada by vm.creada.collectAsState()

    LaunchedEffect(date, profesionalId) {
        selected = null

        vm.disponibilidad(
            profesionalId,
            date
        )
    }

    LaunchedEffect(creada) {
        if (creada) {
            vm.consumirCreada()
            onBack()
        }
    }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2500)
            vm.clearMessage()
        }
    }

    Column(
        modifier = Modifier.verticalScroll(
            rememberScrollState()
        )
    ) {

        TopAppBar(
            title = {
                Text("Agendar cita")
            },
            navigationIcon = {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    ),
                ) {
                    Text("Atrás")
                }
            },
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {

            Text(
                text = "Fecha: $date"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                Button(
                    onClick = {
                        date = date.minusDays(1)
                    },
                    enabled = date.isAfter(
                        LocalDate.now().plusDays(1)
                    ),
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    ),
                ) {
                    Text("Día anterior")
                }

                Button(
                    onClick = {
                        date = date.plusDays(1)
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    ),
                ) {
                    Text("Día siguiente")
                }
            }

            Text(
                text = "Horarios disponibles",
                style = MaterialTheme.typography.titleMedium,
            )

            val available = slots.filter {
                it.disponible
            }

            if (available.isEmpty()) {

                Text(
                    "No hay horarios disponibles para este día."
                )

            } else {

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {

                    available.forEach { slot ->

                        FilterChip(
                            selected = selected == slot,
                            onClick = {

                                selected = slot

                                val horaInicio =
                                    runCatching {
                                        LocalTime.parse(
                                            slot.horaInicio
                                        )
                                    }.getOrNull()

                                if (horaInicio != null) {
                                    vm.validarCita(
                                        date,
                                        horaInicio
                                    )
                                }
                            },
                            label = {
                                Text(
                                    slot.horaInicio
                                )
                            },
                            modifier = Modifier.heightIn(
                                min = 48.dp
                            ),
                        )
                    }
                }
            }

            Text(
                text = "Modalidad",
                style = MaterialTheme.typography.titleMedium,
            )

            Row {

                FilterChip(
                    selected =
                        modalidad == ModalidadCita.ONLINE,
                    onClick = {
                        modalidad =
                            ModalidadCita.ONLINE
                    },
                    label = {
                        Text("Online")
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    ),
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                FilterChip(
                    selected =
                        modalidad == ModalidadCita.PRESENCIAL,
                    onClick = {
                        modalidad =
                            ModalidadCita.PRESENCIAL
                    },
                    label = {
                        Text("Presencial")
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    ),
                )
            }

            AnimatedVisibility(
                visible = message != null,
            ) {
                Text(
                    text = message.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = {

                    selected?.let { slot ->

                        vm.crear(
                            CrearCitaRequest(
                                profesionalId = profesionalId,
                                boxId = null,
                                fecha = date.toString(),
                                horaInicio = slot.horaInicio,
                                horaFin = slot.horaFin,
                                modalidad = modalidad,
                            )
                        )
                    }
                },

                enabled =
                    selected != null &&
                            run {

                                val horaInicio =
                                    selected?.horaInicio
                                        ?.let { hora ->
                                            runCatching {
                                                LocalTime.parse(hora)
                                            }.getOrNull()
                                        }

                                vm.validarCitaSilencioso(
                                    date,
                                    horaInicio
                                )
                            },

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 48.dp
                    ),
            ) {
                Text("Confirmar cita")
            }
        }
    }
}