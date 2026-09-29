package cl.sanaesencia.app.ui.admin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.RetrofitClient
@Composable fun AdminDashboardScreen(){
    var stock by remember{mutableStateOf(0)}
    var boxes by remember{mutableStateOf(0)}
    LaunchedEffect(Unit){runCatching{Triple(RetrofitClient.api.productos().count{it.stock<=3},RetrofitClient.api.boxes().count{it.estaHabilitado},0)}.onSuccess{stock=it.first;boxes=it.second}}
    Column(Modifier.fillMaxSize().padding(20.dp)){
        Text("Dashboard Admin",style=MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){
            Text("Boxes habilitados: $boxes / 8");Text("Stock bajo: $stock")
            Text("Deserción: endpoint /admin/desercion")
        }}
        Spacer(Modifier.height(16.dp))
        listOf("Gestionar 8 Boxes","Equipo y disponibilidad","Tienda y stock","Deserción y seguimiento","Promociones","Control global de citas").forEach{
            Button({},Modifier.fillMaxWidth()){Text(it)}
        }
    }
}
