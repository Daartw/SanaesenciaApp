package com.example.sanaesencia_app.ui.agenda

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.domain.model.EstadoCita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    onVolver: () -> Unit,
    viewModel: AgendaViewModel = AgendaViewModel()
) {
    val citas by viewModel.citas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi agenda") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
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
            items(citas) { cita ->
                CitaCard(cita)
            }
        }
    }
}

@Composable
private fun CitaCard(cita: Cita) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = cita.especialidad, style = MaterialTheme.typography.titleMedium)
            Text(text = "Con ${cita.terapeutaNombre}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${cita.fecha} · ${cita.hora}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Modalidad: ${cita.modalidad.name}" +
                        (cita.boxAsignado?.let { " · Box $it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Estado: ${estadoLegible(cita.estado)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun estadoLegible(estado: EstadoCita): String = when (estado) {
    EstadoCita.AGENDADA -> "Agendada"
    EstadoCita.COMPLETADA -> "Completada"
    EstadoCita.CANCELADA -> "Cancelada"
    EstadoCita.INACTIVA -> "Inactiva / posible deserción"
}