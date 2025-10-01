package dev.trimpsuz.sealfin.ui.viewmodel.libraries

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.serializer.toUUID
import javax.inject.Inject

@HiltViewModel
class LibrariesViewModel @Inject constructor(
    private val jellyfinClient: JellyfinClientWrapper,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _libraries = MutableStateFlow<List<BaseItemDto>>(emptyList())
    val libraries: StateFlow<List<BaseItemDto>> = _libraries.asStateFlow()

    private val _libraryItems = MutableStateFlow<List<BaseItemDto>>(emptyList())
    val libraryItems: StateFlow<List<BaseItemDto>> = _libraryItems.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isRefreshingLibraryItems = MutableStateFlow(false)
    val isRefreshingLibraryItems: StateFlow<Boolean> = _isRefreshingLibraryItems.asStateFlow()

    val activeServer: StateFlow<Server?> = dataStore.activeServerFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            dataStore.activeServerFlow
                .filterNotNull()
                .distinctUntilChanged()
                .collect { server ->
                    loadLibraries(server.baseUrl, server.accessToken)
                }
        }
    }

    private suspend fun loadLibraries(baseUrl: String, token: String) {
        try {
            withContext(Dispatchers.IO) {
                val api = jellyfinClient.createApiClient(baseUrl, token)
                val response = api.itemsApi.getItems(
                    parentId = null,
                    sortBy = listOf(ItemSortBy.SORT_NAME)
                )
                _libraries.value = response.content.items.filter { item ->
                    val type = item.collectionType
                    type !in listOf(CollectionType.UNKNOWN, CollectionType.PLAYLISTS, CollectionType.FOLDERS)
                }
            }
        } catch (e: Exception) {
            _libraries.value = emptyList()
        }
    }


    fun loadLibraryItems(libraryId: String) {
        viewModelScope.launch {
            _isRefreshingLibraryItems.value = true
            val server = dataStore.activeServerFlow.firstOrNull() ?: return@launch
            try {
                withContext(Dispatchers.IO) {
                    val api = jellyfinClient.createApiClient(server.baseUrl, server.accessToken)
                    val response = api.itemsApi.getItems(
                        parentId = libraryId.toUUID(),
                        sortBy = listOf(ItemSortBy.SORT_NAME)
                    )
                    _libraryItems.value = response.content.items
                }
            } catch (e: Exception) {
                _libraryItems.value = emptyList()
            } finally {
                _isRefreshingLibraryItems.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val server = dataStore.activeServerFlow.firstOrNull() ?: return@launch
                loadLibraries(server.baseUrl, server.accessToken)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

