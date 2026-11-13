import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
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
}