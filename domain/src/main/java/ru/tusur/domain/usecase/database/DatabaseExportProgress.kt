package ru.tusur.domain.usecase.database

sealed class DatabaseExportProgress {
    data class Started(val totalBytes: Long) : DatabaseExportProgress()
    data class Progress(val writtenBytes: Long, val totalBytes: Long) : DatabaseExportProgress()
    object Finished : DatabaseExportProgress()
    data class Error(val message: String) : DatabaseExportProgress()
}
