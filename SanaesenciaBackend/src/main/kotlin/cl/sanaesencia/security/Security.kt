package cl.sanaesencia.security

import cl.sanaesencia.domain.RolUsuario
import cl.sanaesencia.repository.UsuarioRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Service
import org.springframework.web.filter.OncePerRequestFilter
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

    /** Devuelve el id del usuario si el token es válido y no expiró; null en cualquier otro caso. */
    fun userIdFromToken(token: String): Long? = runCatching {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject.toLong()
    }.getOrNull()

    fun userIdFromBearer(authorization: String?): Long {
        if (authorization.isNullOrBlank() || !authorization.startsWith("Bearer ")) {
            throw NoAutenticadoException("Debes iniciar sesión para continuar")
        }
        val token = authorization.removePrefix("Bearer ").trim()
        return userIdFromToken(token) ?: throw NoAutenticadoException("Tu sesión expiró. Inicia sesión nuevamente")
    }
}

/** 401: no hay token, es inválido o expiró. La app debe volver al login. */
class NoAutenticadoException(message: String) : RuntimeException(message)

/** 403: hay sesión, pero el rol no tiene permiso para esta acción. */
class SinPermisoException(message: String = "No tienes permisos para realizar esta acción") : RuntimeException(message)

/**
 * RNF-06: filtro JWT de Spring Security.
 * Lee "Authorization: Bearer <token>", valida la firma y la expiración, comprueba que la cuenta
 * siga activa y registra al usuario con su rol (ROLE_PACIENTE, ROLE_PROFESIONAL o ROLE_ADMIN).
 */
class JwtAuthFilter(
    private val jwt: JwtService,
    private val users: UsuarioRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().authentication == null) {
            val userId = jwt.userIdFromToken(header.removePrefix("Bearer ").trim())
            val usuario = userId?.let { users.findById(it).orElse(null) }
            if (usuario != null && usuario.estaActivo) {
                val auth = UsernamePasswordAuthenticationToken(
                    usuario.id,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${usuario.rol.name}")),
                )
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        chain.doFilter(request, response)
    }
}

@Configuration
class SecurityConfig {
    @Bean fun passwordEncoder() = BCryptPasswordEncoder()

    /**
     * RNF-06: control de acceso por rol (RBAC).
     * Las rutas públicas son solo de lectura del catálogo; todo lo demás exige un JWT válido
     * y las rutas de cada perfil exigen su rol. Los servicios repiten la validación por seguridad.
     */
    @Bean
    fun filterChain(http: HttpSecurity, jwt: JwtService, users: UsuarioRepository): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { headers -> headers.frameOptions { it.sameOrigin() } }
            .authorizeHttpRequests {
                it.requestMatchers(ant(HttpMethod.POST, "/auth/login")).permitAll()
                    .requestMatchers(
                        ant(HttpMethod.GET, "/profesionales/**"),
                        ant(HttpMethod.GET, "/productos/**"),
                        ant(HttpMethod.GET, "/boxes/**"),
                        ant(HttpMethod.GET, "/promociones/**"),
                        ant(HttpMethod.GET, "/citas/disponibilidad"),
                    ).permitAll()
                    .requestMatchers(ant(null, "/error"), ant(null, "/h2-console/**")).permitAll()
                    .requestMatchers(ant(null, "/admin/**")).hasRole("ADMIN")
                    .requestMatchers(ant(HttpMethod.POST, "/promociones/**")).hasRole("ADMIN")
                    .requestMatchers(ant(HttpMethod.POST, "/pedidos/**")).hasRole("PACIENTE")
                    .requestMatchers(ant(HttpMethod.POST, "/citas")).hasRole("PACIENTE")
                    .requestMatchers(ant(null, "/citas/agenda")).hasRole("PROFESIONAL")
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    escribirError(response, 401, "Debes iniciar sesión para continuar")
                }
                it.accessDeniedHandler { _, response, _ ->
                    escribirError(response, 403, "No tienes permisos para realizar esta acción")
                }
            }
            .addFilterBefore(JwtAuthFilter(jwt, users), UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    // AntPathRequestMatcher evita el conflicto entre DispatcherServlet y la consola H2.
    // Sin método = cualquier verbo HTTP (antMatcher(method, ...) no acepta null).
    private fun ant(method: HttpMethod?, pattern: String): AntPathRequestMatcher =
        if (method == null) AntPathRequestMatcher.antMatcher(pattern) else AntPathRequestMatcher.antMatcher(method, pattern)

    private fun escribirError(response: HttpServletResponse, status: Int, mensaje: String) {
        response.status = status
        response.contentType = "application/json;charset=UTF-8"
        response.writer.write("""{"status":$status,"mensaje":"$mensaje"}""")
    }
}
