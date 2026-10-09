package cl.sanaesencia.api

import cl.sanaesencia.repository.*
import cl.sanaesencia.service.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val service: AuthService) {
    @PostMapping("/login") fun login(@RequestBody r: LoginRequest) = service.login(r)
    @GetMapping("/perfil") fun perfil(@RequestHeader("Authorization", required = false) auth: String?) = service.perfil(auth)
    @PutMapping("/perfil") fun actualizarPerfil(@RequestHeader("Authorization", required = false) auth: String?, @RequestBody r: ActualizarPerfilRequest) = service.actualizarPerfil(auth, r)
}

@RestController
@RequestMapping("/profesionales")
class ProfesionalController(private val repo: ProfesionalRepository) {
    @GetMapping fun all() = repo.findAll().map { ProfesionalResponse(it.id,it.usuarioId,it.titulo,it.categoriaEspecialidad,it.descripcionEnfoque,it.certificaciones,it.anosExperiencia,it.estado,it.fotoUrl) }
}

@RestController
@RequestMapping("/boxes")
class BoxController(private val repo: BoxRepository) {
    @GetMapping fun all() = repo.findAllByOrderByNumeroBoxAsc().map { BoxResponse(it.id,it.numeroBox,it.estaHabilitado) }
}

@RestController
@RequestMapping("/productos")
class ProductoController(private val repo: ProductoRepository) {
    @GetMapping fun all() = repo.findAll().filter { it.estaActivo }.map { ProductoResponse(it.id,it.nombre,it.descripcion,it.precio,it.stock,it.imagenUrl,it.etiquetasTerapeuticas,it.estaActivo) }
}

@RestController
@RequestMapping("/promociones")
class PromocionController(private val repo: PromocionRepository) {
    @GetMapping fun all() = repo.findAll()
    @PostMapping fun create(@RequestBody p: cl.sanaesencia.domain.Promocion) = repo.save(p)
}

@RestController
@RequestMapping("/citas")
class CitaController(private val service: CitaService) {
    @PostMapping fun create(@RequestHeader("Authorization", required = false) auth: String?, @RequestBody r: CrearCitaRequest) = service.crear(auth, r)
    @GetMapping("/mis-citas") fun misCitas(@RequestHeader("Authorization", required = false) auth: String?) = service.misCitas(auth)
    @DeleteMapping("/{id}") fun cancelar(@RequestHeader("Authorization", required = false) auth: String?, @PathVariable id: Long) = service.cancelar(auth,id)
    @PutMapping("/{id}/reagendar") fun reagendar(@RequestHeader("Authorization", required = false) auth: String?, @PathVariable id: Long, @RequestBody r: ReagendarCitaRequest) = service.reagendar(auth,id,r)
    @GetMapping("/disponibilidad") fun disponibilidad(@RequestParam profesionalId: Long, @RequestParam fecha: java.time.LocalDate) = service.disponibilidad(profesionalId,fecha)
    @GetMapping("/agenda") fun agenda(@RequestHeader("Authorization", required = false) auth: String?) = service.agenda(auth)
    @GetMapping("/paciente/{pacienteId}/historial") fun historial(@RequestHeader("Authorization", required = false) auth: String?, @PathVariable pacienteId: Long) = service.historial(pacienteId,auth)
    @PutMapping("/{id}/estado") fun estado(@RequestHeader("Authorization", required = false) auth: String?, @PathVariable id: Long, @RequestBody request: EstadoCitaRequest) = service.actualizarEstado(auth,id,request)
}

@RestController
@RequestMapping("/pedidos")
class PedidoController(private val service: PedidoService) {
    @PostMapping fun crear(@RequestHeader("Authorization", required = false) auth: String?, @RequestBody request: CrearPedidoRequest) = service.crear(auth,request)
    @GetMapping("/mis-pedidos") fun historial(@RequestHeader("Authorization", required = false) auth: String?) = service.historial(auth)
}

@RestController
@RequestMapping("/admin")
class AdminController(private val service: AdminService, private val desertion: DesercionService) {
    @GetMapping("/stock-bajo") fun stockBajo(@RequestHeader("Authorization", required = false) auth: String?) = service.stockBajo(auth)
    @GetMapping("/desercion") fun desercion(@RequestHeader("Authorization", required = false) auth: String?): List<DesercionResponse> { service.profesionales(auth); return desertion.alertas() }
    @GetMapping("/profesionales") fun profesionales(@RequestHeader("Authorization", required = false) auth: String?) = service.profesionales(auth)
    @GetMapping("/boxes") fun boxes(@RequestHeader("Authorization", required = false) auth: String?) = service.boxes(auth)
    @PostMapping("/productos") fun crearProducto(@RequestHeader("Authorization", required = false) auth: String?, @RequestBody r: ProductoAdminRequest) = service.crearProducto(auth,r)
    @PutMapping("/productos/{id}") fun editarProducto(@RequestHeader("Authorization", required = false) auth: String?, @PathVariable id: Long, @RequestBody r: ProductoAdminRequest) = service.editarProducto(auth,id,r)
    @PutMapping("/boxes/{id}") fun toggleBox(@RequestHeader("Authorization", required = false) auth: String?, @PathVariable id: Long, @RequestBody r: EstadoBoxRequest) = service.toggleBox(auth,id,r)
    @PostMapping("/profesionales") fun crearProfesional(@RequestHeader("Authorization", required = false) auth: String?, @RequestBody r: ProfesionalAdminRequest) = service.crearProfesional(auth,r)
    @PutMapping("/profesionales/{id}/estado") fun estadoProfesional(@RequestHeader("Authorization", required = false) auth: String?, @PathVariable id: Long, @RequestBody r: Map<String,String>) = service.estadoProfesional(auth,id,r["estado"] ?: "DISPONIBLE")
}

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun badRequest(ex: RuntimeException) = mapOf("status" to 400, "mensaje" to (ex.message ?: "Solicitud inválida"))
}
