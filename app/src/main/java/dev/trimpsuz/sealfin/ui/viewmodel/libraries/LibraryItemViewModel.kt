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
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.UserItemDataDto
import org.jellyfin.sdk.model.serializer.toUUID
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryItemViewModel @Inject constructor(
    private val jellyfinClient: JellyfinClientWrapper,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _item = MutableStateFlow<BaseItemDto?>(null)
    val item: StateFlow<BaseItemDto?> = _item.asStateFlow()

    private val _seasons = MutableStateFlow<List<BaseItemDto>>(emptyList())
    val seasons: StateFlow<List<BaseItemDto>> = _seasons.asStateFlow()

    val activeServer: StateFlow<Server?> =
        dataStore.activeServerFlow.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            val server = dataStore.activeServerFlow.firstOrNull() ?: return@launch
            try {
                withContext(Dispatchers.IO) {
                    val api = jellyfinClient.createApiClient(server.baseUrl, server.accessToken)

                    val response = api.itemsApi.getItems(
                        ids = listOf(itemId.toUUID()),
                        fields = listOf(
                            ItemFields.OVERVIEW,
                            ItemFields.GENRES,
                            ItemFields.PEOPLE
                        )
                    )
                    val loadedItem = response.content.items.firstOrNull()
                    _item.value = loadedItem

                    if (loadedItem?.type == BaseItemKind.SERIES) {
                        val seasonsResponse = api.itemsApi.getItems(
                            parentId = itemId.toUUID(),
                            includeItemTypes = listOf(BaseItemKind.SEASON),
                            sortBy = listOf(ItemSortBy.SORT_NAME)
                        )
                        _seasons.value = seasonsResponse.content.items
                    } else {
                        _seasons.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _item.value = null
                _seasons.value = emptyList()
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
        _item.update { item ->
            if (item?.id == id) {
                item.copy(userData = item.userData?.let(transform))
            } else item
        }
    }
}

