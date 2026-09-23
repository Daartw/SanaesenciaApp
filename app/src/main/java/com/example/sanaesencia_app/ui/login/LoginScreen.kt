package com.example.sanaesencia_app.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sanaesencia_app.domain.model.RolUsuario

@Composable
fun LoginScreen(onLoginSuccess: (RolUsuario) -> Unit) {
    var selectedRole by remember { mutableStateOf(RolUsuario.PACIENTE) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sanaesencia", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Servicios terapéuticos, bienestar y agendamiento", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        Text("Selecciona un perfil de demostración", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        RolUsuario.values().forEach { role ->
            FilterChip(
                selected = selectedRole == role,
                onClick = { selectedRole = role },
                label = { Text(role.name.lowercase().replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onLoginSuccess(selectedRole) }, Modifier.fillMaxWidth()) { Text("Ingresar como ${selectedRole.name.lowercase()}") }
        Text("Datos de demostración anonimizados", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 16.dp))
    }
}
