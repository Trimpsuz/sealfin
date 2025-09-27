package dev.trimpsuz.sealfin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.trimpsuz.sealfin.ui.composables.EpisodePopup
import dev.trimpsuz.sealfin.ui.composables.LibraryItemCard
import dev.trimpsuz.sealfin.ui.composables.SeriesItemCard
import dev.trimpsuz.sealfin.ui.viewmodel.AuthViewModel
import dev.trimpsuz.sealfin.ui.viewmodel.HomeViewModel
import org.jellyfin.sdk.model.api.BaseItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLibrarySelected: (String, String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onLibraryItemSelected: (String, String) -> Unit
) {
    val continueWatching by homeViewModel.continueWatching.collectAsState()
    val nextUp by homeViewModel.nextUp.collectAsState()
    val recentlyAdded by homeViewModel.recentlyAdded.collectAsState()
    val activeServer by authViewModel.activeServer.collectAsState()

    var selectedEpisode by remember { mutableStateOf<BaseItemDto?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (continueWatching.isNotEmpty()) {
                item {
                    Text(
                        "Continue Watching",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(continueWatching, key = { it.id }) { item ->
                            SeriesItemCard(item, activeServer?.baseUrl, { item -> selectedEpisode = item })
                        }
                    }
                }
            }

            if (nextUp.isNotEmpty()) {
                item {
                    Text(
                        "Next Up",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(nextUp, key = { it.id }) { item ->
                            SeriesItemCard(item, activeServer?.baseUrl, { item -> selectedEpisode = item })
                        }
                    }
                }
            }

            recentlyAdded.forEach { library ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Latest in ${library.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = {
                            onLibrarySelected(
                                library.id,
                                library.name
                            )
                        }) {
                            Text("View More")
                        }
                    }
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(library.items) { item ->
                            LibraryItemCard(item, activeServer?.baseUrl, onLibraryItemSelected)
                        }
                    }
                }
            }
        }
    }

    if (selectedEpisode != null) {
        EpisodePopup(
            episode = selectedEpisode!!,
            seriesName = selectedEpisode!!.seriesName ?: "Series",
            onDismiss = { selectedEpisode = null },
            activeServer = activeServer
        )
    }
}