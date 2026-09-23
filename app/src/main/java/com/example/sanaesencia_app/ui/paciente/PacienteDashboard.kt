package com.example.sanaesencia_app.ui.paciente

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesencia_app.domain.model.EstadoTerapeuta
import com.example.sanaesencia_app.domain.model.ModalidadAtencion
import com.example.sanaesencia_app.domain.model.Promocion
import com.example.sanaesencia_app.domain.model.Producto
import com.example.sanaesencia_app.domain.model.Profesional
import com.example.sanaesencia_app.ui.catalogo.UbicacionSedeCard

@Composable
fun PacienteDashboard(
    context: Context,
    onProfesional: (Profesional) -> Unit,
    onAgenda: () -> Unit,
    onHistorial: () -> Unit
) {
    val vm: PacienteViewModel = viewModel(
        factory = PacienteViewModel.factory(context)
    )

    val profesionales by vm.filtrados.collectAsState(initial = emptyList())
    val especialidad by vm.especialidad.collectAsState(initial = "Todas")
    val modalidad by vm.modalidad.collectAsState(initial = null)
    val texto by vm.texto.collectAsState(initial = "")
    val experiencia by vm.experienciaMinima.collectAsState(initial = 0)

    val profesionalesDisponibles by vm.profesionales.collectAsState(initial = emptyList())

    val especialidades = listOf("Todas") +
            profesionalesDisponibles
                .map { it.especialidad }
                .distinct()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "Hola, paciente",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Encuentra un profesional y agenda tu atención.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            OutlinedTextField(
                value = texto,
                onValueChange = {
                    vm.texto.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Buscar por nombre, carrera o certificación")
                }
            )
        }

        item {
            Text(
                text = "Especialidad",
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                especialidades
                    .take(3)
                    .forEach { value ->

                        FilterChip(
                            selected = especialidad == value,
                            onClick = {
                                vm.especialidad.value = value
                            },
                            label = {
                                Text(value)
                            }
                        )
                    }
            }

            if (especialidades.size > 3) {
                var expanded by remember {
                    mutableStateOf(false)
                }

                Box {
                    OutlinedButton(
                        onClick = {
                            expanded = true
                        }
                    ) {
                        Text("Más especialidades")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        especialidades
                            .drop(3)
                            .forEach { value ->

                                DropdownMenuItem(
                                    text = {
                                        Text(value)
                                    },
                                    onClick = {
                                        vm.especialidad.value = value
                                        expanded = false
                                    }
                                )
                            }
                    }
                }
            }
        }

        item {
            Text(
                text = "Modalidad",
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(
                    selected = modalidad == null,
                    onClick = {
                        vm.modalidad.value = null
                    },
                    label = {
                        Text("Todas")
                    }
                )

                ModalidadAtencion.values().forEach { mode ->

                    FilterChip(
                        selected = modalidad == mode,
                        onClick = {
                            vm.modalidad.value = mode
                        },
                        label = {
                            Text(
                                if (mode == ModalidadAtencion.ONLINE) {
                                    "Online"
                                } else {
                                    "Presencial"
                                }
                            )
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = "Experiencia mínima: $experiencia años",
                style = MaterialTheme.typography.labelLarge
            )

            Slider(
                value = experiencia.toFloat(),
                onValueChange = {
                    vm.experienciaMinima.value = it.toInt()
                },
                valueRange = 0f..10f,
                steps = 9
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = onAgenda,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mi agenda")
                }

                OutlinedButton(
                    onClick = onHistorial,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Historial")
                }
            }
        }

        item {
            UbicacionSedeCard()
        }

        item {
            Text(
                text = "Profesionales (${profesionales.size})",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(
            items = profesionales,
            key = { it.id }
        ) { profesional ->

            ProfesionalCard(
                profesional = profesional,
                onClick = onProfesional
            )
        }

        item {
            ProductosSection(vm.productos)
        }

        item {
            PromocionesSection(vm.promociones)
        }
    }
}

@Composable
private fun ProfesionalCard(
    profesional: Profesional,
    onClick: (Profesional) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = profesional.nombre,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = profesional.especialidad,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${profesional.carrera} · ${profesional.aniosExperiencia} años de experiencia aprox."
            )

            Text(
                text = "Certificaciones: ${
                    profesional.certificaciones.joinToString()
                }"
            )

            Text(
                text = "Capacitaciones: ${
                    profesional.capacitaciones.joinToString()
                }"
            )

            Text(
                text = "Modalidad: ${
                    profesional.modalidades.joinToString {
                        if (it == ModalidadAtencion.ONLINE) {
                            "Online"
                        } else {
                            "Presencial"
                        }
                    }
                }"
            )

            AssistChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        "Estado: ${
                            profesional.estado.name.lowercase()
                        }"
                    )
                }
            )

            Button(
                onClick = {
                    onClick(profesional)
                },
                enabled = profesional.estado == EstadoTerapeuta.DISPONIBLE,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver perfil y agendar")
            }
        }
    }
}

@Composable
private fun ProductosSection(
    productos: List<Producto>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Catálogo de productos",
            style = MaterialTheme.typography.titleLarge
        )

        productos.forEach { producto ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(producto.descripcion)

                    Text(
                        text = "Precio referencial: $${producto.precioReferencial}"
                    )
                }
            }
        }
    }
}

@Composable
private fun PromocionesSection(
    promociones: List<Promocion>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Promociones",
            style = MaterialTheme.typography.titleLarge
        )

        promociones
            .filter { it.activa }
            .forEach { promocion ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = promocion.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(promocion.descripcion)

                        Text(
                            text = "Vigencia: ${promocion.vigencia}"
                        )
                    }
                }
            }
    }
}