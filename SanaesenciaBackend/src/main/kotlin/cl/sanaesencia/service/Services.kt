package cl.sanaesencia.service

import cl.sanaesencia.api.*
import cl.sanaesencia.domain.*
import cl.sanaesencia.repository.*
import cl.sanaesencia.security.JwtService
import cl.sanaesencia.security.NoAutenticadoException
import cl.sanaesencia.security.SinPermisoException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private val CHILE_CLOCK: Clock = Clock.system(ZoneId.of("America/Santiago"))

private fun Usuario.toPerfil() = PerfilResponse(id, rut, nombreCompleto, email, telefono, fechaNacimiento, consentimientoSalud)
private fun Profesional.toResponse() = ProfesionalResponse(id, usuarioId, titulo, categoriaEspecialidad, descripcionEnfoque, certificaciones, anosExperiencia, estado, fotoUrl)
private fun Producto.toResponse() = ProductoResponse(id, nombre, descripcion, precio, stock, imagenUrl, etiquetasTerapeuticas, estaActivo)
private fun Box.toResponse() = BoxResponse(id, numeroBox, estaHabilitado)
private fun Cita.toResponse() = CitaResponse(id, pacienteId, profesionalId, boxId, fecha, horaInicio, horaFin, modalidad, estado)

@Service
class AuthService(
    private val users: UsuarioRepository,
    private val encoder: PasswordEncoder,
    private val jwt: JwtService,
) {
    fun login(r: LoginRequest): LoginResponse {
        val u = users.findByEmail(r.email.trim().lowercase()) ?: throw IllegalArgumentException("Credenciales inválidas")
        require(encoder.matches(r.password, u.passwordHash)) { "Credenciales inválidas" }
        return LoginResponse(jwt.generate(u.id, u.email, u.rol), u.id, u.nombreCompleto, u.rol)
    }

    fun perfil(authorization: String?): PerfilResponse = users.findById(jwt.userIdFromBearer(authorization)).orElseThrow { NoAutenticadoException("Tu sesión ya no es válida. Inicia sesión nuevamente") }.toPerfil()

    @Transactional
    fun actualizarPerfil(authorization: String?, r: ActualizarPerfilRequest): PerfilResponse {
        val u = users.findById(jwt.userIdFromBearer(authorization)).orElseThrow { NoAutenticadoException("Tu sesión ya no es válida. Inicia sesión nuevamente") }
        require(r.rut.isNotBlank()) { "El RUT es obligatorio" }
        require(r.nombreCompleto.isNotBlank()) { "El nombre es obligatorio" }
        require(r.telefono.isNotBlank()) { "El teléfono es obligatorio" }
        require(r.consentimientoSalud) { "Debes aceptar el tratamiento de datos para guardar el perfil" }
        u.rut = r.rut.trim()
        u.nombreCompleto = r.nombreCompleto.trim()
        u.telefono = r.telefono.trim()
        u.fechaNacimiento = r.fechaNacimiento
        u.consentimientoSalud = true
        return users.save(u).toPerfil()
    }
}

