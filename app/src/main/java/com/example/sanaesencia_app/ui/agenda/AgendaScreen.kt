package com.example.sanaesencia_app.ui.agenda

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
import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.domain.model.EstadoCita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    context: Context,
    onVolver: () -> Unit
) {
    val vm: AgendaViewModel = viewModel(
        factory = AgendaViewModel.factory(context)
    )

    val citas by vm.citas.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi agenda")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onVolver
                    ) {
                        Text("Atrás")
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (citas.isEmpty()) {
                item {
                    Text("No tienes citas registradas.")
                }
            }

            items(
                items = citas,
                key = { it.id }
            ) { cita ->
                CitaCard(cita)
            }
        }
    }
}

@Composable
private fun CitaCard(cita: Cita) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = cita.especialidad,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Con ${cita.terapeutaNombre}"
            )

            Text(
                text = "${cita.fecha} · ${cita.hora}"
            )

            Text(
                text = buildString {
                    append(
                        "Modalidad: ${
                            if (cita.modalidad.name == "ONLINE") {
                                "Online"
                            } else {
                                "Presencial"
                            }
                        }"
                    )

                    cita.boxAsignado?.let {
                        append(" · Box $it")
                    }
                }
            )

            Text(
                text = "Estado: ${
                    when (cita.estado) {
                        EstadoCita.AGENDADA ->
                            "Agendada"

                        EstadoCita.COMPLETADA ->
                            "Completada"

                        EstadoCita.CANCELADA ->
                            "Cancelada"

                        EstadoCita.INACTIVA ->
                            "Inactiva / posible deserción"
                    }
                }"
            )
        }
    }
}