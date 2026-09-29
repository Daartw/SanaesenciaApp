package cl.sanaesencia.service
import cl.sanaesencia.domain.*
import cl.sanaesencia.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class SeedData {
    @Bean fun seed(
        users:UsuarioRepository, professionals:ProfesionalRepository,
        boxes:BoxRepository, products:ProductoRepository, encoder:PasswordEncoder
    )=CommandLineRunner {
        if(users.count()==0L){
            users.save(Usuario(rut="11.111.111-1",nombreCompleto="Paciente Demo",email="paciente@sanaesencia.demo",telefono="+56900000001",rol=RolUsuario.PACIENTE,passwordHash=encoder.encode("Demo123!")))
            users.save(Usuario(rut="22.222.222-2",nombreCompleto="Profesional Demo",email="profesional@sanaesencia.demo",telefono="+56900000002",rol=RolUsuario.PROFESIONAL,passwordHash=encoder.encode("Demo123!")))
            users.save(Usuario(rut="33.333.333-3",nombreCompleto="Administrador Demo",email="admin@sanaesencia.demo",telefono="+56900000003",rol=RolUsuario.ADMIN,passwordHash=encoder.encode("Demo123!")))
            professionals.save(Profesional(usuarioId=2,titulo="Psicóloga",categoriaEspecialidad="Psicología",descripcionEnfoque="Atención psicológica demo.",certificaciones="Demo",anosExperiencia=5))
            (1..8).forEach{boxes.save(Box(numeroBox=it))}
            products.save(Producto(nombre="Roll-on Relajación",descripcion="Producto demo.",precio=7990.0,stock=2,etiquetasTerapeuticas="relajación,descanso"))
            products.save(Producto(nombre="Bruma Sueño",descripcion="Producto demo.",precio=9990.0,stock=8,etiquetasTerapeuticas="sueño,relajación"))
        }
    }
}
