package com.example.sanaesencia_app.ui.catalogo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

// Coordenadas aproximadas de la sede: Caupolicán 220, Los Vilos, Chile.
// TODO: ajustar a la geolocalización exacta cuando se confirme con el centro.
private const val SEDE_LAT = -31.909816212777898
private const val SEDE_LNG = -71.5124847104571
private const val SEDE_NOMBRE = "Sanaesencia"

/**
 * Cumple el requerimiento del caso: "Servicios de localización: uso de GPS
 * para orientar la llegada a la sede presencial y para diferenciar la oferta
 * entre atención presencial u online".
 */
@Composable
fun UbicacionSedeCard() {
    val context = LocalContext.current
    var distanciaKm by remember { mutableStateOf<Double?>(null) }
    var permisoDenegado by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            permisoDenegado = false
            obtenerDistanciaALaSede(context) { distanciaKm = it }
        } else {
            permisoDenegado = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Sede presencial", style = MaterialTheme.typography.titleMedium)
            Text(text = "Caupolicán 220, Los Vilos", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            distanciaKm?.let { km ->
                Text(
                    text = "Estás a %.1f km de la sede.".format(km),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (km <= 50)
                        "Sugerencia: tienes la sede cerca, puedes agendar atención presencial."
                    else
                        "Sugerencia: dada la distancia, te recomendamos modalidad online.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (permisoDenegado) {
                Text(
                    text = "Sin permiso de ubicación. Puedes agendar en modalidad online igualmente.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        obtenerDistanciaALaSede(context) { distanciaKm = it }
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }) {
                    Text("Calcular distancia")
                }

                Button(onClick = {
                    val uri = Uri.parse("geo:$SEDE_LAT,$SEDE_LNG?q=$SEDE_LAT,$SEDE_LNG($SEDE_NOMBRE)")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }) {
                    Text("Cómo llegar")
                }
            }
        }
    }
}

@SuppressLint("MissingPermission") // Se verifica el permiso antes de llamar a esta función.
private fun obtenerDistanciaALaSede(context: Context, onResult: (Double) -> Unit) {
    val tienePermiso = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!tienePermiso) return

    val client = LocationServices.getFusedLocationProviderClient(context)
    client.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            val sede = Location("sede").apply {
                latitude = SEDE_LAT
                longitude = SEDE_LNG
            }
            onResult(location.distanceTo(sede) / 1000.0)
        }
    }
}
