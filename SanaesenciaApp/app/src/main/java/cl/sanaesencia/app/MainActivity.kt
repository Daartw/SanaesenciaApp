package cl.sanaesencia.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cl.sanaesencia.app.ui.navigation.SanaesenciaNavGraph
import cl.sanaesencia.app.ui.theme.SanaesenciaTheme
class MainActivity:ComponentActivity(){
    override fun onCreate(state:Bundle?){
        super.onCreate(state)
        setContent{ SanaesenciaTheme{ SanaesenciaNavGraph() } }
    }
}
