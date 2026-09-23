package com.example.sanaesencia_app.ui.terapeuta

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TerapeutaDashboard(context: Context, onLogout: () -> Unit) {
    val vm: TerapeutaViewModel = viewModel(factory = TerapeutaViewModel.factory(context))
    val citas by vm.citas.collectAsStateWithLifecycle()
    val profesional = vm.profesionales.firstOrNull { it.id == com.example.sanaesencia_app.session.DemoSession.terapeutaDemoId }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Panel del terapeuta", style = MaterialTheme.typography.headlineSmall); Text(profesional?.nombre ?: "Terapeuta Demo") }; TextButton(onClick = onLogout) { Text("Salir") } } }
        item { Text("Agenda asignada", style = MaterialTheme.typography.titleLarge) }
        items(citas, key = { it.id }) { cita -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(cita.fecha + " · " + cita.hora, style = MaterialTheme.typography.titleMedium); Text("Paciente: ${cita.pacienteId}"); Text(cita.especialidad); Text("Modalidad: ${cita.modalidad.name}") } } }
        item { Text("Continuidad de pacientes", style = MaterialTheme.typography.titleLarge) }
        items(vm.pacientes) { p -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(p.nombre, style = MaterialTheme.typography.titleMedium); Text("Última sesión: ${p.ultimaSesion ?: "Sin registro"}"); Text("Días de inactividad: ${p.diasInactivo}") } } }
    }
}
