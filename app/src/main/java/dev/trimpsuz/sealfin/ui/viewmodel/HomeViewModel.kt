package dev.trimpsuz.sealfin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.sealfin.data.DataStoreManager
import dev.trimpsuz.sealfin.data.JellyfinClientWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val jellyfinClient: JellyfinClientWrapper,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _continueWatching = MutableStateFlow<List<BaseItemDto>>(emptyList())
    val continueWatching: StateFlow<List<BaseItemDto>> = _continueWatching.asStateFlow()

    private val _nextUp = MutableStateFlow<List<BaseItemDto>>(emptyList())
    val nextUp: StateFlow<List<BaseItemDto>> = _nextUp.asStateFlow()

    private val _recentlyAdded = MutableStateFlow<List<LibraryWithItems>>(emptyList())
    val recentlyAdded: StateFlow<List<LibraryWithItems>> = _recentlyAdded.asStateFlow()


    init {
        viewModelScope.launch {
            dataStore.activeServerFlow.collectLatest { server ->
                if (server != null) {
                    loadContinueWatching(server.baseUrl, server.accessToken)
                    loadNextUp(server.baseUrl, server.accessToken)
                    loadRecentlyAdded(server.baseUrl, server.accessToken)
                } else {
                    _continueWatching.value = emptyList()
                    _recentlyAdded.value = emptyList()
                }
            }
        }
    }

    private suspend fun loadContinueWatching(baseUrl: String, token: String) {
        withContext(Dispatchers.IO) {
            try {
                val api = jellyfinClient.createApiClient(baseUrl, token)
                val response = api.itemsApi.getResumeItems()
                _continueWatching.value = response.content.items
            } catch (e: Exception) {
                _continueWatching.value = emptyList()
            }
        }
    }

    private suspend fun loadNextUp(baseUrl: String, token: String) {
        withContext(Dispatchers.IO) {
            try {
                val api = jellyfinClient.createApiClient(baseUrl, token)
                val response = api.tvShowsApi.getNextUp()
                _nextUp.value = response.content.items
            } catch (e: Exception) {
                _nextUp.value = emptyList()
            }
        }
    }

    private suspend fun loadRecentlyAdded(baseUrl: String, token: String) {
        withContext(Dispatchers.IO) {
            try {
                val api = jellyfinClient.createApiClient(baseUrl, token)

                val librariesResponse = api.itemsApi.getItems(
                    includeItemTypes = listOf(BaseItemKind.COLLECTION_FOLDER),
                    recursive = false
                )
                val libraries = librariesResponse.content.items.filter { item ->
                    val type = item.collectionType
                    type !in listOf(CollectionType.UNKNOWN, CollectionType.PLAYLISTS, CollectionType.FOLDERS)
                }

                val results = mutableListOf<LibraryWithItems>()

                for (library in libraries) {
                    val libId = library.id
                    val libName = library.name

                    val itemsResponse = api.itemsApi.getItems(
                        parentId = libId,
                        sortBy = listOf(ItemSortBy.DATE_CREATED),
                        sortOrder = listOf(SortOrder.DESCENDING),
                        limit = 10
                    )
                    val items = itemsResponse.content.items
                    results.add(LibraryWithItems(id = libId.toString(), name = libName ?: "Unknown", items = items))
                }

                _recentlyAdded.value = results
            } catch (e: Exception) {
                _recentlyAdded.value = emptyList()
            }
        }
    }

}

data class LibraryWithItems(
    val id: String,
    val name: String,
    val items: List<BaseItemDto>
)