@Service
class CitaService(
    private val citas: CitaRepository,
    private val profesionales: ProfesionalRepository,
    private val boxes: BoxRepository,
    private val users: UsuarioRepository,
    private val jwt: JwtService,
) {
    private val cancelada = EstadoCita.CANCELADA

    private fun validarAnticipacion(fecha: LocalDate, hora: LocalTime) {
        require(!LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now(CHILE_CLOCK).plusHours(15))) {
            "Debes agendar con al menos 15 horas de anticipación"
        }
    }

    private fun validarModalidad(boxId: Long?, modalidad: ModalidadCita): Long? {
        if (modalidad == ModalidadCita.ONLINE) {
            require(boxId == null) { "Una atención online no usa box" }
            return null
        }
        val id = boxId ?: boxes.findAllByOrderByNumeroBoxAsc().firstOrNull { it.estaHabilitado }?.id
            ?: throw IllegalArgumentException("No hay boxes habilitados disponibles")
        val box = boxes.findById(id).orElseThrow { IllegalArgumentException("Box no encontrado") }
        require(box.estaHabilitado && box.numeroBox in 1..8) { "Box no habilitado" }
        return id
    }

    private fun validarHorarioBase(fecha: LocalDate, inicio: LocalTime, fin: LocalTime) {
        require(fin.isAfter(inicio)) { "La hora de término debe ser posterior a la de inicio" }
        validarAnticipacion(fecha, inicio)
        require(!inicio.isBefore(LocalTime.of(9, 0)) && !fin.isAfter(LocalTime.of(17, 0))) { "El horario debe estar entre 09:00 y 17:00" }
    }

    @Transactional
    fun crear(authorization: String?, r: CrearCitaRequest): CitaResponse {
        val pacienteId = jwt.userIdFromBearer(authorization)
        val paciente = users.findById(pacienteId).orElseThrow { NoAutenticadoException("Tu sesión ya no es válida. Inicia sesión nuevamente") }
        if (paciente.rol != RolUsuario.PACIENTE) throw SinPermisoException("Solo un paciente puede reservar citas")
        validarHorarioBase(r.fecha, r.horaInicio, r.horaFin)
        val profesional = profesionales.findById(r.profesionalId).orElseThrow { IllegalArgumentException("Profesional no encontrado") }
        require(profesional.estado == EstadoProfesional.DISPONIBLE) { "El profesional no tiene agenda disponible" }
        val boxId = validarModalidad(r.boxId, r.modalidad)
        require(!citas.profesionalSolapaExcluyendo(-1, r.profesionalId, r.fecha, r.horaInicio, r.horaFin, cancelada)) { "El profesional ya está ocupado" }
        require(!citas.pacienteSolapa(pacienteId, r.fecha, r.horaInicio, r.horaFin, cancelada)) { "El paciente ya tiene una cita en ese horario" }
        if (boxId != null) require(!citas.boxSolapa(boxId, r.fecha, r.horaInicio, r.horaFin, cancelada)) { "El box ya está ocupado" }
        return citas.save(Cita(pacienteId = pacienteId, profesionalId = r.profesionalId, boxId = boxId, fecha = r.fecha, horaInicio = r.horaInicio, horaFin = r.horaFin, modalidad = r.modalidad)).toResponse()
    }

    fun misCitas(authorization: String?): List<CitaResponse> = citas.findAllByPacienteIdOrderByFechaDescHoraInicioDesc(jwt.userIdFromBearer(authorization)).map { it.toResponse() }

    @Transactional
    fun cancelar(authorization: String?, id: Long): Map<String, String> {
        val userId = jwt.userIdFromBearer(authorization)
        val c = citas.findById(id).orElseThrow { IllegalArgumentException("Cita no encontrada") }
        if (c.pacienteId != userId) throw SinPermisoException("No puedes cancelar esta cita")
        validarAnticipacion(c.fecha, c.horaInicio)
        c.estado = EstadoCita.CANCELADA
        citas.save(c)
        return mapOf("mensaje" to "Cita cancelada")
    }

    @Transactional
    fun reagendar(authorization: String?, id: Long, r: ReagendarCitaRequest): CitaResponse {
        val userId = jwt.userIdFromBearer(authorization)
        val c = citas.findById(id).orElseThrow { IllegalArgumentException("Cita no encontrada") }
        if (c.pacienteId != userId) throw SinPermisoException("No puedes modificar esta cita")
        validarAnticipacion(c.fecha, c.horaInicio)
        validarHorarioBase(r.fecha, r.horaInicio, r.horaFin)
        val p = profesionales.findById(r.profesionalId).orElseThrow { IllegalArgumentException("Profesional no encontrado") }
        require(p.estado == EstadoProfesional.DISPONIBLE) { "El profesional no tiene agenda disponible" }
        val boxId = validarModalidad(r.boxId, r.modalidad)
        require(!citas.profesionalSolapaExcluyendo(c.id, r.profesionalId, r.fecha, r.horaInicio, r.horaFin, cancelada)) { "El profesional ya está ocupado" }
        require(!citas.pacienteSolapaExcluyendo(c.id, userId, r.fecha, r.horaInicio, r.horaFin, cancelada)) { "El paciente ya tiene una cita en ese horario" }
        if (boxId != null) require(!citas.boxSolapaExcluyendo(c.id, boxId, r.fecha, r.horaInicio, r.horaFin, cancelada)) { "El box ya está ocupado" }
        c.profesionalId = r.profesionalId; c.boxId = boxId; c.fecha = r.fecha; c.horaInicio = r.horaInicio; c.horaFin = r.horaFin; c.modalidad = r.modalidad
        return citas.save(c).toResponse()
    }

    fun disponibilidad(profesionalId: Long, fecha: LocalDate): List<DisponibilidadResponse> {
        val p = profesionales.findById(profesionalId).orElseThrow { IllegalArgumentException("Profesional no encontrado") }
        return (9 until 17).map { hour ->
            val inicio = LocalTime.of(hour, 0); val fin = inicio.plusHours(1)
            val reason = when {
                p.estado != EstadoProfesional.DISPONIBLE -> "Profesional no disponible"
                LocalDateTime.of(fecha, inicio).isBefore(LocalDateTime.now(CHILE_CLOCK).plusHours(15)) -> "Requiere 15 horas de anticipación"
                citas.profesionalSolapaExcluyendo(-1, profesionalId, fecha, inicio, fin, cancelada) -> "Horario ocupado"
                else -> null
            }
            DisponibilidadResponse(fecha, inicio, fin, reason == null, reason)
        }
    }

    fun agenda(authorization: String?): List<CitaResponse> {
        val userId = jwt.userIdFromBearer(authorization)
        val p = profesionales.findByUsuarioId(userId) ?: throw SinPermisoException("Esta sección es solo para profesionales")
        return citas.findAllByProfesionalIdOrderByFechaAscHoraInicioAsc(p.id).map { it.toResponse() }
    }

    fun historial(pacienteId: Long, authorization: String?): List<FichaClinicaResponse> {
        val userId = jwt.userIdFromBearer(authorization)
        val actor = users.findById(userId).orElseThrow { NoAutenticadoException("Tu sesión ya no es válida. Inicia sesión nuevamente") }
        if (actor.rol == RolUsuario.PACIENTE && actor.id != pacienteId) throw SinPermisoException("No puedes consultar otro paciente")
        if (actor.rol == RolUsuario.PROFESIONAL) {
            val professional = profesionales.findByUsuarioId(actor.id) ?: throw SinPermisoException("Perfil profesional no encontrado")
            if (citas.findAllByProfesionalIdOrderByFechaAscHoraInicioAsc(professional.id).none { it.pacienteId == pacienteId }) throw SinPermisoException("Este paciente no está asignado a ti")
        }
        return citas.findAllByPacienteIdOrderByFechaDescHoraInicioDesc(pacienteId).map { FichaClinicaResponse(it.id, it.fecha, it.modalidad, it.estado, it.notasAtencion) }
    }

    @Transactional
    fun actualizarEstado(authorization: String?, id: Long, request: EstadoCitaRequest): CitaResponse {
        val userId = jwt.userIdFromBearer(authorization)
        val actor = users.findById(userId).orElseThrow { NoAutenticadoException("Tu sesión ya no es válida. Inicia sesión nuevamente") }
        val c = citas.findById(id).orElseThrow { IllegalArgumentException("Cita no encontrada") }
        if (actor.rol == RolUsuario.PROFESIONAL) {
            val p = profesionales.findByUsuarioId(actor.id) ?: throw SinPermisoException("Perfil profesional no encontrado")
            if (c.profesionalId != p.id) throw SinPermisoException("No puedes modificar esta cita")
        } else if (actor.rol != RolUsuario.ADMIN) throw SinPermisoException()
        c.estado = request.estado
        c.notasAtencion = request.notasAtencion
        return citas.save(c).toResponse()
    }
}

