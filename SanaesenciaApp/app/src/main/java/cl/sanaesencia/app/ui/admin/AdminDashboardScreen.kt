@file:OptIn(ExperimentalMaterial3Api::class)

package cl.sanaesencia.app.ui.admin

import android.util.Log

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import cl.sanaesencia.app.data.remote.BoxResponse
import cl.sanaesencia.app.data.remote.DesercionResponse
import cl.sanaesencia.app.data.remote.EstadoBoxRequest
import cl.sanaesencia.app.data.remote.ProductoAdminRequest
import cl.sanaesencia.app.data.remote.ProductoResponse
import cl.sanaesencia.app.data.remote.ProfesionalAdminRequest
import cl.sanaesencia.app.data.remote.ProfesionalResponse
import cl.sanaesencia.app.data.remote.SanaesenciaApi
import cl.sanaesencia.app.data.repository.CatalogRepository
import cl.sanaesencia.app.domain.model.EstadoProfesional
import cl.sanaesencia.app.domain.usecase.VerificarStockBajoUseCase
import cl.sanaesencia.app.domain.validacion.ResultadoValidacion
import cl.sanaesencia.app.domain.validacion.ValidadorEmail
import cl.sanaesencia.app.domain.validacion.ValidadorIdPositivo
import cl.sanaesencia.app.domain.validacion.ValidadorNombre
import cl.sanaesencia.app.domain.validacion.ValidadorPrecio
import cl.sanaesencia.app.domain.validacion.ValidadorStock
import cl.sanaesencia.app.domain.validacion.ValidadorTextoMin3
import cl.sanaesencia.app.presentation.BaseViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.CampoFormulario
import cl.sanaesencia.app.ui.common.PantallaEstado
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import cl.sanaesencia.app.data.remote.EstadoProfesionalRequest
import cl.sanaesencia.app.data.remote.EstadoProductoRequest
import cl.sanaesencia.app.data.remote.mensajeUsuario
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/* ================================================================= */
/* MODELOS                                                           */
/* ================================================================= */

data class AdminData(
    val boxes: List<BoxResponse>,
    val products: List<ProductoResponse>,
    val professionals: List<ProfesionalResponse>,
    val desertion: List<DesercionResponse>,
)

data class AdminFormErrors(
    val name: String? = null,
    val title: String? = null,
    val category: String? = null,
    val price: String? = null,
    val stock: String? = null,
    val userId: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(name, title, category, price, stock, userId, fullName, email, password).any { it != null }
}

data class ProfesionalCreado(val nombre: String, val email: String, val password: String)

const val CONTRASENA_INICIAL = "Demo123!"

enum class AdminSection(val label: String, val icon: ImageVector) {
    INICIO("Inicio", Icons.Default.Home),
    BOXES("Boxes", Icons.Default.MeetingRoom),
    TIENDA("Tienda", Icons.Default.Store),
    PROFESIONALES("Profesionales", Icons.Default.Group),
}

/* ================================================================= */
/* VIEWMODEL                                                         */
/* ================================================================= */

