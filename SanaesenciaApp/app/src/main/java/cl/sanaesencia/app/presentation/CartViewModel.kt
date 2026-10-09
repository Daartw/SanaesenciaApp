package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope

import cl.sanaesencia.app.data.local.CarritoItemEntity
import cl.sanaesencia.app.data.repository.CartRepository
import cl.sanaesencia.app.data.repository.PedidoRepository
import cl.sanaesencia.app.domain.model.Producto
import cl.sanaesencia.app.domain.validacion.ValidadorCvv
import cl.sanaesencia.app.domain.validacion.ValidadorTarjeta
import cl.sanaesencia.app.domain.validacion.ValidadorTitularTarjeta
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CartViewModel(
    private val repo: CartRepository,
    private val pedidos: PedidoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : BaseViewModel() {
    val items = repo.items()
    val total = items.map { rows -> rows.sumOf { it.precio * it.cantidad } }
    val message = MutableStateFlow<String?>(null)
    private val _paymentErrors = MutableStateFlow(PaymentErrors())
    private val _paying = MutableStateFlow(false)
    val paying: StateFlow<Boolean> = _paying.asStateFlow()
    val paymentErrors: StateFlow<PaymentErrors> = _paymentErrors.asStateFlow()

    fun add(producto: Producto) = viewModelScope.launch(dispatcher) {
        val current = items.first().firstOrNull { it.productoId == producto.id }
        val quantity = (current?.cantidad ?: 0) + 1
        if (quantity <= producto.stock) repo.add(producto, quantity) else message.value = "Stock máximo alcanzado"
    }
    fun minus(row: CarritoItemEntity) = viewModelScope.launch(dispatcher) { if (row.cantidad > 1) repo.updateQuantity(row.productoId, row.cantidad - 1) }
    fun remove(id: Long) = viewModelScope.launch(dispatcher) { repo.remove(id); message.value = "Producto eliminado" }
    fun clear() = viewModelScope.launch(dispatcher) { repo.clear(); message.value = "Carrito vaciado" }
    fun validatePayment(holder: String, card: String, cvv: String) {
        _paymentErrors.value = PaymentErrors(
            holder = invalid(ValidadorTitularTarjeta()(holder)),
            card = invalid(ValidadorTarjeta()(card)),
            cvv = invalid(ValidadorCvv()(cvv)),
        )
    }
    fun pagar(holder: String, card: String, cvv: String) = viewModelScope.launch(dispatcher) {
        validatePayment(holder, card, cvv)
        if (_paymentErrors.value.hasErrors) return@launch
        val rows = items.first()
        if (rows.isEmpty()) {
            message.value = "El carrito está vacío"
            return@launch
        }
        _paying.value = true
        runCatching { pedidos.pagar(rows) }
            .onSuccess { repo.clear(); message.value = "Pago aprobado y pedido #${it.id} confirmado" }
            .onFailure { message.value = it.message ?: "No fue posible pagar" }
            .also { _paying.value = false }
    }
    fun clearMessage() { message.value = null }
    private fun invalid(result: cl.sanaesencia.app.domain.validacion.ResultadoValidacion) = (result as? cl.sanaesencia.app.domain.validacion.ResultadoValidacion.Invalido)?.mensaje
}

data class PaymentErrors(val holder: String? = null, val card: String? = null, val cvv: String? = null) { val hasErrors get() = holder != null || card != null || cvv != null }
