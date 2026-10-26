
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
    val fotoUrl: String? = null
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
    val nombre: String,
    val descripcion: String,
    val porcentajeDescuento: Int
)

// El backend recibe {"estado": "DISPONIBLE" | "AUSENTE" | ...}
data class EstadoProfesionalRequest(
    val estado: String
)

// El backend espera el campo "habilitado".
data class EstadoBoxRequest(
    val habilitado: Boolean
)

data class ProfesionalAdminRequest(
    val usuarioId: Long,
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
