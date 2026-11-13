# Reglas de ProGuard/R8 para la variante release.
#
# La ofuscación está desactivada (isMinifyEnabled = false) porque Moshi y Room
# resuelven clases y propiedades por reflexión. Estas reglas quedan declaradas para
# que, si en el futuro se activa la ofuscación, el APK firmado siga funcionando.

# --- Modelos de datos serializados por Moshi -------------------------------
-keep class cl.sanaesencia.app.data.remote.** { *; }
-keep class cl.sanaesencia.app.domain.model.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}

# --- Moshi / Retrofit / OkHttp ---------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# --- Room -------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# --- Metadatos de Kotlin ----------------------------------------------------
-keep class kotlin.Metadata { *; }
