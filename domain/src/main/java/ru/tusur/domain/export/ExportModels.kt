package ru.tusur.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportEntry(
    val id: Long,
    val timestamp: Long,
    val year: Int?,
    val brand: String?,
    val modelName: String?,
    val modelBrand: String?,
    val modelYear: Int?,
    val location: String?,
    val title: String,
    val description: String,
    val notes: String?,
    val images: List<String>
)

@Serializable
data class ExportDatabase(
    val entries: List<ExportEntry>
)
