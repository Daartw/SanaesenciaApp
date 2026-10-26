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

## Firebase
No se incluyen credenciales. Agregar `google-services.json` y dependencias Firebase cuando se conecte el proyecto real.

## MEDIlink
No se inventan endpoints, API keys ni credenciales. Se dejó un adaptador vacío para una futura integración oficial.

## Release
Configurar un keystore propio desde Android Studio. Nunca subir keystore, passwords, secretos JWT o `google-services.json` a GitHub.
