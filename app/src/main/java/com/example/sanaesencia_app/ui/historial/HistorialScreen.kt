package com.example.sanaesencia_app.ui.historial

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
import com.example.sanaesencia_app.worker.WorkScheduler
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(context: Context, onVolver: () -> Unit) {
    val vm: HistorialViewModel = viewModel(factory = HistorialViewModel.factory(context))
    val historial by vm.historial.collectAsStateWithLifecycle()
    val riesgo = vm.hayRiesgoDesercion(historial)
    val appContext = LocalContext.current
    Scaffold(topBar = { TopAppBar(title = { Text("Historial y trazabilidad") }, navigationIcon = { TextButton(onClick = onVolver) { Text("Atrás") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (riesgo) item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) { Text("Alerta: se detectó inactividad en el historial demo.", Modifier.padding(16.dp)) } }
            item { OutlinedButton(onClick = { WorkScheduler.ejecutarChequeoAhora(appContext) }, Modifier.fillMaxWidth()) { Text("Ejecutar chequeo de inactividad") } }
            items(historial, key = { it.id }) { HistorialItem(it) }
        }
    }
}

@Composable private fun HistorialItem(cita: Cita) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(cita.especialidad, style = MaterialTheme.typography.titleMedium); Text("${cita.fecha} · ${cita.hora}"); Text("${cita.terapeutaNombre} · ${cita.estado.name}") } } }
