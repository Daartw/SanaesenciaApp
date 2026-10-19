package cl.sanaesencia.app.ui.perfil

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import cl.sanaesencia.app.data.remote.PerfilResponse
import cl.sanaesencia.app.presentation.PerfilViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.services.ContactService
import cl.sanaesencia.app.ui.common.CampoFormulario
import cl.sanaesencia.app.ui.common.PantallaEstado
import cl.sanaesencia.app.ui.privacy.PrivacyPolicyDialog
import java.io.File

@Composable
fun PerfilScreen(
    vm: PerfilViewModel,
    onLogout: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showPrivacy by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.cargar()
    }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbar)
        },
    ) { padding ->

        PantallaEstado(
            loading = state is UiState.Cargando,
            error = (state as? UiState.Error)?.mensaje,
            empty = state is UiState.Vacio,
            onRetry = vm::cargar,
        ) {

            val perfil =
                (state as? UiState.Exito<PerfilResponse>)?.datos

            if (perfil != null) {
                PerfilForm(
                    perfil = perfil,
                    vm = vm,
                    onLogout = onLogout,
                    onWhatsApp = {
                        ContactService.openWhatsApp(context)
                    },
                    onPrivacy = {
                        showPrivacy = true
                    },
                    snackbar = snackbar,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    if (showPrivacy) {
        PrivacyPolicyDialog(
            onDismiss = {
                showPrivacy = false
            }
        )
    }
}

@Composable
private fun PerfilForm(
    perfil: PerfilResponse,
    vm: PerfilViewModel,
    onLogout: () -> Unit,
    onWhatsApp: () -> Unit,
    onPrivacy: () -> Unit,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val errors by vm.errores.collectAsState()

    /*
     * IMPORTANTE:
     * PerfilResponse entrega rut y telefono como String?.
     * La pantalla trabaja con String no nullable.
     * Por eso usamos orEmpty().
     */
    var rut by remember {
        mutableStateOf(formatearRut(perfil.rut.orEmpty()))
    }

    var nombre by remember {
        mutableStateOf(perfil.nombreCompleto)
    }

    var telefono by remember {
        mutableStateOf(perfil.telefono.orEmpty())
    }

    var fechaNacimiento by remember {
        mutableStateOf(
            formatearFecha(
                perfil.fechaNacimiento?.format(
                    PerfilViewModel.FECHA_UI
                ).orEmpty()
            )
        )
    }

    var consentimiento by remember {
        mutableStateOf(
            perfil.consentimientoSalud
        )
    }

    var photoPath by remember {
        mutableStateOf(
            loadProfilePhoto(context)
        )
    }

    var photoVersion by remember {
        mutableIntStateOf(0)
    }

    var cameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var photoMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(perfil.id) {
        vm.validarCampoRut(rut)
        vm.validarCampoNombre(nombre)
        vm.validarCampoTelefono(telefono)
        vm.validarCampoFechaNacimiento(fechaNacimiento)
        vm.validarConsentimiento(consentimiento)
    }

    LaunchedEffect(photoMessage) {
        photoMessage?.let {
            snackbar.showSnackbar(it)
            photoMessage = null
        }
    }

    val gallery =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            val copied =
                copyToProfilePhoto(
                    context,
                    uri
                )

            if (
                copied != null &&
                File(copied).exists()
            ) {
                photoPath = copied
                photoVersion++
                photoMessage =
                    "Foto de perfil actualizada"
            } else {
                photoMessage =
                    "No fue posible guardar la foto"
            }
        }

    val camera =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            val source = cameraUri

            if (
                success &&
                source != null
            ) {

                val copied =
                    copyCameraToProfilePhoto(
                        context,
                        source
                    )

                if (
                    copied != null &&
                    File(copied).exists()
                ) {
                    photoPath = copied
                    photoVersion++
                    photoMessage =
                        "Foto de perfil actualizada"
                } else {
                    photoMessage =
                        "No fue posible guardar la foto"
                }
            }
        }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(12.dp),
    ) {

        Text(
            "Perfil",
            style = MaterialTheme.typography.headlineSmall
        )

        // ============================================================
        // FOTO DE PERFIL
        // ============================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.96f
                    )
            ),
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp),
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp),
                ) {

                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                            ),
                        contentAlignment =
                            Alignment.Center,
                    ) {

                        val bitmap =
                            photoPath?.let { path ->
                                remember(
                                    path,
                                    photoVersion
                                ) {
                                    BitmapFactory
                                        .decodeFile(path)
                                }
                            }

                        if (bitmap != null) {

                            Image(
                                bitmap =
                                    bitmap.asImageBitmap(),
                                contentDescription =
                                    "Foto de perfil",
                                modifier =
                                    Modifier.fillMaxSize(),
                                contentScale =
                                    ContentScale.Crop,
                            )

                        } else {

                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription =
                                    "Agregar foto de perfil",
                                modifier =
                                    Modifier.size(38.dp),
                            )
                        }
                    }

                    Column(
                        Modifier.weight(1f)
                    ) {

                        Text(
                            "Foto de perfil",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium
                        )

                        Text(
                            "Opcional",
                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge
                        )

                        Text(
                            "Agrega una foto para personalizar tu cuenta."
                        )
                    }
                }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                ) {

                    Button(
                        onClick = {

                            gallery.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts
                                        .PickVisualMedia
                                        .ImageOnly
                                )
                            )
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .heightIn(
                                    min = 48.dp
                                ),
                    ) {

                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null
                        )

                        Text(
                            "Galería",
                            modifier =
                                Modifier.padding(
                                    start = 6.dp
                                )
                        )
                    }

                    Button(
                        onClick = {

                            val file =
                                File(
                                    context.filesDir,
                                    "camera_profile.jpg"
                                )

                            file.parentFile?.mkdirs()

                            cameraUri =
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file,
                                )

                            cameraUri?.let(
                                camera::launch
                            )
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .heightIn(
                                    min = 48.dp
                                ),
                    ) {

                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null
                        )

                        Text(
                            "Cámara",
                            modifier =
                                Modifier.padding(
                                    start = 6.dp
                                )
                        )
                    }
                }
            }
        }

        // ============================================================
        // DATOS PERSONALES
        // ============================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.96f
                    )
            ),
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),
            ) {

                CampoFormulario(
                    "RUT",
                    rut,
                    { input ->
                        val formatted = formatearRut(input)
                        rut = formatted
                        vm.validarCampoRut(formatted)
                    },
                    errors.rut
                )

                CampoFormulario(
                    "Nombre completo",
                    nombre,
                    {
                        nombre = it
                        vm.validarCampoNombre(it)
                    },
                    errors.nombre
                )

                CampoFormulario(
                    "Teléfono",
                    telefono,
                    {
                        telefono = it
                        vm.validarCampoTelefono(it)
                    },
                    errors.telefono,
                    keyboard = KeyboardType.Phone,
                )

                CampoFormulario(
                    "Fecha de nacimiento",
                    fechaNacimiento,
                    { input ->
                        val formatted = formatearFecha(input)
                        fechaNacimiento = formatted
                        vm.validarCampoFechaNacimiento(formatted)
                    },
                    errors.fechaNacimiento,
                    keyboard = KeyboardType.Number,
                )

                Text(
                    "Formato: dd/MM/yyyy",
                    style =
                        MaterialTheme.typography.bodySmall,
                )

                // ====================================================
                // CONSENTIMIENTO
                // ====================================================

                Text(
                    "Consentimiento de datos de salud",
                    style =
                        MaterialTheme.typography.titleMedium
                )

                Text(
                    "Se guardan solo los datos necesarios para prestar y gestionar tus atenciones."
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = consentimiento,
                        onCheckedChange = {
                            consentimiento = it
                            vm.validarConsentimiento(it)
                        },
                    )

                    Text(
                        "Acepto el tratamiento de mis datos para la atención"
                    )
                }

                errors.consentimiento?.let {

                    Text(
                        it,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }

                // ====================================================
                // GUARDAR
                // ====================================================

                Button(
                    onClick = {
                        vm.guardar(
                            rut,
                            nombre,
                            telefono,
                            fechaNacimiento,
                            consentimiento
                        )
                    },
                    enabled =
                        !errors.tieneErrores &&
                                consentimiento,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = 48.dp
                            ),
                ) {

                    Text("Guardar perfil")
                }
            }
        }

        // ============================================================
        // WHATSAPP
        // ============================================================

        OutlinedButton(
            onClick = onWhatsApp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 48.dp
                    ),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor =
                        androidx.compose.ui.graphics.Color(
                            0xFF1B7F3A
                        )
                ),
        ) {

            Icon(
                Icons.Default.Phone,
                contentDescription = null
            )

            Text(
                "Contactar por WhatsApp · +56 9 2366 5073",
                modifier =
                    Modifier.padding(
                        start = 8.dp
                    )
            )
        }

        // ============================================================
        // PRIVACIDAD
        // ============================================================

        OutlinedButton(
            onClick = onPrivacy,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 48.dp
                    ),
        ) {

            Text(
                "Políticas de privacidad y datos"
            )
        }

        // ============================================================
        // CERRAR SESIÓN
        // ============================================================

        OutlinedButton(
            onClick = onLogout,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 48.dp
                    ),
        ) {

            Icon(
                Icons.Default.Logout,
                contentDescription = null
            )

            Text(
                "Cerrar sesión",
                modifier =
                    Modifier.padding(
                        start = 8.dp
                    )
            )
        }
    }
}

