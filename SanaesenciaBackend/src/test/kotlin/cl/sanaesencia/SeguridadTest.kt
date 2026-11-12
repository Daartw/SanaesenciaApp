package cl.sanaesencia

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.util.Date

/** Pruebas de autenticación y control de acceso contra la aplicación completa (H2 en memoria). */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadTest {
    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper

    private fun login(email: String, password: String = "Demo123!"): ResultActions =
        mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("email" to email, "password" to password)))
        )

    private fun sesion(email: String): JsonNode =
        mapper.readTree(login(email).andExpect(status().isOk).andReturn().response.contentAsString)

    private fun bearer(token: String) = "Bearer $token"

    @Test
    fun `las rutas privadas exigen iniciar sesion`() {
        mvc.perform(get("/admin/citas")).andExpect(status().isUnauthorized)
        mvc.perform(get("/citas/mis-citas")).andExpect(status().isUnauthorized)
        mvc.perform(get("/auth/perfil")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `el catalogo publico no exige sesion`() {
        mvc.perform(get("/productos")).andExpect(status().isOk)
        mvc.perform(get("/profesionales")).andExpect(status().isOk)
    }

    @Test
    fun `solo el administrador entra al panel admin`() {
        val paciente = sesion("paciente@sanaesencia.demo")["token"].asText()
        val admin = sesion("admin@sanaesencia.demo")["token"].asText()
        mvc.perform(get("/admin/citas").header("Authorization", bearer(paciente))).andExpect(status().isForbidden)
        mvc.perform(get("/admin/citas").header("Authorization", bearer(admin))).andExpect(status().isOk)
    }

    @Test
    fun `un token firmado con el secreto antiguo del repositorio es rechazado`() {
        val adminId = sesion("admin@sanaesencia.demo")["usuarioId"].asLong()
        val secretoPublico = "CHANGE_THIS_SECRET_BEFORE_PRODUCTION_12345678901234567890"
        val falso = Jwts.builder()
            .subject(adminId.toString())
            .claim("rol", "ADMIN")
            .expiration(Date(System.currentTimeMillis() + 60_000))
            .signWith(Keys.hmacShaKeyFor(secretoPublico.toByteArray(StandardCharsets.UTF_8)))
            .compact()
        mvc.perform(get("/admin/citas").header("Authorization", bearer(falso))).andExpect(status().isUnauthorized)
    }

    @Test
    fun `la consola H2 esta cerrada por defecto`() {
        mvc.perform(get("/h2-console")).andExpect(status().isUnauthorized)
        mvc.perform(get("/h2-console/login.do")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `el login se bloquea tras varios intentos fallidos`() {
        val email = "profesional@sanaesencia.demo"
        repeat(5) { login(email, "incorrecta").andExpect(status().isBadRequest) }
        // bloqueado: ni siquiera la contraseña correcta entra hasta que pase la ventana
        login(email, "incorrecta").andExpect(status().isTooManyRequests)
        login(email).andExpect(status().isTooManyRequests)
    }

    @Test
    fun `el login no revela si el correo existe ni devuelve el hash`() {
        val inexistente = login("nadie@sanaesencia.demo", "incorrecta").andExpect(status().isBadRequest)
            .andReturn().response.contentAsString
        val existente = login("paciente@sanaesencia.demo", "incorrecta").andExpect(status().isBadRequest)
            .andReturn().response.contentAsString
        assertEquals(inexistente, existente)
        assertFalse(sesion("paciente@sanaesencia.demo").toString().contains("passwordHash"))
    }

    @Test
    fun `crear un profesional exige una contrasena inicial`() {
        val admin = sesion("admin@sanaesencia.demo")["token"].asText()
        val cuerpo = mapOf(
            "nombreCompleto" to "Nueva Profesional",
            "email" to "nueva@sanaesencia.demo",
            "titulo" to "Nutricionista",
            "categoriaEspecialidad" to "Nutrición",
        )
        mvc.perform(
            post("/admin/profesionales").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(cuerpo))
        ).andExpect(status().isBadRequest)
        mvc.perform(
            post("/admin/profesionales").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo + ("password" to "Clave-Inicial-2026")))
        ).andExpect(status().isOk)
        // la contraseña pública de las cuentas demo ya no sirve como valor por defecto
        login("nueva@sanaesencia.demo").andExpect(status().isBadRequest)
        login("nueva@sanaesencia.demo", "Clave-Inicial-2026").andExpect(status().isOk)
    }
}
