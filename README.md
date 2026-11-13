# Sanaesencia DSY1105 — Full Stack Kotlin

[![CI](https://github.com/Daartw/SanaesenciaApp/actions/workflows/ci.yml/badge.svg)](https://github.com/Daartw/SanaesenciaApp/actions/workflows/ci.yml)

Proyecto académico full-stack para el Centro Integrativo Sanaesencia: app Android nativa para pacientes,
profesionales y administración (agenda de citas, tienda, boxes y seguimiento), con backend propio.

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
- Autenticación con JWT y control de acceso por rol.
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
| API externa | Inicio paciente: clima de la sede desde Open-Meteo (ver sección API externa) |
| Pruebas | Pruebas unitarias con informe de cobertura JaCoCo (ver sección Pruebas) |
| APK firmado | `signingConfigs` + `keystore.properties` (ver sección Release) |

## Sesión y permisos
- El backend responde **401** cuando falta el token, es inválido o expiró, y **403** cuando el rol no tiene permiso. Los errores de datos siguen siendo 400 con un `mensaje` legible.
- La app guarda la sesión: al abrirla entra directo al inicio de su rol. Si el backend responde 401, cierra la sesión y vuelve al login con el aviso "Tu sesión expiró".
- Cada pantalla verifica el rol (paciente, profesional o admin). Si alguien llega a una pantalla que no le corresponde, la app lo envía a su propio inicio.
- Al cerrar sesión se borran el token, el carrito y las citas guardadas en el teléfono, así la siguiente cuenta no ve datos de la anterior.

## Demo
Cuentas de demostración que el backend crea al arrancar con la base vacía:
- `paciente@sanaesencia.demo` / `Demo123!`
- `profesional@sanaesencia.demo` / `Demo123!`
- `admin@sanaesencia.demo` / `Demo123!`

Estas contraseñas son públicas: sirven solo para desarrollo. Con `APP_DEMO_SEED=false` no se crean.
Los botones de acceso rápido del login aparecen únicamente en la compilación de depuración.

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

Pruebas (incluyen autenticación y control de acceso):
```bash
cd SanaesenciaBackend
./gradlew test
```

### Variables de entorno del backend
| Variable | Descripción |
|---|---|
| `JWT_SECRET` | Clave de firma de los tokens (32+ caracteres). Si falta, se genera una temporal en cada arranque: sirve para desarrollo, pero las sesiones se cierran al reiniciar |
| `JWT_EXPIRATION_MS` | Duración de la sesión (por defecto 8 horas) |
| `APP_DEMO_SEED` | `false` para no crear las cuentas de demostración |
| `H2_CONSOLE_ENABLED` | `true` abre la consola H2 en `/api/h2-console` (solo desarrollo) |
| `SERVER_ADDRESS` | Interfaz de red del backend (por defecto `0.0.0.0`) |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión del perfil `mysql` |
| `MEDILINK_BASE_URL`, `MEDILINK_CITAS_PATH`, `MEDILINK_API_KEY` | Integración MEDIlink |

Ver `.env.example`.

## Android
Abrir `SanaesenciaApp` en Android Studio.
El emulador Android usa `http://10.0.2.2:8080/api/` (el backend tiene que estar corriendo en el mismo PC).
Para un celular físico, indicar la IP local del PC al compilar, sin editar archivos:
```bash
./gradlew installDebug -PAPI_BASE_URL=http://192.168.1.20:8080/api/
```

## Firebase (RF-13 notificaciones)
- Sin configurar nada, la app ya muestra **recordatorios locales**: aviso 24 h antes de cada cita, alerta de inactividad (21 días sin sesión) y promociones nuevas (WorkManager, cada 6 horas).
- Para recibir además **push de Firebase Cloud Messaging**: crear el proyecto en Firebase, descargar `google-services.json` y dejarlo en `SanaesenciaApp/app/`. El plugin de Google Services se activa solo si ese archivo existe. Al iniciar sesión, el token del teléfono se guarda en el backend (`PUT /api/auth/push-token`).
- `google-services.json` no se sube a GitHub.

## MEDIlink (RF-14)
- El admin tiene el botón **Sincronizar** en su inicio (`POST /api/admin/medilink/sincronizar`).
- No se inventan endpoints ni credenciales: se configuran con las variables `MEDILINK_BASE_URL`, `MEDILINK_CITAS_PATH` y `MEDILINK_API_KEY`. Mientras falten, la sincronización responde "MEDIlink sin configurar" e indica cuántas citas quedan listas para enviar.

## Seguridad
- El secreto JWT no está en el repositorio: se entrega con `JWT_SECRET`.
- El login se bloquea 15 minutos tras 5 intentos fallidos para la misma cuenta desde la misma IP.
- Al crear un profesional, el administrador define su contraseña inicial (mínimo 8 caracteres); no hay contraseña por defecto.
- La consola H2 está apagada por defecto y MySQL de desarrollo solo escucha en `127.0.0.1`.
- La versión de publicación de la app no admite HTTP sin cifrar ni respaldo de datos.

Más detalle, y la lista de pendientes antes de un despliegue real, en [SECURITY.md](SECURITY.md).

## API externa: clima de la sede (Open-Meteo)
La app consume una **API externa de terceros** desde el propio cliente Android, además de
sus microservicios: el pronóstico de [Open-Meteo](https://open-meteo.com) para la sede de
Los Vilos. Aparece en el inicio del paciente, bajo la tarjeta de la sede, porque responde a
la misma decisión: cómo y cuándo llegar a una cita presencial.

- Servicio público, gratuito y **sin API key**, así que funciona sin configurar nada.
- Cliente propio (`data/remote/externo/ClimaRetrofit.kt`) con su **propia URL base**, separado
  del cliente del backend: no lleva el interceptor de JWT, de modo que el token de la persona
  nunca viaja a un servidor de terceros, y un 401 de Open-Meteo no puede cerrar la sesión.
- Tiempos de espera más cortos (8 s) y degradación elegante: si el servicio falla, la tarjeta
  muestra un aviso con botón de reintento y el resto del inicio sigue funcionando igual.

| Pieza | Archivo |
|---|---|
| Interfaz y DTOs | `data/remote/externo/ClimaApi.kt` |
| Cliente HTTP independiente | `data/remote/externo/ClimaRetrofit.kt` |
| Conversión a dominio | `data/repository/ClimaRepository.kt` |
| Modelo y códigos WMO | `domain/model/Clima.kt` |
| Estado de pantalla | `presentation/ClimaViewModel.kt` |
| Tarjeta en la interfaz | `ui/clima/ClimaSedeCard.kt` |

## Pruebas unitarias y cobertura
```bash
cd SanaesenciaApp
./gradlew testDebugUnitTest     # solo ejecutar las pruebas
./gradlew jacocoTestReport      # pruebas + informe de cobertura
```
El informe queda en `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

La cobertura se mide sobre el **código lógico**: dominio (validadores, casos de uso, modelos),
capa de presentación (ViewModels) y mapeadores. Quedan excluidas las clases sin lógica propia o
que no son ejecutables en la JVM: pantallas Compose y tema, servicios de Android (FCM, GPS,
WorkManager), DTOs de red, repositorios que envuelven Room y Retrofit, y el código generado por
Room, KSP y Compose. La lista exacta está en `app/build.gradle.kts` (`exclusionesDeCobertura`).

## Release: generar el APK firmado
El proyecto trae la configuración de firma lista; solo falta el keystore, que **nunca** se sube
a GitHub (`*.jks` y `keystore.properties` están en `.gitignore`).

**1. Crear el keystore** (una sola vez). Desde la carpeta `SanaesenciaApp`:
```bash
keytool -genkeypair -v \
  -keystore sanaesencia-release.jks \
  -alias sanaesencia \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Sanaesencia, OU=DSY1105, O=Centro Integrativo Sanaesencia, L=Los Vilos, C=CL"
```
También se puede desde Android Studio: *Build → Generate Signed App Bundle / APK → APK → Create new…*

**2. Declarar las credenciales.** Copiar `keystore.properties.example` como `keystore.properties`
(en la carpeta `SanaesenciaApp`) y completar:
```properties
storeFile=sanaesencia-release.jks
storePassword=...
keyAlias=sanaesencia
keyPassword=...
```
Alternativa sin archivo: las variables de entorno `SANAESENCIA_STORE_FILE`,
`SANAESENCIA_STORE_PASSWORD`, `SANAESENCIA_KEY_ALIAS` y `SANAESENCIA_KEY_PASSWORD`.

**3. Generar el APK:**
```bash
./gradlew assembleRelease
```
Queda en `app/build/outputs/apk/release/app-release.apk`.

**4. Verificar la firma:**
```bash
"$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --verbose app/build/outputs/apk/release/app-release.apk
```

Detalles de la configuración (`app/build.gradle.kts`):
- `signingConfigs.release` lee `keystore.properties` y, si no existe, las variables de entorno.
- Firma con los esquemas **v1 y v2**.
- `isMinifyEnabled = false`: Moshi y Room resuelven clases por reflexión y R8 las eliminaría.
  Las reglas quedan declaradas en `app/proguard-rules.pro` por si se activa la ofuscación.
- Si no hay keystore configurado, el proyecto **compila igual** y la variante release queda sin
  firmar, en vez de fallar la compilación.

## Autor
Daniel Araya Rebolledo ([@Daartw](https://github.com/Daartw)) — Proyecto académico DSY1105, Centro Integrativo Sanaesencia.
