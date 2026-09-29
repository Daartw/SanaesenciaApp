package cl.sanaesencia.api
import cl.sanaesencia.domain.*
import cl.sanaesencia.repository.*
import cl.sanaesencia.service.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val service:AuthService){
    @PostMapping("/login") fun login(@RequestBody r:LoginRequest)=service.login(r)
}

@RestController
@RequestMapping("/profesionales")
class ProfesionalController(private val repo:ProfesionalRepository){
    @GetMapping fun all()=repo.findAll()
}

@RestController
@RequestMapping("/boxes")
class BoxController(private val repo:BoxRepository){
    @GetMapping fun all()=repo.findAllByOrderByNumeroBoxAsc()
}

@RestController
@RequestMapping("/productos")
class ProductoController(private val repo:ProductoRepository){
    @GetMapping fun all()=repo.findAll().filter{it.estaActivo}
}

@RestController
@RequestMapping("/promociones")
class PromocionController(private val repo:PromocionRepository){
    @GetMapping fun all()=repo.findAll()
    @PostMapping fun create(@RequestBody p:Promocion)=repo.save(p)
}

@RestController
@RequestMapping("/citas")
class CitaController(private val service:CitaService){
    @PostMapping fun create(@RequestBody r:CrearCitaRequest)=service.crear(r)
    @GetMapping("/paciente/{id}") fun historial(@PathVariable id:Long)=service.historial(id)
}

@RestController
@RequestMapping("/admin")
class AdminController(
    private val products:ProductoRepository,
    private val desertion:DesercionService
){
    @GetMapping("/stock-bajo") fun stockBajo()=products.findAll().filter{it.estaActivo && it.stock<=3}
    @GetMapping("/desercion") fun desercion()=desertion.alertas()
}
