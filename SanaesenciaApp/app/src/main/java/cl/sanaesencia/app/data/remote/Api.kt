
package cl.sanaesencia.app.data.remote

import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.EstadoProfesional
import cl.sanaesencia.app.domain.model.ModalidadCita
import cl.sanaesencia.app.domain.model.RolUsuario
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SanaesenciaApi {

    // =========================================================
    // AUTENTICACIÓN
    // =========================================================

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @GET("auth/perfil")
    suspend fun perfil(): PerfilResponse

    @PUT("auth/perfil")
    suspend fun actualizarPerfil(
        @Body request: ActualizarPerfilRequest
    ): PerfilResponse

    // RF-13: registra el token de Firebase del teléfono para recibir notificaciones push
    @PUT("auth/push-token")
    suspend fun registrarTokenPush(
        @Body request: TokenPushRequest
    ): Map<String, String>


    // =========================================================
    // PROFESIONALES
    // =========================================================

    @GET("profesionales")
    suspend fun profesionales(): List<ProfesionalResponse>

    @GET("citas/disponibilidad")
    suspend fun disponibilidad(
        @Query("profesionalId") profesionalId: Long,
        @Query("fecha") fecha: String
    ): List<DisponibilidadResponse>


    // =========================================================
    // BOXES
    // =========================================================

    @GET("boxes")
    suspend fun boxes(): List<BoxResponse>

    @GET("admin/boxes")
    suspend fun adminBoxes(): List<BoxResponse>

    // El backend usa PUT /admin/boxes/{id}
    @PUT("admin/boxes/{id}")
    suspend fun adminBox(
        @Path("id") id: Long,
        @Body request: EstadoBoxRequest
    ): BoxResponse


    // =========================================================
    // PRODUCTOS
    // =========================================================

    @GET("productos")
    suspend fun productos(): List<ProductoResponse>


    // =========================================================
    // PROMOCIONES
    // =========================================================

    @GET("promociones")
    suspend fun promociones(): List<PromocionResponse>

    @POST("promociones")
    suspend fun crearPromocion(
        @Body request: PromocionRequest
    ): PromocionResponse


    // =========================================================
    // CITAS
    // =========================================================

    @POST("citas")
    suspend fun crearCita(
        @Body request: CrearCitaRequest
    ): CitaResponse

    @GET("citas/mis-citas")
    suspend fun misCitas(): List<CitaResponse>

    // El backend usa DELETE /citas/{id}
    @DELETE("citas/{id}")
    suspend fun cancelarCita(
        @Path("id") citaId: Long
    ): Map<String, String>

    // El backend usa PUT /citas/{id}/reagendar
    @PUT("citas/{id}/reagendar")
    suspend fun reagendar(
        @Path("id") citaId: Long,
        @Body request: ReagendarCitaRequest
    ): CitaResponse

    // El backend usa PUT /citas/{id}/estado
    @PUT("citas/{id}/estado")
    suspend fun actualizarEstado(
        @Path("id") citaId: Long,
        @Body request: EstadoCitaRequest
    ): CitaResponse

    @GET("citas/agenda")
    suspend fun agendaProfesional(): List<CitaResponse>

    // Ruta definida en CitaController
    @GET("citas/paciente/{pacienteId}/historial")
    suspend fun historialPaciente(
        @Path("pacienteId") pacienteId: Long
    ): List<FichaClinicaResponse>


    // =========================================================
    // PEDIDOS / CARRITO
    // =========================================================

    @POST("pedidos")
    suspend fun crearPedido(
        @Body request: CrearPedidoRequest
    ): PedidoResponse

    @GET("pedidos/mis-pedidos")
    suspend fun misPedidos(): List<PedidoResponse>


    // =========================================================
    // ADMINISTRACIÓN
    // =========================================================

    @GET("admin/profesionales")
    suspend fun adminProfesionales(): List<ProfesionalResponse>

    @POST("admin/profesionales")
    suspend fun adminCrearProfesional(
        @Body request: ProfesionalAdminRequest
    ): ProfesionalResponse

    @GET("admin/productos")
    suspend fun adminProductos(): List<ProductoResponse>

    // RF-08: el administrador revisa y gestiona todas las citas
    @GET("admin/citas")
    suspend fun adminCitas(
        @Query("fecha") fecha: String? = null
    ): List<CitaAdminResponse>

    // RF-11: ocupación de los 8 boxes en un día
    @GET("admin/boxes/ocupacion")
    suspend fun adminOcupacionBoxes(
        @Query("fecha") fecha: String
    ): List<BoxOcupacionResponse>

    // RF-05: edición y alta/baja de terapeutas
    @PUT("admin/profesionales/{id}")
    suspend fun adminEditarProfesional(
        @Path("id") id: Long,
        @Body request: ProfesionalEditRequest
    ): ProfesionalResponse

    @PUT("admin/profesionales/{id}/cuenta")
    suspend fun adminCuentaProfesional(
        @Path("id") id: Long,
        @Body request: CuentaActivaRequest
    ): ProfesionalResponse

    // RF-14: sincronización con MEDIlink
    @POST("admin/medilink/sincronizar")
    suspend fun adminSincronizarMedilink(): MedilinkSyncResponse

    @DELETE("admin/productos/{id}")
    suspend fun adminEliminarProducto(
        @Path("id") id: Long
    ): Map<String, String>

    @PUT("admin/productos/{id}/activo")
    suspend fun adminActivarProducto(
        @Path("id") id: Long,
        @Body request: EstadoProductoRequest
    ): ProductoResponse

    @POST("admin/productos")
    suspend fun adminCrearProducto(
        @Body request: ProductoAdminRequest
    ): ProductoResponse

    @PUT("admin/productos/{id}")
    suspend fun adminEditarProducto(
        @Path("id") id: Long,
        @Body request: ProductoAdminRequest
    ): ProductoResponse

    @PUT("admin/profesionales/{id}/estado")
    suspend fun adminEstadoProfesional(
        @Path("id") id: Long,
        @Body request: EstadoProfesionalRequest
    ): ProfesionalResponse

    @GET("admin/desercion")
    suspend fun adminDesercion(): List<DesercionResponse>

    @GET("admin/stock-bajo")
    suspend fun adminStockBajo(): List<ProductoResponse>
}


