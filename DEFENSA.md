# Guía de defensa — Centro Integrativo Sanaesencia

Preguntas probables de cada indicador, con la respuesta anclada a archivos concretos del
repositorio. La idea no es memorizar: es saber **qué archivo abrir** cuando pregunten.

Ponderaciones de la pauta de defensa:

| Indicador | Peso | Qué piden |
|---|---|---|
| IE 3.1.1 | 12 % | Demostrar la app funcionando con los microservicios |
| IE 3.1.2 | 12 % | Explicar entidades, endpoints y persistencia |
| **IE 3.1.3** | **40 %** | **Modificar una funcionalidad en vivo** |
| IE 3.2.1 | 12 % | Ejecutar una prueba unitaria y explicarla |
| IE 3.2.2 | 8 % | Explicar para qué sirven las pruebas |
| IE 3.3.1 | 8 % | Explicar la generación del APK firmado |
| IE 3.3.2 | 8 % | Exponer tu participación técnica |

El IE 3.1.3 vale más que los otros seis juntos menos uno. Ensáyalo hasta que salga sin pensar.

---

## IE 3.1.1 — Funcionamiento completo (12 %)

**Qué mostrar, en este orden:** login como paciente → agendar una cita → tienda y carrito →
pedido → perfil → clima de la sede. Después cierra sesión y entra como profesional para ver
la misma cita en su agenda. Ese último paso es el que prueba que la app y el backend están
realmente conectados y no son dos demos separadas.

**¿Cómo sabe la app qué rol tiene el usuario?**
El backend devuelve un JWT en `POST /auth/login` que incluye el rol. La app lo guarda con
`SessionManager` (DataStore) y `Navigation.kt` arma un grafo distinto según sea `PACIENTE`,
`PROFESIONAL` o `ADMIN`. No es que se oculten botones: son pantallas distintas.

**¿Y si alguien llama al endpoint directamente, saltándose la app?**
No sirve. `security/Security.kt` valida el rol en el servidor: `/admin/**` exige ADMIN,
`POST /citas` exige PACIENTE, `/citas/agenda` exige PROFESIONAL. La app esconde opciones por
comodidad; el backend es el que decide.

**¿Qué pasa si se cae el backend?**
La app sigue abriendo y muestra el catálogo guardado en Room (RNF-04). Las operaciones que
necesitan servidor fallan con un mensaje legible, no con un error técnico: eso lo traduce
`data/remote/ApiErrors.kt`, que convierte un 401, un timeout o un `UnknownHostException` en
frases como "Problema de conexión. Verifica tu internet e inténtalo nuevamente."

**Ten listo:** la app instalada y con sesión probada, el backend corriendo, y las credenciales
de demostración a mano (`SeedData.kt` las crea al arrancar).

---

## IE 3.1.2 — Estructura, lógica y persistencia (12 %)

Abre estos cuatro archivos, en este orden. Son la respuesta completa:

| Archivo | Qué muestra |
|---|---|
| `domain/Models.kt` | Las 8 entidades JPA |
| `repository/Repositories.kt` | Los 8 repositorios de Spring Data |
| `api/Controllers.kt` | Los controladores, separados por dominio |
| `security/Security.kt` | JWT, BCrypt y las reglas por rol |

**Las entidades:** `Usuario`, `Profesional`, `Box`, `Cita`, `Producto`, `Promocion`, `Pedido`
y `PedidoItem`. Más cuatro enums que modelan los estados del negocio: `RolUsuario`,
`EstadoProfesional`, `ModalidadCita` y `EstadoCita`.

Dos detalles que conviene señalar sin que los pregunten, porque demuestran criterio:
`Usuario` tiene restricciones de unicidad en `rut` y `email`, y `Box` en `numeroBox`. Son
reglas del negocio sostenidas por la base de datos, no solo por el código.

**Los endpoints** están agrupados por dominio, un controlador por área: `/auth`,
`/profesionales`, `/boxes`, `/productos`, `/promociones`, `/citas`, `/pedidos` y `/admin`.
Todos cuelgan de `/api`, que es el `context-path` definido en `application.yml`.

