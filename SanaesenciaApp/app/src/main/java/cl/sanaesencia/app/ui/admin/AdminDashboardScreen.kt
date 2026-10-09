package cl.sanaesencia.app.ui.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import cl.sanaesencia.app.data.remote.BoxResponse
import cl.sanaesencia.app.data.remote.DesercionResponse
import cl.sanaesencia.app.data.remote.EstadoBoxRequest
import cl.sanaesencia.app.data.remote.ProfesionalAdminRequest
import cl.sanaesencia.app.data.remote.ProfesionalResponse
import cl.sanaesencia.app.data.remote.ProductoAdminRequest
import cl.sanaesencia.app.data.remote.ProductoResponse
import cl.sanaesencia.app.data.remote.SanaesenciaApi
import cl.sanaesencia.app.domain.model.EstadoProfesional
import cl.sanaesencia.app.domain.usecase.VerificarStockBajoUseCase
import cl.sanaesencia.app.domain.validacion.ValidadorIdPositivo
import cl.sanaesencia.app.domain.validacion.ValidadorNombre
import cl.sanaesencia.app.domain.validacion.ValidadorPrecio
import cl.sanaesencia.app.domain.validacion.ValidadorStock
import cl.sanaesencia.app.domain.validacion.ValidadorTextoMin3
import cl.sanaesencia.app.presentation.BaseViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.CampoFormulario
import cl.sanaesencia.app.ui.common.PantallaEstado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale


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
) {
    val hasErrors: Boolean
        get() = listOf(
            name,
            title,
            category,
            price,
            stock,
            userId,
        ).any { it != null }
}


class AdminViewModel(
    private val api: SanaesenciaApi,
    private val stockBajo: VerificarStockBajoUseCase =
        VerificarStockBajoUseCase(),
) : BaseViewModel() {

    val state =
        MutableStateFlow<UiState<AdminData>>(UiState.Cargando)

    val message =
        MutableStateFlow<String?>(null)

    private val _errors =
        MutableStateFlow(AdminFormErrors())

    val errors: StateFlow<AdminFormErrors> =
        _errors.asStateFlow()


    fun cargar() {
        viewModelScope.launch {
            ejecutar(state) {
                AdminData(
                    boxes = api.adminBoxes(),
                    products = api.productos(),
                    professionals = api.adminProfesionales(),
                    desertion = api.adminDesercion(),
                )
            }
        }
    }


    fun toggleBox(box: BoxResponse) {
        viewModelScope.launch {
            runCatching {
                api.adminBox(
                    box.id,
                    EstadoBoxRequest(
                        !box.estaHabilitado,
                    ),
                )

                cargar()

            }.onFailure {
                message.value =
                    it.message
                        ?: "No fue posible actualizar el box"
            }
        }
    }


    fun validateProduct(
        name: String,
        price: String,
        stock: String,
    ) {
        _errors.value =
            _errors.value.copy(
                name = invalid(
                    ValidadorNombre()(name),
                ),
                price = invalid(
                    ValidadorPrecio()(price),
                ),
                stock = invalid(
                    ValidadorStock()(stock),
                ),
            )
    }


    fun validateProfessional(
        userId: String,
        title: String,
        category: String,
    ) {
        _errors.value =
            _errors.value.copy(
                userId = invalid(
                    ValidadorIdPositivo()(userId),
                ),
                title = invalid(
                    ValidadorTextoMin3()(title),
                ),
                category = invalid(
                    ValidadorTextoMin3()(category),
                ),
            )
    }


    fun isStockBajo(
        stock: Int,
    ): Boolean =
        stockBajo(stock)


    fun clearFormErrors() {
        _errors.value =
            AdminFormErrors()
    }


    fun addProfessional(
        userIdText: String,
        title: String,
        category: String,
    ) {
        validateProfessional(
            userIdText,
            title,
            category,
        )

        val userId =
            userIdText.toLongOrNull()
                ?: return

        if (_errors.value.hasErrors) {
            return
        }

        viewModelScope.launch {
            runCatching {

                api.adminCrearProfesional(
                    ProfesionalAdminRequest(
                        usuarioId = userId,
                        titulo = title.trim(),
                        categoriaEspecialidad =
                            category.trim(),
                        descripcionEnfoque =
                            "Atención integrativa",
                        certificaciones = "Demo",
                        anosExperiencia = 1,
                        estado =
                            EstadoProfesional.DISPONIBLE,
                    ),
                )

                cargar()

                message.value =
                    "Profesional creado"

            }.onFailure {
                message.value =
                    it.message
                        ?: "No fue posible crear el profesional"
            }
        }
    }


    fun addProduct(
        name: String,
        priceText: String,
        stockText: String,
    ) {
        validateProduct(
            name,
            priceText,
            stockText,
        )

        val price =
            priceText.toLongOrNull()
                ?: return

        val stock =
            stockText.toIntOrNull()
                ?: return

        if (_errors.value.hasErrors) {
            return
        }

        viewModelScope.launch {
            runCatching {

                api.adminCrearProducto(
                    ProductoAdminRequest(
                        nombre = name.trim(),
                        descripcion =
                            "Producto administrado",
                        precio = price,
                        stock = stock,
                        imagenUrl = null,
                        etiquetasTerapeuticas =
                            "bienestar",
                    ),
                )

                cargar()

                message.value =
                    "Producto creado"

            }.onFailure {
                message.value =
                    it.message
                        ?: "No fue posible crear el producto"
            }
        }
    }


    private fun invalid(
        result:
        cl.sanaesencia.app.domain.validacion.ResultadoValidacion,
    ): String? {
        return (
                result as?
                        cl.sanaesencia.app.domain.validacion.ResultadoValidacion.Invalido
                )?.mensaje
    }
}


