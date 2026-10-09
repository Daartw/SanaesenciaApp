package cl.sanaesencia.app.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
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
        Icon(formIcon(label), contentDescription = null)
    }
    val errorTrailing: @Composable () -> Unit = {
        if (error != null) {
            Icon(Icons.Default.Error, contentDescription = error)
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
        supportingText = {
            AnimatedVisibility(
                visible = error != null,
                enter = expandVertically(animationSpec = androidx.compose.animation.core.tween(250)) + fadeIn(animationSpec = androidx.compose.animation.core.tween(250)),
            ) {
                Text(error.orEmpty())
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
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "estado-pantalla",
    ) { current ->
        when (current) {
            "loading" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            "error" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    if (onRetry != null) {
                        TextButton(
                            onClick = onRetry,
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            "empty" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay información disponible")
            }
            else -> content()
        }
    }
}
