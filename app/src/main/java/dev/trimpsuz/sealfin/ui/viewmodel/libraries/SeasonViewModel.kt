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
import kotlinx.coroutines.launch
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.serializer.toUUID
import javax.inject.Inject

@HiltViewModel
class SeasonViewModel @Inject constructor(
    private val jellyfinClient: JellyfinClientWrapper,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _season = MutableStateFlow<BaseItemDto?>(null)
    val season: StateFlow<BaseItemDto?> = _season.asStateFlow()

    private val _episodes = MutableStateFlow<List<BaseItemDto>>(emptyList())
    val episodes: StateFlow<List<BaseItemDto>> = _episodes.asStateFlow()

    val activeServer: StateFlow<Server?> =
        dataStore.activeServerFlow.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun loadSeason(seasonId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val server = dataStore.activeServerFlow.firstOrNull() ?: return@launch
            val api = jellyfinClient.createApiClient(server.baseUrl, server.accessToken)
            val response = api.itemsApi.getItems(ids = listOf(seasonId.toUUID()))
            _season.value = response.content.items.firstOrNull()
        }
    }

    fun loadEpisodes(seasonId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val server = dataStore.activeServerFlow.firstOrNull() ?: return@launch
            val api = jellyfinClient.createApiClient(server.baseUrl, server.accessToken)
            val response = api.itemsApi.getItems(
                parentId = seasonId.toUUID(),
                sortBy = listOf(ItemSortBy.INDEX_NUMBER),
                fields = listOf(
                    ItemFields.OVERVIEW
                )
            )
            _episodes.value = response.content.items
        }
    }
}