/* ================================================================= */
/* DASHBOARD ADMIN                                                   */
/* ================================================================= */

@Composable
fun AdminDashboardScreen(
    vm: AdminViewModel,
    onProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val message by vm.message.collectAsState()

    var showProduct by remember {
        mutableStateOf(false)
    }

    var showProfessional by remember {
        mutableStateOf(false)
    }


    LaunchedEffect(Unit) {
        vm.cargar()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {

        /* ========================================================= */
        /* ENCABEZADO                                                  */
        /* ========================================================= */

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween,
        ) {

            Text(
                text = "Dashboard Admin",
                style =
                    MaterialTheme.typography.headlineMedium,
            )


            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(4.dp),
            ) {

                TextButton(
                    onClick = onProfile,
                    modifier =
                        Modifier.heightIn(
                            min = 48.dp,
                        ),
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.AccountCircle,
                        contentDescription =
                            "Perfil",
                    )

                    Text(
                        text = "Perfil",
                        modifier =
                            Modifier.padding(
                                start = 4.dp,
                            ),
                    )
                }


                TextButton(
                    onClick = onLogout,
                    modifier =
                        Modifier.heightIn(
                            min = 48.dp,
                        ),
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Logout,
                        contentDescription =
                            "Cerrar sesión",
                    )

                    Text(
                        text = "Cerrar sesión",
                        modifier =
                            Modifier.padding(
                                start = 4.dp,
                            ),
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(8.dp),
        )


        /* ========================================================= */
        /* BOTONES ADMIN                                               */
        /* ========================================================= */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
        ) {

            Button(
                onClick = {
                    vm.clearFormErrors()
                    showProduct = true
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
            ) {
                Text("Agregar producto")
            }


            Button(
                onClick = {
                    vm.clearFormErrors()
                    showProfessional = true
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
            ) {
                Text("Agregar profesional")
            }
        }


        Spacer(
            modifier = Modifier.height(8.dp),
        )


        /* ========================================================= */
        /* CONTENIDO                                                   */
        /* ========================================================= */

        /*
         * PantallaEstado no recibe Modifier.
         *
         * Por eso el Box es quien controla el espacio restante.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {

            PantallaEstado(
                loading =
                    state is UiState.Cargando,

                error =
                    (state as? UiState.Error)
                        ?.mensaje,

                empty =
                    state is UiState.Vacio,

                onRetry =
                    vm::cargar,
            ) {

                /*
                 * IMPORTANTE:
                 *
                 * NO usar:
                 *
                 * val current =
                 *     (state as UiState.Exito).datos
                 *
                 * porque PantallaEstado puede ejecutar
                 * este contenido mientras state sea Cargando.
                 */
                val current =
                    state as? UiState.Exito<AdminData>

                if (current != null) {
                    AdminContent(
                        data = current.datos,
                        vm = vm,
                    )
                }
            }
        }


        /* ========================================================= */
        /* MENSAJE                                                     */
        /* ========================================================= */

        message?.let {

            Text(
                text = it,
                color =
                    MaterialTheme.colorScheme.error,
                modifier =
                    Modifier.padding(
                        top = 4.dp,
                    ),
            )
        }
    }


    /* ============================================================= */
    /* DIALOGO PRODUCTO                                               */
    /* ============================================================= */

    if (showProduct) {

        ProductDialog(
            onDismiss = {
                showProduct = false
                vm.clearFormErrors()
            },

            vm = vm,

            onSave = {
                    name,
                    price,
                    stock ->

                vm.addProduct(
                    name,
                    price,
                    stock,
                )

                /*
                 * La validación ocurre de forma inmediata.
                 * Si no hay errores de formulario, cerramos
                 * el diálogo.
                 *
                 * La creación HTTP continúa en el ViewModel.
                 */
                if (!vm.errors.value.hasErrors) {
                    showProduct = false
                }
            },
        )
    }


    /* ============================================================= */
    /* DIALOGO PROFESIONAL                                            */
    /* ============================================================= */

    if (showProfessional) {

        ProfessionalDialog(
            onDismiss = {
                showProfessional = false
                vm.clearFormErrors()
            },

            vm = vm,

            onSave = {
                    userId,
                    title,
                    category ->

                vm.addProfessional(
                    userId,
                    title,
                    category,
                )

                if (!vm.errors.value.hasErrors) {
                    showProfessional = false
                }
            },
        )
    }
}


