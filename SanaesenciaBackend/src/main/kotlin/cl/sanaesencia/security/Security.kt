package cl.sanaesencia.security

import cl.sanaesencia.domain.RolUsuario
import cl.sanaesencia.repository.UsuarioRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
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
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret:}") secret: String,
    @Value("\${app.jwt.expiration-ms}") private val expiration: Long,
) {
    private val log = LoggerFactory.getLogger(JwtService::class.java)
    private val key: SecretKey = crearClave(secret)

    /**
     * La clave de firma nunca vive en el repositorio: llega por la variable de entorno JWT_SECRET.
     * Sin ella se genera una clave aleatoria en cada arranque (válida para desarrollo y demos:
     * las sesiones se cierran al reiniciar el backend).
     */
    private fun crearClave(secret: String): SecretKey {
        if (secret.isBlank()) {
            log.warn("JWT_SECRET no está definido: se usa una clave temporal y las sesiones se invalidan al reiniciar. Define JWT_SECRET en producción.")
            return Jwts.SIG.HS256.key().build()
        }
        val bytes = secret.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size >= 32) { "JWT_SECRET debe tener al menos 32 caracteres" }
        require(!secret.startsWith("CHANGE_THIS")) { "JWT_SECRET usa el valor de ejemplo público. Genera uno propio." }
        return Keys.hmacShaKeyFor(bytes)
    }

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

/** 429: demasiados intentos fallidos de inicio de sesión. */
class DemasiadosIntentosException(message: String) : RuntimeException(message)

/**
 * Frena los ataques de fuerza bruta contra el login: tras varios intentos fallidos seguidos
 * para la misma cuenta desde la misma IP, bloquea nuevos intentos durante unos minutos.
 * Vive en memoria, suficiente para una sola instancia del backend.
 */
@Component
class LoginAttemptLimiter(
    @Value("\${app.login.max-intentos:5}") private val maxIntentos: Int,
    @Value("\${app.login.bloqueo-minutos:15}") private val bloqueoMinutos: Long,
) {
    private data class Registro(val fallos: Int, val inicio: Long)

    private val intentos = ConcurrentHashMap<String, Registro>()
    private val ventanaMs: Long get() = bloqueoMinutos * 60_000

    fun clave(email: String, ip: String): String = email.take(200) + "|" + ip

    /** Lanza 429 si la cuenta está bloqueada temporalmente. */
    fun verificar(clave: String) {
        val registro = intentos[clave] ?: return
        if (System.currentTimeMillis() - registro.inicio >= ventanaMs) {
            intentos.remove(clave)
            return
        }
        if (registro.fallos >= maxIntentos) {
            throw DemasiadosIntentosException("Demasiados intentos fallidos. Intenta nuevamente en $bloqueoMinutos minutos.")
        }
    }

    fun registrarFallo(clave: String) {
        val ahora = System.currentTimeMillis()
        // acota la memoria si llegan intentos para muchísimas cuentas distintas
        if (intentos.size > 10_000) intentos.entries.removeIf { ahora - it.value.inicio >= ventanaMs }
        intentos.compute(clave) { _, registro ->
            if (registro == null || ahora - registro.inicio >= ventanaMs) Registro(1, ahora)
            else registro.copy(fallos = registro.fallos + 1)
        }
    }

    fun registrarExito(clave: String) {
        intentos.remove(clave)
    }
}

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
class SecurityConfig(
    // La consola H2 solo se abre si se habilita explícitamente (H2_CONSOLE_ENABLED=true) en desarrollo.
    @Value("\${spring.h2.console.enabled:false}") private val consolaH2Habilitada: Boolean,
) {
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
            .headers { headers ->
                // La consola H2 usa iframes; fuera de ese caso la API no se puede incrustar en otra página.
                headers.frameOptions { if (consolaH2Habilitada) it.sameOrigin() else it.deny() }
            }
            .authorizeHttpRequests {
                it.requestMatchers(ant(HttpMethod.POST, "/auth/login")).permitAll()
                    .requestMatchers(
                        ant(HttpMethod.GET, "/profesionales/**"),
                        ant(HttpMethod.GET, "/productos/**"),
                        ant(HttpMethod.GET, "/boxes/**"),
                        ant(HttpMethod.GET, "/promociones/**"),
                        ant(HttpMethod.GET, "/citas/disponibilidad"),
                    ).permitAll()
                    .requestMatchers(ant(null, "/error")).permitAll()
                if (consolaH2Habilitada) it.requestMatchers(ant(null, "/h2-console/**")).permitAll()
                it.requestMatchers(ant(null, "/admin/**")).hasRole("ADMIN")
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
