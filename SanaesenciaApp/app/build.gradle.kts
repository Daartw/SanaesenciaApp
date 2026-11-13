import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    // IE 3.2.1: mide la cobertura de las pruebas unitarias.
    id("jacoco")
}

// RF-13: el plugin de Google Services se aplica solo cuando el proyecto tiene su google-services.json
// (las credenciales de Firebase no se suben a GitHub). Sin él, la app compila igual y usa
// recordatorios locales; con él, además recibe notificaciones push de FCM.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

// URL del backend. Por defecto apunta al PC anfitrión visto desde el emulador.
// Para otro destino: ./gradlew assembleDebug -PAPI_BASE_URL=https://api.midominio.cl/api/
// (la versión de publicación solo acepta HTTPS: usesCleartextTraffic está desactivado).
val apiBaseUrl: String = (project.findProperty("API_BASE_URL") as String?) ?: "http://10.0.2.2:8080/api/"

// RF/IE 3.3.1: datos del keystore para firmar la versión de publicación.
// Nunca se suben a GitHub: se leen de `keystore.properties` (ignorado por Git) y, si no
// existe, de variables de entorno. Sin ninguno de los dos el proyecto compila igual:
// solo queda sin firmar la variante release.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun datoDeFirma(clave: String, variableDeEntorno: String): String? =
    (keystoreProperties.getProperty(clave) ?: System.getenv(variableDeEntorno))
        ?.takeIf { it.isNotBlank() }

val rutaKeystore = datoDeFirma("storeFile", "SANAESENCIA_STORE_FILE")
val hayKeystore = rutaKeystore != null && rootProject.file(rutaKeystore).exists()

android {
    namespace = "cl.sanaesencia.app"
    // Compose 1.12 y OkHttp 5 exigen compilar contra la API 37
    compileSdk = 37

    defaultConfig {
        applicationId = "cl.sanaesencia.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"$apiBaseUrl\""
        )
    }

    signingConfigs {
        create("release") {
            if (hayKeystore) {
                storeFile = rootProject.file(rutaKeystore!!)
                storePassword = datoDeFirma("storePassword", "SANAESENCIA_STORE_PASSWORD")
                keyAlias = datoDeFirma("keyAlias", "SANAESENCIA_KEY_ALIAS")
                keyPassword = datoDeFirma("keyPassword", "SANAESENCIA_KEY_PASSWORD")
                // Esquemas de firma APK v1 + v2: exigido desde Android 7 en adelante.
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        getByName("debug") {
            // Habilita el agente de JaCoCo en las pruebas unitarias (IE 3.2.1).
            enableUnitTestCoverage = true
        }
        getByName("release") {
            // Sin ofuscar: Moshi y Room resuelven clases por reflexión y R8 las eliminaría.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Si no hay keystore configurado, la variante queda sin firmar en vez de fallar.
            signingConfig = if (hayKeystore) signingConfigs.getByName("release") else null
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Las clases del framework de Android que no se usan en la lógica devuelven su
    // valor por defecto en vez de lanzar "not mocked".
    testOptions.unitTests.isReturnDefaultValues = true
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))

    implementation("androidx.activity:activity-compose:1.10.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.navigation:navigation-compose:2.10.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    implementation("androidx.datastore:datastore-preferences:1.3.0")

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.5.1")

    implementation("com.google.android.gms:play-services-location:21.4.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // RF-13: notificaciones (FCM + recordatorios programados)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.work:work-runtime-ktx:2.12.0")

    testImplementation("junit:junit:4.13.2")
    // IE 3.2.1: control del hilo principal y del tiempo virtual en las pruebas de ViewModel.
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}

// ---------------------------------------------------------------------------
// IE 3.2.1 — Cobertura de pruebas unitarias
//
// Se mide el CÓDIGO LÓGICO de la app: dominio (validadores, casos de uso, modelos),
// capa de presentación (ViewModels) y mapeadores. Quedan fuera las clases que no
// contienen lógica propia o que no son ejecutables en la JVM: pantallas Compose,
// tema, servicios de Android (FCM, GPS, WorkManager), DTOs de red y el código
// generado por Room, KSP y el compilador de Compose.
//
//   ./gradlew jacocoTestReport
//   Informe: app/build/reports/jacoco/jacocoTestReport/html/index.html
// ---------------------------------------------------------------------------
jacoco {
    toolVersion = "0.8.12"
}

val exclusionesDeCobertura = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    // Interfaz de usuario: se valida en la demostración, no con pruebas unitarias JVM.
    "**/ui/**",
    "**/*Screen*.*",
    "**/*ComposableSingletons*",
    "**/*Composable*",
    // Servicios que dependen del framework de Android.
    "**/services/**",
    "**/data/session/**",
    "**/data/privacy/**",
    "**/data/local/**",
    // DTOs de red y cliente HTTP: son datos y configuración, sin lógica propia.
    "**/data/remote/Api*.*",
    "**/data/remote/RetrofitClient*.*",
    "**/data/remote/externo/ClimaRetrofit*.*",
    // Repositorios que envuelven Room y Retrofit: se validan con pruebas de
    // integración contra el backend, no con pruebas unitarias en la JVM.
    // (ClimaRepository sí se mide: su lógica de conversión es pura.)
    "**/data/repository/AuthRepository*.*",
    "**/data/repository/CatalogRepository*.*",
    "**/data/repository/CitaRepository*.*",
    "**/data/repository/CartRepository*.*",
    "**/data/repository/PedidoRepository*.*",
    "**/data/repository/Resultado*.*",
    // Código generado.
    "**/*_Impl*",
    "**/*_Factory*",
    "**/*\$\$*",
    "**/AppContainer*",
    "**/MainActivity*",
)

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Genera el informe de cobertura de las pruebas unitarias (variante debug)."
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }

    val carpetaBuild = layout.buildDirectory.get().asFile

    classDirectories.setFrom(
        files(
            fileTree(File(carpetaBuild, "tmp/kotlin-classes/debug")) {
                exclude(exclusionesDeCobertura)
            },
            fileTree(File(carpetaBuild, "intermediates/javac/debug")) {
                exclude(exclusionesDeCobertura)
            },
        )
    )

    sourceDirectories.setFrom(files("src/main/java"))

    // Se apunta a los archivos exactos: recorrer todo `build/` haría que Gradle
    // detecte dependencias implícitas con otras tareas y falle la validación.
    executionData.setFrom(
        files(
            File(carpetaBuild, "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"),
            File(carpetaBuild, "jacoco/testDebugUnitTest.exec"),
        ).filter { it.exists() }
    )
}