# Política de seguridad

## Reportar una vulnerabilidad

Si encuentras un problema de seguridad, **no abras un issue público**. Usa
**Security → Report a vulnerability** en este repositorio o escribe a
**danie.arayar@duocuc.cl** con:

- Descripción del problema y su impacto.
- Pasos para reproducirlo.
- Versión o commit afectado.

Responderé dentro de 7 días.

## Medidas implementadas

**Backend**
- JWT firmado con una clave que se entrega por variable de entorno (`JWT_SECRET`); no hay secretos en el repositorio.
- Spring Security sin estado, con control de acceso por rol en el filtro y repetido en los servicios.
- Contraseñas con BCrypt. No existen contraseñas por defecto para cuentas nuevas.
- Bloqueo temporal del login tras intentos fallidos y respuesta idéntica para correos existentes e inexistentes.
- Consola H2 apagada por defecto; MySQL de desarrollo publicado solo en `127.0.0.1`.
- Los errores no entregan detalles internos.

**App Android**
- Tráfico en claro deshabilitado en la versión de publicación (`usesCleartextTraffic=false`) y `allowBackup=false`.
- El token de sesión no se escribe en el log y se borra, junto con los datos locales, al cerrar sesión.
- Los accesos rápidos de demostración solo existen en compilaciones de depuración.

## Antes de un despliegue real

Este es un proyecto académico. Para usarlo con datos reales de pacientes, como mínimo:

1. Define `JWT_SECRET` (32+ caracteres aleatorios) y `APP_DEMO_SEED=false`.
2. Usa MySQL con credenciales propias y conexión cifrada (`DB_URL`, `DB_USER`, `DB_PASSWORD`).
3. Publica el backend solo por HTTPS, detrás de un proxy inverso.
4. Cifra el token en el dispositivo (Android Keystore) y activa la ofuscación (R8) en la versión de publicación.
5. Revisa los requisitos de la Ley 21.719 y la Ley 20.584 para datos de salud (registro de accesos, retención, consentimiento).
