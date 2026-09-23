package com.example.sanaesencia_app.ui.historial

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.worker.WorkScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    context: Context,
    onVolver: () -> Unit
) {
    val vm: HistorialViewModel = viewModel(
        factory = HistorialViewModel.factory(context)
    )

    val historial by vm.historial.collectAsState(initial = emptyList())

    val riesgo = vm.hayRiesgoDesercion(historial)

    val appContext = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Historial y trazabilidad")
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

            if (riesgo) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Alerta: se detectó inactividad en el historial demo.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        WorkScheduler.ejecutarChequeoAhora(appContext)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ejecutar chequeo de inactividad")
                }
            }

            items(
                items = historial,
                key = { it.id }
            ) { cita ->
                HistorialItem(cita)
            }
        }
    }
}

@Composable
private fun HistorialItem(cita: Cita) {
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
                text = "${cita.fecha} · ${cita.hora}"
            )

            Text(
                text = "${cita.terapeutaNombre} · ${cita.estado.name}"
            )
        }
    }
}