@Service
class PedidoService(
    private val products: ProductoRepository,
    private val pedidos: PedidoRepository,
    private val items: PedidoItemRepository,
    private val users: UsuarioRepository,
    private val jwt: JwtService,
) {
    @Transactional
    fun crear(authorization: String?, request: CrearPedidoRequest): PedidoResponse {
        val userId = jwt.userIdFromBearer(authorization)
        val user = users.findById(userId).orElseThrow { NoAutenticadoException("Tu sesión ya no es válida. Inicia sesión nuevamente") }
        if (user.rol != RolUsuario.PACIENTE) throw SinPermisoException("Solo los pacientes pueden comprar en la tienda")
        require(request.items.isNotEmpty()) { "El carrito está vacío" }
        val pedido = pedidos.save(Pedido(pacienteId = userId, estado = "PAGADO_DEMO"))
        var total = 0L
        val saved = request.items.map { req ->
            require(req.cantidad > 0) { "Cantidad inválida" }
            val p = products.findById(req.productoId).orElseThrow { IllegalArgumentException("Producto no encontrado") }
            require(p.estaActivo) { "El producto no está disponible" }
            require(p.stock >= req.cantidad) { "Stock insuficiente para ${p.nombre}" }
            p.stock -= req.cantidad
            products.save(p)
            val subtotal = p.precio * req.cantidad
            total += subtotal
            items.save(PedidoItem(pedidoId = pedido.id, productoId = p.id, nombreProducto = p.nombre, precioUnitario = p.precio, cantidad = req.cantidad, subtotal = subtotal))
        }
        pedido.total = total
        pedidos.save(pedido)
        return PedidoResponse(pedido.id, total, pedido.estado, saved.map { PedidoItemResponse(it.productoId, it.nombreProducto, it.precioUnitario, it.cantidad, it.subtotal) })
    }

    fun historial(authorization: String?): List<PedidoResponse> {
        val userId = jwt.userIdFromBearer(authorization)
        return pedidos.findAllByPacienteIdOrderByIdDesc(userId).map { pedido ->
            PedidoResponse(pedido.id, pedido.total, pedido.estado, items.findAllByPedidoId(pedido.id).map { PedidoItemResponse(it.productoId, it.nombreProducto, it.precioUnitario, it.cantidad, it.subtotal) })
        }
    }
}

