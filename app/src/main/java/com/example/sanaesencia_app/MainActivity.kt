package com.example.sanaesencia_app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.sanaesencia_app.ui.navigation.SanaesenciaNavGraph
import com.example.sanaesencia_app.ui.theme.SanaesenciaTheme
import com.example.sanaesencia_app.util.NotificationHelper
import com.example.sanaesencia_app.worker.WorkScheduler

class MainActivity : ComponentActivity() {

    // Android 13+ exige pedir permiso en tiempo de ejecución para notificaciones.
    // Si el usuario lo rechaza, la app sigue funcionando normalmente, solo sin alertas push.
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado no bloquea el flujo de la app */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.crearCanal(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Motor de alertas predictivas: programa el chequeo periódico de deserción.
        WorkScheduler.programarChequeoDesercion(this)

        setContent {
            SanaesenciaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SanaesenciaNavGraph()
                }
            }
        }
    }
}
