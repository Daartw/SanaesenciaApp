package cl.sanaesencia.app.data.remote

import android.content.Context
import cl.sanaesencia.app.BuildConfig
import cl.sanaesencia.app.data.session.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    private lateinit var sessionManager: SessionManager

    fun initialize(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->

        val original = chain.request()

        val token = if (::sessionManager.isInitialized) {
            runBlocking {
                sessionManager.session.first()?.token
            }
        } else {
            null
        }

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer $token"
                )
                .build()
        } else {
            original
        }

        chain.proceed(request)
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .build()

    val api: SanaesenciaApi by lazy {
        retrofit.create(SanaesenciaApi::class.java)
    }
}