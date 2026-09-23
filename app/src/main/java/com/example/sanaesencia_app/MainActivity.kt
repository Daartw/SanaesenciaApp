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
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.crearCanal(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !getPreferences(MODE_PRIVATE).getBoolean("notification_permission_requested", false)
        ) {
            getPreferences(MODE_PRIVATE).edit().putBoolean("notification_permission_requested", true).apply()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        WorkScheduler.programarChequeoDesercion(this)
        setContent {
            SanaesenciaTheme { Surface(Modifier.fillMaxSize()) { SanaesenciaNavGraph() } }
        }
    }
}