**La persistencia:** Spring Data JPA sobre Hibernate. En desarrollo corre sobre H2 en memoria
con `ddl-auto: create-drop`, así que el esquema se crea en cada arranque y `SeedData.kt`
lo puebla. Para persistencia real existe el perfil `mysql` (`application-mysql.yml`) con
`ddl-auto: update`, apuntando al MySQL del `docker-compose.yml`.

### La pregunta incómoda: "¿dónde están los microservicios?"

**Respóndela de frente, no la esquives.** La arquitectura es un backend REST en Spring Boot
con los controladores separados por dominio, que es exactamente lo que especifica el RNF-05
del documento de requerimientos: *"Backend REST API en Spring Boot enlazado con MySQL"*.

Muestra `Controllers.kt` y recorre la separación: cada dominio tiene su controlador, su
servicio y sus repositorios, sin que uno acceda a las tablas de otro. Si te preguntan qué
faltaría para que fueran microservicios de verdad, la respuesta honesta es: desplegar cada
dominio como un proceso independiente, con su propia base de datos y comunicación por HTTP o
mensajería. La separación lógica ya está; falta la separación física.

Esa respuesta vale más que inventar que es algo que no es.

---

## IE 3.1.3 — Modificación en vivo (40 %)

Piden modificar una funcionalidad de la app que afecte también a los microservicios o a la
base de datos. **La regla de las 15 horas (RF-07) es la mejor candidata**, porque está
validada en las dos capas y el cambio se ve de inmediato.

Hazlo en tres actos. El segundo es el que impresiona.

### Acto 1 — Cambiar solo la app

`SanaesenciaApp/app/src/main/java/cl/sanaesencia/app/domain/validacion/Validadores.kt`,
línea 76:

```kotlin
val minimo = LocalDateTime.now(clock).plusHours(15)   // cámbialo a plusHours(2)
```

Recompila e intenta agendar una cita para dentro de 3 horas. **La app ahora la deja pasar,
pero el backend la rechaza** con "Debes agendar con al menos 15 horas de anticipación".

Esto es lo que hay que decir en voz alta: *"la validación del cliente es por comodidad del
usuario; la que manda es la del servidor, porque el cliente se puede modificar."* Acabas de
demostrar en vivo la interacción app–microservicio, que es justamente lo que pide el IE 3.1.1.

### Acto 2 — Cambiar el backend

`SanaesenciaBackend/src/main/kotlin/cl/sanaesencia/service/Services.kt`, línea 133:

```kotlin
require(!LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now(CHILE_CLOCK).plusHours(15)))
```

Cámbialo también a `plusHours(2)`. Hay una segunda aparición en la línea 210, en el cálculo
de disponibilidad: cámbiala igual o los horarios seguirán mostrándose bloqueados.

Reinicia el backend, agenda de nuevo y ahora sí se crea. Muestra la cita guardada: en la
agenda del profesional, o directamente en la base de datos.

### Acto 3 — Actualizar la prueba

El cambio rompe las pruebas que afirman la regla de 15 horas. Ejecuta:

```
gradlew testDebugUnitTest
```

Fallan `ValidadoresCompletosTest` y `AgendarCitaUseCaseTest`. **Eso es bueno y hay que decirlo:**
la red de pruebas detectó que cambiaste una regla de negocio. Ajusta las pruebas al nuevo
valor, vuelve a correrlas y quedan en verde.

Si te alcanza el tiempo, deja el valor de vuelta en 15 y vuelve a correr las pruebas.

### Plan B, si algo se cae

Dos cambios más pequeños que también cruzan capas:

- **Umbral de deserción (RF-12):** `DetectarDesercionUseCase(umbralDias = 21)` en la app y
  `app.desercion.dias-inactividad` en `application.yml`. Se ve en el panel de administración.
- **Stock bajo (RF-04):** `VerificarStockBajoUseCase(umbral = 3)`. Cambia qué productos
  aparecen marcados en la tienda.

**Ensaya el acto completo al menos dos veces antes de la defensa**, cronometrando. Lo que
suele fallar no es el código: es no encontrar el archivo rápido. Deja los dos archivos
abiertos en pestañas antes de empezar.

