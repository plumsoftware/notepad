package ru.plumsoftware.notepad.data.model

import kotlinx.serialization.Serializable

/**
 * Одна область форматирования в тексте заметки.
 * [style] — один из [TextSpanStyle]. Диапазон [start, end) в символах поля description.
 */
@Serializable
data class TextSpan(
    val start: Int,
    val end: Int,
    val style: String
)

object TextSpanStyle {
    const val BOLD = "B"
    const val ITALIC = "I"
    const val UNDERLINE = "U"
    const val STRIKETHROUGH = "S"
}
