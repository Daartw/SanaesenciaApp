
package cl.sanaesencia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import cl.sanaesencia.app.data.privacy.PrivacyManager
import cl.sanaesencia.app.ui.navigation.SanaesenciaNavGraph
import cl.sanaesencia.app.ui.privacy.PrivacyGateScreen
import cl.sanaesencia.app.ui.theme.SanaesenciaTheme

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer
    private lateinit var privacyManager: PrivacyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        container = AppContainer(applicationContext)
        privacyManager = PrivacyManager(applicationContext)

        setContent {
            SanaesenciaTheme {
                // null = todavía leyendo la preferencia (evita que la pantalla de privacidad
                // aparezca un instante cada vez que se abre la app).
                val accepted by privacyManager.accepted
                    .collectAsState<Boolean?>(initial = null)

                val scope = rememberCoroutineScope()

                when (accepted) {
                    null -> Unit
                    false -> PrivacyGateScreen(
                        privacyManager = privacyManager,
                        scope = scope
                    )
                    true -> SanaesenciaNavGraph(
                        container = container
                    )
                }
            }
        }
    }
}
