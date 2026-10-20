package cl.sanaesencia.app.data.repository

import cl.sanaesencia.app.data.local.AppDatabase
import cl.sanaesencia.app.data.local.CarritoDao
import cl.sanaesencia.app.data.local.CarritoItemEntity
import cl.sanaesencia.app.data.mappers.toDomain
import cl.sanaesencia.app.data.mappers.toEntity
import cl.sanaesencia.app.data.remote.ActualizarPerfilRequest
import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.data.remote.CrearCitaRequest
import cl.sanaesencia.app.data.remote.CrearPedidoRequest
import cl.sanaesencia.app.data.remote.DisponibilidadResponse
import cl.sanaesencia.app.data.remote.EstadoCitaRequest
import cl.sanaesencia.app.data.remote.FichaClinicaResponse
import cl.sanaesencia.app.data.remote.LoginRequest
import cl.sanaesencia.app.data.remote.LoginResponse
import cl.sanaesencia.app.data.remote.PedidoItemRequest
import cl.sanaesencia.app.data.remote.PedidoResponse
import cl.sanaesencia.app.data.remote.ReagendarCitaRequest
import cl.sanaesencia.app.data.remote.SanaesenciaApi
import cl.sanaesencia.app.data.session.Session
import cl.sanaesencia.app.data.session.SessionManager
import cl.sanaesencia.app.domain.model.Producto
import cl.sanaesencia.app.domain.model.Profesional
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate


// ============================================================================
// RESULTADO GENERAL
// ============================================================================

sealed interface Resultado<out T> {

    data object Cargando : Resultado<Nothing>

    data class Exito<T>(
        val datos: T
    ) : Resultado<T>

    data class Error(
        val mensaje: String
    ) : Resultado<Nothing>

    data object Vacio : Resultado<Nothing>
}


// ============================================================================
// AUTENTICACIÓN
// ============================================================================

class AuthRepository(
    private val api: SanaesenciaApi,
    private val sessions: SessionManager,
) {

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {

        val response = api.login(
            LoginRequest(
                email = email,
                password = password
            )
        )

        sessions.save(
            Session(
                response.token,
                response.usuarioId,
                response.rol,
                response.nombre
            )
        )

        return response
    }

    fun session() = sessions.session

    suspend fun logout() {
        sessions.clear()
    }

    suspend fun perfil() =
        api.perfil()

    suspend fun actualizarPerfil(
        request: ActualizarPerfilRequest
    ) = api.actualizarPerfil(request)
}


// ============================================================================
// CATÁLOGO
// ============================================================================

class CatalogRepository(
    private val api: SanaesenciaApi,
    private val db: AppDatabase,
) {

    fun profesionales(): Flow<List<Profesional>> =
        db.profesionales()
            .all()
            .map { rows ->
                rows.map { entity ->
                    entity.toDomain()
                }
            }

    fun productos(): Flow<List<Producto>> =
        db.productos()
            .all()
            .map { rows ->
                rows.map { entity ->
                    entity.toDomain()
                }
            }

    suspend fun refresh() {

        val profesionales = api.profesionales()

        db.profesionales().upsertAll(
            profesionales.map { response ->
                response.toEntity()
            }
        )

        val productos = api.productos()

        db.productos().upsertAll(
            productos.map { response ->
                response.toEntity()
            }
        )
    }
}


// ============================================================================
// CITAS
// ============================================================================

class CitaRepository(
    private val api: SanaesenciaApi,
    @Suppress("UNUSED_PARAMETER")
    private val db: AppDatabase,
) {

    suspend fun crear(
        request: CrearCitaRequest
    ) = api.crearCita(request)

    suspend fun misCitas(): List<CitaResponse> =
        api.misCitas()

    suspend fun cancelar(
        id: Long
    ) = api.cancelarCita(id)

    suspend fun disponibilidad(
        profesionalId: Long,
        fecha: LocalDate,
    ): List<DisponibilidadResponse> =
        api.disponibilidad(
            profesionalId = profesionalId,
            fecha = fecha.toString()
        )

    suspend fun reagendar(
        id: Long,
        request: ReagendarCitaRequest
    ) = api.reagendar(
        citaId = id,
        request = request
    )

    suspend fun agenda() =
        api.agendaProfesional()

    suspend fun historialPaciente(
        id: Long
    ): List<FichaClinicaResponse> =
        api.historialPaciente(id)

    suspend fun actualizarEstado(
        id: Long,
        request: EstadoCitaRequest
    ) = api.actualizarEstado(
        citaId = id,
        request = request
    )
}


// ============================================================================
// CARRITO
// ============================================================================

open class CartRepository(
    private val dao: CarritoDao
) {

    open fun items(): Flow<List<CarritoItemEntity>> =
        dao.all()

    open suspend fun add(
        producto: Producto,
        cantidad: Int
    ) {

        dao.upsert(
            CarritoItemEntity(
                productoId = producto.id,
                nombre = producto.nombre,
                precio = producto.precio,
                cantidad = cantidad,
                stock = producto.stock,
            )
        )
    }

    open suspend fun updateQuantity(
        productoId: Long,
        cantidad: Int
    ) {

        val row = dao
            .all()
            .first()
            .firstOrNull { item ->
                item.productoId == productoId
            }
            ?: return

        dao.upsert(
            row.copy(
                cantidad = cantidad
            )
        )
    }

    open suspend fun remove(
        id: Long
    ) {
        dao.delete(id)
    }

    open suspend fun clear() {
        dao.clear()
    }
}


// ============================================================================
// PEDIDOS
// ============================================================================

open class PedidoRepository(
    private val api: SanaesenciaApi
) {

    open suspend fun pagar(
        items: List<CarritoItemEntity>
    ): PedidoResponse {

        return api.crearPedido(
            CrearPedidoRequest(
                items = items.map { item ->
                    PedidoItemRequest(
                        productoId = item.productoId,
                        cantidad = item.cantidad
                    )
                }
            )
        )
    }

    open suspend fun historial() =
        api.misPedidos()
}