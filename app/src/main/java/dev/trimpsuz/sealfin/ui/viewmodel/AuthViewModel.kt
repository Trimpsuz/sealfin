package dev.trimpsuz.sealfin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.sealfin.data.DataStoreManager
import dev.trimpsuz.sealfin.data.JellyfinClientWrapper
import dev.trimpsuz.sealfin.data.Server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.extensions.systemApi
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val jellyfinClient: JellyfinClientWrapper,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    val servers: StateFlow<List<Server>> = dataStore.serversFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    val activeServer: StateFlow<Server?> = dataStore.activeServerFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, null
    )

    fun addServer(serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            val result = withContext(Dispatchers.IO) {
                jellyfinClient.authenticate(serverUrl, username, password)
            }

            if (result.isSuccess) {
                val (token, userId) = result.getOrThrow()

                val serverName = withContext(Dispatchers.IO) {
                    val api = jellyfinClient.createApiClient(serverUrl, token)

                    return@withContext api.systemApi.getPublicSystemInfo().content.serverName ?: serverUrl
                }

                val newServer = Server(
                    id = userId,
                    name = serverName,
                    baseUrl = serverUrl,
                    username = username,
                    accessToken = token
                )

                dataStore.addServer(newServer)
                _loginState.value = LoginUiState.Success(newServer.id)
            } else {
                _loginState.value = LoginUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                )
            }
        }
    }

    fun switchServer(serverId: String) {
        viewModelScope.launch {
            dataStore.switchServer(serverId)
        }
    }

    fun removeServer(serverId: String) {
        viewModelScope.launch {
            dataStore.removeServer(serverId)
        }
    }
}

