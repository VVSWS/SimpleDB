package ru.tusur.core.files

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorage {

    fun createImageFile(context: Context): File {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        return File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
    }

    fun resolveImageFile(context: Context, relativePath: String): File {
        return File(context.filesDir, relativePath)
    }

    fun deleteImageFile(baseDir: File, relativePath: String) {
        val file = File(baseDir, relativePath)
        if (file.exists()) file.delete()
    }

    fun savePickedImage(context: Context, sourceUri: Uri): String {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val file = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(sourceUri).use { input ->
            FileOutputStream(file).use { output ->
                input?.copyTo(output)
            }
        }

        return "images/${file.name}"
    }
}
