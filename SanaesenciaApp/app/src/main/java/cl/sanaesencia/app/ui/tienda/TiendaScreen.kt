package cl.sanaesencia.app.ui.tienda
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.*
@Composable fun TiendaScreen(onBack:()->Unit){
    var data by remember{mutableStateOf<List<ProductoResponse>>(emptyList())}
    LaunchedEffect(Unit){runCatching{RetrofitClient.api.productos()}.onSuccess{data=it}}
    Column(Modifier.fillMaxSize().padding(16.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            Text("Tienda",style=MaterialTheme.typography.headlineSmall);TextButton(onBack){Text("Volver")}
        }
        LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){
            items(data){p->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){
                Text(p.nombre,style=MaterialTheme.typography.titleLarge);Text(p.descripcion)
                Text("$${"%,.0f".format(p.precio)}")
                Text(if(p.stock<=3)"⚠ Stock bajo: ${p.stock}" else "Stock: ${p.stock}")
                Text("Etiquetas: ${p.etiquetasTerapeuticas}")
                Button({}){Text("Agregar al carrito")}
            }}}
        }
    }
}
