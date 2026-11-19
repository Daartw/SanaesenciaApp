# Arquitectura y flujo — Sanaesencia

Material de apoyo para el IE 3.1.2 de la defensa. Todo lo que está aquí sale del código;
cada afirmación se puede comprobar abriendo el archivo que se indica.

## Visión general

Dos piezas que se hablan por HTTP y nada más:

- **App Android** (`SanaesenciaApp/`) — Kotlin, Jetpack Compose, arquitectura MVVM por capas.
- **Backend REST** (`SanaesenciaBackend/`) — Kotlin, Spring Boot, JPA sobre H2 o MySQL.

No comparten base de datos ni código. La app no sabe qué motor de base de datos hay detrás;
el backend no sabe que existe Compose. El único contrato entre ambos son los endpoints REST
y el formato JSON de los DTOs.

### Capas de la app

| Capa | Carpeta | Responsabilidad |
|---|---|---|
| Interfaz | `ui/` | Pantallas Compose. No contienen reglas de negocio. |
| Presentación | `presentation/` | ViewModels: estado de pantalla y traducción de errores. |
| Dominio | `domain/` | Reglas de negocio, validadores y casos de uso. Sin Android. |
| Datos | `data/` | Retrofit (red), Room (caché local) y mapeadores entre ambos. |

La capa de dominio no importa nada de Android: por eso se puede probar con JUnit normal en la
JVM, que es lo que permite la cobertura del 86,62 %.

### Capas del backend

| Capa | Archivo | Responsabilidad |
|---|---|---|
| API | `api/Controllers.kt` | Recibe HTTP, delega y devuelve DTOs. |
| DTOs | `api/Dto.kt` | Contrato de entrada y salida. |
| Servicios | `service/Services.kt` | Reglas de negocio y transacciones. |
| Dominio | `domain/Models.kt` | 8 entidades JPA y 4 enums de estado. |
| Persistencia | `repository/Repositories.kt` | 8 repositorios de Spring Data. |
| Seguridad | `security/Security.kt` | JWT, BCrypt y reglas de acceso por rol. |

---

## Los dominios del backend

Cada dominio tiene su controlador, su servicio cuando hay lógica, y sus repositorios. Ninguno
entra a las tablas de otro: si `PedidoService` necesita un usuario, pasa por `UsuarioRepository`.

### `/auth` — Identidad
**Para qué sirve:** es la puerta de entrada. Sin pasar por aquí no se obtiene token y ningún
otro endpoint responde.

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/auth/login` | Valida credenciales y devuelve el JWT con el rol |
| GET | `/auth/perfil` | Datos del usuario autenticado |
| PUT | `/auth/perfil` | Actualiza RUT, nombre, teléfono, nacimiento y consentimiento |
| PUT | `/auth/push-token` | Registra el token de Firebase del dispositivo (RF-13) |

`AuthService` guarda las contraseñas con BCrypt y bloquea la cuenta tras 5 intentos fallidos
durante 15 minutos, contando por cuenta y por IP.

### `/citas` — Agendamiento
**Para qué sirve:** es el corazón del sistema y donde está casi toda la lógica de negocio.

| Método | Ruta | Rol |
|---|---|---|
| POST | `/citas` | PACIENTE |
| GET | `/citas/mis-citas` | PACIENTE |
| GET | `/citas/disponibilidad` | autenticado |
| PUT | `/citas/{id}/reagendar` | PACIENTE |
| DELETE | `/citas/{id}` | PACIENTE o ADMIN |
| GET | `/citas/agenda` | PROFESIONAL |
| GET | `/citas/paciente/{id}/historial` | PROFESIONAL |
| PUT | `/citas/{id}/estado` | PROFESIONAL |

### `/pedidos` — Tienda
**Para qué sirve:** convierte el carrito en un pedido y descuenta stock. `PedidoService`
valida stock disponible y guarda `Pedido` con sus `PedidoItem` dentro de una transacción: o se
guarda todo, o no se guarda nada.

| Método | Ruta | Rol |
|---|---|---|
| POST | `/pedidos` | PACIENTE |
| GET | `/pedidos/mis-pedidos` | PACIENTE |

### `/productos`, `/profesionales`, `/boxes`, `/promociones` — Catálogos
**Para qué sirven:** son consultas de lectura que alimentan la app. Filtran lo que no
corresponde mostrar: `/productos` solo devuelve los activos y `/profesionales` solo los de
cuenta activa. El filtro vive en el servidor, no en la app.

### `/admin` — Administración
**Para qué sirve:** todo lo que solo puede hacer el centro. Es el dominio más grande: CRUD de
productos, profesionales y boxes, ocupación de boxes, alertas de stock bajo, detección de
deserción (RF-12) y la sincronización con MEDIlink (RF-14).

Tres servicios lo respaldan: `AdminService`, `DesercionService` y `MedilinkService`.

### Piezas transversales

`JwtAuthFilter` (en `Security.kt`) intercepta **todas** las peticiones antes de que lleguen a
cualquier controlador: extrae el token de la cabecera `Authorization`, lo valida y deja al
usuario en el contexto de seguridad.

`ApiExceptionHandler` (al final de `Controllers.kt`) traduce las excepciones a códigos HTTP:
400 para datos inválidos, 401 sin sesión, 403 sin permisos, 409 por dato duplicado y 429 por
demasiados intentos. Así la app nunca recibe un stacktrace.

---

## Ejemplo real: agendar una cita

Este es el recorrido completo, con los nombres reales de cada paso.

### Ida

**1. Pantalla.** El paciente elige profesional, fecha y hora en `CitasScreen` y pulsa reservar.

**2. ViewModel.** `CitasViewModel.crear()` pasa la solicitud al caso de uso.

**3. Dominio.** `AgendarCitaUseCase` valida la regla de las 15 horas con `ValidadorCita`
(`Validadores.kt:76`). Si no se cumple, **corta aquí**: nunca se gasta una llamada de red.

**4. Datos.** `CitaRepository.crear()` llama a `SanaesenciaApi.crearCita()`, declarada como
`@POST("citas")`.

**5. Red.** El interceptor de `RetrofitClient.kt` añade la cabecera
`Authorization: Bearer <token>` con el JWT guardado en DataStore. Sale un
`POST http://<host>:8080/api/citas` con el cuerpo en JSON.

