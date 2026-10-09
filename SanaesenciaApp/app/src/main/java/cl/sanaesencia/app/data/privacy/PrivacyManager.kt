package cl.sanaesencia.app.data.privacy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.privacyStore by preferencesDataStore(name = "privacy")

class PrivacyManager(private val context: Context) {

    private val acceptedKey =
        booleanPreferencesKey("privacy_notice_accepted_v2")

    val accepted: Flow<Boolean> =
        context.privacyStore.data.map { preferences ->
            preferences[acceptedKey] ?: false
        }

    suspend fun accept() {
        context.privacyStore.edit { preferences ->
            preferences[acceptedKey] = true
        }
    }
}