class AdminViewModel(
    private val api: SanaesenciaApi,
    private val stockBajo: VerificarStockBajoUseCase = VerificarStockBajoUseCase(),
    private val catalogRepository: CatalogRepository? = null,
) : BaseViewModel() {

    val state = MutableStateFlow<UiState<AdminData>>(UiState.Cargando)
    val message = MutableStateFlow<String?>(null)

    private val _errors = MutableStateFlow(AdminFormErrors())
    val errors: StateFlow<AdminFormErrors> = _errors.asStateFlow()

    /* ----------------------------- Carga ------------------------- */

    /** Carga completa: muestra el estado "Cargando" (primera vez / reintentar). */
    fun cargar() {
        viewModelScope.launch {
            ejecutar(state) { fetchAll() }
        }
    }

    /** Recarga en segundo plano: NO pasa por "Cargando", así la UI no parpadea. */
    private suspend fun refrescar() {
        try {
            state.value = UiState.Exito(fetchAll())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Se conserva el dato anterior; el error ya se informa en la acción que lo originó.
        }
    }

    /**
     * Carga todo el panel. Si el token expiró o la cuenta no es ADMIN, el error se propaga
     * (antes se ocultaba y el panel aparecía vacío sin explicación).
     */
    private suspend fun fetchAll(): AdminData = coroutineScope {
        val boxes = async { api.adminBoxes() }
        // El admin ve TODOS los productos, incluidos los deshabilitados.
        val products = async { api.adminProductos() }
        val professionals = async { api.adminProfesionales() }
        val desertion = async { api.adminDesercion() }
        AdminData(
            boxes = boxes.await(),
            products = products.await(),
            professionals = professionals.await(),
            desertion = desertion.await(),
        )
    }

    fun clearMessage() {
        message.value = null
    }

    private fun datosActuales(): AdminData? =
        (state.value as? UiState.Exito<AdminData>)?.datos

    /* ----------------------------- Boxes ------------------------- */

    /** Actualización optimista: cambia la tarjeta al instante y revierte si falla. */
    fun toggleBox(box: BoxResponse) {
        val nuevo = !box.estaHabilitado
        actualizarBoxLocal(box.id, nuevo)
        viewModelScope.launch {
            runCatching {
                api.adminBox(box.id, EstadoBoxRequest(nuevo))
            }.onFailure {
                actualizarBoxLocal(box.id, !nuevo)
                message.value = it.mensajeUsuario("No fue posible actualizar el box")
            }
        }
    }

    private fun actualizarBoxLocal(id: Any, habilitado: Boolean) {
        val actual = datosActuales() ?: return
        state.value = UiState.Exito(
            actual.copy(
                boxes = actual.boxes.map {
                    if (it.id == id) it.copy(estaHabilitado = habilitado) else it
                },
            ),
        )
    }

    /* --------------------------- Validación ---------------------- */

    fun validateProduct(name: String, price: String, stock: String) {
        _errors.value = _errors.value.copy(
            name = invalid(ValidadorNombre()(name)),
            price = invalid(ValidadorPrecio()(price)),
            stock = invalid(ValidadorStock()(stock)),
        )
    }

    fun validateProfessional(
        fullName: String,
        email: String,
        password: String,
        title: String,
        category: String,
    ) {
        _errors.value = _errors.value.copy(
            userId = null,
            fullName = invalid(ValidadorNombre()(fullName)),
            email = invalid(ValidadorEmail()(email)),
            password = if (password.isBlank() || password.length >= 6) null else "Mínimo 6 caracteres",
            title = invalid(ValidadorTextoMin3()(title)),
            category = invalid(ValidadorTextoMin3()(category)),
        )
    }

    fun isStockBajo(stock: Int): Boolean = stockBajo(stock)

    fun clearFormErrors() {
        _errors.value = AdminFormErrors()
    }

    /* ------------------------- Profesionales --------------------- */

    /**
     * Crea el profesional con nombre y correo. Si la persona no tiene cuenta, el backend la crea
     * (rol PROFESIONAL) con la contraseña indicada o "Demo123!". No hace falta registrarla antes.
     */
    fun addProfessional(
        fullName: String,
        email: String,
        password: String,
        title: String,
        category: String,
    ) {
        validateProfessional(fullName, email, password, title, category)
        if (_errors.value.hasErrors) return

        message.value = null
        viewModelScope.launch {
            runCatching {
                val nuevo = api.adminCrearProfesional(
                    ProfesionalAdminRequest(
                        nombreCompleto = fullName.trim(),
                        email = email.trim().lowercase(),
                        password = password.ifBlank { null },
                        titulo = title.trim(),
                        categoriaEspecialidad = category.trim(),
                        descripcionEnfoque = "Atención integrativa",
                        certificaciones = "Certificación Clínica",
                        anosExperiencia = 3,
                        estado = EstadoProfesional.DISPONIBLE,
                    ),
                )
                runCatching { catalogRepository?.saveProfesional(nuevo) }
                refrescar()
            }.onSuccess {
                ultimoProfesionalCreado.value = ProfesionalCreado(
                    nombre = fullName.trim(),
                    email = email.trim().lowercase(),
                    password = password.ifBlank { CONTRASENA_INICIAL },
                )
                message.value = "Profesional creado"
            }.onFailure {
                message.value = it.mensajeUsuario("No se pudo crear el profesional")
            }
        }
    }

    /** Datos de acceso del último profesional creado, para mostrarlos al admin una vez. */
    val ultimoProfesionalCreado = MutableStateFlow<ProfesionalCreado?>(null)

    fun cerrarCredenciales() {
        ultimoProfesionalCreado.value = null
    }

    fun cambiarEstadoProfesional(profesional: ProfesionalResponse, estado: EstadoProfesional) {
        viewModelScope.launch {
            runCatching {
                val actualizado = api.adminEstadoProfesional(profesional.id, EstadoProfesionalRequest(estado.name))
                catalogRepository?.saveProfesional(actualizado)
                refrescar()
            }.onSuccess {
                message.value = "Estado actualizado"
            }.onFailure {
                message.value = it.mensajeUsuario("No fue posible actualizar el estado del profesional")
            }
        }
    }

    /* ----------------------------- Productos --------------------- */

    fun addProduct(name: String, priceText: String, stockText: String) {
        validateProduct(name, priceText, stockText)

        val price = priceText.toLongOrNull() ?: return
        val stock = stockText.toIntOrNull() ?: return
        if (_errors.value.hasErrors) return

        message.value = null
        viewModelScope.launch {
            runCatching {
                val nuevo = api.adminCrearProducto(
                    ProductoAdminRequest(
                        nombre = name.trim(),
                        descripcion = "Producto administrado",
                        precio = price,
                        stock = stock,
                        imagenUrl = null,
                        etiquetasTerapeuticas = "bienestar",
                    ),
                )
                runCatching { catalogRepository?.saveProducto(nuevo) }
                runCatching { catalogRepository?.refresh() }
                refrescar()
                message.value = "Producto creado"
            }.onFailure {
                message.value = it.mensajeUsuario("No fue posible crear el producto")
            }
        }
    }

    fun editProduct(
        product: ProductoResponse,
        name: String,
        priceText: String,
        stockText: String,
    ) {
        validateProduct(name, priceText, stockText)

        val price = priceText.toLongOrNull() ?: return
        val stock = stockText.toIntOrNull() ?: return
        if (_errors.value.hasErrors) return

        message.value = null
        viewModelScope.launch {
            runCatching {
                val editado = api.adminEditarProducto(
                    product.id,
                    ProductoAdminRequest(
                        nombre = name.trim(),
                        descripcion = product.descripcion,
                        precio = price,
                        stock = stock,
                        imagenUrl = product.imagenUrl,
                        etiquetasTerapeuticas = product.etiquetasTerapeuticas,
                        // Si estaba deshabilitado por falta de stock y ahora tiene stock, se vuelve a habilitar.
                        estaActivo = product.estaActivo || (product.stock <= 0 && stock > 0),
                    ),
                )
                runCatching { catalogRepository?.saveProducto(editado) }
                runCatching { catalogRepository?.refresh() }
                refrescar()
                message.value = "Producto actualizado"
            }.onFailure {
                message.value = it.mensajeUsuario("No fue posible actualizar el producto")
            }
        }
    }

    /** Habilita o deshabilita un producto (se ve o no en la tienda). Sin stock no se puede habilitar. */
    fun cambiarActivoProducto(product: ProductoResponse, activo: Boolean) {
        actualizarProductoLocal(product.copy(estaActivo = activo))
        viewModelScope.launch {
            runCatching { api.adminActivarProducto(product.id, EstadoProductoRequest(activo)) }
                .onSuccess {
                    actualizarProductoLocal(it)
                    runCatching { catalogRepository?.refresh() }
                    message.value = if (activo) "Producto habilitado" else "Producto deshabilitado"
                }
                .onFailure {
                    actualizarProductoLocal(product)
                    message.value = it.mensajeUsuario("No fue posible cambiar el estado del producto")
                }
        }
    }

    fun eliminarProducto(product: ProductoResponse) {
        viewModelScope.launch {
            runCatching { api.adminEliminarProducto(product.id) }
                .onSuccess {
                    val actual = datosActuales()
                    if (actual != null) {
                        state.value = UiState.Exito(actual.copy(products = actual.products.filterNot { it.id == product.id }))
                    }
                    runCatching { catalogRepository?.refresh() }
                    message.value = "Producto eliminado"
                }
                .onFailure { message.value = it.mensajeUsuario("No fue posible eliminar el producto") }
        }
    }

    private fun actualizarProductoLocal(product: ProductoResponse) {
        val actual = datosActuales() ?: return
        state.value = UiState.Exito(
            actual.copy(products = actual.products.map { if (it.id == product.id) product else it }),
        )
    }

    private fun invalid(result: ResultadoValidacion): String? =
        (result as? ResultadoValidacion.Invalido)?.mensaje
}

