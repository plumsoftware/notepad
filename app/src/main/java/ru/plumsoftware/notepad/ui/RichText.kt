package ru.plumsoftware.notepad.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import ru.plumsoftware.notepad.data.model.TextSpan
import ru.plumsoftware.notepad.data.model.TextSpanStyle

/** Куда ведёт кликабельный фрагмент текста заметки. */
sealed class LinkTarget {
    data class Url(val url: String) : LinkTarget()
    data class Phone(val number: String) : LinkTarget()
}

private val urlRegex = Regex(
    "(https?://[^\\s]+|www\\.[^\\s]+|[a-zA-Z0-9][a-zA-Z0-9\\-]*(?:\\.[a-zA-Z0-9\\-]+)+(?:/[^\\s]*)?)"
)
// Телефоны формата +7 999 123-45-67, 8 (495) 123 45 67 и т.п.
private val phoneRegex = Regex("\\+?\\d[\\d\\s\\-()]{8,}\\d")

private data class DetectedLink(val start: Int, val end: Int, val target: LinkTarget)

private fun detectLinks(text: String): List<DetectedLink> {
    val result = mutableListOf<DetectedLink>()
    // Сначала ссылки
    urlRegex.findAll(text).forEach { m ->
        // Отсекаем случаи, когда это на самом деле похоже на телефон (только цифры/скобки)
        val value = m.value
        if (value.any { it.isLetter() }) {
            result.add(DetectedLink(m.range.first, m.range.last + 1, LinkTarget.Url(value)))
        }
    }
    // Телефоны — только там, где нет пересечения со ссылками
    phoneRegex.findAll(text).forEach { m ->
        val start = m.range.first
        val end = m.range.last + 1
        val overlaps = result.any { start < it.end && end > it.start }
        // Требуем достаточно цифр, чтобы не ловить случайные числа
        val digitCount = m.value.count { it.isDigit() }
        if (!overlaps && digitCount in 10..15) {
            result.add(DetectedLink(start, end, LinkTarget.Phone(m.value.trim())))
        }
    }
    return result.sortedBy { it.start }
}

private fun formattingStyle(style: String): SpanStyle = when (style) {
    TextSpanStyle.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
    TextSpanStyle.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
    TextSpanStyle.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
    TextSpanStyle.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    else -> SpanStyle()
}

/** Собирает SpanStyle форматирования для позиции [index] из всех перекрывающих спанов. */
private fun combinedFormattingAt(index: Int, spans: List<TextSpan>): SpanStyle {
    var weight: FontWeight? = null
    var italic: FontStyle? = null
    var underline = false
    var strike = false
    spans.forEach { s ->
        if (index >= s.start && index < s.end) {
            when (s.style) {
                TextSpanStyle.BOLD -> weight = FontWeight.Bold
                TextSpanStyle.ITALIC -> italic = FontStyle.Italic
                TextSpanStyle.UNDERLINE -> underline = true
                TextSpanStyle.STRIKETHROUGH -> strike = true
            }
        }
    }
    val decoration = when {
        underline && strike -> TextDecoration.combine(
            listOf(TextDecoration.Underline, TextDecoration.LineThrough)
        )
        underline -> TextDecoration.Underline
        strike -> TextDecoration.LineThrough
        else -> null
    }
    return SpanStyle(fontWeight = weight, fontStyle = italic, textDecoration = decoration)
}

/**
 * Строит [AnnotatedString] тела заметки: применяет форматирование [spans]
 * и подсвечивает ссылки/телефоны. Если [onLinkClick] задан — делает их кликабельными.
 */
fun buildNoteAnnotatedString(
    text: String,
    spans: List<TextSpan>,
    linkColor: Color,
    onLinkClick: ((LinkTarget) -> Unit)? = null
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    val links = detectLinks(text)
    val linkStyle = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)

    return buildAnnotatedString {
        var i = 0
        // Разбиваем текст на «обычные» участки и участки-ссылки
        fun appendFormatted(from: Int, to: Int) {
            var j = from
            while (j < to) {
                val style = combinedFormattingAt(j, spans)
                var k = j + 1
                while (k < to && combinedFormattingAt(k, spans) == style) k++
                withStyle(style) { append(text.substring(j, k)) }
                j = k
            }
        }

        for (link in links) {
            if (link.start > i) appendFormatted(i, link.start)
            val chunk = text.substring(link.start, link.end)
            if (onLinkClick != null) {
                val annotation = when (val t = link.target) {
                    is LinkTarget.Url -> LinkAnnotation.Clickable(
                        tag = "url",
                        styles = TextLinkStyles(linkStyle)
                    ) { onLinkClick(t) }
                    is LinkTarget.Phone -> LinkAnnotation.Clickable(
                        tag = "phone",
                        styles = TextLinkStyles(linkStyle)
                    ) { onLinkClick(t) }
                }
                withLink(annotation) { append(chunk) }
            } else {
                withStyle(linkStyle) { append(chunk) }
            }
            i = link.end
        }
        if (i < text.length) appendFormatted(i, text.length)
    }
}

