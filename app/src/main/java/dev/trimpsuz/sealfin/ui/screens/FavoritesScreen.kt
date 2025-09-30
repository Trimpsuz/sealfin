package dev.trimpsuz.sealfin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.trimpsuz.sealfin.ui.composables.EpisodePopup
import dev.trimpsuz.sealfin.ui.composables.LibraryItemCard
import dev.trimpsuz.sealfin.ui.composables.SeasonCard
import dev.trimpsuz.sealfin.ui.composables.SeriesItemCard
import dev.trimpsuz.sealfin.ui.viewmodel.FavoritesViewModel
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onSeasonSelected: (String, String, String) -> Unit,
    onLibraryItemSelected: (String, String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()

    var selectedEpisodeId by remember { mutableStateOf<UUID?>(null) }
    val selectedEpisode = favorites.values.flatten().find { it.id == selectedEpisodeId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Favorites") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            favorites.forEach { (category, items) ->
                if (items.isNotEmpty()) {
                    item {
                        Text(
                            category,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                when (item.type) {
                                    BaseItemKind.EPISODE -> {
                                        SeriesItemCard(item, activeServer?.baseUrl, { item -> selectedEpisodeId = item.id })
                                    }
                                    BaseItemKind.SEASON -> {
                                        SeasonCard(item, activeServer?.baseUrl, onSeasonSelected)
                                    }
                                    else -> {
                                        LibraryItemCard(item, activeServer?.baseUrl, onLibraryItemSelected)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedEpisode != null) {
        EpisodePopup(
            episode = selectedEpisode,
            seriesName = selectedEpisode.seriesName ?: "Series",
            onDismiss = { selectedEpisodeId = null },
            activeServer = activeServer,
            updatePlayed = viewModel::updatePlayed,
            updateFavorite = viewModel::updateFavorite,
            onLibraryItemSelected = onLibraryItemSelected
        )
    }
}
