package cl.sanaesencia.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

enum class RolUsuario { PACIENTE, PROFESIONAL, ADMIN }
enum class EstadoProfesional { DISPONIBLE, AUSENTE, VACACIONES, AGENDA_PAUSADA }
enum class ModalidadCita { PRESENCIAL, ONLINE }
enum class EstadoCita { RESERVADA, CONFIRMADA, COMPLETADA, CANCELADA, NO_ASISTIO }

@Entity
@Table(name = "usuarios", uniqueConstraints = [UniqueConstraint(columnNames = ["rut"]), UniqueConstraint(columnNames = ["email"])])
class Usuario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var rut: String = "",
    var nombreCompleto: String = "",
    var email: String = "",
    var telefono: String = "",
    var fechaNacimiento: LocalDate? = null,
    var consentimientoSalud: Boolean = false,
    @Enumerated(EnumType.STRING) var rol: RolUsuario = RolUsuario.PACIENTE,
    var passwordHash: String = "",
    // null se trata como activo, así las bases MySQL existentes no dejan cuentas bloqueadas al agregar la columna.
    var activo: Boolean? = true,
    // RF-13: token de Firebase Cloud Messaging del teléfono del usuario.
    @Column(length = 500) var pushToken: String? = null,
) {
    @get:jakarta.persistence.Transient
    val estaActivo: Boolean get() = activo != false
}

@Entity
@Table(name = "profesionales")
class Profesional(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var usuarioId: Long = 0,
    var titulo: String = "",
    var categoriaEspecialidad: String = "",
    @Column(length = 2000) var descripcionEnfoque: String = "",
    @Column(length = 2000) var certificaciones: String = "",
    var anosExperiencia: Int = 0,
    @Enumerated(EnumType.STRING) var estado: EstadoProfesional = EstadoProfesional.DISPONIBLE,
    var fotoUrl: String? = null,
)

@Entity
@Table(name = "boxes", uniqueConstraints = [UniqueConstraint(columnNames = ["numeroBox"])])
class Box(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var numeroBox: Int = 1,
    var estaHabilitado: Boolean = true,
)

@Entity
@Table(name = "citas")
class Cita(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var pacienteId: Long = 0,
    var profesionalId: Long = 0,
    var boxId: Long? = null,
    var fecha: LocalDate = LocalDate.now(),
    var horaInicio: LocalTime = LocalTime.of(9, 0),
    var horaFin: LocalTime = LocalTime.of(10, 0),
    @Enumerated(EnumType.STRING) var modalidad: ModalidadCita = ModalidadCita.ONLINE,
    @Enumerated(EnumType.STRING) var estado: EstadoCita = EstadoCita.RESERVADA,
    @Column(length = 2000) var notasAtencion: String? = null,
)

@Entity
@Table(name = "productos")
class Producto(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var nombre: String = "",
    @Column(length = 2000) var descripcion: String = "",
    var precio: Long = 0,
    var stock: Int = 0,
    var imagenUrl: String? = null,
    @Column(length = 1000) var etiquetasTerapeuticas: String = "",
    var estaActivo: Boolean = true,
)

@Entity
@Table(name = "promociones")
class Promocion(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var titulo: String = "",
    var porcentajeDescuento: Double = 0.0,
    var fechaInicio: LocalDate = LocalDate.now(),
    var fechaFin: LocalDate = LocalDate.now(),
    var esActiva: Boolean = true,
)

@Entity
@Table(name = "pedidos")
class Pedido(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var pacienteId: Long = 0,
    var total: Long = 0,
    var estado: String = "PAGADO_DEMO",
)

@Entity
@Table(name = "pedido_items")
class PedidoItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var pedidoId: Long = 0,
    var productoId: Long = 0,
    var nombreProducto: String = "",
    var precioUnitario: Long = 0,
    var cantidad: Int = 0,
    var subtotal: Long = 0,
)
