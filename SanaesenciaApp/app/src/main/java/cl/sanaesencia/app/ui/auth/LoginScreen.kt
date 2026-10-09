package cl.sanaesencia.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.LoginResponse
import cl.sanaesencia.app.presentation.LoginViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.CampoFormulario
import cl.sanaesencia.app.ui.privacy.PrivacyPolicyDialog

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLogged: (LoginResponse) -> Unit,
) {
    val email by vm.email.collectAsState()
    val password by vm.password.collectAsState()
    val errors by vm.errors.collectAsState()
    val state by vm.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is UiState.Exito) {
            onLogged((state as UiState.Exito<LoginResponse>).datos)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Sanaesencia",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "Bienestar integral, más cerca de ti",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
                Text("Ingresa con tu correo y contraseña para acceder a la aplicación.")

                CampoFormulario(
                    label = "Correo electrónico",
                    value = email,
                    onValueChange = vm::onEmailChanged,
                    error = errors.email,
                )
                CampoFormulario(
                    label = "Contraseña",
                    value = password,
                    onValueChange = vm::onPasswordChanged,
                    error = errors.password,
                    leading = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            )
                        }
                    },
                    visual = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                )

                Spacer(Modifier.height(4.dp))
                Button(
                    enabled = !errors.hasErrors && email.isNotBlank() && password.isNotBlank() && state !is UiState.Cargando,
                    onClick = vm::login,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    AnimatedContent(
                        targetState = state is UiState.Cargando,
                        label = "login-cargando",
                    ) { loading ->
                        if (loading) {
                            androidx.compose.material3.CircularProgressIndicator()
                        } else {
                            Text("Ingresar")
                        }
                    }
                }

                AnimatedVisibility(state is UiState.Error) {
                    Text(
                        (state as? UiState.Error)?.mensaje.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Text(
                    "Datos demo: paciente@sanaesencia.demo / Demo123! · profesional@sanaesencia.demo / Demo123! · admin@sanaesencia.demo / Demo123!",
                    style = MaterialTheme.typography.bodySmall,
                )

                OutlinedButton(
                    onClick = { showPrivacy = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text("Políticas de privacidad y datos")
                }
            }
        }
    }

    if (showPrivacy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacy = false })
    }
}
