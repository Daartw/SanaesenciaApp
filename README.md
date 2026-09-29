# Sanaesencia DSY1105 — Full Stack Kotlin

Proyecto académico full-stack para el Centro Integrativo Sanaesencia.

## Componentes
- `SanaesenciaApp`: Android nativo, 100 % Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture, Room, Retrofit, DataStore.
- `SanaesenciaBackend`: Spring Boot en Kotlin, REST, Spring Data JPA, JWT, H2 para demo y MySQL para desarrollo.
- `docker-compose.yml`: MySQL.
- `docs/`: arquitectura, RF/RNF, reglas y feedback.

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

## Demo
- `paciente@sanaesencia.demo` / `Demo123!`
- `profesional@sanaesencia.demo` / `Demo123!`
- `admin@sanaesencia.demo` / `Demo123!`

## Backend
Con Java 21 instalado:
```bash
cd SanaesenciaBackend
./gradlew bootRun
```
API: `http://localhost:8080/api`

MySQL:
```bash
docker compose up -d mysql
```

## Android
Abrir `SanaesenciaApp` en Android Studio.
El emulador Android usa `http://10.0.2.2:8080/api/`.

## Firebase
No se incluyen credenciales. Agregar `google-services.json` y dependencias Firebase cuando se conecte el proyecto real.

## MEDIlink
No se inventan endpoints, API keys ni credenciales. Se dejó un adaptador vacío para una futura integración oficial.

## Release
Configurar un keystore propio desde Android Studio. Nunca subir keystore, passwords, secretos JWT o `google-services.json` a GitHub.
