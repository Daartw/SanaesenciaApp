package cl.sanaesencia.app.ui.home
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
@Composable fun HomeScreen(onProf:()->Unit,onShop:()->Unit,onProfile:()->Unit){
    val c=LocalContext.current
    Column(Modifier.fillMaxSize().padding(20.dp)){
        Text("Bienvenido/a a Sanaesencia",style=MaterialTheme.typography.headlineMedium)
        Text("Bienestar integrativo en un solo lugar.")
        Spacer(Modifier.height(20.dp))
        Button(onProf,Modifier.fillMaxWidth()){Text("Profesionales y especialidades")}
        Button(onShop,Modifier.fillMaxWidth()){Text("Tienda")}
        OutlinedButton(onProfile,Modifier.fillMaxWidth()){Text("Perfil e historial")}
        OutlinedButton({c.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/56923665073")))},Modifier.fillMaxWidth()){Text("WhatsApp +56 9 2366 5073")}
        OutlinedButton({c.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=Caupolicán+220,+Los+Vilos")))},Modifier.fillMaxWidth()){Text("Cómo llegar")}
    }
}
