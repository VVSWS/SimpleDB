package ru.tusur.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportEntry(
    val id: Long,
    val timestamp: Long,

    // Domain dictionary values
    val year: Int?,
    val brand: String?,
    val modelName: String?,
    val location: String?,

    // Entry content
    val title: String,
    val description: String,

    // Image URIs (files exported separately)
    val images: List<String>
)

@Serializable
data class ExportDatabase(
    val entries: List<ExportEntry>
)
