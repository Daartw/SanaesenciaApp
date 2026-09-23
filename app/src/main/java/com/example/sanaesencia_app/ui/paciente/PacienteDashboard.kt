package com.example.sanaesencia_app.ui.paciente

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesencia_app.domain.model.*
import com.example.sanaesencia_app.ui.catalogo.UbicacionSedeCard

@Composable
fun PacienteDashboard(
    context: Context,
    onProfesional: (Profesional) -> Unit,
    onAgenda: () -> Unit,
    onHistorial: () -> Unit
) {
    val vm: PacienteViewModel = viewModel(factory = PacienteViewModel.factory(context))
    val profesionales by vm.filtrados.collectAsStateWithLifecycle()
    val especialidad by vm.especialidad.collectAsStateWithLifecycle()
    val modalidad by vm.modalidad.collectAsStateWithLifecycle()
    val texto by vm.texto.collectAsStateWithLifecycle()
    val experiencia by vm.experienciaMinima.collectAsStateWithLifecycle()
    val especialidades = listOf("Todas") + vm.profesionales.value.map { it.especialidad }.distinct()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Hola, paciente", style = MaterialTheme.typography.headlineSmall)
            Text("Encuentra un profesional y agenda tu atención.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            OutlinedTextField(texto, { vm.texto.value = it }, Modifier.fillMaxWidth(), label = { Text("Buscar por nombre, carrera o certificación") })
        }
        item {
            Text("Especialidad", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                especialidades.take(3).forEach { value -> FilterChip(especialidad == value, { vm.especialidad.value = value }, label = { Text(value) }) }
            }
            if (especialidades.size > 3) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }) { Text("Más especialidades") }
                    DropdownMenu(expanded, { expanded = false }) {
                        especialidades.drop(3).forEach { value -> DropdownMenuItem({ Text(value) }, { vm.especialidad.value = value; expanded = false }) }
                    }
                }
            }
        }
        item {
            Text("Modalidad", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(modalidad == null, { vm.modalidad.value = null }, label = { Text("Todas") })
                ModalidadAtencion.values().forEach { mode -> FilterChip(modalidad == mode, { vm.modalidad.value = mode }, label = { Text(if (mode == ModalidadAtencion.ONLINE) "Online" else "Presencial") }) }
            }
        }
        item {
            Text("Experiencia mínima: $experiencia años", style = MaterialTheme.typography.labelLarge)
            Slider(value = experiencia.toFloat(), onValueChange = { vm.experienciaMinima.value = it.toInt() }, valueRange = 0f..10f, steps = 9)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onAgenda, Modifier.weight(1f)) { Text("Mi agenda") }
                OutlinedButton(onClick = onHistorial, Modifier.weight(1f)) { Text("Historial") }
            }
        }
        item { UbicacionSedeCard() }
        item { Text("Profesionales (${profesionales.size})", style = MaterialTheme.typography.titleLarge) }
        items(profesionales, key = { it.id }) { profesional -> ProfesionalCard(profesional, onProfesional) }
        item { ProductosSection(vm.productos) }
        item { PromocionesSection(vm.promociones) }
    }
}

@Composable
private fun ProfesionalCard(profesional: Profesional, onClick: (Profesional) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(profesional.nombre, style = MaterialTheme.typography.titleLarge)
            Text(profesional.especialidad, style = MaterialTheme.typography.titleMedium)
            Text("${profesional.carrera} · ${profesional.aniosExperiencia} años de experiencia aprox.")
            Text("Certificaciones: ${profesional.certificaciones.joinToString()}")
            Text("Capacitaciones: ${profesional.capacitaciones.joinToString()}")
            Text("Modalidad: ${profesional.modalidades.joinToString { if (it == ModalidadAtencion.ONLINE) "Online" else "Presencial" }}")
            AssistChip(onClick = {}, enabled = false, label = { Text("Estado: ${profesional.estado.name.lowercase()}") })
            Button(onClick = { onClick(profesional) }, enabled = profesional.estado == EstadoTerapeuta.DISPONIBLE, Modifier.fillMaxWidth()) { Text("Ver perfil y agendar") }
        }
    }
}

@Composable
private fun ProductosSection(productos: List<Producto>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Catálogo de productos", style = MaterialTheme.typography.titleLarge)
        productos.forEach { p -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(p.nombre, style = MaterialTheme.typography.titleMedium); Text(p.descripcion); Text("Precio referencial: $${p.precioReferencial}") } } }
    }
}

@Composable
private fun PromocionesSection(promociones: List<Promocion>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Promociones", style = MaterialTheme.typography.titleLarge)
        promociones.filter { it.activa }.forEach { p -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(p.titulo, style = MaterialTheme.typography.titleMedium); Text(p.descripcion); Text("Vigencia: ${p.vigencia}") } } }
    }
}