// =============================================================
// RESPUESTAS
// =============================================================

data class LoginResponse(
    val token: String,
    val usuarioId: Long,
    val nombre: String,
    val rol: RolUsuario
)

data class PerfilResponse(
    val id: Long,
    val rut: String?,
    val nombreCompleto: String,
    val email: String,
    val telefono: String?,
    val fechaNacimiento: String?,
    val consentimientoSalud: Boolean
)

data class ProfesionalResponse(
    val id: Long,
    val usuarioId: Long,
    val titulo: String,
    val categoriaEspecialidad: String,
    val descripcionEnfoque: String,
    val certificaciones: String = "",
    val anosExperiencia: Int = 0,
    val estado: EstadoProfesional,
    val fotoUrl: String? = null,
    val nombre: String = "",
    val email: String = "",
    val cuentaActiva: Boolean = true
)

data class ProductoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Long,
    val stock: Int,
    val imagenUrl: String? = null,
    val etiquetasTerapeuticas: String,
    val estaActivo: Boolean = true
)

data class BoxResponse(
    val id: Long,
    val numeroBox: Int,
    val nombre: String = "Box $numeroBox",
    val estaHabilitado: Boolean
)

// Coincide con la entidad Promocion del backend.
data class PromocionResponse(
    val id: Long,
    val titulo: String = "",
    val porcentajeDescuento: Double = 0.0,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val esActiva: Boolean = true
)

data class TokenPushRequest(
    val token: String
)

data class CitaResponse(
    val id: Long,
    val pacienteId: Long,
    val profesionalId: Long,
    val boxId: Long?,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val modalidad: ModalidadCita,
    val estado: EstadoCita
)

data class DisponibilidadResponse(
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val disponible: Boolean,
    val motivo: String?,
    val boxesLibres: Int = 0
)

data class FichaClinicaResponse(
    val citaId: Long,
    val fecha: String,
    val modalidad: ModalidadCita,
    val estado: EstadoCita,
    val notasAtencion: String?
)

data class PedidoResponse(
    val id: Long,
    val total: Long,
    val estado: String,
    val items: List<PedidoItemResponse>
)

data class PedidoItemResponse(
    val productoId: Long,
    val nombreProducto: String,
    val precioUnitario: Long,
    val cantidad: Int,
    val subtotal: Long
)


// =============================================================
// SOLICITUDES
// =============================================================

data class LoginRequest(
    val email: String,
    val password: String
)

data class ActualizarPerfilRequest(
    val rut: String,
    val nombreCompleto: String,
    val telefono: String,
    val fechaNacimiento: String?,
    val consentimientoSalud: Boolean
)

data class CrearCitaRequest(
    val profesionalId: Long,
    val boxId: Long?,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val modalidad: ModalidadCita
)

data class ReagendarCitaRequest(
    val profesionalId: Long,
    val boxId: Long?,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val modalidad: ModalidadCita
)

data class EstadoCitaRequest(
    val estado: EstadoCita,
    val notasAtencion: String? = null
)

data class CrearPedidoRequest(
    val items: List<PedidoItemRequest>
)

data class PedidoItemRequest(
    val productoId: Long,
    val cantidad: Int
)

data class PromocionRequest(
    val titulo: String,
    val porcentajeDescuento: Double,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val esActiva: Boolean = true
)

// El backend recibe {"estado": "DISPONIBLE" | "AUSENTE" | ...}
data class EstadoProfesionalRequest(
    val estado: String
)

// El backend espera el campo "habilitado".
data class EstadoBoxRequest(
    val habilitado: Boolean
)

data class EstadoProductoRequest(
    val activo: Boolean
)

data class ProfesionalEditRequest(
    val titulo: String,
    val categoriaEspecialidad: String,
    val descripcionEnfoque: String? = null,
    val anosExperiencia: Int? = null
)

data class CuentaActivaRequest(
    val activo: Boolean
)

data class CitaAdminResponse(
    val id: Long,
    val pacienteId: Long,
    val pacienteNombre: String = "",
    val profesionalId: Long,
    val profesionalNombre: String = "",
    val boxId: Long? = null,
    val numeroBox: Int? = null,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val modalidad: ModalidadCita,
    val estado: EstadoCita
)

data class BoxOcupacionResponse(
    val id: Long,
    val numeroBox: Int,
    val estaHabilitado: Boolean,
    val citas: List<CitaAdminResponse> = emptyList()
)

data class MedilinkSyncResponse(
    val estado: String,
    val mensaje: String,
    val citasEnviadas: Int = 0,
    val citasPendientes: Int = 0
)

/**
 * Para crear un profesional basta con nombre + correo: si la cuenta no existe,
 * el backend la crea con la contraseña indicada (o "Demo123!").
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
    val fotoUrl: String? = null
)

data class ProductoAdminRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Long,
    val stock: Int,
    val imagenUrl: String? = null,
    val etiquetasTerapeuticas: String,
    val estaActivo: Boolean = true
)

data class DesercionResponse(
    val pacienteId: Long,
    val ultimaSesion: String? = null,
    val diasSinSesion: Long? = null,
    val estado: String = "",
    val alerta: Boolean = false
)