/* ================================================================= */
/* CONTENIDO ADMIN                                                   */
/* ================================================================= */

@Composable
private fun AdminContent(
    data: AdminData,
    vm: AdminViewModel,
) {
    val low =
        data.products.count {
            vm.isStockBajo(it.stock)
        }


    val animated by
    animateColorAsState(
        targetValue =
            if (low > 0) {
                MaterialTheme
                    .colorScheme
                    .errorContainer
            } else {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
            },
        label = "stock",
    )


    /*
     * UN SOLO LazyColumn.
     *
     * Esto evita tener un Card fuera del LazyColumn
     * y evita problemas de scroll/medición.
     */
    LazyColumn(
        modifier = Modifier.fillMaxSize(),

        verticalArrangement =
            Arrangement.spacedBy(8.dp),
    ) {

        /* ========================================================= */
        /* RESUMEN                                                     */
        /* ========================================================= */

        item(
            key = "admin_summary",
        ) {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor = animated,
                    ),
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp),
                ) {

                    Text(
                        text =
                            "Boxes habilitados: " +
                                    "${data.boxes.count { it.estaHabilitado }} / 8",
                    )

                    Text(
                        text =
                            "Productos con stock bajo: $low",
                    )

                    Text(
                        text =
                            "Profesionales: " +
                                    "${data.professionals.size}",
                    )

                    Text(
                        text =
                            "Alertas de deserción: " +
                                    "${data.desertion.size}",
                    )
                }
            }
        }


        /* ========================================================= */
        /* TÍTULO BOXES                                                */
        /* ========================================================= */

        item(
            key = "boxes_title",
        ) {

            Text(
                text = "Boxes",
                style =
                    MaterialTheme.typography.titleLarge,
                modifier =
                    Modifier.padding(
                        top = 8.dp,
                    ),
            )
        }


        /* ========================================================= */
        /* BOXES                                                       */
        /* ========================================================= */

        items(
            items = data.boxes,

            /*
             * IMPORTANTE:
             *
             * No usamos solamente box.id porque
             * otras listas también pueden tener id = 1.
             */
            key = {
                    box ->
                "box_${box.id}"
            },
        ) { box ->

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f),
                    ) {

                        Text(
                            text =
                                "Box ${box.numeroBox}",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,
                        )

                        Text(
                            text =
                                if (box.estaHabilitado) {
                                    "Estado: habilitado"
                                } else {
                                    "Estado: pausado"
                                },
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(1.dp),
                    )


                    Button(
                        onClick = {
                            vm.toggleBox(box)
                        },
                        modifier =
                            Modifier.heightIn(
                                min = 48.dp,
                            ),
                    ) {

                        Text(
                            text =
                                if (box.estaHabilitado) {
                                    "Pausar"
                                } else {
                                    "Habilitar"
                                },
                        )
                    }
                }
            }
        }


        /* ========================================================= */
        /* TÍTULO PRODUCTOS                                            */
        /* ========================================================= */

        item(
            key = "products_title",
        ) {

            Text(
                text = "Productos",
                style =
                    MaterialTheme.typography.titleLarge,
                modifier =
                    Modifier.padding(
                        top = 8.dp,
                    ),
            )
        }


        /* ========================================================= */
        /* PRODUCTOS                                                    */
        /* ========================================================= */

        items(
            items = data.products,

            key = {
                    product ->
                "product_${product.id}"
            },
        ) { product ->

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {

                Column(
                    modifier =
                        Modifier.padding(12.dp),
                ) {

                    Text(
                        text =
                            product.nombre,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                    )


                    Text(
                        text =
                            "${
                                String.format(
                                    Locale.US,
                                    "%,d",
                                    product.precio,
                                )
                            } CLP · stock ${product.stock}",
                    )
                }
            }
        }


        /* ========================================================= */
        /* TÍTULO PROFESIONALES                                        */
        /* ========================================================= */

        item(
            key = "professionals_title",
        ) {

            Text(
                text = "Profesionales",
                style =
                    MaterialTheme.typography.titleLarge,
                modifier =
                    Modifier.padding(
                        top = 8.dp,
                    ),
            )
        }


        /* ========================================================= */
        /* PROFESIONALES                                                */
        /* ========================================================= */

        items(
            items = data.professionals,

            key = {
                    professional ->
                "professional_${professional.id}"
            },
        ) { professional ->

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {

                Column(
                    modifier =
                        Modifier.padding(12.dp),
                ) {

                    Text(
                        text =
                            professional.titulo,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                    )


                    Text(
                        text =
                            professional
                                .categoriaEspecialidad,
                    )


                    Text(
                        text =
                            "Estado: " +
                                    professional.estado,
                    )
                }
            }
        }


        /* ========================================================= */
        /* TÍTULO DESERCIÓN                                            */
        /* ========================================================= */

        item(
            key = "desertion_title",
        ) {

            Text(
                text = "Deserción",
                style =
                    MaterialTheme.typography.titleLarge,
                modifier =
                    Modifier.padding(
                        top = 8.dp,
                    ),
            )
        }


        /* ========================================================= */
        /* DESERCIÓN                                                   */
        /* ========================================================= */

        items(
            items = data.desertion,

            key = {
                    desertion ->
                "desertion_${desertion.hashCode()}"
            },
        ) { desertion ->

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {

                Column(
                    modifier =
                        Modifier.padding(12.dp),
                ) {

                    Text(
                        text = "Alerta de deserción",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                    )


                    Text(
                        text =
                            desertion.toString(),
                    )
                }
            }
        }


        item(
            key = "admin_bottom_space",
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp),
            )
        }
    }
}


