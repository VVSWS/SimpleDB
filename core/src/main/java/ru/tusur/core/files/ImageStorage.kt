package ru.tusur.core.files

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorage {

    private const val IMAGES_DIR = "images"

    // ---------------------------------------------------------
    // Path normalization
    // ---------------------------------------------------------

    /**
     * Ensures the stored path always looks like:
     *   images/filename.jpg
     *
     * Accepts:
     *   - "images/filename.jpg"
     *   - "filename.jpg"
     *   - "file:///data/.../images/filename.jpg"
     *   - "content://..."
     *   - anything containing "images/"
     */
    private fun normalize(relativePath: String): String {
        val clean = relativePath
            .removePrefix("file://")
            .removePrefix("content://")
            .substringAfterLast("$IMAGES_DIR/")
        return "$IMAGES_DIR/$clean"
    }

    // ---------------------------------------------------------
    // Resolve file
    // ---------------------------------------------------------

    fun resolveImageFile(context: Context, relativePath: String): File {
        val cleanPath = normalize(relativePath)
        return File(context.filesDir, cleanPath)
    }

    // ---------------------------------------------------------
    // Save image
    // ---------------------------------------------------------

    /**
     * Saves a picked image into:
     *   files/images/img_<timestamp>.jpg
     *
     * Returns the relative DB path:
     *   images/img_<timestamp>.jpg
     */
    fun savePickedImage(context: Context, sourceUri: Uri): String {
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val filename = "img_${System.currentTimeMillis()}.jpg"
        val file = File(imagesDir, filename)

        context.contentResolver.openInputStream(sourceUri).use { input ->
            FileOutputStream(file).use { output ->
                input?.copyTo(output)
            }
        }

        return "$IMAGES_DIR/$filename"
    }

    // ---------------------------------------------------------
    // Delete image
    // ---------------------------------------------------------

    /**
     * Deletes the actual file on disk.
     * Works even if the path came from UI or DB in different formats.
     */
    fun deleteImageFile(context: Context, relativePath: String) {
        val file = resolveImageFile(context, relativePath)
        if (file.exists()) file.delete()
    }

    // ---------------------------------------------------------
    // Clear all images
    // ---------------------------------------------------------

    fun clearAllImages(filesDir: File) {
        val imagesDir = File(filesDir, "images")
        if (imagesDir.exists()) {
            imagesDir.deleteRecursively()
            imagesDir.mkdirs()
        }
    }
}
