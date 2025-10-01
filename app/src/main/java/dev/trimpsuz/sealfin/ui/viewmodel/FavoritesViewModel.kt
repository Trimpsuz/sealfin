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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.playStateApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemFilter
import org.jellyfin.sdk.model.api.LocationType
import org.jellyfin.sdk.model.api.UserItemDataDto
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val jellyfinClient: JellyfinClientWrapper,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _favorites = MutableStateFlow<Map<String, List<BaseItemDto>>>(emptyMap())
    val favorites: StateFlow<Map<String, List<BaseItemDto>>> = _favorites.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val activeServer: StateFlow<Server?> = dataStore.activeServerFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            dataStore.activeServerFlow
                .filterNotNull()
                .distinctUntilChanged()
                .collect { server ->
                    loadFavorites(server.baseUrl, server.accessToken)
                }
        }
    }

    private suspend fun loadFavorites(baseUrl: String, token: String) {
        withContext(Dispatchers.IO) {
            try {
                val api = jellyfinClient.createApiClient(baseUrl, token)

                val types = listOf(
                    BaseItemKind.MOVIE to "Movies",
                    BaseItemKind.SERIES to "Shows",
                    BaseItemKind.SEASON to "Seasons",
                    BaseItemKind.EPISODE to "Episodes"
                )

                val resultMap = mutableMapOf<String, List<BaseItemDto>>()

                for ((kind, label) in types) {
                    val response = api.itemsApi.getItems(
                        includeItemTypes = listOf(kind),
                        filters = listOf(ItemFilter.IS_FAVORITE),
                        recursive = true,
                        excludeLocationTypes = listOf(LocationType.VIRTUAL),
                    )
                    resultMap[label] = response.content.items
                }

                _favorites.value = resultMap
            } catch (e: Exception) {
                _favorites.value = emptyMap()
            }
        }
    }

    fun updatePlayed(id: UUID, isPlayed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val server = dataStore.activeServerFlow.firstOrNull() ?: return@launch
            val api = jellyfinClient.createApiClient(server.baseUrl, server.accessToken)

            if (isPlayed) api.playStateApi.markUnplayedItem(id)
            else api.playStateApi.markPlayedItem(id)

            updateUserDataLocally(id) { it.copy(played = !isPlayed) }
        }
    }

    fun updateFavorite(id: UUID, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val server = dataStore.activeServerFlow.firstOrNull() ?: return@launch
            val api = jellyfinClient.createApiClient(server.baseUrl, server.accessToken)

            if (isFavorite) api.userLibraryApi.unmarkFavoriteItem(id)
            else api.userLibraryApi.markFavoriteItem(id)

            updateUserDataLocally(id) { it.copy(isFavorite = !isFavorite) }
        }
    }

    private fun updateUserDataLocally(
        id: UUID,
        transform: (UserItemDataDto) -> UserItemDataDto
    ) {
        _favorites.update { map ->
            map.mapValues { (_, list) ->
                list.map { item ->
                    if (item.id == id) item.copy(userData = item.userData?.let(transform))
                    else item
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val server = dataStore.activeServerFlow.firstOrNull() ?: return@launch
                loadFavorites(server.baseUrl, server.accessToken)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
