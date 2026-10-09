package cl.sanaesencia.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.sanaesencia.app.domain.model.RolUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionStore by preferencesDataStore(name = "session")
data class Session(val token: String, val userId: Long, val role: RolUsuario, val name: String)

class SessionManager(private val context: Context) {
    private val token = stringPreferencesKey("token")
    private val uid = longPreferencesKey("uid")
    private val role = stringPreferencesKey("role")
    private val name = stringPreferencesKey("name")

    val session: Flow<Session?> = context.sessionStore.data.map { p ->
        val savedToken = p[token] ?: return@map null
        Session(savedToken, p[uid] ?: 0L, runCatching { RolUsuario.valueOf(p[role] ?: RolUsuario.PACIENTE.name) }.getOrDefault(RolUsuario.PACIENTE), p[name] ?: "")
    }

    suspend fun save(s: Session) { context.sessionStore.edit { p -> p[token] = s.token; p[uid] = s.userId; p[role] = s.role.name; p[name] = s.name } }
    suspend fun clear() { context.sessionStore.edit { it.clear() } }
}
