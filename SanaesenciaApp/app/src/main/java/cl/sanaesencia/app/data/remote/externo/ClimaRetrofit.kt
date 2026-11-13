package cl.sanaesencia.app.data.remote.externo

import cl.sanaesencia.app.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * IE 3.1.4 — Cliente HTTP de la API externa.
 *
 * Deliberadamente SEPARADO de [cl.sanaesencia.app.data.remote.RetrofitClient]:
 *
 * - URL base propia (Open-Meteo), distinta de la del backend de Sanaesencia.
 * - Sin el interceptor de autenticación: el token JWT de la persona NUNCA se envía
 *   a un servidor de terceros.
 * - Sin el manejo de sesión expirada: un 401 de Open-Meteo no puede cerrar la sesión
 *   del paciente.
 * - Tiempos de espera más cortos: si el servicio externo se demora, la app no se queda
 *   esperando.
 *
 * Así el consumo de la API externa no interfiere con los microservicios propios.
 */
object ClimaRetrofit {

    const val BASE_URL = "https://api.open-meteo.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val cliente: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    val api: ClimaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ClimaApi::class.java)
    }
}
