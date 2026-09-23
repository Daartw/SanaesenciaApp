# Sanaesencia App — DSY1105

Aplicación Android nativa para el caso académico **Centralización de Servicios Terapéuticos, Agendamiento y Trazabilidad de Pacientes — Centro Integrativo Sanaesencia**.

> **Código fuente:** 100% Kotlin. No se mantienen clases Java escritas por el proyecto. Los archivos `.java` que Room/KSP puede generar durante una compilación son artefactos internos y están excluidos de Git mediante `.gitignore`.

## Stack

- Kotlin
- Android Studio
- Jetpack Compose + Material 3
- MVVM + separación Domain/Data/UI
- Room / SQLite para persistencia local de citas
- Retrofit 2 + Gson para la capa REST preparada para Spring Boot
- WorkManager para chequeos periódicos de inactividad
- Play Services Location para orientación a la sede
- Notificaciones locales como demostración del flujo de alertas
- Pixel 9 API / Android API 35 como configuración objetivo

## Paleta

```kotlin
val SanaesenciaGreen = Color(0xFF4CAF50)
val SanaesenciaPurple = Color(0xFF7E57C2)
val BackgroundLight = Color(0xFFF9FBE7)
val TextPrimary = Color(0xFF2E3D49)
```

## Arquitectura

```text
UI (Compose)
   ↓
ViewModels (MVVM)
   ↓
Domain / Repository contract
   ↓
Repository implementation
   ├── Room / SQLite (persistencia local)
   └── Retrofit (API REST preparada)
```

Los datos de demostración son ficticios y anonimizados. La app puede funcionar sin backend para la demostración académica.

## Roles

### PACIENTE

- Explorar profesionales y especialidades.
- Filtrar por especialidad.
- Filtrar por modalidad presencial/online.
- Buscar por nombre, carrera, certificaciones y capacitaciones.
- Filtrar por años de experiencia.
- Ver perfil profesional.
- Consultar fechas y horarios disponibles.
- Seleccionar modalidad y reservar.
- Consultar agenda.
- Consultar historial y trazabilidad.
- Consultar productos y promociones.
- Usar GPS para calcular distancia/orientarse a la sede.

### TERAPEUTA

- Dashboard propio.
- Agenda asignada.
- Visualización de pacientes ficticios.
- Seguimiento de última sesión e inactividad.
- Consulta de continuidad del tratamiento.

### ADMIN

- Dashboard administrativo.
- Agregar terapeutas de demostración.
- Cambiar estado de terapeutas:
  - DISPONIBLE
  - AUSENTE
  - VACACIONES
- Gestión del estado de los 8 boxes.
- Visualización de pacientes con inactividad.
- Promociones.
- Ejecución manual del chequeo de inactividad.

## Permisos

La app evita pedir permisos innecesarios.

- **POST_NOTIFICATIONS:** se solicita una vez en Android 13+ para las alertas.
- **ACCESS_FINE_LOCATION:** se solicita solamente cuando el paciente pulsa la función de cálculo de distancia.
- Internet se declara para Retrofit.

La ubicación no es obligatoria para utilizar el resto de la aplicación.

## Requerimientos funcionales cubiertos

| RF | Implementación |
|---|---|
| RF-01 | Especialidades y profesionales en el dashboard del paciente |
| RF-02 | Catálogo de productos naturales ficticios |
| RF-03 | Filtro presencial / online |
| RF-04 | Fechas y horarios disponibles por profesional |
| RF-05 | Reserva con profesional, fecha, hora y modalidad |
| RF-06 | Gestión de los 8 boxes desde administración |
| RF-07 | Agenda e historial persistidos localmente con Room |
| RF-08 | Detección demostrativa de inactividad/deserción |
| RF-09 | Notificaciones y WorkManager para alertas demostrativas |
| RF-10 | Visualización de promociones y gestión en dashboard admin |
| RF-11 | GPS y orientación a Caupolicán 220, Los Vilos |

## Datos externos

La capa Retrofit está preparada para un backend Spring Boot. En esta versión académica, el funcionamiento principal usa datos ficticios locales para no depender de un servidor externo.

No se afirma que exista una conexión real con MEDIlink, FCM o un backend Spring Boot mientras esos servicios no estén disponibles.

## Ejecutar en Android Studio

1. Abrir la carpeta `SanaesenciaApp`.
2. Esperar el Gradle Sync.
3. Tener instalado Android SDK API 35.
4. Crear/iniciar un emulador **Pixel 9 API 35**.
5. Ejecutar `app`.

El ZIP original no contenía el `gradle-wrapper.jar`; por eso este repositorio conserva `gradle-wrapper.properties`, pero se recomienda abrirlo desde Android Studio con el Gradle configurado por el IDE o regenerar el wrapper localmente antes de usar `./gradlew` desde una terminal.

## Usuarios de demostración

La pantalla inicial permite seleccionar directamente el rol:

- Paciente
- Terapeuta
- Administrador

No se utilizan contraseñas ni datos reales: es un flujo de demostración académica.

## Release

Para generar el APK release desde Android Studio:

`Build → Generate Signed App Bundle / APK → APK → release`

Se recomienda usar una clave de firma propia y **no subir el `.jks` al repositorio**.

## GitHub

Antes de subir:

```bash
git init
git add .
git commit -m "feat: Sanaesencia app full Kotlin"
git branch -M main
git remote add origin <REPOSITORIO_GITHUB>
git push -u origin main
```

El `.gitignore` excluye `build/`, `.gradle/`, `.idea/`, `local.properties`, APKs, AABs y claves locales.

## Nota sobre archivos Java generados

Room/KSP puede crear durante `build` archivos como:

```text
AppDatabase_Impl.java
CitaDao_Impl.java
```

Eso es código generado por la herramienta y no código fuente Java del proyecto. No se versiona y se recreará automáticamente cuando corresponda.