/* ================================================================= */
/* DIALOGO PROFESIONAL                                               */
/* ================================================================= */

@Composable
private fun ProfessionalDialog(
    onDismiss: () -> Unit,
    vm: AdminViewModel,
    onSave: (
        String,
        String,
        String,
    ) -> Unit,
) {

    var userId by remember {
        mutableStateOf("")
    }

    var title by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    val errors by vm.errors.collectAsState()


    val specialties =
        listOf(
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

        onDismissRequest =
            onDismiss,

        confirmButton = {

            TextButton(

                onClick = {
                    onSave(
                        userId,
                        title,
                        category,
                    )
                },

                enabled =
                    !errors.hasErrors &&
                            userId.isNotBlank() &&
                            title.isNotBlank() &&
                            category.isNotBlank(),
            ) {

                Text(
                    text = "Guardar",
                )
            }
        },


        dismissButton = {

            TextButton(
                onClick = onDismiss,
            ) {

                Text(
                    text = "Cancelar",
                )
            }
        },


        title = {

            Text(
                text = "Nuevo profesional",
            )
        },


        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {

                CampoFormulario(
                    label = "ID de usuario",

                    value = userId,

                    onValueChange = {
                        userId = it

                        vm.validateProfessional(
                            it,
                            title,
                            category,
                        )
                    },

                    error = errors.userId,

                    keyboard =
                        androidx.compose.ui.text
                            .input
                            .KeyboardType
                            .Number,
                )


                CampoFormulario(
                    label = "Título",

                    value = title,

                    onValueChange = {
                        title = it

                        vm.validateProfessional(
                            userId,
                            it,
                            category,
                        )
                    },

                    error = errors.title,
                )


                Box(
                    modifier =
                        Modifier.fillMaxWidth(),
                ) {

                    OutlinedButton(

                        onClick = {
                            expanded = true
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(
                                    min = 56.dp,
                                ),
                    ) {

                        Text(
                            text =
                                category.ifBlank {
                                    "Especialidad"
                                },

                            modifier =
                                Modifier.weight(1f),

                            color =
                                if (category.isBlank()) {
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                                },
                        )


                        Icon(
                            imageVector =
                                Icons.Default.ArrowDropDown,

                            contentDescription =
                                "Seleccionar especialidad",
                        )
                    }


                    DropdownMenu(

                        expanded =
                            expanded,

                        onDismissRequest = {
                            expanded = false
                        },
                    ) {

                        specialties.forEach {
                                specialty ->

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        text = specialty,
                                    )
                                },

                                onClick = {

                                    category =
                                        specialty

                                    expanded =
                                        false

                                    vm.validateProfessional(
                                        userId,
                                        title,
                                        specialty,
                                    )
                                },
                            )
                        }
                    }
                }


                errors.category?.let {
                        error ->

                    Text(
                        text = error,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                    )
                }
            }
        },
    )
}


