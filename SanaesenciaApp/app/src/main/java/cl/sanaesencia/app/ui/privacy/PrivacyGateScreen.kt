package cl.sanaesencia.app.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.privacy.PrivacyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PrivacyGateScreen(
    privacyManager: PrivacyManager,
    scope: CoroutineScope,
) {
    var accepted by remember { mutableStateOf(false) }
    var showPolicy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Privacidad y datos", style = MaterialTheme.typography.headlineMedium)
        Text("Antes de comenzar")
        Text(
            "SanaesenciaApp es una demostración académica. Para proteger la privacidad, utiliza únicamente datos de prueba anonimizados o ficticios. No ingreses datos personales reales ni información sensible de salud real."
        )
        Text(
            "El diseño considera la Ley N.º 21.719 y la Ley N.º 19.628, con énfasis en finalidad, proporcionalidad, seguridad, transparencia y confidencialidad."
        )
        Text(
            "La ubicación solo se solicita si eliges consultar la distancia a la sede y se explica su finalidad antes de pedir el permiso."
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = accepted,
                onCheckedChange = { accepted = it },
            )
            Text("Confirmo que usaré únicamente datos ficticios o anonimizados en esta demo.")
        }

        OutlinedButton(
            onClick = { showPolicy = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text("Leer políticas de privacidad")
        }

        Button(
            onClick = { scope.launch { privacyManager.accept() } },
            enabled = accepted,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text("Aceptar y continuar")
        }
    }

    if (showPolicy) {
        PrivacyPolicyDialog(onDismiss = { showPolicy = false })
    }
}
