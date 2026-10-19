package cl.sanaesencia.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.LoginResponse
import cl.sanaesencia.app.presentation.LoginViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.CampoFormulario
import cl.sanaesencia.app.ui.privacy.PrivacyPolicyDialog

@OptIn(ExperimentalLayoutApi::class)
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
        (state as? UiState.Exito<LoginResponse>)?.datos?.let { response ->
            onLogged(response)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // BRANDING BADGE & TITLE
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Sanaesencia",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Salud Integrativa & Bienestar",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))

            // LOGIN CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "Ingresa con tu correo y contraseña para acceder.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

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
                        leading = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailing = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        visual = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    )

                    Spacer(Modifier.height(4.dp))

                    Button(
                        enabled = !errors.hasErrors && email.isNotBlank() && password.isNotBlank() && state !is UiState.Cargando,
                        onClick = vm::login,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                    ) {
                        AnimatedContent(
                            targetState = state is UiState.Cargando,
                            label = "login-cargando",
                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                Text(
                                    text = "Ingresar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = state is UiState.Error) {
                        Text(
                            text = (state as? UiState.Error)?.mensaje.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    // DEMO ACCOUNT QUICK CHIPS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Acceso rápido Demo:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = email == "paciente@sanaesencia.demo",
                                onClick = {
                                    vm.onEmailChanged("paciente@sanaesencia.demo")
                                    vm.onPasswordChanged("Demo123!")
                                },
                                label = { Text("Paciente") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                            FilterChip(
                                selected = email == "profesional@sanaesencia.demo",
                                onClick = {
                                    vm.onEmailChanged("profesional@sanaesencia.demo")
                                    vm.onPasswordChanged("Demo123!")
                                },
                                label = { Text("Profesional") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                            FilterChip(
                                selected = email == "admin@sanaesencia.demo",
                                onClick = {
                                    vm.onEmailChanged("admin@sanaesencia.demo")
                                    vm.onPasswordChanged("Demo123!")
                                },
                                label = { Text("Admin") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = { showPrivacy = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Políticas de Privacidad y Datos")
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Demo académica · Sanaesencia © 2025",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (showPrivacy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacy = false })
    }
}
