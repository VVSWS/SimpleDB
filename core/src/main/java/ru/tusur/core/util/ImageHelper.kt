package ru.tusur.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.time.Instant

object ImageHelper {

    fun saveImageToFiles(context: Context, bitmap: Bitmap, quality: Int = 90): Uri? {
        return try {
            val imagesDir = File(context.filesDir, "images")
            imagesDir.mkdirs()
            val fileName = "img_${Instant.now().epochSecond}.jpg"
            val file = File(imagesDir, fileName)
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }
            file.toUri()
        } catch (e: Exception) {
            null
        }
    }

    fun deleteImage(context: Context, imageUri: String): Boolean {
        return try {
            val file = Uri.parse(imageUri).path?.let { File(it) }
            file?.exists() == true && file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }
}