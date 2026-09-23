package com.example.sanaesencia_app.ui.terapeuta

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesencia_app.session.DemoSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerapeutaDashboard(
    context: Context,
    onLogout: () -> Unit
) {
    val vm: TerapeutaViewModel = viewModel(
        factory = TerapeutaViewModel.factory(context)
    )

    val citas by vm.citas.collectAsState(
        initial = emptyList()
    )

    val profesional = vm.profesionales.firstOrNull {
        it.id == DemoSession.terapeutaDemoId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Panel del terapeuta")
                },
                actions = {
                    TextButton(
                        onClick = onLogout
                    ) {
                        Text("Salir")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = profesional?.nombre ?: "Terapeuta Demo",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            item {
                Text(
                    text = "Agenda asignada",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(
                items = citas,
                key = { it.id }
            ) { cita ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${cita.fecha} · ${cita.hora}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Paciente: ${cita.pacienteId}"
                        )

                        Text(
                            text = cita.especialidad
                        )

                        Text(
                            text = "Modalidad: ${cita.modalidad.name}"
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Continuidad de pacientes",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(
                items = vm.pacientes
            ) { paciente ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = paciente.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Última sesión: ${
                                paciente.ultimaSesion ?: "Sin registro"
                            }"
                        )

                        Text(
                            text = "Días de inactividad: ${paciente.diasInactivo}"
                        )
                    }
                }
            }
        }
    }
}