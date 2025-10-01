package dev.trimpsuz.sealfin.ui.screens.libraries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import dev.trimpsuz.sealfin.ui.viewmodel.libraries.LibrariesViewModel
import org.jellyfin.sdk.model.api.ImageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(
    viewModel: LibrariesViewModel = hiltViewModel(),
    onLibrarySelected: (String, String) -> Unit
) {
    val libraries by viewModel.libraries.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Libraries") }) }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(libraries) { library ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable {
                                onLibrarySelected(
                                    library.id.toString(),
                                    library.name ?: "Library"
                                )
                            },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = "${activeServer?.baseUrl}/Items/${library.id}/Images/Primary?tag=${
                                    library.imageTags?.get(
                                        ImageType.PRIMARY
                                    )
                                }",
                                contentDescription = library.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                error = rememberVectorPainter(Icons.Outlined.Folder),
                                placeholder = rememberVectorPainter(Icons.Outlined.Folder)
                            )

                            Text(
                                text = library.name ?: "Unknown",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
