package cl.sanaesencia.app.ui.tienda

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
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
    val data by vm.productos.collectAsState(initial = emptyList())
    val cartItems by cartVm.items.collectAsState(initial = emptyList())
    val cartCount = cartItems.sumOf { it.cantidad }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Text(
                    "Tienda de Bienestar",
                    fontWeight = FontWeight.Bold,
                )
            },
            actions = {
                IconButton(onClick = onCart) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                ) {
                                    AnimatedContent(
                                        targetState = cartCount,
                                        label = "badge-carrito",
                                    ) { count ->
                                        Text(count.toString())
                                    }
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Carrito",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        if (data.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No hay productos disponibles en la tienda por el momento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 24.dp,
                ),
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
}

@Composable
private fun ProductCard(
    product: Producto,
    cartVm: CartViewModel,
    isStockBajo: (Int) -> Boolean,
) {
    var pressVersion by remember { mutableIntStateOf(0) }

    val scale by animateFloatAsState(
        targetValue = if (pressVersion % 2 == 1) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.descripcionCorta(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$${"%,d".format(item.precioFinal())} CLP",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            // STOCK STATUS BADGE
            val stock = product.stock
            val isLow = isStockBajo(stock)
            val hasStock = stock > 0

            val badgeBg = when {
                !hasStock -> MaterialTheme.colorScheme.errorContainer
                isLow -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
            val badgeTextColor = when {
                !hasStock -> MaterialTheme.colorScheme.onErrorContainer
                isLow -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            }
            val badgeText = when {
                !hasStock -> "Agotado"
                isLow -> "Stock bajo: $stock"
                else -> "Stock: $stock"
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLow || !hasStock) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = badgeTextColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeTextColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
        ) {
            BotonPrimario(
                texto = if (product.stock > 0) "Agregar al carrito" else "Sin stock",
                onClick = {
                    pressVersion++
                    cartVm.add(product)
                },
                enabled = product.stock > 0,
            )
        }
    }
}
