package com.example.sanaesencia_app.ui.admin

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
import com.example.sanaesencia_app.domain.model.*
import com.example.sanaesencia_app.worker.WorkScheduler

@Composable
fun AdminDashboard(context: Context, onLogout: () -> Unit) {
    val vm: AdminViewModel = viewModel(factory = AdminViewModel.factory(context))
    val profesionales by vm.profesionales.collectAsStateWithLifecycle()
    val boxes by vm.boxes.collectAsStateWithLifecycle()
    val inactivos = vm.pacientes.count { it.diasInactivo >= 21 }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Administración", style = MaterialTheme.typography.headlineSmall); Text("Gestión de centro y boxes") }; TextButton(onClick = onLogout) { Text("Salir") } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { StatCard("Terapeutas", profesionales.size.toString(), Modifier.weight(1f)); StatCard("Riesgo", inactivos.toString(), Modifier.weight(1f)); StatCard("Boxes", "8", Modifier.weight(1f)) } }
        item { Button(onClick = { vm.agregarDemo() }, Modifier.fillMaxWidth()) { Text("Añadir nuevo terapeuta") } }
        item { Text("Terapeutas", style = MaterialTheme.typography.titleLarge) }
        items(profesionales, key = { it.id }) { p -> AdminProfesionalRow(p, vm) }
        item { Text("Gestión de 8 boxes", style = MaterialTheme.typography.titleLarge) }
        items(boxes, key = { it.numero }) { box -> BoxRow(box, vm) }
        item { Text("Promociones activas", style = MaterialTheme.typography.titleLarge) }
        items(vm.promociones.filter { it.activa }) { Text("• ${it.titulo} — ${it.vigencia}") }
        item { OutlinedButton(onClick = { WorkScheduler.ejecutarChequeoAhora(context) }, Modifier.fillMaxWidth()) { Text("Ejecutar análisis de inactividad") } }
    }
}

@Composable private fun StatCard(titulo: String, valor: String, modifier: Modifier) { Card(modifier) { Column(Modifier.padding(12.dp)) { Text(valor, style = MaterialTheme.typography.headlineSmall); Text(titulo) } } }

@Composable private fun AdminProfesionalRow(p: Profesional, vm: AdminViewModel) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) {
        Text(p.nombre, style = MaterialTheme.typography.titleMedium); Text("${p.especialidad} · ${p.aniosExperiencia} años"); Text("Estado: ${p.estado.name}")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = { vm.cambiarEstado(p.id, EstadoTerapeuta.DISPONIBLE) }) { Text("Disponible") }
            TextButton(onClick = { vm.cambiarEstado(p.id, EstadoTerapeuta.AUSENTE) }) { Text("Ausente") }
            TextButton(onClick = { vm.cambiarEstado(p.id, EstadoTerapeuta.VACACIONES) }) { Text("Vacaciones") }
        }
    } }
}

@Composable private fun BoxRow(box: Box, vm: AdminViewModel) {
    Card(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Box ${box.numero}", style = MaterialTheme.typography.titleMedium); Text(box.estado.name) }; Row { TextButton(onClick = { vm.cambiarBox(box.numero, EstadoBox.DISPONIBLE) }) { Text("Libre") }; TextButton(onClick = { vm.cambiarBox(box.numero, EstadoBox.MANTENCION) }) { Text("Mant.") } } } }
}
