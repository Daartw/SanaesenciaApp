package cl.sanaesencia.app.ui.tienda

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.domain.model.ItemCatalogo
import cl.sanaesencia.app.domain.model.Producto
import cl.sanaesencia.app.presentation.CartViewModel
import cl.sanaesencia.app.presentation.CatalogViewModel
import cl.sanaesencia.app.ui.common.BotonPrimario
import cl.sanaesencia.app.ui.common.TarjetaBase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiendaScreen(
    vm: CatalogViewModel,
    cartVm: CartViewModel,
    onCart: () -> Unit,
    isStockBajo: (Int) -> Boolean,
) {
    val data by vm.productos.collectAsState(
        initial = emptyList(),
    )

    val cartItems by cartVm.items.collectAsState(
        initial = emptyList(),
    )

    val cartCount = cartItems.sumOf {
        it.cantidad
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Text("Tienda")
            },
            actions = {
                BadgedBox(
                    badge = {
                        Badge {
                            AnimatedContent(
                                targetState = cartCount,
                                label = "badge-carrito",
                            ) { count ->
                                Text(count.toString())
                            }
                        }
                    },
                ) {
                    Button(
                        onClick = onCart,
                        modifier = Modifier.heightIn(
                            min = 48.dp,
                        ),
                    ) {
                        Text("Carrito")
                    }
                }
            },
        )

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = data,
                key = { it.id },
            ) { product ->
                ProductCard(
                    product = product,
                    cartVm = cartVm,
                    isStockBajo = isStockBajo,
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Producto,
    cartVm: CartViewModel,
    isStockBajo: (Int) -> Boolean,
) {
    var pressVersion by remember {
        mutableIntStateOf(0)
    }

    val scale by animateFloatAsState(
        targetValue = if (pressVersion % 2 == 1) {
            1.04f
        } else {
            1f
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
        ),
        label = "rebote-agregar",
    )

    LaunchedEffect(pressVersion) {
        if (pressVersion % 2 == 1) {
            pressVersion++
        }
    }

    TarjetaBase(
        modifier = Modifier.animateContentSize(),
    ) {
        val item: ItemCatalogo = product

        Text(
            text = item.nombre,
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = item.descripcionCorta(),
        )

        Text(
            text = "$${"%,d".format(item.precioFinal())} CLP",
        )

        AnimatedContent(
            targetState = product.stock,
            label = "stock",
        ) { stock ->
            Text(
                text = if (isStockBajo(stock)) {
                    "Stock bajo: $stock"
                } else {
                    "Stock: $stock"
                },
            )
        }

        androidx.compose.foundation.layout.Box(
            modifier = Modifier.scale(scale),
        ) {
            BotonPrimario(
                texto = if (product.stock > 0) {
                    "Agregar al carrito"
                } else {
                    "Sin stock"
                },
                onClick = {
                    pressVersion++
                    cartVm.add(product)
                },
                enabled = product.stock > 0,
            )
        }
    }
}