/* ================================================================= */
/* DIALOGO PRODUCTO                                                  */
/* ================================================================= */

@Composable
private fun ProductDialog(
    onDismiss: () -> Unit,
    vm: AdminViewModel,
    onSave: (
        String,
        String,
        String,
    ) -> Unit,
) {

    var name by remember {
        mutableStateOf("")
    }

    var price by remember {
        mutableStateOf("")
    }

    var stock by remember {
        mutableStateOf("")
    }

    val errors by vm.errors.collectAsState()


    AlertDialog(

        onDismissRequest =
            onDismiss,

        confirmButton = {

            TextButton(

                onClick = {

                    onSave(
                        name,
                        price,
                        stock,
                    )
                },

                enabled =
                    !errors.hasErrors &&
                            name.isNotBlank() &&
                            price.isNotBlank() &&
                            stock.isNotBlank(),
            ) {

                Text(
                    text = "Guardar",
                )
            }
        },


        dismissButton = {

            TextButton(
                onClick = onDismiss,
            ) {

                Text(
                    text = "Cancelar",
                )
            }
        },


        title = {

            Text(
                text = "Nuevo producto",
            )
        },


        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {

                CampoFormulario(
                    label = "Nombre",

                    value = name,

                    onValueChange = {
                        name = it

                        vm.validateProduct(
                            it,
                            price,
                            stock,
                        )
                    },

                    error = errors.name,
                )


                CampoFormulario(
                    label = "Precio CLP",

                    value = price,

                    onValueChange = {
                        price = it

                        vm.validateProduct(
                            name,
                            it,
                            stock,
                        )
                    },

                    error = errors.price,

                    keyboard =
                        androidx.compose.ui.text
                            .input
                            .KeyboardType
                            .Number,
                )


                CampoFormulario(
                    label = "Stock",

                    value = stock,

                    onValueChange = {
                        stock = it

                        vm.validateProduct(
                            name,
                            price,
                            it,
                        )
                    },

                    error = errors.stock,

                    keyboard =
                        androidx.compose.ui.text
                            .input
                            .KeyboardType
                            .Number,
                )
            }
        },
    )
}