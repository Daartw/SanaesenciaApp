package cl.sanaesencia.app.ui.auth
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.*
import cl.sanaesencia.app.domain.model.RolUsuario
import kotlinx.coroutines.launch
@Composable fun LoginScreen(onLogged:(RolUsuario)->Unit){
    var email by remember{mutableStateOf("paciente@sanaesencia.demo")}
    var pass by remember{mutableStateOf("Demo123!")}
    var error by remember{mutableStateOf<String?>(null)}
    var loading by remember{mutableStateOf(false)}
    val scope=rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.Center){
        Text("Sanaesencia",style=MaterialTheme.typography.headlineLarge)
        Text("Centro Integrativo")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(email,{email=it},label={Text("Correo")},modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(pass,{pass=it},label={Text("Contraseña")},modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))
        Button(enabled=!loading,onClick={
            scope.launch{
                loading=true; error=null
                runCatching{RetrofitClient.api.login(LoginRequest(email,pass))}
                    .onSuccess{onLogged(it.rol)}
                    .onFailure{error=it.message?:"Error de conexión"}
                loading=false
            }
        },modifier=Modifier.fillMaxWidth()){Text(if(loading)"Ingresando..." else "Iniciar sesión")}
        error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
        Spacer(Modifier.height(12.dp))
        Text("Demo: cualquier rol usa Demo123!")
    }
}
