package cl.sanaesencia.app.data.repository

import cl.sanaesencia.app.data.local.AppDatabase
import cl.sanaesencia.app.data.local.CarritoDao
import cl.sanaesencia.app.data.local.CarritoItemEntity
import cl.sanaesencia.app.data.mappers.toDomain
import cl.sanaesencia.app.data.mappers.toEntity
import cl.sanaesencia.app.data.mappers.toResponse
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
import cl.sanaesencia.app.data.remote.ProfesionalResponse
import cl.sanaesencia.app.data.remote.ReagendarCitaRequest
import cl.sanaesencia.app.data.remote.SanaesenciaApi
import cl.sanaesencia.app.data.session.Session
import cl.sanaesencia.app.data.session.SessionManager
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.Producto
import cl.sanaesencia.app.domain.model.Profesional
import androidx.room.withTransaction
import cl.sanaesencia.app.data.remote.esErrorDeConexion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
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
        // Se borra cualquier sesión anterior antes de pedir un token nuevo,
        // así nunca se envía el token de otra cuenta en el login.
        sessions.clear()

        val response = try {
            api.login(
                LoginRequest(
                    email = email.trim().lowercase(),
                    password = password
                )
            )
        } catch (e: HttpException) {
            if (e.code() in 400..403) {
                throw IllegalArgumentException("Correo o contraseña incorrectos")
            }
            throw e
        }

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

    suspend fun saveProfesional(profesional: ProfesionalResponse) {
        db.profesionales().upsertAll(listOf(profesional.toEntity()))
    }

    /**
     * Descarga profesionales y productos. Si el servidor responde, la copia local se
     * REEMPLAZA (así no quedan profesionales inventados o borrados que luego fallan al agendar).
     * Si no hay conexión, se mantiene lo guardado y se informa el error.
     */
    suspend fun refresh() {
        var error: Exception? = null

        try {
            val remoteProfs = api.profesionales()
            db.withTransaction {
                db.profesionales().clear()
                db.profesionales().upsertAll(remoteProfs.map { it.toEntity() })
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e
        }

        try {
            val productos = api.productos()
            db.withTransaction {
                db.productos().clear()
                db.productos().upsertAll(productos.map { it.toEntity() })
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (error == null) error = e
        }

        error?.let { throw it }
    }
}


// ============================================================================
// CITAS
// ============================================================================

class CitaRepository(
    private val api: SanaesenciaApi,
    private val db: AppDatabase,
    private val sessions: SessionManager,
) {

    private suspend fun usuarioActual(): Long =
        sessions.session.first()?.userId ?: 0L

    /** Crea la cita en el servidor. Si falla, el error llega a la pantalla (no se inventa una cita local). */
    suspend fun crear(
        request: CrearCitaRequest
    ): CitaResponse {
        val response = api.crearCita(request)
        runCatching {
            db.citas().upsertAll(listOf(response.toEntity()))
        }
        return response
    }

    /**
     * Citas del paciente que inició sesión. Con conexión se usa el servidor y se actualiza la
     * copia local de ESE usuario; sin conexión se muestran sus citas guardadas.
     */
    suspend fun misCitas(): List<CitaResponse> {
        val userId = usuarioActual()
        return try {
            val remote = api.misCitas()
            runCatching {
                db.withTransaction {
                    db.citas().borrarDePaciente(userId)
                    db.citas().upsertAll(remote.map { it.toEntity() })
                }
            }
            remote
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!e.esErrorDeConexion) throw e
            val local = db.citas().historial(userId).first().map { it.toResponse() }
            if (local.isEmpty()) throw e
            local
        }
    }

    suspend fun cancelar(
        id: Long
    ): Map<String, String> {
        val result = api.cancelarCita(id)
        runCatching {
            val target = db.citas().historial(usuarioActual()).first().firstOrNull { it.id == id }
            if (target != null) {
                db.citas().upsertAll(listOf(target.copy(estado = EstadoCita.CANCELADA)))
            }
        }
        return result
    }

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