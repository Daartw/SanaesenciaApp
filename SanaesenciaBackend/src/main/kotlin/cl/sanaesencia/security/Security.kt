package cl.sanaesencia.security

import cl.sanaesencia.domain.RolUsuario
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") secret: String,
    @Value("\${app.jwt.expiration-ms}") private val expiration: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun generate(userId: Long, email: String, rol: RolUsuario): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("rol", rol.name)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact()

    fun userIdFromBearer(authorization: String?): Long {
        if (authorization.isNullOrBlank() || !authorization.startsWith("Bearer ")) {
            throw NoAutenticadoException("Debes iniciar sesión para continuar")
        }
        val token = authorization.removePrefix("Bearer ").trim()
        return runCatching {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject.toLong()
        }.getOrElse { throw NoAutenticadoException("Tu sesión expiró. Inicia sesión nuevamente") }
    }
}

/** 401: no hay token, es inválido o expiró. La app debe volver al login. */
class NoAutenticadoException(message: String) : RuntimeException(message)

/** 403: hay sesión, pero el rol no tiene permiso para esta acción. */
class SinPermisoException(message: String = "No tienes permisos para realizar esta acción") : RuntimeException(message)

@Configuration
class SecurityConfig {
    @Bean fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): org.springframework.security.web.SecurityFilterChain {
        http.csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
        return http.build()
    }
}
