package cl.sanaesencia.app.ui.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Políticas de privacidad y datos") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    "SanaesenciaApp es una demostración académica. Esta versión utiliza únicamente datos de prueba, anonimizados o ficticios. No se deben ingresar datos personales reales ni información sensible de salud real."
                )
                Text(
                    "\nMarco de referencia\nLa aplicación se diseña considerando la Ley N.º 19.628 y la Ley N.º 21.719 de Chile, priorizando licitud, finalidad, proporcionalidad, seguridad, transparencia y confidencialidad. La Ley N.º 21.719 entra en vigencia el 1 de diciembre de 2026."
                )
                Text(
                    "\nDatos de demostración\nLos usuarios, profesionales, productos, citas y demás registros usados por la demo son ficticios. La aplicación no debe utilizarse para registrar fichas clínicas reales ni información sanitaria real."
                )
                Text(
                    "\nUbicación\nLa ubicación solo se solicita cuando eliges consultar la distancia a la sede. Antes de pedir el permiso, la aplicación explica su finalidad. La ubicación no se envía al backend de Sanaesencia; se usa localmente para calcular una distancia aproximada y para abrir una ruta."
                )
                Text(
                    "\nWhatsApp\nEl botón de WhatsApp abre la aplicación o navegador mediante un enlace externo. No se envían datos clínicos ni información sensible de salud desde SanaesenciaApp."
                )
                Text(
                    "\nConsentimiento\nEl consentimiento para datos de salud se muestra como parte del flujo demostrativo. En esta versión académica debe utilizarse exclusivamente con información ficticia. La política completa y el aviso de demostración permanecen accesibles desde la aplicación."
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
