package com.example.sanaesencia_app.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sanaesencia_app.domain.model.Cita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onVolver: () -> Unit,
    viewModel: HistorialViewModel = HistorialViewModel()
) {
    val historial by viewModel.historial.collectAsState()
    val riesgoDesercion = viewModel.hayRiesgoDesercion()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial del paciente") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (riesgoDesercion) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFB26A00))
                        Text(
                            text = "Alerta: posible riesgo de deserción detectado en este paciente.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historial) { cita ->
                    HistorialItem(cita)
                }
            }
        }
    }
}

@Composable
private fun HistorialItem(cita: Cita) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = cita.especialidad, style = MaterialTheme.typography.titleMedium)
            Text(text = "${cita.fecha} · ${cita.hora}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Estado: ${cita.estado.name}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}