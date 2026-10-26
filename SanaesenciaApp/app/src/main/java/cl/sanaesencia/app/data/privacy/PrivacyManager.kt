package cl.sanaesencia.app.data.privacy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.privacyStore by preferencesDataStore(name = "privacy")

/**
 * Guarda la aceptación de los Términos y Condiciones (Leyes 19.628, 20.584 y 21.719).
 * Si el texto legal cambia, se sube [VERSION_TERMINOS] y todos los usuarios deben aceptarlo de nuevo.
 */
class PrivacyManager(private val context: Context) {

    companion object {
        const val VERSION_TERMINOS = 3
    }

    private val acceptedKey = booleanPreferencesKey("terminos_aceptados_v$VERSION_TERMINOS")
    private val acceptedAtKey = longPreferencesKey("terminos_aceptados_fecha")

    val accepted: Flow<Boolean> =
        context.privacyStore.data.map { preferences ->
            preferences[acceptedKey] ?: false
        }

    /** Fecha (epoch ms) en que se aceptaron los términos, como registro del consentimiento. */
    val acceptedAt: Flow<Long?> =
        context.privacyStore.data.map { it[acceptedAtKey] }

    suspend fun accept() {
        context.privacyStore.edit { preferences ->
            preferences[acceptedKey] = true
            preferences[acceptedAtKey] = System.currentTimeMillis()
        }
    }
}
