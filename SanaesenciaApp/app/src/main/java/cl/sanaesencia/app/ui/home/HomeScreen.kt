package cl.sanaesencia.app.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cl.sanaesencia.app.services.ContactService
import cl.sanaesencia.app.services.LocationService
import cl.sanaesencia.app.ui.privacy.PrivacyPolicyDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun HomeScreen(
    onProf: () -> Unit,
    onShop: () -> Unit,
    onProfile: () -> Unit,
    onAppointments: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var locationText by remember { mutableStateOf("Ubicación no consultada") }
    var explainPermission by remember { mutableStateOf(false) }
    var deniedForever by remember { mutableStateOf(false) }
    var requestedOnce by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

    fun calculateDistance() {
        LocationService(context).lastLocation { location ->
            if (location == null) {
                locationText = "No se pudo obtener tu ubicación"
                return@lastLocation
            }

            scope.launch {
                val sede = withContext(Dispatchers.IO) {
                    runCatching {
                        Geocoder(context, Locale("es", "CL"))
                            .getFromLocationName(
                                "Av. Caupolicán 220, Los Vilos, Chile",
                                1,
                            )
                            ?.firstOrNull()
                    }.getOrNull()
                }

                locationText = if (sede == null) {
                    "No se pudo geocodificar la sede; usa Cómo llegar"
                } else {
                    val distance = distanceKm(
                        location.latitude,
                        location.longitude,
                        sede.latitude,
                        sede.longitude,
                    )
                    "Estás a %.1f km de Caupolicán 220, Los Vilos".format(distance)
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.any { it }) {
            deniedForever = false
            calculateDistance()
        } else {
            val activity = context as? Activity
            deniedForever = requestedOnce && activity != null && result.keys.any {
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            }
            if (!deniedForever) {
                locationText = "Permiso de ubicación denegado. Puedes intentarlo nuevamente."
            }
        }
    }

    fun requestOrCalculate() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            calculateDistance()
        } else {
            explainPermission = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "Hola, bienvenido/a",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text("¿Cómo te sientes hoy?")
            }

            Icon(
                Icons.Default.PersonSearch,
                contentDescription = "Buscar profesionales",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        OutlinedButton(
            onClick = onProf,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Default.PersonSearch, contentDescription = null)
            Text(
                "Buscar profesional o especialidad",
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Text(
            "Especialidades destacadas",
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = false,
                onClick = onProf,
                label = { Text("Psicología") },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = false,
                onClick = onProf,
                label = { Text("Nutrición") },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = false,
                onClick = onProf,
                label = { Text("Terapias") },
                modifier = Modifier.weight(1f),
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Tu bienestar en buenas manos",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    "Encuentra profesionales, agenda tus horas y gestiona tus productos de bienestar desde un solo lugar.",
                    modifier = Modifier.padding(top = 6.dp),
                )
                Button(
                    onClick = onProf,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .heightIn(min = 48.dp),
                ) {
                    Text("Buscar profesionales")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickActionCard(
                title = "Mis horas",
                icon = Icons.Default.CalendarMonth,
                onClick = onAppointments,
                modifier = Modifier.weight(1f),
            )
            QuickActionCard(
                title = "Tienda",
                icon = Icons.Default.ShoppingBag,
                onClick = onShop,
                modifier = Modifier.weight(1f),
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Text(
                        "  Sede Sanaesencia",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    "Av. Caupolicán 220, Los Vilos",
                    modifier = Modifier.padding(top = 4.dp),
                )
                OutlinedButton(
                    onClick = ::requestOrCalculate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(min = 48.dp),
                ) {
                    Text("Consultar distancia")
                }
                AnimatedContent(
                    targetState = locationText,
                    label = "distancia",
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:0,0?q=Caupolicán+220,+Los+Vilos"),
                            ),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text("Cómo llegar")
                }
            }
        }

        OutlinedButton(
            onClick = { ContactService.openWhatsApp(context) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Icon(Icons.Default.Phone, contentDescription = null)
            Text(
                "Contactar por WhatsApp · +56 9 2366 5073",
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        OutlinedButton(
            onClick = onProfile,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Perfil y consentimiento")
        }

        OutlinedButton(
            onClick = { showPrivacy = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Icon(Icons.Default.PrivacyTip, contentDescription = null)
            Text(
                "Políticas de privacidad y datos",
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Text(
            "Demo académica: utiliza únicamente datos ficticios o anonimizados; no ingreses datos reales.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (explainPermission) {
            AlertDialog(
                onDismissRequest = { explainPermission = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            explainPermission = false
                            requestedOnce = true
                            launcher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                    ) {
                        Text("Permitir")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { explainPermission = false },
                    ) {
                        Text("Ahora no")
                    }
                },
                title = { Text("Ubicación") },
                text = {
                    Text(
                        "Usaremos tu ubicación solo para calcular la distancia hasta la sede de Sanaesencia en Los Vilos y ayudarte a llegar.",
                    )
                },
            )
        }

        if (deniedForever) {
            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}"),
                        ),
                    )
                },
            ) {
                Text("Permiso denegado: abrir ajustes")
            }
        }
    }

    if (showPrivacy) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacy = false },
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .heightIn(min = 112.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null)
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun distanceKm(
    latitudeA: Double,
    longitudeA: Double,
    latitudeB: Double,
    longitudeB: Double,
): Double {
    val radius = 6371.0
    val radians = PI / 180
    val x = sin((latitudeB - latitudeA) * radians / 2).pow(2) +
        cos(latitudeA * radians) *
        cos(latitudeB * radians) *
        sin((longitudeB - longitudeA) * radians / 2).pow(2)
    return 2 * radius * atan2(sqrt(x), sqrt(1 - x))
}
