package cl.sanaesencia.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import cl.sanaesencia.app.domain.model.RolUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionStore by preferencesDataStore(name = "session")

data class Session(
    val token: String,
    val userId: Long,
    val role: RolUsuario,
    val name: String
)

class SessionManager(
    private val context: Context
) {

    private val token = stringPreferencesKey("token")
    private val uid = longPreferencesKey("uid")
    private val role = stringPreferencesKey("role")
    private val name = stringPreferencesKey("name")

    val session: Flow<Session?> = context.sessionStore.data.map { preferences ->
        val savedToken = preferences[token] ?: return@map null

        Session(
            token = savedToken,
            userId = preferences[uid] ?: 0L,
            role = runCatching {
                RolUsuario.valueOf(
                    preferences[role] ?: RolUsuario.PACIENTE.name
                )
            }.getOrDefault(RolUsuario.PACIENTE),
            name = preferences[name] ?: ""
        )
    }

    suspend fun save(session: Session) {
        context.sessionStore.edit { preferences ->
            preferences[token] = session.token
            preferences[uid] = session.userId
            preferences[role] = session.role.name
            preferences[name] = session.name
        }
    }

    suspend fun clear() {
        context.sessionStore.edit { preferences ->
            preferences.clear()
        }
    }
}