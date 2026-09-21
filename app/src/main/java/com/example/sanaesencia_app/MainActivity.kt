package com.example.sanaesencia_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.sanaesencia_app.ui.navigation.SanaesenciaNavGraph
import com.example.sanaesencia_app.ui.theme.SanaesenciaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SanaesenciaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SanaesenciaNavGraph()
                }
            }
        }
    }
}
