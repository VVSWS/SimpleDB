package ru.tusur.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ImageThumbnail(
    relativePath: String,
    contentDescription: String = "Fault image",
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val file = File(context.filesDir, relativePath)

    AsyncImage(
        model = file,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(relativePath) }
    )
}