@Service
class AdminService(
    private val products: ProductoRepository,
    private val professionals: ProfesionalRepository,
    private val users: UsuarioRepository,
    private val boxes: BoxRepository,
    private val jwt: JwtService,
) {
    /** Lanza 401 si no hay sesión válida y 403 si el usuario no es ADMIN. Siempre se llama ANTES de leer o modificar datos. */
    fun verificarAdmin(authorization: String?) {
        val id = jwt.userIdFromBearer(authorization)
        val user = users.findById(id).orElseThrow { NoAutenticadoException("Tu sesión ya no es válida. Inicia sesión nuevamente") }
        if (user.rol != RolUsuario.ADMIN) throw SinPermisoException("Esta sección es solo para administradores")
    }
    private fun requireAdmin(authorization: String?) = verificarAdmin(authorization)
    fun stockBajo(auth: String?): List<ProductoResponse> { requireAdmin(auth); return products.findAll().filter { it.estaActivo && it.stock <= 3 }.map { it.toResponse() } }
    fun profesionales(auth: String?): List<ProfesionalResponse> { requireAdmin(auth); return professionals.findAll().map { it.toResponse() } }
    fun boxes(auth: String?): List<BoxResponse> { requireAdmin(auth); return boxes.findAllByOrderByNumeroBoxAsc().map { it.toResponse() } }
    @Transactional
    fun crearProducto(auth: String?, r: ProductoAdminRequest): ProductoResponse { requireAdmin(auth); return products.save(Producto(nombre=r.nombre, descripcion=r.descripcion, precio=r.precio, stock=r.stock, imagenUrl=r.imagenUrl, etiquetasTerapeuticas=r.etiquetasTerapeuticas, estaActivo=r.estaActivo)).toResponse() }
    fun editarProducto(auth: String?, id: Long, r: ProductoAdminRequest): ProductoResponse { requireAdmin(auth); val p=products.findById(id).orElseThrow{IllegalArgumentException("Producto no encontrado")}; p.nombre=r.nombre;p.descripcion=r.descripcion;p.precio=r.precio;p.stock=r.stock;p.imagenUrl=r.imagenUrl;p.etiquetasTerapeuticas=r.etiquetasTerapeuticas;p.estaActivo=r.estaActivo;return products.save(p).toResponse() }
    fun toggleBox(auth: String?, id: Long, r: EstadoBoxRequest): BoxResponse { requireAdmin(auth); val b=boxes.findById(id).orElseThrow{IllegalArgumentException("Box no encontrado")};b.estaHabilitado=r.habilitado;return boxes.save(b).toResponse() }
    fun crearProfesional(auth: String?, r: ProfesionalAdminRequest): ProfesionalResponse { requireAdmin(auth); val u=users.findById(r.usuarioId).orElseThrow{IllegalArgumentException("Usuario no encontrado")};require(u.rol==RolUsuario.PROFESIONAL){"El usuario debe tener rol PROFESIONAL"};require(professionals.findByUsuarioId(u.id)==null){"El usuario ya tiene perfil profesional"};return professionals.save(Profesional(usuarioId=u.id,titulo=r.titulo,categoriaEspecialidad=r.categoriaEspecialidad,descripcionEnfoque=r.descripcionEnfoque,certificaciones=r.certificaciones,anosExperiencia=r.anosExperiencia,estado=r.estado,fotoUrl=r.fotoUrl)).toResponse() }
    fun estadoProfesional(auth: String?, id: Long, estado: String): ProfesionalResponse { requireAdmin(auth); val p=professionals.findById(id).orElseThrow{IllegalArgumentException("Profesional no encontrado")};p.estado=runCatching{EstadoProfesional.valueOf(estado)}.getOrElse{throw IllegalArgumentException("Estado profesional inválido")};return professionals.save(p).toResponse() }
}

@Service
class DesercionService(
    private val users: UsuarioRepository,
    private val citas: CitaRepository,
    @Value("\${app.desercion.dias-inactividad}") private val dias: Int,
) {
    fun alertas(): List<DesercionResponse> = users.findAll().filter { it.rol == RolUsuario.PACIENTE }.mapNotNull { u ->
        val ultima = citas.findAllByPacienteIdOrderByFechaDescHoraInicioDesc(u.id).firstOrNull { it.estado == EstadoCita.COMPLETADA }?.fecha ?: return@mapNotNull null
        val d = ChronoUnit.DAYS.between(ultima, LocalDate.now(CHILE_CLOCK))
        DesercionResponse(u.id, ultima.toString(), d, if (d >= dias) "ALERTA" else "NORMAL", d >= dias)
    }.filter { it.alerta }
}
