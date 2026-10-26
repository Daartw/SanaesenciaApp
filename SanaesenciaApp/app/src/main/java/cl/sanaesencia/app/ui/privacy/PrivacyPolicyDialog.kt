package cl.sanaesencia.app.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Términos y Condiciones / Política de Privacidad, accesibles en cualquier momento desde la app. */
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Términos y condiciones") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TerminosLegales.secciones.forEach { seccion ->
                    Text(seccion.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(seccion.texto, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Última actualización: ${TerminosLegales.ACTUALIZACION}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text("Cerrar")
            }
        },
    )
}
