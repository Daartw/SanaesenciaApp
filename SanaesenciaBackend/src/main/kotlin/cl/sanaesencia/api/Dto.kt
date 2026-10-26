package cl.sanaesencia.api

import cl.sanaesencia.domain.*
import java.time.LocalDate
import java.time.LocalTime

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val usuarioId: Long, val nombre: String, val rol: RolUsuario)

data class ProfesionalResponse(
    val id: Long,
    val usuarioId: Long,
    val titulo: String,
    val categoriaEspecialidad: String,
    val descripcionEnfoque: String,
    val certificaciones: String,
    val anosExperiencia: Int,
    val estado: EstadoProfesional,
    val fotoUrl: String?,
)

data class ProductoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Long,
    val stock: Int,
    val imagenUrl: String?,
    val etiquetasTerapeuticas: String,
    val estaActivo: Boolean,
)

data class BoxResponse(val id: Long, val numeroBox: Int, val estaHabilitado: Boolean)

data class CrearCitaRequest(
    val profesionalId: Long,
    val boxId: Long?,
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val modalidad: ModalidadCita,
)

data class ReagendarCitaRequest(
    val profesionalId: Long,
    val boxId: Long?,
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val modalidad: ModalidadCita,
)

data class CitaResponse(
    val id: Long,
    val pacienteId: Long,
    val profesionalId: Long,
    val boxId: Long?,
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val modalidad: ModalidadCita,
    val estado: EstadoCita,
)

data class DisponibilidadResponse(
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val disponible: Boolean,
    val motivo: String?,
)

data class FichaClinicaResponse(
    val citaId: Long,
    val fecha: LocalDate,
    val modalidad: ModalidadCita,
    val estado: EstadoCita,
    val notasAtencion: String?,
)

data class PerfilResponse(
    val id: Long,
    val rut: String,
    val nombreCompleto: String,
    val email: String,
    val telefono: String,
    val fechaNacimiento: LocalDate?,
    val consentimientoSalud: Boolean,
)

data class ActualizarPerfilRequest(
    val rut: String,
    val nombreCompleto: String,
    val telefono: String,
    val fechaNacimiento: LocalDate?,
    val consentimientoSalud: Boolean,
)

data class PedidoItemRequest(val productoId: Long, val cantidad: Int)
data class CrearPedidoRequest(val items: List<PedidoItemRequest>)
data class PedidoItemResponse(
    val productoId: Long,
    val nombreProducto: String,
    val precioUnitario: Long,
    val cantidad: Int,
    val subtotal: Long,
)
data class PedidoResponse(
    val id: Long,
    val total: Long,
    val estado: String,
    val items: List<PedidoItemResponse>,
)

data class EstadoCitaRequest(val estado: EstadoCita, val notasAtencion: String?)
data class DesercionResponse(
    val pacienteId: Long,
    val ultimaSesion: String?,
    val diasSinSesion: Long?,
    val estado: String,
    val alerta: Boolean,
)
data class EstadoBoxRequest(val habilitado: Boolean)
data class ProductoAdminRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Long,
    val stock: Int,
    val imagenUrl: String?,
    val etiquetasTerapeuticas: String,
    val estaActivo: Boolean = true,
)
data class EstadoProductoRequest(val activo: Boolean)

/**
 * Para crear un profesional hay dos opciones:
 * - Enviar nombreCompleto + email: si la cuenta no existe se crea automáticamente
 *   (rol PROFESIONAL, contraseña inicial = password o "Demo123!").
 * - Enviar usuarioId de una cuenta ya existente.
 */
data class ProfesionalAdminRequest(
    val usuarioId: Long? = null,
    val nombreCompleto: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val rut: String? = null,
    val password: String? = null,
    val titulo: String,
    val categoriaEspecialidad: String,
    val descripcionEnfoque: String = "",
    val certificaciones: String = "",
    val anosExperiencia: Int = 0,
    val estado: EstadoProfesional = EstadoProfesional.DISPONIBLE,
    val fotoUrl: String? = null,
)