/* ================================================================= */
/* PANTALLA PRINCIPAL                                                */
/* ================================================================= */

@Composable
fun AdminDashboardScreen(
    vm: AdminViewModel,
    onLogout: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val message by vm.message.collectAsState()
    val creado by vm.ultimoProfesionalCreado.collectAsState()

    var section by rememberSaveable { mutableStateOf(AdminSection.INICIO) }
    var showProduct by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductoResponse?>(null) }
    var showProfessional by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.cargar() }

    // Los diálogos se cierran solo después de que la operación haya tenido éxito.
    LaunchedEffect(message) {
        val current = message ?: return@LaunchedEffect
        when (current) {
            "Producto creado", "Producto actualizado" -> {
                showProduct = false
                productToEdit = null
                vm.clearFormErrors()
            }
            "Profesional creado" -> {
                showProfessional = false
                vm.clearFormErrors()
            }
        }
        snackbar.showSnackbar(current)
        vm.clearMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AdminHeader(
                title = if (section == AdminSection.INICIO) "Dashboard Admin" else section.label,
                onLogout = onLogout,
            )
        },
        bottomBar = {
            NavigationBar {
                AdminSection.values().forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = { section = item },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }
        },
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
        ) {
            // PantallaEstado no recibe Modifier: el Box controla el espacio.
            PantallaEstado(
                loading = state is UiState.Cargando,
                error = (state as? UiState.Error)?.mensaje,
                empty = state is UiState.Vacio,
                onRetry = vm::cargar,
            ) {
                // No usar `as UiState.Exito`: este bloque puede ejecutarse mientras state es Cargando.
                val current = state as? UiState.Exito<AdminData>
                if (current != null) {
                    val data = current.datos
                    when (section) {
                        AdminSection.INICIO -> InicioSection(
                            data = data,
                            vm = vm,
                            onGo = { section = it },
                        )
                        AdminSection.BOXES -> BoxesSection(
                            boxes = data.boxes,
                            onToggle = vm::toggleBox,
                        )
                        AdminSection.TIENDA -> TiendaSection(
                            products = data.products,
                            vm = vm,
                            onAdd = {
                                vm.clearFormErrors()
                                productToEdit = null
                                showProduct = true
                            },
                            onEdit = {
                                vm.clearFormErrors()
                                productToEdit = it
                                showProduct = true
                            },
                        )
                        AdminSection.PROFESIONALES -> ProfesionalesSection(
                            professionals = data.professionals,
                            onAdd = {
                                vm.clearFormErrors()
                                showProfessional = true
                            },
                            onChangeState = vm::cambiarEstadoProfesional,
                        )
                    }
                }
            }
        }
    }

    if (showProduct) {
        ProductDialog(
            initial = productToEdit,
            vm = vm,
            onDismiss = {
                showProduct = false
                productToEdit = null
                vm.clearFormErrors()
            },
            onSave = { name, price, stock ->
                val editing = productToEdit
                if (editing == null) {
                    vm.addProduct(name, price, stock)
                } else {
                    vm.editProduct(editing, name, price, stock)
                }
            },
        )
    }

    if (showProfessional) {
        ProfessionalDialog(
            vm = vm,
            onDismiss = {
                showProfessional = false
                vm.clearFormErrors()
            },
            onSave = { fullName, email, password, title, category ->
                vm.addProfessional(fullName, email, password, title, category)
            },
        )
    }

    // Datos de acceso del profesional recién creado, para que el admin se los entregue.
    creado?.let { info ->
        AlertDialog(
            onDismissRequest = vm::cerrarCredenciales,
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            title = { Text("Profesional creado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${info.nombre} ya puede ingresar a la app con:")
                    Text("Correo: ${info.email}", style = MaterialTheme.typography.titleSmall)
                    Text("Contraseña: ${info.password}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Si la persona ya tenía cuenta, conserva su contraseña actual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = { TextButton(onClick = vm::cerrarCredenciales) { Text("Entendido") } },
        )
    }
}

@Composable
private fun AdminHeader(
    title: String,
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = {
                Log.d("SanaesenciaLogout", "Se pulsó el botón Salir")
                onLogout()
            },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = "Cerrar sesión"
            )
            Text(
                "Salir",
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/* ================================================================= */
/* 1. INICIO                                                         */
/* ================================================================= */

@Composable
private fun InicioSection(
    data: AdminData,
    vm: AdminViewModel,
    onGo: (AdminSection) -> Unit,
) {
    val enabled = data.boxes.count { it.estaHabilitado }
    val low = data.products.count { vm.isStockBajo(it.stock) }
    val alerts = data.desertion.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "summary") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Boxes habilitados",
                        value = "$enabled / ${data.boxes.size}",
                        icon = Icons.Default.MeetingRoom,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { onGo(AdminSection.BOXES) },
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = if (low > 0) "Productos · $low con stock bajo" else "Productos",
                        value = "${data.products.size}",
                        icon = if (low > 0) Icons.Default.Warning else Icons.Default.Store,
                        container = if (low > 0) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        onClick = { onGo(AdminSection.TIENDA) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Profesionales",
                        value = "${data.professionals.size}",
                        icon = Icons.Default.Group,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { onGo(AdminSection.PROFESIONALES) },
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Alertas de deserción",
                        value = "$alerts",
                        icon = Icons.Default.Warning,
                        container = if (alerts > 0) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        onClick = null,
                    )
                }
            }
        }

        item(key = "quick_title") {
            Text(
                "Accesos rápidos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        item(key = "quick_actions") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction(Modifier.weight(1f), "Boxes", Icons.Default.MeetingRoom) {
                    onGo(AdminSection.BOXES)
                }
                QuickAction(Modifier.weight(1f), "Tienda", Icons.Default.Store) {
                    onGo(AdminSection.TIENDA)
                }
                QuickAction(Modifier.weight(1f), "Equipo", Icons.Default.Group) {
                    onGo(AdminSection.PROFESIONALES)
                }
            }
        }

        if (alerts > 0) {
            item(key = "alerts_title") {
                Text(
                    "Alertas recientes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            itemsIndexed(
                items = data.desertion.take(3),
                key = { index, _ -> "alert_$index" },
            ) { _, alert ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Text(
                            text = alert.toString(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    container: Color,
    onClick: (() -> Unit)?,
) {
    val colors = CardDefaults.cardColors(containerColor = container)
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(icon, contentDescription = null)
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, colors = colors) { content() }
    } else {
        Card(modifier = modifier, colors = colors) { content() }
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

/* ================================================================= */
/* 2. BOXES                                                          */
/* ================================================================= */

@Composable
private fun BoxesSection(
    boxes: List<BoxResponse>,
    onToggle: (BoxResponse) -> Unit,
) {
    val enabled = boxes.count { it.estaHabilitado }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "$enabled habilitados · ${boxes.size - enabled} pausados",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            gridItems(boxes, key = { "box_${it.id}" }) { box ->
                BoxCard(box = box, onToggle = { onToggle(box) })
            }
        }
    }
}

// Helper para listas dentro de LazyVerticalGrid (evita choque con lazy.items)
private fun <T> androidx.compose.foundation.lazy.grid.LazyGridScope.gridItems(
    items: List<T>,
    key: (T) -> Any,
    content: @Composable (T) -> Unit,
) {
    items(count = items.size, key = { key(items[it]) }) { content(items[it]) }
}

@Composable
private fun BoxCard(
    box: BoxResponse,
    onToggle: () -> Unit,
) {
    val on = box.estaHabilitado
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (on) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = if (on) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Box ${box.numeroBox}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            // El estado se comunica con icono + texto, no solo con color.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = if (on) Icons.Default.CheckCircle else Icons.Default.PauseCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = if (on) "Habilitado" else "Pausado",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            FilledTonalButton(
                onClick = onToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(if (on) "Pausar" else "Habilitar")
            }
        }
    }
}