---

## IE 3.2.1 — Ejecutar una prueba unitaria (12 %)

Usa `AgendarCitaUseCaseTest`. Es la más fácil de explicar porque prueba una regla de negocio
real, no un detalle técnico.

```
gradlew testDebugUnitTest --tests "*AgendarCitaUseCaseTest*"
```

**Propósito:** verificar que la app no deje agendar una cita con menos de 15 horas de
anticipación, antes siquiera de gastar una llamada al servidor.

**Implementación:** tiene tres partes que conviene nombrar.

El reloj está fijo: `Clock.fixed(Instant.parse("2026-11-10T13:00:00Z"), ZoneId.of("America/Santiago"))`.
Si usara la hora real, la prueba pasaría hoy y fallaría mañana. Una prueba tiene que dar
siempre el mismo resultado.

El repositorio es un doble, `CitaRepositorioFalso`, que no toca la red ni la base de datos.
Por eso la prueba corre en milisegundos y funciona sin backend.

Cada caso sigue la estructura preparar–ejecutar–verificar. En el caso del rechazo se comprueba
además que `repo.creadas` quedó vacío: no basta con que lance el error, hay que confirmar que
**no llamó al backend**.

**Validación del resultado:** la prueba cubre los tres casos que importan: una cita con
anticipación suficiente, una sin ella, y el límite exacto de 15 horas. Ese tercero es el que
suele tener errores de borde.

Si prefieres algo más visual, `ValidadoresCompletosTest` prueba el RUT chileno con dígito
verificador, el algoritmo de Luhn para tarjetas y los teléfonos chilenos.

---

## IE 3.2.2 — Para qué sirven las pruebas (8 %)

Responde con las tres palabras de la pauta y un ejemplo propio para cada una.

**Confiabilidad.** La cobertura es de 86,62 % de instrucciones sobre el código lógico,
medida con JaCoCo en cada push. Cubre validadores, casos de uso, mapeadores, traducción de
errores y los ViewModels.

**Estabilidad.** Las pruebas detectan cuando un cambio rompe algo que ya funcionaba. Es
exactamente lo que vas a demostrar en el IE 3.1.3: al tocar la regla de las 15 horas, las
pruebas se ponen en rojo solas.

**Seguridad.** Las pruebas de validación protegen los datos antes de que lleguen al servidor:
RUT con dígito verificador, teléfono chileno, tarjeta con Luhn, consentimiento obligatorio
para datos de salud. Y en el backend, `SeguridadTest` verifica las reglas de acceso por rol.

**Si preguntan por qué no es 100 %:** porque se mide el código lógico. Quedan fuera las
pantallas Compose, los servicios de Android (FCM, GPS, WorkManager), los DTOs de red y el
código generado por Room y KSP: no tienen lógica propia o no son ejecutables en la JVM. La
lista exacta está en `exclusionesDeCobertura`, en `app/build.gradle.kts`. Perseguir el 100 %
llevaría a escribir pruebas sin valor solo para subir un número.

**Un ejemplo real que puedes contar:** el CI detectó tres errores antes de llegar a `main`
—un import faltante, una dependencia implícita de Gradle y un cambio de rutas en AGP 9— que
habrían reventado la compilación en el peor momento.

---

## IE 3.3.1 — APK firmado paso a paso (8 %)

Son cuatro pasos. Ten la terminal y `app/build.gradle.kts` abiertos.

**1. Crear el keystore.** Desde la carpeta `SanaesenciaApp`:

```
keytool -genkeypair -v -keystore sanaesencia-release.jks -alias sanaesencia ^
  -keyalg RSA -keysize 2048 -validity 10000
```

Genera un par de claves RSA de 2048 bits válido por 10.000 días. La clave privada firma el
APK; la pública queda en el certificado dentro del archivo.

**2. Declarar las credenciales** en `keystore.properties`, copiado de
`keystore.properties.example`: `storeFile`, `storePassword`, `keyAlias` y `keyPassword`.

**3. Compilar:** `gradlew assembleRelease`. Sale en `app/build/outputs/apk/release/`.

