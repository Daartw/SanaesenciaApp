package cl.sanaesencia.app.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cl.sanaesencia.app.data.remote.RetrofitClient
import cl.sanaesencia.app.data.session.SessionManager
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.RolUsuario
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * RF-13: notificaciones "predictivas" que funcionan aunque Firebase no esté configurado.
 * Cada algunas horas revisa, para el paciente con sesión iniciada:
 *  - Recordatorio: citas que empiezan dentro de las próximas 24 horas.
 *  - Inactividad: si no tiene citas futuras y su última cita fue hace 21 días o más.
 *  - Promociones: promociones activas que aún no se le han mostrado.
 * Guarda lo ya avisado para no repetir notificaciones.
 */
class RecordatoriosWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val prefs = context.getSharedPreferences("notificaciones", Context.MODE_PRIVATE)
    private val zona = ZoneId.of("America/Santiago")

    override suspend fun doWork(): Result {
        val sesion = SessionManager(applicationContext).session.first() ?: return Result.success()
        if (sesion.role != RolUsuario.PACIENTE) return Result.success()

        RetrofitClient.initialize(applicationContext)
        val api = RetrofitClient.api

        return try {
            val citas = api.misCitas()
            val ahora = LocalDateTime.now(zona)

            // 1) Recordatorio 24 h antes
            citas.filter { it.estado == EstadoCita.RESERVADA || it.estado == EstadoCita.CONFIRMADA }
                .forEach { cita ->
                    val inicio = runCatching {
                        LocalDateTime.of(LocalDate.parse(cita.fecha.take(10)), LocalTime.parse(cita.horaInicio))
                    }.getOrNull() ?: return@forEach
                    val horas = Duration.between(ahora, inicio).toHours()
                    val clave = "cita_${cita.id}"
                    if (inicio.isAfter(ahora) && horas < 24 && !prefs.getBoolean(clave, false)) {
                        Notificaciones.mostrar(
                            applicationContext,
                            id = cita.id.toInt(),
                            canal = Notificaciones.CANAL_RECORDATORIOS,
                            titulo = "Recordatorio de tu cita",
                            texto = "Tienes una cita el ${cita.fecha.take(10)} a las ${cita.horaInicio.take(5)}. " +
                                if (cita.modalidad.name == "ONLINE") "Será online." else "Te esperamos en Caupolicán 220, Los Vilos.",
                        )
                        prefs.edit().putBoolean(clave, true).apply()
                    }
                }

            // 2) Alerta de inactividad (misma regla de 21 días que la deserción del admin)
            val tieneFutura = citas.any {
                (it.estado == EstadoCita.RESERVADA || it.estado == EstadoCita.CONFIRMADA) &&
                    runCatching { !LocalDate.parse(it.fecha.take(10)).isBefore(ahora.toLocalDate()) }.getOrDefault(false)
            }
            val ultima = citas.mapNotNull { runCatching { LocalDate.parse(it.fecha.take(10)) }.getOrNull() }.maxOrNull()
            val dias = ultima?.let { ChronoUnit.DAYS.between(it, ahora.toLocalDate()) }
            val ultimoAviso = prefs.getLong("inactividad_aviso", 0L)
            val haceUnaSemana = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            if (!tieneFutura && dias != null && dias >= DIAS_INACTIVIDAD && ultimoAviso < haceUnaSemana) {
                Notificaciones.mostrar(
                    applicationContext,
                    id = ID_INACTIVIDAD,
                    canal = Notificaciones.CANAL_SEGUIMIENTO,
                    titulo = "¿Cómo has estado?",
                    texto = "Han pasado $dias días desde tu última sesión. Agenda tu próxima hora para continuar tu proceso.",
                )
                prefs.edit().putLong("inactividad_aviso", System.currentTimeMillis()).apply()
            }

            // 3) Promociones nuevas
            runCatching { api.promociones() }.getOrNull().orEmpty()
                .filter { it.esActiva }
                .forEach { promo ->
                    val clave = "promo_${promo.id}"
                    if (!prefs.getBoolean(clave, false)) {
                        Notificaciones.mostrar(
                            applicationContext,
                            id = ID_PROMO_BASE + promo.id.toInt(),
                            canal = Notificaciones.CANAL_PROMOCIONES,
                            titulo = "Nueva promoción en la tienda",
                            texto = "${promo.titulo}: ${promo.porcentajeDescuento.toInt()}% de descuento en esencias seleccionadas.",
                        )
                        prefs.edit().putBoolean(clave, true).apply()
                    }
                }

            Result.success()
        } catch (e: Exception) {
            // Sin conexión: se vuelve a intentar en la próxima ejecución.
            Result.retry()
        }
    }

    companion object {
        private const val NOMBRE = "sanaesencia_recordatorios"
        private const val DIAS_INACTIVIDAD = 21L
        private const val ID_INACTIVIDAD = 900_001
        private const val ID_PROMO_BASE = 800_000

        /** Programa la revisión periódica (cada 6 horas). Llamarlo varias veces no duplica el trabajo. */
        fun programar(context: Context) {
            val request = PeriodicWorkRequestBuilder<RecordatoriosWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NOMBRE, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        /** Revisión inmediata (por ejemplo, justo después de iniciar sesión o de agendar). */
        fun ejecutarAhora(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "${NOMBRE}_ahora",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RecordatoriosWorker>().build(),
            )
        }
    }
}
