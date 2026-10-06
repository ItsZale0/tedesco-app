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
    val generatedAt: String = "",
    val guida: GuidaDto? = null,
    val words: List<WordDto> = emptyList(),
    val lessons: List<LessonDto> = emptyList()
)

@Serializable
data class GuidaDto(
    val titolo: String = "",
    val docUrl: String = "",
    val sezioni: List<SezioneDto> = emptyList()
)

@Serializable
data class SezioneDto(
    val titolo: String = "",
    val testo: String = ""
)

@Serializable
data class LessonDto(
    val numero: Int = 1,
    val titolo: String = "",
    val data: String = "",
    val pdfUrl: String = ""
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
