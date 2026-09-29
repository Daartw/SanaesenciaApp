package cl.sanaesencia.app.ui.profesionales
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.*
@Composable fun ProfesionalesScreen(onBack:()->Unit){
    var data by remember{mutableStateOf<List<ProfesionalResponse>>(emptyList())}
    var specialty by remember{mutableStateOf("Todas")}
    LaunchedEffect(Unit){runCatching{RetrofitClient.api.profesionales()}.onSuccess{data=it}}
    val filtered=data.filter{specialty=="Todas"||it.categoriaEspecialidad.equals(specialty,true)}
    Column(Modifier.fillMaxSize().padding(16.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            Text("Profesionales",style=MaterialTheme.typography.headlineSmall);TextButton(onBack){Text("Volver")}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            listOf("Todas","Psicología","Fonoaudiología").forEach{
                FilterChip(specialty==it,{specialty=it},{Text(it)})
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){
            items(filtered){p->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){
                Text(p.titulo,style=MaterialTheme.typography.titleLarge)
                Text(p.categoriaEspecialidad);Text(p.descripcionEnfoque);Text("Estado: ${p.estado}")
                Button({}){Text("Ver disponibilidad / Agendar")}
            }}}
        }
    }
}
