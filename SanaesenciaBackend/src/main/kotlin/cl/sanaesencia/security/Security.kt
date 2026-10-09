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
        require(!authorization.isNullOrBlank() && authorization.startsWith("Bearer ")) {
            "Sesión no autenticada"
        }
        val token = authorization.removePrefix("Bearer ").trim()
        return runCatching {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject.toLong()
        }.getOrElse { throw IllegalArgumentException("Sesión inválida o expirada") }
    }
}

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
