package cl.sanaesencia.app.ui.privacy

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.sanaesencia.app.data.privacy.PrivacyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Primera pantalla de la app (RNF-09):
 *  Paso 1 — Términos y Condiciones según las Leyes 19.628, 20.584 y 21.719 (obligatorio).
 *  Paso 2 — Permiso de ubicación para la sede de Los Vilos (RF-15), con el diálogo nativo de Android.
 * Hasta completar ambos pasos no se puede usar la aplicación.
 */
@Composable
fun PrivacyGateScreen(
    privacyManager: PrivacyManager,
    scope: CoroutineScope,
) {
    val context = LocalContext.current
    var paso by remember { mutableStateOf(1) }

    fun ubicacionConcedida() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun finalizar() {
        scope.launch { privacyManager.accept() }
    }

    // Diálogo nativo de permisos de Android. Se acepte o no, se entra a la app (la ubicación es opcional).
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ -> finalizar() }

    when (paso) {
        1 -> PasoTerminos(
            onAceptar = {
                if (ubicacionConcedida()) finalizar() else paso = 2
            },
        )
        else -> PasoUbicacion(
            onPermitir = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            },
            onAhoraNo = { finalizar() },
        )
    }
}

@Composable
private fun PasoTerminos(onAceptar: () -> Unit) {
    var aceptaTerminos by remember { mutableStateOf(false) }
    var aceptaDatosSalud by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoPaso(Icons.Default.Shield)
            Spacer(Modifier.size(12.dp))
            Column {
                Text("Paso 1 de 2", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Términos y condiciones", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            "Antes de usar Sanaesencia, lee cómo protegemos tus datos según la Ley N.º 19.628, " +
                "la Ley N.º 20.584 y la Ley N.º 21.719.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Texto legal desplazable
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminosLegales.secciones.forEachIndexed { index, seccion ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(seccion.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(seccion.texto, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Última actualización: ${TerminosLegales.ACTUALIZACION}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        CasillaConsentimiento(
            marcada = aceptaTerminos,
            onCambio = { aceptaTerminos = it },
            texto = "He leído y acepto los Términos y Condiciones y la Política de Privacidad.",
        )
        CasillaConsentimiento(
            marcada = aceptaDatosSalud,
            onCambio = { aceptaDatosSalud = it },
            texto = "Autorizo el tratamiento de mis datos de salud (datos sensibles) para mi atención, " +
                "conforme a las Leyes N.º 19.628, 20.584 y 21.719.",
        )

        Button(
            onClick = onAceptar,
            enabled = aceptaTerminos && aceptaDatosSalud,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
        ) {
            Text("Aceptar y continuar", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PasoUbicacion(
    onPermitir: () -> Unit,
    onAhoraNo: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Paso 2 de 2", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "¿Nos permites usar tu ubicación?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "La usamos solo para calcular la distancia a nuestra sede en Caupolicán 220, Los Vilos, " +
                "y guiarte para llegar. Se procesa en tu teléfono y no se guarda en nuestros servidores.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onPermitir,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
        ) {
            Text("Permitir ubicación", fontWeight = FontWeight.SemiBold)
        }
        TextButton(
            onClick = onAhoraNo,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Ahora no")
        }
        Text(
            "Puedes cambiar este permiso cuando quieras en los ajustes de Android.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CasillaConsentimiento(
    marcada: Boolean,
    onCambio: (Boolean) -> Unit,
    texto: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Checkbox(checked = marcada, onCheckedChange = onCambio)
        Text(texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun IconoPaso(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}
