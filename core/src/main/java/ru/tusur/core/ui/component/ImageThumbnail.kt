package ru.tusur.core.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ImageThumbnail(
    uri: String,
    contentDescription: String = "Fault image",
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val model = remember(uri) {
        when {
            uri.startsWith("content://") -> uri.toUri()
            uri.startsWith("file://") -> uri.toUri()
            uri.startsWith("/") -> File(uri)
            else -> File(context.filesDir, uri)
        }
    }


    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(uri) }
    )

}