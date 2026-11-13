package cl.sanaesencia.app.dobles

import cl.sanaesencia.app.data.remote.ActualizarPerfilRequest
import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.data.remote.CrearCitaRequest
import cl.sanaesencia.app.data.remote.DisponibilidadResponse
import cl.sanaesencia.app.data.remote.EstadoCitaRequest
import cl.sanaesencia.app.data.remote.FichaClinicaResponse
import cl.sanaesencia.app.data.remote.LoginResponse
import cl.sanaesencia.app.data.remote.PerfilResponse
import cl.sanaesencia.app.data.remote.ReagendarCitaRequest
import cl.sanaesencia.app.data.repository.AuthRepositorio
import cl.sanaesencia.app.data.repository.CatalogRepositorio
import cl.sanaesencia.app.data.repository.CitaRepositorio
import cl.sanaesencia.app.data.session.Session
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.ModalidadCita
import cl.sanaesencia.app.domain.model.Producto
import cl.sanaesencia.app.domain.model.Profesional
import cl.sanaesencia.app.domain.model.RolUsuario
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Dobles de prueba de los repositorios.
 *
 * Implementan los contratos del paquete `data.repository`, así que las pruebas de los
 * ViewModels corren sin Room, sin DataStore y sin backend. Cada doble permite inyectar
 * un error para comprobar también los caminos de fallo.
 */

// --------------------------------------------------------------- autenticación

class AuthRepositorioFalso(
    var perfilGuardado: PerfilResponse = perfilDemo(),
    var error: Throwable? = null,
) : AuthRepositorio {

    var ultimoEmail: String? = null
    var ultimaActualizacion: ActualizarPerfilRequest? = null
    var cerroSesion = false

    override suspend fun login(email: String, password: String): LoginResponse {
        error?.let { throw it }
        ultimoEmail = email
        return LoginResponse("token-demo", 1, "Ana Paciente", RolUsuario.PACIENTE)
    }

    override fun session(): Flow<Session?> =
        flowOf(Session("token-demo", 1, RolUsuario.PACIENTE, "Ana Paciente"))

    override suspend fun logout() {
        cerroSesion = true
    }

    override suspend fun perfil(): PerfilResponse {
        error?.let { throw it }
        return perfilGuardado
    }

    override suspend fun actualizarPerfil(request: ActualizarPerfilRequest): PerfilResponse {
        error?.let { throw it }
        ultimaActualizacion = request
        return perfilGuardado.copy(
            rut = request.rut,
            nombreCompleto = request.nombreCompleto,
            telefono = request.telefono,
            fechaNacimiento = request.fechaNacimiento,
            consentimientoSalud = request.consentimientoSalud,
        )
    }
}

fun perfilDemo() = PerfilResponse(
    id = 1,
    rut = "11111111-1",
    nombreCompleto = "Ana Paciente",
    email = "paciente@sanaesencia.demo",
    telefono = "+56912345678",
    fechaNacimiento = "1998-05-20",
    consentimientoSalud = true,
)

// ------------------------------------------------------------------- catálogo

class CatalogRepositorioFalso(
    var error: Throwable? = null,
) : CatalogRepositorio {

    val profesionalesState = MutableStateFlow<List<Profesional>>(emptyList())
    val productosState = MutableStateFlow<List<Producto>>(emptyList())
    var refrescos = 0

    override fun profesionales(): Flow<List<Profesional>> = profesionalesState

    override fun productos(): Flow<List<Producto>> = productosState

    override suspend fun refresh() {
        refrescos++
        error?.let { throw it }
    }
}

// ----------------------------------------------------------------------- citas

class CitaRepositorioFalso(
    var citas: List<CitaResponse> = listOf(citaDemo()),
    var error: Throwable? = null,
) : CitaRepositorio {

    var creadas = mutableListOf<CrearCitaRequest>()
    var canceladas = mutableListOf<Long>()
    var reagendadas = mutableListOf<Pair<Long, ReagendarCitaRequest>>()
    var estados = mutableListOf<Pair<Long, EstadoCitaRequest>>()
    var fichas: List<FichaClinicaResponse> = emptyList()
    var disponibilidad: List<DisponibilidadResponse> = emptyList()

    override suspend fun crear(request: CrearCitaRequest): CitaResponse {
        error?.let { throw it }
        creadas += request
        return citaDemo()
    }

    override suspend fun misCitas(): List<CitaResponse> {
        error?.let { throw it }
        return citas
    }

    override suspend fun cancelar(id: Long): Map<String, String> {
        error?.let { throw it }
        canceladas += id
        return mapOf("mensaje" to "Cita cancelada")
    }

    override suspend fun disponibilidad(
        profesionalId: Long,
        fecha: LocalDate,
    ): List<DisponibilidadResponse> {
        error?.let { throw it }
        return disponibilidad
    }

    override suspend fun reagendar(id: Long, request: ReagendarCitaRequest): CitaResponse {
        error?.let { throw it }
        reagendadas += id to request
        return citaDemo()
    }

    override suspend fun agenda(): List<CitaResponse> {
        error?.let { throw it }
        return citas
    }

    override suspend fun historialPaciente(id: Long): List<FichaClinicaResponse> {
        error?.let { throw it }
        return fichas
    }

    override suspend fun actualizarEstado(id: Long, request: EstadoCitaRequest): CitaResponse {
        error?.let { throw it }
        estados += id to request
        return citaDemo()
    }
}

fun citaDemo(
    id: Long = 1,
    fecha: String = "2026-12-01",
) = CitaResponse(
    id = id,
    pacienteId = 1,
    profesionalId = 2,
    boxId = 3,
    fecha = fecha,
    horaInicio = "10:00",
    horaFin = "11:00",
    modalidad = ModalidadCita.PRESENCIAL,
    estado = EstadoCita.RESERVADA,
)

fun solicitudCitaDemo(
    fecha: String = "2026-12-01",
    horaInicio: String = "10:00",
) = CrearCitaRequest(
    profesionalId = 2,
    boxId = 3,
    fecha = fecha,
    horaInicio = horaInicio,
    horaFin = "11:00",
    modalidad = ModalidadCita.PRESENCIAL,
)
