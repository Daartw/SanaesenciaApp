# Sanaesencia App

Proyecto Android (MVP) para el caso DSY1105 — Centro Integrativo Sanaesencia.

## Cómo abrir el proyecto

1. Abre Android Studio.
2. **File → Open** y selecciona la carpeta `SanaesenciaApp` (la que contiene `settings.gradle.kts`).
3. Si Android Studio pregunta por el Gradle Wrapper, deja que lo genere automáticamente
   ("Create Gradle wrapper" / "Use Gradle from: wrapper") o usa la opción de Gradle
   embebido en el IDE.
4. Espera a que termine el **Gradle Sync** (puede tardar unos minutos la primera vez,
   descarga dependencias).
5. Ejecuta la app (▶) sobre un emulador o dispositivo con Android 7.0 (API 24) o superior.

## Estructura del proyecto (arquitectura MVVM)

```
com.example.sanaesencia_app
├── data
│   ├── local        → Room (CitaEntity, CitaDao, AppDatabase)
│   ├── remote        → Retrofit (SanaesenciaApi, RetrofitInstance)
│   └── repository    → CitaRepositoryImpl (implementación, hoy con datos mock)
├── domain
│   ├── model         → Cita, Paciente, Especialidad
│   └── repository    → CitaRepository (interfaz / contrato)
├── ui
│   ├── theme         → Color.kt, Theme.kt, Type.kt (Material 3)
│   ├── navigation     → Screen.kt, NavGraph.kt (Navigation Compose)
│   ├── login          → LoginScreen.kt
│   ├── catalogo       → CatalogoScreen.kt, CatalogoViewModel.kt
│   ├── agenda         → AgendaScreen.kt, AgendaViewModel.kt
│   └── historial      → HistorialScreen.kt, HistorialViewModel.kt
└── MainActivity.kt
```

## Estado actual

- La app corre con **datos de prueba (mock)** en `CitaRepositoryImpl`, para que la UI
  funcione de inmediato sin depender del backend.
- Room y Retrofit ya están configurados (dependencias + clases base) pero aún no
  conectados a la UI — el siguiente paso es que `CitaRepositoryImpl` combine Room
  (caché local) y Retrofit (API Spring Boot) en vez de las listas mock.
- Flujo de pantallas: **Login → Catálogo → Agenda / Historial**.
- El `HistorialViewModel` incluye una señal simple de alerta de riesgo de deserción
  (marca una cita como `INACTIVA`), como base del motor de alertas predictivas
  pedido en el caso.

## Próximos pasos sugeridos

1. Conectar `CitaRepositoryImpl` a Room para persistencia real en el dispositivo.
2. Apuntar `RetrofitInstance.BASE_URL` a la URL real del backend Spring Boot.
3. Agregar pantalla de "Administración" (boxes, métricas de deserción, promociones).
4. Implementar notificaciones push (recordatorios, inactividad, promociones).
5. Integrar GPS para la orientación hacia la sede en Los Vilos.
