plugins {
    // AGP 9 trae Kotlin integrado: ya no se aplica el plugin org.jetbrains.kotlin.android
    id("com.android.application") version "9.5.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
    id("com.google.devtools.ksp") version "2.3.13" apply false
    // RF-13: Firebase Cloud Messaging. Solo se activa si existe app/google-services.json.
    id("com.google.gms.google-services") version "4.5.0" apply false
}
