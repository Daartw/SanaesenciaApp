package com.example.sanaesencia_app.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sanaesencia_app.domain.model.Especialidad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onIrAAgenda: () -> Unit,
    onIrAHistorial: () -> Unit,
    viewModel: CatalogoViewModel = CatalogoViewModel()
) {
    val especialidades by viewModel.especialidades.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Catálogo de especialidades") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onIrAAgenda, modifier = Modifier.fillMaxWidth()) {
                    Text("Ir a mi agenda")
                }
                Button(onClick = onIrAHistorial, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver historial")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(especialidades) { especialidad ->
                    EspecialidadCard(especialidad)
                }
            }
        }
    }
}

@Composable
private fun EspecialidadCard(especialidad: Especialidad) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = especialidad.nombre, style = MaterialTheme.typography.titleMedium)
            Text(text = especialidad.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Profesional: ${especialidad.profesional}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Modalidad: " + especialidad.modalidades.joinToString(" / ") { it.name },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}