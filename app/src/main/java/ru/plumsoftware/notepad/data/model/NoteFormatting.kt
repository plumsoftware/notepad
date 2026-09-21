package ru.plumsoftware.notepad.data.model

/** Преобразование форматирования заметки в HTML для системного уведомления. */
object NoteFormatting {

    fun spansToHtml(text: String, spans: List<TextSpan>): String {
        if (text.isEmpty()) return ""
        if (spans.isEmpty()) return escape(text)
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val styles = stylesAt(i, spans)
            var j = i + 1
            while (j < text.length && stylesAt(j, spans) == styles) j++

            val open = StringBuilder()
            val close = StringBuilder()
            // Порядок открытия: b, i, u, s; закрытие — в обратном порядке
            if (TextSpanStyle.BOLD in styles) { open.append("<b>"); close.insert(0, "</b>") }
            if (TextSpanStyle.ITALIC in styles) { open.append("<i>"); close.insert(0, "</i>") }
            if (TextSpanStyle.UNDERLINE in styles) { open.append("<u>"); close.insert(0, "</u>") }
            if (TextSpanStyle.STRIKETHROUGH in styles) { open.append("<s>"); close.insert(0, "</s>") }

            sb.append(open)
            sb.append(escape(text.substring(i, j)))
            sb.append(close)
            i = j
        }
        return sb.toString()
    }

    private fun stylesAt(index: Int, spans: List<TextSpan>): Set<String> =
        spans.asSequence()
            .filter { index >= it.start && index < it.end }
            .map { it.style }
            .toSet()

    private fun escape(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br>")
}
