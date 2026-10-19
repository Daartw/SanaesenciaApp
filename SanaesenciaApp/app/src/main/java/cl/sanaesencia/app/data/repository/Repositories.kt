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
import cl.sanaesencia.app.domain.model.EstadoProfesional
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

    suspend fun refresh() {
        runCatching {
            val remoteProfs = api.profesionales()
            if (remoteProfs.isNotEmpty()) {
                db.profesionales().upsertAll(
                    remoteProfs.map { response ->
                        response.toEntity()
                    }
                )
            }
        }

        // Si la base de datos está vacía y no pudimos cargar del API, sembramos datos iniciales
        val currentLocal = db.profesionales().all().first()
        if (currentLocal.isEmpty()) {
            val semilla = listOf(
                ProfesionalResponse(
                    id = 1,
                    usuarioId = 10,
                    titulo = "Dr. Alexis Vega",
                    categoriaEspecialidad = "Med. General & Salud Mental",
                    descripcionEnfoque = "Atención médica integral con enfoque preventivo y salud mental.",
                    estado = EstadoProfesional.DISPONIBLE
                ),
                ProfesionalResponse(
                    id = 2,
                    usuarioId = 11,
                    titulo = "Dra. Camila Soto",
                    categoriaEspecialidad = "Psicología Infanto Juvenil",
                    descripcionEnfoque = "Especialista en desarrollo emocional infantil y adolescente.",
                    estado = EstadoProfesional.DISPONIBLE
                ),
                ProfesionalResponse(
                    id = 3,
                    usuarioId = 12,
                    titulo = "Lic. Felipe Morales",
                    categoriaEspecialidad = "Nutrición Integral",
                    descripcionEnfoque = "Planes de alimentación consciente y salud metabólica.",
                    estado = EstadoProfesional.DISPONIBLE
                ),
                ProfesionalResponse(
                    id = 4,
                    usuarioId = 13,
                    titulo = "Lic. María José Rivas",
                    categoriaEspecialidad = "Fonoaudiología",
                    descripcionEnfoque = "Terapia del lenguaje, voz y deglución para todas las edades.",
                    estado = EstadoProfesional.DISPONIBLE
                )
            )
            db.profesionales().upsertAll(semilla.map { it.toEntity() })
        }

        runCatching {
            val productos = api.productos()
            if (productos.isNotEmpty()) {
                db.productos().upsertAll(
                    productos.map { response ->
                        response.toEntity()
                    }
                )
            }
        }
    }
}


// ============================================================================
// CITAS
// ============================================================================

class CitaRepository(
    private val api: SanaesenciaApi,
    private val db: AppDatabase,
) {

    suspend fun crear(
        request: CrearCitaRequest
    ): CitaResponse {
        return try {
            val response = api.crearCita(request)
            runCatching {
                db.citas().upsertAll(listOf(response.toEntity()))
            }
            response
        } catch (e: Exception) {
            val localCita = CitaResponse(
                id = System.currentTimeMillis(),
                pacienteId = 1L,
                profesionalId = request.profesionalId,
                boxId = request.boxId,
                fecha = request.fecha,
                horaInicio = request.horaInicio,
                horaFin = request.horaFin,
                modalidad = request.modalidad,
                estado = EstadoCita.RESERVADA
            )
            runCatching {
                db.citas().upsertAll(listOf(localCita.toEntity()))
            }
            localCita
        }
    }

    suspend fun misCitas(): List<CitaResponse> {
        val remote = runCatching { api.misCitas() }.getOrNull().orEmpty()
        val localEntities = runCatching {
            db.citas().historial(1L).first()
        }.getOrNull().orEmpty()

        val localResponses = localEntities.map { it.toResponse() }

        if (remote.isNotEmpty()) {
            runCatching {
                db.citas().upsertAll(remote.map { it.toEntity() })
            }
        }

        val combined = (remote + localResponses).distinctBy { it.id }
        return combined.ifEmpty { localResponses }
    }

    suspend fun cancelar(
        id: Long
    ): Map<String, String> {
        val result = runCatching { api.cancelarCita(id) }.getOrNull()
        // Actualizar en base de datos local
        runCatching {
            val localEntities = db.citas().historial(1L).first()
            val target = localEntities.firstOrNull { it.id == id }
            if (target != null) {
                db.citas().upsertAll(
                    listOf(target.copy(estado = EstadoCita.CANCELADA))
                )
            }
        }
        return result ?: mapOf("mensaje" to "Cita cancelada")
    }

    suspend fun disponibilidad(
        profesionalId: Long,
        fecha: LocalDate,
    ): List<DisponibilidadResponse> {
        val remote = runCatching {
            api.disponibilidad(
                profesionalId = profesionalId,
                fecha = fecha.toString()
            )
        }.getOrNull().orEmpty()

        if (remote.isNotEmpty()) {
            return remote
        }

        // Horarios por defecto si el servidor no los provee o está offline
        val horariosDefecto = listOf(
            "09:00" to "10:00",
            "10:00" to "11:00",
            "11:00" to "12:00",
            "15:00" to "16:00",
            "16:00" to "17:00",
            "17:00" to "18:00"
        )

        val localCitas = runCatching {
            db.citas().historial(1L).first()
                .filter { it.profesionalId == profesionalId && it.fecha == fecha && it.estado != EstadoCita.CANCELADA }
        }.getOrNull().orEmpty()

        val horasOcupadas = localCitas.map { it.inicio.toString() }

        return horariosDefecto.map { (inicio, fin) ->
            val estaOcupada = horasOcupadas.contains(inicio)
            DisponibilidadResponse(
                fecha = fecha.toString(),
                horaInicio = inicio,
                horaFin = fin,
                disponible = !estaOcupada,
                motivo = if (estaOcupada) "Reservado" else null
            )
        }
    }

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