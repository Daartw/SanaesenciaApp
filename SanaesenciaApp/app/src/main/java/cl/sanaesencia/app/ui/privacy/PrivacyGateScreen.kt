package cl.sanaesencia.app.ui.privacy

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.privacy.PrivacyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PrivacyGateScreen(
    privacyManager: PrivacyManager,
    scope: CoroutineScope,
) {
    var acceptedLaws by remember { mutableStateOf(false) }
    var acceptedLocation by remember { mutableStateOf(false) }
    var showPolicy by remember { mutableStateOf(false) }

    // Launcher para solicitar los permisos nativos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Una vez solicitados los permisos (se hayan concedido o no), guardamos la aceptación
        scope.launch {
            privacyManager.accept()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Bienvenido a Sanaesencia",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "Consentimiento de Privacidad y Permisos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cumplimiento Legal y Protección de Datos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SanaesenciaApp cumple con el marco legal chileno sobre protección de datos personales y regulación en salud:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Ley N.º 19.628: Protección de la Vida Privada y Datos Personales.\n" +
                            "• Ley N.º 20.584: Regulación de Derechos y Deberes de los Pacientes en Salud.\n" +
                            "• Ley N.º 21.719: Nueva Ley de Protección de Datos Personales en Chile.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Checkbox 1: Leyes de Privacidad y Salud
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = acceptedLaws,
                onCheckedChange = { acceptedLaws = it }
            )
            Text(
                text = "Acepto los términos sobre tratamiento de datos personales y sensibles conforme a las Leyes N.º 19.628, N.º 20.584 y N.º 21.719.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Checkbox 2: Permisos de Ubicación
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = acceptedLocation,
                onCheckedChange = { acceptedLocation = it }
            )
            Text(
                text = "Autorizo el uso de mi ubicación para encontrar sedes médicas, centros de atención cercanos y calcular distancias.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedButton(
            onClick = { showPolicy = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text("Ver políticas de privacidad")
        }

        Button(
            onClick = {
                if (acceptedLocation) {
                    // Lanza el diálogo nativo de permisos del sistema Android
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    // Si el usuario no marcó ubicación, guarda de todas formas el consentimiento y entra a la app
                    scope.launch {
                        privacyManager.accept()
                    }
                }
            },
            enabled = acceptedLaws,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text("Aceptar y continuar")
        }
    }

    if (showPolicy) {
        PrivacyPolicyDialog(onDismiss = { showPolicy = false })
    }
}
