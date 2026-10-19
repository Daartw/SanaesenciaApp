package cl.sanaesencia.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CampoFormulario(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    leading: (@Composable (() -> Unit))? = null,
    trailing: (@Composable (() -> Unit))? = null,
    visual: VisualTransformation = VisualTransformation.None,
    keyboard: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val defaultLeading: @Composable () -> Unit = {
        Icon(formIcon(label), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
    val errorTrailing: @Composable () -> Unit = {
        if (error != null) {
            Icon(Icons.Default.Error, contentDescription = error, tint = MaterialTheme.colorScheme.error)
        } else if (trailing != null) {
            trailing()
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leading ?: defaultLeading,
        trailingIcon = errorTrailing,
        isError = error != null,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
        ),
        supportingText = {
            AnimatedVisibility(
                visible = error != null,
                enter = expandVertically(animationSpec = tween(250)) +
                        fadeIn(animationSpec = tween(250)),
            ) {
                Text(
                    text = error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        visualTransformation = visual,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = modifier.heightIn(min = 64.dp),
    )
}

private fun formIcon(label: String) = when {
    label.contains("correo", true) || label.contains("email", true) -> Icons.Default.Email
    label.contains("contraseña", true) || label.contains("password", true) || label.contains("cvv", true) -> Icons.Default.Lock
    label.contains("rut", true) -> Icons.Default.Badge
    label.contains("teléfono", true) || label.contains("telefono", true) -> Icons.Default.Phone
    label.contains("fecha", true) -> Icons.Default.CalendarMonth
    label.contains("titular", true) -> Icons.Default.Person
    label.contains("tarjeta", true) -> Icons.Default.CreditCard
    label.contains("precio", true) -> Icons.Default.AttachMoney
    label.contains("stock", true) -> Icons.Default.Numbers
    label.contains("producto", true) || label.contains("nombre", true) -> Icons.Default.Inventory
    label.contains("id", true) -> Icons.Default.Numbers
    label.contains("título", true) || label.contains("especialidad", true) -> Icons.Default.Person
    else -> Icons.Default.AccountCircle
}

@Composable
fun PantallaEstado(
    loading: Boolean,
    error: String?,
    empty: Boolean,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val state = when {
        loading -> "loading"
        error != null -> "error"
        empty -> "empty"
        else -> "content"
    }
    Crossfade(
        targetState = state,
        animationSpec = tween(300),
        label = "estado-pantalla",
    ) { current ->
        when (current) {
            "loading" -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Cargando información...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            "error" -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = "Ocurrió un inconveniente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (onRetry != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onRetry,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            "empty" -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(56.dp),
                    )
                    Text(
                        text = "No hay registros disponibles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> content()
        }
    }
}
