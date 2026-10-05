package com.alessandro.tedesco.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Formato del file JSON pubblicato dall'insegnante.
 * UTF-8 obbligatorio: contiene umlaut (ä ö ü) e la ß.
 */
@Serializable
data class FeedDto(
    val version: Int = 1,
    val lesson: Int = 1,
    val publishedAt: String = "",
    val source: String = "",
    val words: List<WordDto> = emptyList()
)

@Serializable
data class WordDto(
    val id: String,
    val german: String,
    val italian: String,
    val example: String = "",
    val article: String? = null,
    val pronunciation: String? = null,
    val level: String = "A1",
    val lesson: Int = 1,
    val tags: List<String> = emptyList(),
    val archived: Boolean = false
)
