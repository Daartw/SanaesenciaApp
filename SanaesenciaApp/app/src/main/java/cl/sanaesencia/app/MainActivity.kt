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

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        container = AppContainer(applicationContext)
        privacyManager = PrivacyManager(applicationContext)

        setContent {
            SanaesenciaTheme {

                val accepted by privacyManager.accepted
                    .collectAsState(initial = false)

                val scope = rememberCoroutineScope()

                if (!accepted) {
                    PrivacyGateScreen(
                        privacyManager = privacyManager,
                        scope = scope
                    )
                } else {
                    SanaesenciaNavGraph(container)
                }
            }
        }
    }
}