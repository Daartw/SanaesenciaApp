package cl.sanaesencia.app.presentation

import cl.sanaesencia.app.data.local.CarritoDao
import cl.sanaesencia.app.data.local.CarritoItemEntity
import cl.sanaesencia.app.data.remote.EstadoBoxRequest
import cl.sanaesencia.app.data.remote.PedidoResponse
import cl.sanaesencia.app.data.remote.ProductoAdminRequest
import cl.sanaesencia.app.data.remote.ProfesionalAdminRequest
import cl.sanaesencia.app.data.remote.PromocionRequest
import cl.sanaesencia.app.data.remote.SanaesenciaApi
import cl.sanaesencia.app.data.repository.CartRepository
import cl.sanaesencia.app.data.repository.PedidoRepository
import cl.sanaesencia.app.domain.model.Producto
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class CartViewModelTest {
    @Test
    fun agregar_superando_stock_muestra_error() = runBlocking {
        val repository = FakeCartRepository()
        val vm = CartViewModel(repository, FakePedidoRepository(), Dispatchers.Unconfined)
        repository.itemsState.value = listOf(CarritoItemEntity(1, "Producto", 1000, 1, 1))

        vm.add(Producto(1, "Producto", "", 1000, 1, emptyList()))

        assertEquals("Stock máximo alcanzado", vm.message.value)
    }

    @Test
    fun pagar_exitoso_vacia_carrito_y_muestra_confirmacion() = runBlocking {
        val repository = FakeCartRepository()
        val pedido = FakePedidoRepository()
        val vm = CartViewModel(repository, pedido, Dispatchers.Unconfined)
        repository.itemsState.value = listOf(CarritoItemEntity(1, "Producto", 1000, 1, 2))

        vm.pagar("Ana Pérez", "4111111111111111", "123")

        assertEquals(emptyList<CarritoItemEntity>(), repository.itemsState.value)
        assertEquals("Pago aprobado y pedido #1 confirmado", vm.message.value)
    }

    private class FakeCartRepository : CartRepository(FakeCarritoDao()) {
        val itemsState = MutableStateFlow<List<CarritoItemEntity>>(emptyList())

        override fun items(): Flow<List<CarritoItemEntity>> = itemsState

        override suspend fun add(producto: Producto, cantidad: Int) {
            itemsState.value = listOf(
                CarritoItemEntity(producto.id, producto.nombre, producto.precio, cantidad, producto.stock),
            )
        }

        override suspend fun updateQuantity(productoId: Long, cantidad: Int) {
            itemsState.value = itemsState.value.map {
                if (it.productoId == productoId) it.copy(cantidad = cantidad) else it
            }
        }

        override suspend fun remove(id: Long) {
            itemsState.value = itemsState.value.filterNot { it.productoId == id }
        }

        override suspend fun clear() {
            itemsState.value = emptyList()
        }
    }

    private class FakePedidoRepository : PedidoRepository(FakeApi()) {
        override suspend fun pagar(items: List<CarritoItemEntity>): PedidoResponse =
            PedidoResponse(1, 1000, "PAGADO", emptyList())
    }

    private class FakeCarritoDao : CarritoDao {
        private val state = MutableStateFlow<List<CarritoItemEntity>>(emptyList())

        override fun all(): Flow<List<CarritoItemEntity>> = state

        override suspend fun upsert(item: CarritoItemEntity) {
            state.value = state.value.filterNot { it.productoId == item.productoId } + item
        }

        override suspend fun delete(id: Long) {
            state.value = state.value.filterNot { it.productoId == id }
        }

        override suspend fun clear() {
            state.value = emptyList()
        }
    }

    private class FakeApi : SanaesenciaApi {
        override suspend fun login(request: cl.sanaesencia.app.data.remote.LoginRequest) = TODO()
        override suspend fun perfil() = TODO()
        override suspend fun actualizarPerfil(request: cl.sanaesencia.app.data.remote.ActualizarPerfilRequest) = TODO()
        override suspend fun profesionales() = TODO()
        override suspend fun productos() = TODO()
        override suspend fun boxes() = TODO()
        override suspend fun crearCita(request: cl.sanaesencia.app.data.remote.CrearCitaRequest) = TODO()
        override suspend fun misCitas() = TODO()
        override suspend fun cancelarCita(id: Long) = TODO()
        override suspend fun reagendar(id: Long, request: cl.sanaesencia.app.data.remote.ReagendarCitaRequest) = TODO()
        override suspend fun disponibilidad(profesionalId: Long, fecha: String) = TODO()
        override suspend fun agendaProfesional() = TODO()
        override suspend fun historialPaciente(pacienteId: Long) = TODO()
        override suspend fun promociones() = TODO()
        override suspend fun crearPromocion(request: PromocionRequest) = TODO()
        override suspend fun actualizarEstado(id: Long, request: cl.sanaesencia.app.data.remote.EstadoCitaRequest) = TODO()
        override suspend fun crearPedido(request: cl.sanaesencia.app.data.remote.CrearPedidoRequest) = TODO()
        override suspend fun misPedidos() = TODO()
        override suspend fun adminStockBajo() = TODO()
        override suspend fun adminDesercion() = TODO()
        override suspend fun adminProfesionales() = TODO()
        override suspend fun adminBoxes() = TODO()
        override suspend fun adminCrearProducto(request: ProductoAdminRequest) = TODO()
        override suspend fun adminBox(id: Long, request: EstadoBoxRequest) = TODO()
        override suspend fun adminCrearProfesional(request: ProfesionalAdminRequest) = TODO()
        override suspend fun adminEditarProducto(id: Long, request: ProductoAdminRequest) = TODO()
        override suspend fun adminEstadoProfesional(id: Long, request: cl.sanaesencia.app.data.remote.EstadoProfesionalRequest) = TODO()
    }
}