/** VisualTransformation для редактора: подсветка форматирования и ссылок без изменения текста. */
class RichTextVisualTransformation(
    private val spans: List<TextSpan>,
    private val linkColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = buildNoteAnnotatedString(
            text = text.text,
            spans = spans,
            linkColor = linkColor,
            onLinkClick = null
        )
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

// --- Редактирование спанов ---

/** Сдвигает/подрезает спаны при изменении текста (диффом по общим префиксу и суффиксу). */
fun shiftSpansOnEdit(oldText: String, newText: String, spans: List<TextSpan>): List<TextSpan> {
    if (oldText == newText || spans.isEmpty()) return spans
    val oldLen = oldText.length
    val newLen = newText.length

    var prefix = 0
    val maxPrefix = minOf(oldLen, newLen)
    while (prefix < maxPrefix && oldText[prefix] == newText[prefix]) prefix++

    var suffix = 0
    while (suffix < (maxPrefix - prefix) &&
        oldText[oldLen - 1 - suffix] == newText[newLen - 1 - suffix]
    ) suffix++

    val removedStart = prefix
    val removedEnd = oldLen - suffix
    val insertedLen = newLen - suffix - prefix
    val delta = insertedLen - (removedEnd - removedStart)

    fun adjust(pos: Int): Int = when {
        pos <= removedStart -> pos
        pos >= removedEnd -> pos + delta
        else -> removedStart // позиция внутри удалённой области схлопывается
    }

    return spans.mapNotNull { s ->
        val ns = adjust(s.start)
        val ne = adjust(s.end)
        if (ne > ns) s.copy(start = ns, end = ne) else null
    }
}

/** Возвращает true, если весь диапазон [start, end) уже покрыт стилем [style]. */
fun isRangeFullyStyled(spans: List<TextSpan>, start: Int, end: Int, style: String): Boolean {
    if (start >= end) return false
    for (i in start until end) {
        val covered = spans.any { it.style == style && i >= it.start && i < it.end }
        if (!covered) return false
    }
    return true
}

/** Переключает стиль [style] на диапазоне [start, end): добавляет либо снимает его. */
fun toggleStyle(spans: List<TextSpan>, start: Int, end: Int, style: String): List<TextSpan> {
    if (start >= end) return spans
    val fullyStyled = isRangeFullyStyled(spans, start, end, style)
    val result = mutableListOf<TextSpan>()

    // Разрезаем существующие спаны этого стиля по границам диапазона; чужие стили не трогаем
    for (s in spans) {
        if (s.style != style) {
            result.add(s)
            continue
        }
        // Оставляем части, лежащие вне [start, end)
        if (s.start < start) result.add(s.copy(end = minOf(s.end, start)))
        if (s.end > end) result.add(s.copy(start = maxOf(s.start, end)))
        // Часть внутри [start, end) удаляется (при снятии) либо будет пересоздана ниже
    }
    if (!fullyStyled) {
        result.add(TextSpan(start = start, end = end, style = style))
    }
    return result.filter { it.end > it.start }.let { mergeSpans(it) }
}

/** Склеивает соседние/перекрывающиеся спаны одного стиля. */
private fun mergeSpans(spans: List<TextSpan>): List<TextSpan> {
    val byStyle = spans.groupBy { it.style }
    val result = mutableListOf<TextSpan>()
    byStyle.forEach { (style, list) ->
        val sorted = list.sortedBy { it.start }
        var cur: TextSpan? = null
        for (s in sorted) {
            val c = cur
            cur = if (c == null) {
                s
            } else if (s.start <= c.end) {
                c.copy(end = maxOf(c.end, s.end))
            } else {
                result.add(c); s
            }
        }
        cur?.let { result.add(it) }
    }
    return result
}
