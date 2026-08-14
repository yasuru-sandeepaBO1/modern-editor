package com.example.modern_editor.editor

/**
 * Maps a 1-based line number to a character offset in [text].
 * Empty documents and out-of-range requests clamp to a valid caret position.
 */
fun offsetForLine(text: String, line: Int): Int {
    if (text.isEmpty()) return 0
    val lines = text.split('\n')
    val targetIndex = (line - 1).coerceIn(0, lines.lastIndex)
    var offset = 0
    for (i in 0 until targetIndex) {
        offset += lines[i].length + 1
    }
    return offset.coerceIn(0, text.length)
}
