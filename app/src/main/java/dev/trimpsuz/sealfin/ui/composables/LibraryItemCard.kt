package dev.trimpsuz.sealfin.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType

@Composable
fun LibraryItemCard(
    item: BaseItemDto,
    baseUrl: String?,
    onLibraryItemSelected: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onLibraryItemSelected(item.id.toString(), item.name ?: "") }
    ) {
        AsyncImage(
            model = "$baseUrl/Items/${item.id}/Images/Primary?tag=${item.imageTags?.get(ImageType.PRIMARY)}",
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            error = rememberVectorPainter(Icons.Outlined.Image),
            placeholder = rememberVectorPainter(Icons.Outlined.Image)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            item.name ?: "Unknown",
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