**4. Verificar:** `apksigner verify --verbose app-release.apk`.

### Lo que van a preguntar

**¿Por qué el `.jks` no está en GitHub?**
Porque contiene la clave privada. Quien la tenga puede firmar un APK que Android acepta como
actualización legítima de tu app. Por eso `*.jks` y `keystore.properties` están en
`.gitignore`, y el build lee las credenciales desde ese archivo local o desde variables de
entorno.

**¿Qué pasa si alguien clona el repo sin el keystore?**
Compila igual. `build.gradle.kts` comprueba si existe el keystore: si no está, la variante
release queda sin firmar en vez de fallar la compilación.

**¿Qué es la firma v1 y v2?**
v1 firma archivo por archivo dentro del ZIP; v2 firma el APK completo como un bloque, es más
rápida de verificar y detecta modificaciones del contenedor. Están las dos activadas.

**¿Por qué `isMinifyEnabled = false`?**
Porque Moshi y Room resuelven clases por reflexión y R8 las eliminaría al no ver referencias
directas. Las reglas quedan escritas en `proguard-rules.pro` por si se activa más adelante.

**Advertencia para la demo:** el manifest de release tiene `usesCleartextTraffic="false"`, así
que el APK firmado **no se conecta a un backend por HTTP sin cifrar**. La demostración en vivo
va con la build debug; el APK firmado se muestra aparte como evidencia. Si te preguntan por
qué, la respuesta es buena: es una decisión de seguridad deliberada para la versión de
publicación.

---

## IE 3.3.2 — Participación técnica (8 %)

Piden evidencia en **GitHub y Trello**. Ten los dos abiertos en pestañas.

En GitHub muestra el historial de commits, los Pull Requests y el CI en verde. En Trello, el
tablero con las tarjetas por requerimiento y su estado.

### Si preguntan por el uso de IA

Los commits recientes están firmados con `Co-Authored-By: Claude`, y eso se ve en el
historial. **Dilo tú antes de que lo pregunten.** Queda mucho mejor que si lo descubren.

Lo que se evalúa no es si usaste herramientas, sino si entiendes lo que entregas. Prepárate
para explicar sin leer:

- Por qué el cliente de Open-Meteo está separado del cliente del backend (para que el token
  JWT de la sesión no viaje a un servidor de terceros, y para que un 401 externo no pueda
  cerrar la sesión del paciente).
- Por qué los ViewModels dependen de interfaces `AuthRepositorio`, `CatalogRepositorio` y
  `CitaRepositorio` en vez de las clases concretas (inversión de dependencias: permite
  probarlos sin Room, sin DataStore y sin backend).
- Por qué la regla de las 15 horas está validada dos veces.

Si puedes explicar esas tres cosas con tus palabras, el indicador está cubierto.

---

## Checklist del día

**La noche anterior**

- [ ] Backend arranca: `gradlew bootRun`
- [ ] Pruebas en verde: `gradlew testDebugUnitTest`
- [ ] App instalada en el teléfono y login probado
- [ ] APK firmado generado y verificado con `apksigner`
- [ ] Ensayo completo del IE 3.1.3, cronometrado, dos veces
- [ ] Tablero de Trello actualizado

**Escenario de red, probado antes**

- [ ] Hotspot del teléfono encendido, notebook conectado a él
- [ ] IP anotada (`ipconfig`)
- [ ] Puerto 8080 abierto en el firewall de Windows (el hotspot se clasifica como red Pública)
- [ ] App compilada con esa IP:
      `gradlew installDebug -PAPI_BASE_URL=http://<IP>:8080/api/` (la barra final es obligatoria)

**Pestañas abiertas antes de empezar**

- [ ] `Validadores.kt` y `Services.kt` (el cambio en vivo)
- [ ] `Models.kt` y `Controllers.kt` (las entidades y endpoints)
- [ ] `app/build.gradle.kts` (la firma y la cobertura)
- [ ] GitHub y Trello en el navegador
- [ ] Una terminal en el backend y otra en la app

**Plan B**

- [ ] Emulador configurado y probado, por si el teléfono falla
- [ ] Capturas de la app funcionando, por si se cae todo
