package com.example.sanaesencia_app.ui.agendamiento

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesencia_app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilProfesionalScreen(profesional: Profesional, context: Context, onBack: () -> Unit) {
    val vm: AgendamientoViewModel = viewModel(factory = AgendamientoViewModel.factory(context))
    val confirmada by vm.confirmada.collectAsStateWithLifecycle()
    val disponibilidades = vm.disponibilidad(profesional.id)
    var seleccion by remember { mutableStateOf<Disponibilidad?>(null) }
    var modalidad by remember { mutableStateOf<ModalidadAtencion?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text(profesional.nombre) }, navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(profesional.especialidad, style = MaterialTheme.typography.headlineSmall)
                Text(profesional.descripcion)
                Text("Carrera: ${profesional.carrera}")
                Text("Experiencia aproximada: ${profesional.aniosExperiencia} años")
                Text("Certificaciones", style = MaterialTheme.typography.titleMedium)
                profesional.certificaciones.forEach { Text("• $it") }
                Text("Capacitaciones", style = MaterialTheme.typography.titleMedium)
                profesional.capacitaciones.forEach { Text("• $it") }
                Spacer(Modifier.height(8.dp))
                Text("Modalidad", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { profesional.modalidades.forEach { m -> FilterChip(modalidad == m, { modalidad = m }, label = { Text(if (m == ModalidadAtencion.ONLINE) "Online" else "Presencial") }) } }
            }
            item { Text("Fechas y horarios disponibles", style = MaterialTheme.typography.titleLarge) }
            items(disponibilidades.filter { modalidad == null || it.modalidad == modalidad }, key = { it.id }) { slot ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(slot.fecha, style = MaterialTheme.typography.titleMedium); Text("${slot.hora} · ${if (slot.modalidad == ModalidadAtencion.ONLINE) "Online" else "Presencial"}"); slot.box?.let { Text("Box $it") } }
                        Button(onClick = { seleccion = slot }) { Text("Agendar") }
                    }
                }
            }
            if (confirmada) {
                item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Text("Cita agendada correctamente. Quedó guardada localmente.", Modifier.padding(16.dp)) } }
            }
        }
    }

    seleccion?.let { slot ->
        AlertDialog(
            onDismissRequest = { seleccion = null },
            title = { Text("Confirmar cita") },
            text = { Text("${profesional.nombre}\n${slot.fecha} a las ${slot.hora}\nModalidad: ${if (slot.modalidad == ModalidadAtencion.ONLINE) "Online" else "Presencial"}${slot.box?.let { "\nBox $it" } ?: ""}") },
            confirmButton = { Button(onClick = { vm.agendar(profesional, slot); seleccion = null }) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = { seleccion = null }) { Text("Cancelar") } }
        )
    }
}
