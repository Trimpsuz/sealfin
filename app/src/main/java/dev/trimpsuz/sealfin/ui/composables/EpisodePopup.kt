package dev.trimpsuz.sealfin.ui.composables

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import dev.trimpsuz.sealfin.data.Server
import org.jellyfin.sdk.model.api.BaseItemDto
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodePopup(
    episode: BaseItemDto,
    seriesName: String,
    onDismiss: () -> Unit,
    activeServer: Server?,
    updatePlayed: (UUID, Boolean) -> Unit,
    updateFavorite: (UUID, Boolean) -> Unit
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        tonalElevation = 8.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = "${activeServer?.baseUrl}/Items/${episode.id}/Images/Primary",
                    contentDescription = episode.name,
                    modifier = Modifier
                        .width(180.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .matchParentSize()
                            .verticalScroll(rememberScrollState(), enabled = false)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = seriesName,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "S${episode.parentIndexNumber}:E${episode.indexNumber} — ${episode.name}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        BottomSheetDefaults.ContainerColor
                                    )
                                )
                            )
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                episode.premiereDate?.let { date ->
                    Text(
                        date.format(DateTimeFormatter.ofPattern("dd.M.yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                episode.runTimeTicks?.let { ticks ->
                    Text(
                        "${ticks / 600000000L} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                episode.communityRating?.let { rating ->
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
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Row {
                Button(onClick = { /* TODO play */ }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Play")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = {
                    updatePlayed(episode.id, episode.userData?.played == true)
                }) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (episode.userData?.played == true) Color(0xffd14747) else LocalContentColor.current,
                    )
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = {
                    updateFavorite(episode.id, episode.userData?.isFavorite == true)
                }) {
                    Icon(
                        if (episode.userData?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (episode.userData?.isFavorite == true) Color(0xffd14747) else LocalContentColor.current,
                    )
                }
            }

            if (!episode.overview.isNullOrBlank()) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        TextView(context).apply {
                            setTextColor(onSurfaceColor.toArgb())
                            textSize = 14f
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

            Spacer(Modifier.height(16.dp))
        }
    }
}
