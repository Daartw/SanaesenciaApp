package cl.sanaesencia.app.ui.carrito

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.local.CarritoItemEntity
import cl.sanaesencia.app.domain.model.ItemCatalogo
import cl.sanaesencia.app.domain.model.Producto
import cl.sanaesencia.app.presentation.CartViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.data.remote.PedidoResponse
import cl.sanaesencia.app.ui.common.CampoFormulario

@Composable
fun CartScreen(
    vm: CartViewModel,
    onBack: () -> Unit,
) {
    val items by vm.items.collectAsState(initial = emptyList())
    val total by vm.total.collectAsState(initial = 0L)
    val message by vm.message.collectAsState()
    val errors by vm.paymentErrors.collectAsState()
    val paying by vm.paying.collectAsState()

    var confirm by remember {
        mutableStateOf(false)
    }

    var showPedidos by remember {
        mutableStateOf(false)
    }

    val pedidos by vm.pedidos.collectAsState()

    var showPayment by remember {
        mutableStateOf(false)
    }

    var holder by remember {
        mutableStateOf("")
    }

    var card by remember {
        mutableStateOf("")
    }

    var cvv by remember {
        mutableStateOf("")
    }

    var holderTouched by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Carrito",
                style = MaterialTheme.typography.headlineSmall,
            )

            Row {
                TextButton(
                    onClick = {
                        vm.cargarPedidos()
                        showPedidos = true
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Mis pedidos")
                }
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Volver")
                }
            }
        }

        AnimatedContent(
            targetState = items.size,
            label = "contador-carrito",
        ) { count ->
            Text("Productos en carrito: $count")
        }

        if (items.isEmpty()) {
            Text("Tu carrito está vacío")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = items,
                    key = { it.productoId },
                ) { row ->
                    CartRow(
                        row = row,
                        vm = vm,
                        modifier = Modifier.animateContentSize(),
                    )
                }
            }

            Text(
                text = "Total IVA incluido: $${"%,d".format(total)} CLP",
            )

            Button(
                onClick = {
                    showPayment = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                AnimatedContent(
                    targetState = showPayment,
                    label = "estado-pagar",
                ) { open ->
                    Text(
                        if (open) {
                            "Pago"
                        } else {
                            "Pagar"
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = showPayment,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CampoFormulario(
                        label = "Titular",
                        value = holder,
                        onValueChange = {
                            holder = it

                            if (holderTouched) {
                                vm.validatePayment(
                                    holder,
                                    card,
                                    cvv,
                                )
                            }
                        },
                        error = if (holderTouched) {
                            errors.holder
                        } else {
                            null
                        },
                    )

                    CampoFormulario(
                        label = "Tarjeta",
                        value = card,
                        onValueChange = {
                            card = it
                                .filter(Char::isDigit)
                                .take(16)

                            vm.validatePayment(
                                holder,
                                card,
                                cvv,
                            )
                        },
                        error = errors.card,
                        keyboard = androidx.compose.ui.text.input.KeyboardType.Number,
                    )

                    CampoFormulario(
                        label = "CVV",
                        value = cvv,
                        onValueChange = {
                            cvv = it
                                .filter(Char::isDigit)
                                .take(3)

                            vm.validatePayment(
                                holder,
                                card,
                                cvv,
                            )
                        },
                        error = errors.cvv,
                        keyboard = androidx.compose.ui.text.input.KeyboardType.Number,
                    )

                    Button(
                        onClick = {
                            holderTouched = true
                            vm.pagar(
                                holder,
                                card,
                                cvv,
                            )
                        },
                        enabled = !errors.hasErrors &&
                                holder.isNotBlank() &&
                                card.isNotBlank() &&
                                cvv.isNotBlank() &&
                                !paying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                    ) {
                        AnimatedContent(
                            targetState = paying,
                            label = "pago-cargando",
                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator()
                            } else {
                                Text("Confirmar pago")
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    confirm = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Vaciar carrito")
            }
        }

        AnimatedVisibility(
            visible = message != null,
        ) {
            Text(
                text = message.orEmpty(),
                modifier = Modifier.padding(8.dp),
            )
        }
    }

    // RF-03: historial de pedidos del paciente
    if (showPedidos) {
        AlertDialog(
            onDismissRequest = { showPedidos = false },
            confirmButton = {
                TextButton(onClick = { showPedidos = false }) { Text("Cerrar") }
            },
            title = { Text("Mis pedidos") },
            text = {
                when (val p = pedidos) {
                    is UiState.Cargando -> CircularProgressIndicator()
                    is UiState.Error -> Text(p.mensaje, color = MaterialTheme.colorScheme.error)
                    is UiState.Vacio -> Text("Aún no tienes pedidos.")
                    is UiState.Exito<List<PedidoResponse>> -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 420.dp),
                    ) {
                        items(items = p.datos, key = { it.id }) { pedido ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        "Pedido #${pedido.id} · ${pedido.estado.replace('_', ' ')}",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    pedido.items.forEach { item ->
                                        Text(
                                            "${item.cantidad} × ${item.nombreProducto}",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    Text(
                                        "Total: $${"%,d".format(pedido.total).replace(',', '.')} CLP",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            },
        )
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = {
                confirm = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirm = false
                        vm.clear()
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Vaciar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirm = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Cancelar")
                }
            },
            title = {
                Text("Vaciar carrito")
            },
            text = {
                Text("¿Quieres eliminar todos los productos?")
            },
        )
    }
}

@Composable
private fun CartRow(
    row: CarritoItemEntity,
    vm: CartViewModel,
    modifier: Modifier = Modifier,
) {
    val item: ItemCatalogo = Producto(
        row.productoId,
        row.nombre,
        "",
        row.precio,
        row.stock,
        emptyList(),
    )

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(item.nombre)

            Text(
                "$${"%,d".format(item.precioFinal())} CLP",
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        vm.minus(row)
                    },
                    enabled = row.cantidad > 1,
                ) {
                    Text("−")
                }

                Text(row.cantidad.toString())

                IconButton(
                    onClick = {
                        vm.add(
                            Producto(
                                row.productoId,
                                row.nombre,
                                "",
                                row.precio,
                                row.stock,
                                emptyList(),
                            ),
                        )
                    },
                    enabled = row.cantidad < row.stock,
                ) {
                    Text("+")
                }

                TextButton(
                    onClick = {
                        vm.remove(row.productoId)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}