### En el servidor

**6. Filtro.** `JwtAuthFilter` valida el token. Si está vencido o falta, responde 401 y no
llega a ningún controlador.

**7. Seguridad.** `SecurityConfig` comprueba que `POST /citas` lo pida alguien con rol
PACIENTE. Un profesional con token válido recibe 403.

**8. Controlador.** `CitaController.create()` recibe el `CrearCitaRequest` y delega.

**9. Servicio.** `CitaService.crear()` aplica las reglas, en este orden:

- el usuario del token existe y es PACIENTE;
- la hora de término es posterior a la de inicio;
- **las 15 horas de anticipación, otra vez** (`Services.kt:133`);
- el horario cae entre 09:00 y 17:00;
- el profesional está `DISPONIBLE` y su cuenta activa;
- si es presencial, el box está habilitado y es del 1 al 8; si es online, no lleva box;
- el profesional no tiene otra cita solapada;
- el paciente no tiene otra cita solapada;
- el box no está ocupado en ese bloque.

**10. Persistencia.** `citas.save(...)` dentro de un método `@Transactional`. Hibernate emite
el `INSERT` sobre la tabla `citas`.

### Vuelta

**11.** El servicio devuelve un `CitaResponse`, que Spring serializa a JSON.

**12.** Moshi lo convierte de vuelta a objeto en la app.

**13.** `CitaRepository` guarda una copia en Room (`db.citas().upsertAll`), para que la cita
siga visible sin conexión.

**14.** El ViewModel emite `UiState.Exito` y la pantalla se actualiza.

### Cuando algo falla

Si el servidor responde 400 porque el box está ocupado, `ApiErrors.mensajeUsuario()` convierte
la excepción de Retrofit en el mensaje del servidor. Si no hay red, devuelve "Problema de
conexión. Verifica tu internet e inténtalo nuevamente." La pantalla muestra `UiState.Error` y
la app no se cierra.

---

## Por qué la regla de las 15 horas está dos veces

No es duplicación por descuido, y conviene decirlo antes de que lo pregunten.

La validación de la app es por **experiencia de usuario**: avisa al instante, sin esperar al
servidor ni gastar datos. La del backend es por **seguridad**: el cliente se puede modificar,
descompilar o reemplazar, así que la regla que realmente manda es la del servidor.

Es exactamente lo que se demuestra en el acto 1 del IE 3.1.3: si se cambia solo en la app, el
backend sigue rechazando la cita.

---

## Cómo mostrarlo en vivo

Levanta el backend con el registro de peticiones y de SQL encendido:

```
gradlew bootRun --args="--logging.level.web=DEBUG --logging.level.org.hibernate.SQL=DEBUG"
```

Spring Boot no registra las peticiones por defecto: sin esto la consola queda muda aunque todo
funcione. Con esto, al reservar una cita desde el teléfono aparecen en pantalla el
`POST /api/citas` y el `insert into citas` correspondiente.

Es la forma más directa de demostrar la interacción entre la interfaz y los microservicios que
pide el IE 3.1.1, sin tener que explicarla con palabras.

---

## La pregunta sobre microservicios

La arquitectura es un **backend REST modular**, con los dominios separados en controladores,
servicios y repositorios propios. Es lo que especifica el RNF-05 del documento de
requerimientos: *"Backend REST API en Spring Boot enlazado con MySQL"*.

Para que fueran microservicios en sentido estricto faltaría desplegar cada dominio como un
proceso independiente, con su propia base de datos y comunicación por HTTP o mensajería entre
ellos. La separación lógica ya está hecha; la que falta es la física.

Responder esto de frente, mostrando `Controllers.kt` y la separación real, vale más que
sostener que es algo que no es.
