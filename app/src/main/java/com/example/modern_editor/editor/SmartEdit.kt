package com.example.modern_editor.editor

data class SmartEditResult(
    val insertAfter: String,
    val caretOffsetFromInsertStart: Int
)

private val OPENERS = mapOf(
    '{' to '}',
    '(' to ')',
    '[' to ']',
    '"' to '"',
    '`' to '`'
)

/**
 * If [inserted] is a single opener, return the closer to place after the caret
 * (or around a non-empty [selected] range). Returns null when nothing should be added.
 */
fun autoCloseAfter(inserted: String, selected: String, nextChar: Char?): SmartEditResult? {
    if (inserted.length != 1) return null
    val closer = OPENERS[inserted[0]] ?: return null
    if (selected.isNotEmpty()) {
        return SmartEditResult(
            insertAfter = selected + closer,
            caretOffsetFromInsertStart = selected.length
        )
    }
    if (nextChar == closer) return null
    return SmartEditResult(insertAfter = closer.toString(), caretOffsetFromInsertStart = 0)
}

/**
 * Spaces to insert after a newline that was just typed. [lineBeforeNewline] is the
 * previous line's text (without the newline).
 */
fun indentAfterNewline(lineBeforeNewline: String, tabSize: Int): String {
    val size = tabSize.coerceIn(1, 8)
    val indent = lineBeforeNewline.takeWhile { it == ' ' || it == '\t' }.replace("\t", " ".repeat(size))
    val trimmed = lineBeforeNewline.trimEnd()
    val extra = if (trimmed.isNotEmpty() && trimmed.last() in "{[(") " ".repeat(size) else ""
    return indent + extra
}

fun spacesForTab(tabSize: Int): String = " ".repeat(tabSize.coerceIn(1, 8))