// ============================================================================
// FOTO DE PERFIL
// ============================================================================

private fun profilePhoto(
    context: Context
): File =
    File(
        context.filesDir,
        "profile_photo.jpg"
    )

private fun loadProfilePhoto(
    context: Context
): String? =
    profilePhoto(context)
        .takeIf(File::exists)
        ?.path

private fun copyToProfilePhoto(
    context: Context,
    uri: Uri
): String? =
    runCatching {

        context.contentResolver
            .openInputStream(uri)
            ?.use { input ->

                profilePhoto(context)
                    .outputStream()
                    .use(input::copyTo)
            }

        profilePhoto(context)
            .takeIf(File::exists)
            ?.path

    }
        .getOrNull()
        ?.also {
            require(
                File(it).length() > 0
            )
        }

private fun copyCameraToProfilePhoto(
    context: Context,
    cameraUri: Uri
): String? =
    runCatching {

        context.contentResolver
            .openInputStream(cameraUri)
            ?.use { input ->

                profilePhoto(context)
                    .outputStream()
                    .use(input::copyTo)
            }

        profilePhoto(context)
            .takeIf(File::exists)
            ?.path

    }
        .getOrNull()
        ?.also {
            require(
                File(it).length() > 0
            )
        }

fun formatearRut(raw: String): String {
    val limpio = raw.replace(".", "").replace("-", "").uppercase().filter { it.isDigit() || it == 'K' }
    if (limpio.isEmpty()) return ""
    if (limpio.length == 1) return limpio

    val recortado = if (limpio.length > 9) limpio.substring(0, 9) else limpio
    val dv = recortado.last()
    val cuerpo = recortado.substring(0, recortado.length - 1)

    val sb = StringBuilder()
    var count = 0
    for (i in cuerpo.length - 1 downTo 0) {
        if (count > 0 && count % 3 == 0) {
            sb.append('.')
        }
        sb.append(cuerpo[i])
        count++
    }
    return "${sb.reverse()}-$dv"
}

fun formatearFecha(raw: String): String {
    val digitos = raw.filter { it.isDigit() }.take(8)
    return when {
        digitos.isEmpty() -> ""
        digitos.length <= 2 -> digitos
        digitos.length <= 4 -> "${digitos.substring(0, 2)}/${digitos.substring(2)}"
        else -> "${digitos.substring(0, 2)}/${digitos.substring(2, 4)}/${digitos.substring(4)}"
    }
}