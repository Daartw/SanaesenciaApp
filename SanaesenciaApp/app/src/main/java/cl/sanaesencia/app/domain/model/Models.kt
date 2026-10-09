package cl.sanaesencia.app.domain.model

sealed class Usuario(
    val rol: RolUsuario,
) {
    abstract fun pantallaInicial(): String
    abstract fun permisos(): Set<Permiso>

    class Paciente : Usuario(RolUsuario.PACIENTE) {
        override fun pantallaInicial() = "home"
        override fun permisos() = setOf(Permiso.AGENDAR, Permiso.COMPRAR, Permiso.VER_PROPIAS_CITAS)
    }

    class Profesional : Usuario(RolUsuario.PROFESIONAL) {
        override fun pantallaInicial() = "agenda"
        override fun permisos() = setOf(Permiso.VER_AGENDA, Permiso.VER_FICHA_ASIGNADA)
    }

    class Administrador : Usuario(RolUsuario.ADMIN) {
        override fun pantallaInicial() = "admin"
        override fun permisos() = setOf(Permiso.GESTIONAR_PERSONAL, Permiso.GESTIONAR_TIENDA, Permiso.GESTIONAR_BOXES, Permiso.VER_METRICAS)
    }

    companion object {
        fun desdeRol(rol: RolUsuario): Usuario = when (rol) {
            RolUsuario.PACIENTE -> Paciente()
            RolUsuario.PROFESIONAL -> Profesional()
            RolUsuario.ADMIN -> Administrador()
        }
    }
}

enum class Permiso {
    AGENDAR,
    COMPRAR,
    VER_PROPIAS_CITAS,
    VER_AGENDA,
    VER_FICHA_ASIGNADA,
    GESTIONAR_PERSONAL,
    GESTIONAR_TIENDA,
    GESTIONAR_BOXES,
    VER_METRICAS,
}

sealed interface ItemCatalogo {
    val id: Long
    val nombre: String
    val precio: Long

    fun precioFinal(): Long
    fun descripcionCorta(): String
}

data class Producto(
    override val id: Long,
    override val nombre: String,
    val descripcion: String,
    override val precio: Long,
    val stock: Int,
    val etiquetas: List<String>,
) : ItemCatalogo {
    override fun precioFinal(): Long = precio
    override fun descripcionCorta(): String = descripcion.take(80)
}

data class Servicio(
    override val id: Long,
    override val nombre: String,
    override val precio: Long,
    val descripcion: String,
    val duracionMinutos: Int,
) : ItemCatalogo {
    override fun precioFinal(): Long = precio
    override fun descripcionCorta(): String = "$duracionMinutos min · ${descripcion.take(60)}"
}

enum class RolUsuario {
    PACIENTE,
    PROFESIONAL,
    ADMIN,
}

enum class EstadoProfesional {
    DISPONIBLE,
    AUSENTE,
    VACACIONES,
    AGENDA_PAUSADA,
}

enum class ModalidadCita {
    PRESENCIAL,
    ONLINE,
}

enum class EstadoCita {
    RESERVADA,
    CONFIRMADA,
    COMPLETADA,
    CANCELADA,
    NO_ASISTIO,
}

data class Profesional(
    val id: Long,
    val usuarioId: Long,
    val titulo: String,
    val categoriaEspecialidad: String,
    val descripcionEnfoque: String,
    val estado: EstadoProfesional,
)

data class Box(
    val id: Long,
    val numeroBox: Int,
    val estaHabilitado: Boolean,
)