/* ================================================================= */
/* 3. TIENDA                                                         */
/* ================================================================= */

@Composable
private fun TiendaSection(
    products: List<ProductoResponse>,
    vm: AdminViewModel,
    onAdd: () -> Unit,
    onEdit: (ProductoResponse) -> Unit,
) {
    val low = products.count { vm.isStockBajo(it.stock) }
    var productToDelete by remember { mutableStateOf<ProductoResponse?>(null) }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("¿Eliminar producto?") },
            text = {
                Text(
                    "Se eliminará \"${product.nombre}\" de la tienda. Esta acción no se puede deshacer. " +
                        "Si solo quieres ocultarlo, usa el interruptor para deshabilitarlo.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.eliminarProducto(product)
                        productToDelete = null
                    },
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { productToDelete = null }) { Text("Cancelar") } },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "store_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (low > 0) "${products.size} productos · $low con stock bajo"
                    else "${products.size} productos",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onAdd, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Agregar", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        items(products, key = { "product_${it.id}" }) { product ->
            val isLow = vm.isStockBajo(product.stock)
            val sinStock = product.stock <= 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (product.estaActivo) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (product.estaActivo) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Text(
                                "$${formatClp(product.precio)} CLP",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Switch(
                            checked = product.estaActivo,
                            onCheckedChange = { vm.cambiarActivoProducto(product, it) },
                            enabled = !sinStock || product.estaActivo,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLow) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.size(4.dp))
                        }
                        Text(
                            text = when {
                                sinStock -> "Sin stock · deshabilitado"
                                !product.estaActivo -> "Stock: ${product.stock} · deshabilitado"
                                isLow -> "Stock bajo: ${product.stock}"
                                else -> "Stock: ${product.stock} · visible en tienda"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLow || sinStock) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onEdit(product) }, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar ${product.nombre}")
                        }
                        IconButton(onClick = { productToDelete = product }, modifier = Modifier.size(44.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar ${product.nombre}",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        item(key = "orders_note") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text(
                    "Pedidos y ventas estarán disponibles cuando el backend los exponga.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

private fun formatClp(value: Long): String =
    String.format(Locale.US, "%,d", value).replace(',', '.')

/* ================================================================= */
/* 4. PROFESIONALES                                                  */
/* ================================================================= */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfesionalesSection(
    professionals: List<ProfesionalResponse>,
    onAdd: () -> Unit,
    onChangeState: (ProfesionalResponse, EstadoProfesional) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "pro_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${professionals.size} profesionales",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onAdd, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Agregar", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        items(professionals, key = { "professional_${it.id}" }) { professional ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        professional.titulo,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        professional.categoriaEspecialidad,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        EstadoProfesional.values().forEach { estado ->
                            FilterChip(
                                selected = professional.estado.toString()
                                    .equals(estado.name, ignoreCase = true),
                                onClick = { onChangeState(professional, estado) },
                                label = {
                                    Text(
                                        estado.name.lowercase()
                                            .replace('_', ' ')
                                            .replaceFirstChar { it.uppercase() },
                                        maxLines = 1,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ================================================================= */
/* DIÁLOGOS                                                          */
/* ================================================================= */

@Composable
private fun ProfessionalDialog(
    vm: AdminViewModel,
    onDismiss: () -> Unit,
    onSave: (fullName: String, email: String, password: String, title: String, category: String) -> Unit,
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val errors by vm.errors.collectAsState()

    fun validar() = vm.validateProfessional(fullName, email, password, title, category)

    val specialties = listOf(
        "Med. General & Salud Mental",
        "Psicología Infanto Juvenil",
        "Psicología Adolescentes & Adultos",
        "Terapia Ocupacional",
        "Fonoaudiología",
        "Nutrición Integral",
        "Bienestar & Terapias Naturales",
        "Matronería & Salud Femenina",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onSave(fullName, email, password, title, category) },
                enabled = fullName.isNotBlank() && email.isNotBlank() &&
                    title.isNotBlank() && category.isNotBlank() && !errors.hasErrors,
            ) { Text("Crear profesional") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Nuevo profesional") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "No necesita estar registrado: se crea su cuenta automáticamente y podrá ingresar con este correo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CampoFormulario(
                    label = "Nombre completo",
                    value = fullName,
                    onValueChange = { fullName = it; validar() },
                    error = errors.fullName,
                )
                CampoFormulario(
                    label = "Correo electrónico",
                    value = email,
                    onValueChange = { email = it.trim(); validar() },
                    error = errors.email,
                    keyboard = KeyboardType.Email,
                )
                CampoFormulario(
                    label = "Contraseña inicial (opcional)",
                    value = password,
                    onValueChange = { password = it; validar() },
                    error = errors.password,
                    keyboard = KeyboardType.Password,
                )
                Text(
                    "Si la dejas vacía, la contraseña será $CONTRASENA_INICIAL",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CampoFormulario(
                    label = "Título (ej. Psicóloga, Nutricionista)",
                    value = title,
                    onValueChange = { title = it; validar() },
                    error = errors.title,
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                    ) {
                        Text(
                            text = category.ifBlank { "Especialidad" },
                            modifier = Modifier.weight(1f),
                            color = if (category.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Seleccionar especialidad",
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        specialties.forEach { specialty ->
                            DropdownMenuItem(
                                text = { Text(specialty) },
                                onClick = {
                                    category = specialty
                                    expanded = false
                                    vm.validateProfessional(fullName, email, password, title, specialty)
                                },
                            )
                        }
                    }
                }
                errors.category?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
    )
}

/** Sirve para crear (initial = null) y para editar un producto existente. */
@Composable
private fun ProductDialog(
    initial: ProductoResponse?,
    vm: AdminViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.nombre.orEmpty()) }
    var price by remember { mutableStateOf(initial?.precio?.toString().orEmpty()) }
    var stock by remember { mutableStateOf(initial?.stock?.toString().orEmpty()) }

    val errors by vm.errors.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onSave(name, price, stock) },
                enabled = !errors.hasErrors &&
                        name.isNotBlank() &&
                        price.isNotBlank() &&
                        stock.isNotBlank(),
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text(if (initial == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoFormulario(
                    label = "Nombre",
                    value = name,
                    onValueChange = {
                        name = it
                        vm.validateProduct(it, price, stock)
                    },
                    error = errors.name,
                )
                CampoFormulario(
                    label = "Precio CLP",
                    value = price,
                    onValueChange = {
                        price = it
                        vm.validateProduct(name, it, stock)
                    },
                    error = errors.price,
                    keyboard = KeyboardType.Number,
                )
                CampoFormulario(
                    label = "Stock",
                    value = stock,
                    onValueChange = {
                        stock = it
                        vm.validateProduct(name, price, it)
                    },
                    error = errors.stock,
                    keyboard = KeyboardType.Number,
                )
            }
        },
    )
}
