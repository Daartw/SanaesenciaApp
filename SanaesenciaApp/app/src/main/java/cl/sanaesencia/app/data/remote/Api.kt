package cl.sanaesencia.app.data.remote

import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.EstadoProfesional
import cl.sanaesencia.app.domain.model.ModalidadCita
import cl.sanaesencia.app.domain.model.RolUsuario
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SanaesenciaApi {

    // -------------------------------------------------------------------------
    // AUTENTICACIÓN
    // -------------------------------------------------------------------------

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


    // -------------------------------------------------------------------------
    // PROFESIONALES
    // -------------------------------------------------------------------------

    @GET("profesionales")
    suspend fun profesionales(): List<ProfesionalResponse>

    @GET("profesionales/{id}/disponibilidad")
    suspend fun disponibilidad(
        @Path("id") profesionalId: Long,
        @Query("fecha") fecha: String
    ): List<DisponibilidadResponse>


    // -------------------------------------------------------------------------
    // BOXES
    // -------------------------------------------------------------------------

    @GET("boxes")
    suspend fun boxes(): List<BoxResponse>


    // -------------------------------------------------------------------------
    // PRODUCTOS
    // -------------------------------------------------------------------------

    @GET("productos")
    suspend fun productos(): List<ProductoResponse>


    // -------------------------------------------------------------------------
    // PROMOCIONES
    // -------------------------------------------------------------------------

    @GET("promociones")
    suspend fun promociones(): List<PromocionResponse>

    @POST("promociones")
    suspend fun crearPromocion(
        @Body request: PromocionRequest
    ): PromocionResponse


    // -------------------------------------------------------------------------
    // CITAS
    // -------------------------------------------------------------------------

    @POST("citas")
    suspend fun crearCita(
        @Body request: CrearCitaRequest
    ): CitaResponse

    @GET("citas/mis-citas")
    suspend fun misCitas(): List<CitaResponse>

    @GET("citas/paciente/{id}")
    suspend fun citasPaciente(
        @Path("id") pacienteId: Long
    ): List<CitaResponse>

    @GET("citas/profesional/{id}")
    suspend fun citasProfesional(
        @Path("id") profesionalId: Long
    ): List<CitaResponse>

    @PATCH("citas/{id}/cancelar")
    suspend fun cancelarCita(
        @Path("id") citaId: Long
    ): Map<String, String>

    @PATCH("citas/{id}/reagendar")
    suspend fun reagendar(
        @Path("id") citaId: Long,
        @Body request: ReagendarCitaRequest
    ): CitaResponse

    @PATCH("citas/{id}/estado")
    suspend fun actualizarEstado(
        @Path("id") citaId: Long,
        @Body request: EstadoCitaRequest
    ): CitaResponse

    @GET("citas/agenda")
    suspend fun agendaProfesional(): List<CitaResponse>

    @GET("citas/historial/{pacienteId}")
    suspend fun historialPaciente(
        @Path("pacienteId") pacienteId: Long
    ): List<FichaClinicaResponse>


    // -------------------------------------------------------------------------
    // PEDIDOS / CARRITO
    // -------------------------------------------------------------------------

    @POST("pedidos")
    suspend fun crearPedido(
        @Body request: CrearPedidoRequest
    ): PedidoResponse

    @GET("pedidos/mis-pedidos")
    suspend fun misPedidos(): List<PedidoResponse>


    // -------------------------------------------------------------------------
    // ADMIN
    // -------------------------------------------------------------------------

    @GET("admin/boxes")
    suspend fun adminBoxes(): List<BoxResponse>

    @PATCH("admin/boxes/{id}")
    suspend fun adminBox(
        @Path("id") id: Long,
        @Body request: EstadoBoxRequest
    ): BoxResponse

    @GET("admin/profesionales")
    suspend fun adminProfesionales(): List<ProfesionalResponse>

    @POST("admin/profesionales")
    suspend fun adminCrearProfesional(
        @Body request: ProfesionalAdminRequest
    ): ProfesionalResponse

    @POST("admin/productos")
    suspend fun adminCrearProducto(
        @Body request: ProductoAdminRequest
    ): ProductoResponse

    @GET("admin/desercion")
    suspend fun adminDesercion(): List<DesercionResponse>

    @GET("admin/stock-bajo")
    suspend fun adminStockBajo(): List<ProductoResponse>
}


// =============================================================================
// RESPUESTAS
// =============================================================================

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
    val estado: EstadoProfesional
)

data class ProductoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Long,
    val stock: Int,
    val etiquetasTerapeuticas: String
)

data class BoxResponse(
    val id: Long,
    val numeroBox: Int,
    val nombre: String,
    val estaHabilitado: Boolean
)

data class PromocionResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val porcentajeDescuento: Int
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
    val motivo: String?
)

data class FichaClinicaResponse(
    val id: Long,
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


// =============================================================================
// REQUESTS
// =============================================================================

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
    val nombre: String,
    val descripcion: String,
    val porcentajeDescuento: Int
)

data class EstadoBoxRequest(
    val estaHabilitado: Boolean
)

data class ProfesionalAdminRequest(
    val usuarioId: Long,
    val titulo: String,
    val categoriaEspecialidad: String,
    val descripcionEnfoque: String,
    val certificaciones: String,
    val anosExperiencia: Int,
    val estado: EstadoProfesional,
    val fotoUrl: String? = null
)

data class ProductoAdminRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Long,
    val stock: Int,
    val imagenUrl: String? = null,
    val etiquetasTerapeuticas: String
)

data class DesercionResponse(
    val pacienteId: Long,
    val nombrePaciente: String,
    val diasInactividad: Long,
    val nivel: String
)