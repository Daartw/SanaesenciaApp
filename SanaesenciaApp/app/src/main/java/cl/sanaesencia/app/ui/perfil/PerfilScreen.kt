package cl.sanaesencia.app.ui.perfil
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable fun PerfilScreen(onBack:()->Unit){
    Column(Modifier.fillMaxSize().padding(20.dp)){
        Text("Perfil",style=MaterialTheme.typography.headlineSmall)
        Text("Paciente Demo");Text("paciente@sanaesencia.demo")
        Spacer(Modifier.height(20.dp))
        Text("Historial / trazabilidad",style=MaterialTheme.typography.titleMedium)
        Text("El historial consume GET /api/citas/paciente/{id}.")
        Spacer(Modifier.height(20.dp));Button(onBack){Text("Volver")}
    }
}
