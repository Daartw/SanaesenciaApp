package com.example.sanaesencia_app.ui.agenda

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.domain.model.EstadoCita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(context: Context, onVolver: () -> Unit) {
    val vm: AgendaViewModel = viewModel(factory = AgendaViewModel.factory(context))
    val citas by vm.citas.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Mi agenda") }, navigationIcon = { TextButton(onClick = onVolver) { Text("Atrás") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (citas.isEmpty()) item { Text("No tienes citas registradas.") }
            items(citas, key = { it.id }) { CitaCard(it) }
        }
    }
}

@Composable
private fun CitaCard(cita: Cita) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(cita.especialidad, style = MaterialTheme.typography.titleMedium)
        Text("Con ${cita.terapeutaNombre}")
        Text("${cita.fecha} · ${cita.hora}")
        Text("Modalidad: ${if (cita.modalidad.name == "ONLINE") "Online" else "Presencial"}${cita.boxAsignado?.let { " · Box $it" } ?: ""}")
        Text("Estado: ${when (cita.estado) { EstadoCita.AGENDADA -> "Agendada"; EstadoCita.COMPLETADA -> "Completada"; EstadoCita.CANCELADA -> "Cancelada"; EstadoCita.INACTIVA -> "Inactiva / posible deserción" }}")
    } }
}
