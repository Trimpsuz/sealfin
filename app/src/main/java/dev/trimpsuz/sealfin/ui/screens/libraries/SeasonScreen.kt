package dev.trimpsuz.sealfin.ui.screens.libraries

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import dev.trimpsuz.sealfin.ui.viewmodel.libraries.SeasonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonScreen(
    parentId: String,
    seasonId: String,
    seasonName: String,
    onBack: () -> Unit,
    viewModel: SeasonViewModel = hiltViewModel()
) {
    val season by viewModel.season.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(seasonId) {
        viewModel.loadSeason(seasonId)
        viewModel.loadEpisodes(seasonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(seasonName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (season == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        AsyncImage(
                            model = "${activeServer?.baseUrl}/Items/$parentId/Images/Backdrop/0",
                            contentDescription = seasonName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background
                                        ),
                                        startY = 100f
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            AsyncImage(
                                model = "${activeServer?.baseUrl}/Items/${season?.id}/Images/Primary",
                                contentDescription = seasonName,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.Bottom)
                            ) {
                                Text(
                                    text = season?.seriesName ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = seasonName,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                items(episodes) { episode ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { /* TODO open episode */ },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = "${activeServer?.baseUrl}/Items/${episode.id}/Images/Primary",
                                contentDescription = episode.name,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )

                            Spacer(Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .verticalScroll(rememberScrollState(), enabled = false)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "${episode.indexNumber}. ${episode.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = Int.MAX_VALUE,
                                        overflow = TextOverflow.Clip
                                    )
                                    if (!episode.overview.isNullOrBlank()) {
                                        AndroidView(
                                            modifier = Modifier.fillMaxWidth(),
                                            factory = { context ->
                                                TextView(context).apply {
                                                    setTextColor(onBackgroundColor.toArgb())
                                                    textSize = 12f
                                                }
                                            },
                                            update = { tv ->
                                                tv.text = HtmlCompat.fromHtml(
                                                    episode.overview!!,
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                                )
                                            }
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    CardDefaults.cardColors().containerColor
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}
