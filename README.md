# Sanaesencia DSY1105 — Full Stack Kotlin

Proyecto académico full-stack para el Centro Integrativo Sanaesencia.

## Componentes
- `SanaesenciaApp`: Android nativo, 100 % Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture, Room, Retrofit, DataStore.
- `SanaesenciaBackend`: Spring Boot en Kotlin, REST, Spring Data JPA, JWT, H2 para demo y MySQL para desarrollo.
- `docker-compose.yml`: MySQL.

## Cómo abrirlo en cualquier PC

### Requisitos
- **Android Studio** Ladybug (2024.2) o más nuevo, con Android SDK 35 (Android Studio lo ofrece instalar solo).
- **Git**.
- Internet la primera vez (Gradle descarga dependencias y, si hace falta, el JDK 21 del backend).
- No hace falta instalar Gradle: ambos proyectos traen `gradlew`.

### Pasos
1. Clonar:
   ```bash
   git clone <URL-del-repo>
   ```
2. En Android Studio: **File → Open** y elegir la carpeta **`SanaesenciaApp`** (no la carpeta raíz del repo).
3. Esperar el *Gradle Sync*. Si pregunta por el JDK, usar el que trae Android Studio (*Settings → Build Tools → Gradle → Gradle JDK → jbr-21*).
4. Levantar el backend (ver abajo) y ejecutar la app en un emulador.

### Si algo falla
- *SDK location not found*: abrir el proyecto desde Android Studio (crea `local.properties` solo) o instalar el SDK 35 desde *SDK Manager*.
- *Permission denied: ./gradlew* (Mac/Linux): `chmod +x gradlew`.
- Sync raro tras cambiar de PC: *File → Invalidate Caches → Invalidate and Restart*.

## Reglas implementadas
- Citas con `fecha + horaInicio + horaFin`.
- No solapamiento profesional.
- No solapamiento paciente.
- No solapamiento de box.
- Presencial requiere box habilitado; online no usa box.
- Profesional AUSENTE/VACACIONES/AGENDA_PAUSADA no puede recibir nuevas reservas.
- 8 boxes.
- Stock bajo cuando `stock <= 3`.
- Deserción como regla configurable, inicialmente 21 días sin sesión completada.
- Roles PACIENTE / PROFESIONAL / ADMIN.
- JWT preparado.
- MEDIlink desacoplado y sin credenciales inventadas.

## Cobertura de requerimientos (Entrega 2)
| Código | Dónde está |
|---|---|
| RF-01 RBAC | Login con JWT, navegación por rol y filtro de Spring Security por rol |
| RF-02 Perfil | Perfil: nombre, RUT, teléfono, fecha de nacimiento y **edad calculada** |
| RF-03 Tienda | Catálogo, carrito, pago demo y **Mis pedidos** |
| RF-04 Stock | Admin → Tienda: crear, editar, eliminar, habilitar/deshabilitar; sin stock se deshabilita solo |
| RF-05 Terapeutas | Admin → Equipo: crear (sin registro previo), editar especialidad, estado de agenda y **deshabilitar cuenta** |
| RF-06 Disponibilidad | Agendar: horas libres por profesional y **boxes libres** por bloque |
| RF-07 Reserva 15 h | Agendar (validación en app y backend) |
| RF-08 Modificar/cancelar 15 h | Mis citas: **Reagendar** y Cancelar; Admin → Citas: reagendar y cancelar |
| RF-09 Agenda terapeuta | Inicio profesional y Mis horas (futuras y pasadas) |
| RF-10 Historial | Mis horas → Ver historial del paciente (solo pacientes asignados) |
| RF-11 Boxes | Admin → Boxes: habilitar/pausar y **ocupación por día** |
| RF-12 Deserción | Admin → Inicio: alertas de 21 días sin sesión |
| RF-13 Notificaciones | Recordatorios locales + FCM (ver sección Firebase) |
| RF-14 MEDIlink | Admin → Inicio → Sincronizar (ver sección MEDIlink) |
| RF-15 GPS | Inicio paciente: distancia a la sede y "Cómo llegar" |
| RNF-06 | Spring Security con filtro JWT, sesiones stateless y reglas por rol (`/admin/**` solo ADMIN, etc.) |

## Sesión y permisos
- El backend responde **401** cuando falta el token, es inválido o expiró, y **403** cuando el rol no tiene permiso. Los errores de datos siguen siendo 400 con un `mensaje` legible.
- La app guarda la sesión: al abrirla entra directo al inicio de su rol. Si el backend responde 401, cierra la sesión y vuelve al login con el aviso "Tu sesión expiró".
- Cada pantalla verifica el rol (paciente, profesional o admin). Si alguien llega a una pantalla que no le corresponde, la app lo envía a su propio inicio.
- Al cerrar sesión se borran el token, el carrito y las citas guardadas en el teléfono, así la siguiente cuenta no ve datos de la anterior.

## Demo
- `paciente@sanaesencia.demo` / `Demo123!`
- `profesional@sanaesencia.demo` / `Demo123!`
- `admin@sanaesencia.demo` / `Demo123!`

## Backend
Necesita un JDK 17 o más nuevo para arrancar Gradle; el JDK 21 del proyecto se descarga solo si no está.
```bash
cd SanaesenciaBackend
./gradlew bootRun        # Mac / Linux
gradlew.bat bootRun      # Windows
```
También se puede abrir `SanaesenciaBackend` en IntelliJ IDEA y ejecutar `SanaesenciaBackendApplication.kt`.
Por defecto usa H2 en memoria, así que **no necesita MySQL ni Docker** para probar.
API: `http://localhost:8080/api`

MySQL (opcional):
```bash
docker compose up -d mysql
cd SanaesenciaBackend
./gradlew bootRun --args='--spring.profiles.active=mysql'
```

## Android
Abrir `SanaesenciaApp` en Android Studio.
El emulador Android usa `http://10.0.2.2:8080/api/` (el backend tiene que estar corriendo en el mismo PC).
Para un celular físico, cambiar `API_BASE_URL` en `app/build.gradle.kts` por la IP local del PC (ej. `http://192.168.1.20:8080/api/`).

## Firebase (RF-13 notificaciones)
- Sin configurar nada, la app ya muestra **recordatorios locales**: aviso 24 h antes de cada cita, alerta de inactividad (21 días sin sesión) y promociones nuevas (WorkManager, cada 6 horas).
- Para recibir además **push de Firebase Cloud Messaging**: crear el proyecto en Firebase, descargar `google-services.json` y dejarlo en `SanaesenciaApp/app/`. El plugin de Google Services se activa solo si ese archivo existe. Al iniciar sesión, el token del teléfono se guarda en el backend (`PUT /api/auth/push-token`).
- `google-services.json` no se sube a GitHub.

## MEDIlink (RF-14)
- El admin tiene el botón **Sincronizar** en su inicio (`POST /api/admin/medilink/sincronizar`).
- No se inventan endpoints ni credenciales: se configuran con las variables `MEDILINK_BASE_URL`, `MEDILINK_CITAS_PATH` y `MEDILINK_API_KEY`. Mientras falten, la sincronización responde "MEDIlink sin configurar" e indica cuántas citas quedan listas para enviar.

## Release
Configurar un keystore propio desde Android Studio. Nunca subir keystore, passwords, secretos JWT o `google-services.json` a GitHub.

## Autor
DAAR — Proyecto académico DSY1105, Centro Integrativo Sanaesencia.
