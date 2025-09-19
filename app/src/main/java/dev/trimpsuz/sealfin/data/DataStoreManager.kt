package dev.trimpsuz.sealfin.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "sealfin_prefs")

data class Server(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseUrl: String,
    val username: String,
    val accessToken: String
)

class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_SERVERS = stringPreferencesKey("servers") // JSON list
        private val KEY_ACTIVE_SERVER_ID = stringPreferencesKey("active_server_id")
        val KEY_THEME = stringPreferencesKey("theme")
    }

    private val gson = Gson()

    val serversFlow: Flow<List<Server>> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVERS]?.let {
            val type = object : TypeToken<List<Server>>() {}.type
            gson.fromJson<List<Server>>(it, type)
        } ?: emptyList()
    }

    val activeServerFlow: Flow<Server?> = combine(
        serversFlow,
        context.dataStore.data.map { it[KEY_ACTIVE_SERVER_ID] }
    ) { servers, activeId ->
        servers.find { it.id == activeId }
    }

    suspend fun addServer(server: Server) {
        val list = serversFlow.firstOrNull() ?: emptyList()
        val newList = list + server
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVERS] = gson.toJson(newList)
            prefs[KEY_ACTIVE_SERVER_ID] = server.id
        }
    }

    suspend fun removeServer(serverId: String) {
        val list = serversFlow.firstOrNull() ?: emptyList()
        val newList = list.filterNot { it.id == serverId }
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVERS] = gson.toJson(newList)
            if (prefs[KEY_ACTIVE_SERVER_ID] == serverId) {
                prefs.remove(KEY_ACTIVE_SERVER_ID)
            }
        }
    }

    suspend fun switchServer(serverId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_SERVER_ID] = serverId
        }
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "SYSTEM" }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }
}
