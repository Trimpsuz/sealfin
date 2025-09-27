package dev.trimpsuz.sealfin.ui.screens.libraries

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import dev.trimpsuz.sealfin.ui.viewmodel.libraries.LibraryItemViewModel
import org.jellyfin.sdk.model.api.BaseItemKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryItemScreen(
    itemId: String,
    itemName: String,
    onBack: () -> Unit,
    viewModel: LibraryItemViewModel = hiltViewModel(),
    onSeasonSelected: (String, String) -> Unit
) {
    val item by viewModel.item.collectAsState()
    val seasons by viewModel.seasons.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(itemName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    AsyncImage(
                        model = "${activeServer?.baseUrl}/Items/${item?.id}/Images/Backdrop/0",
                        contentDescription = itemName,
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = itemName,
                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val startYear = item?.premiereDate?.year ?: item?.productionYear
                    val endYear = item?.endDate?.year

                    val yearText = when {
                        startYear != null && endYear != null && startYear != endYear -> "$startYear — $endYear"
                        startYear != null && (endYear == null || endYear == startYear) -> startYear.toString()
                        else -> "Unknown"
                    }

                    Text(
                        yearText,
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )

                    item?.runTimeTicks?.let { ticks ->
                        val minutes = ticks / 600000000L
                        Text("$minutes min", style = MaterialTheme.typography.titleMedium)
                    }

                    item?.officialRating?.let { rating ->
                        Text(rating, style = MaterialTheme.typography.titleMedium)
                    }

                    item?.communityRating?.let { rating ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xfff3b731),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", rating),
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row (
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Button(onClick = { /* TODO play */ }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Play")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { /* TODO mark completed */ }) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { /* TODO like */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (!item?.genres.isNullOrEmpty()) {
                    LazyRow (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(item!!.genres!!) { genre ->
                            AssistChip(
                                onClick = { },
                                label = { Text(genre) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                if (!item?.overview.isNullOrBlank()) {
                    AndroidView(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        factory = { context ->
                            TextView(context).apply {
                                setTextColor(onBackgroundColor.toArgb())
                                textSize = 16f
                            }
                        },
                        update = { tv ->
                            tv.text = HtmlCompat.fromHtml(
                                item!!.overview!!,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        }
                    )
                }

                Spacer(Modifier.height(18.dp))

                if (item?.type == BaseItemKind.SERIES && seasons.isNotEmpty()) {
                    Text(
                        "Seasons",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                        items(seasons) { season ->
                            Card(
                                modifier = Modifier
                                    .width(140.dp)
                                    .padding(end = 8.dp),
                                onClick = { onSeasonSelected(season.id.toString(), season.name ?: "") }
                            ) {
                                Column {
                                    AsyncImage(
                                        model = "${activeServer?.baseUrl}/Items/${season.id}/Images/Primary",
                                        contentDescription = season.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentScale = ContentScale.Crop,
                                        placeholder = rememberVectorPainter(Icons.Outlined.Image)
                                    )
                                    Text(
                                        season.name ?: "Season",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }

                if (!item?.people.isNullOrEmpty()) {
                    Text(
                        "Cast & Crew",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                        items(item!!.people!!) { person ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(end = 8.dp)
                            ) {
                                AsyncImage(
                                    model = "${activeServer?.baseUrl}/Items/${person.id}/Images/Primary",
                                    contentDescription = person.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = rememberVectorPainter(Icons.Outlined.Person)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = person.name ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                person.role?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
