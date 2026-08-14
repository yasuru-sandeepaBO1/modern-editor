package com.example.modern_editor.editor

data class BracketPair(val openIndex: Int, val closeIndex: Int)

private val PAIRS = mapOf('(' to ')', '[' to ']', '{' to '}')
private val CLOSERS = PAIRS.entries.associate { it.value to it.key }

/**
 * Matching `()[]{}` pair for the bracket at or just before [caret].
 * Brackets inside double-quoted strings and `//` comments are ignored.
 */
fun matchingBracket(text: String, caret: Int): BracketPair? {
    if (text.isEmpty()) return null
    val index = bracketIndexAt(text, caret) ?: return null
    val ch = text[index]
    val mask = ignoredMask(text)
    if (mask[index]) return null
    return when {
        ch in PAIRS -> findCloser(text, index, ch, PAIRS.getValue(ch), mask)
        ch in CLOSERS -> findOpener(text, index, ch, CLOSERS.getValue(ch), mask)
        else -> null
    }
}

private fun bracketIndexAt(text: String, caret: Int): Int? {
    val at = caret.coerceIn(0, text.length)
    if (at < text.length && (text[at] in PAIRS || text[at] in CLOSERS)) return at
    if (at > 0 && (text[at - 1] in PAIRS || text[at - 1] in CLOSERS)) return at - 1
    return null
}

private fun findCloser(
    text: String,
    openIndex: Int,
    open: Char,
    close: Char,
    mask: BooleanArray
): BracketPair? {
    var depth = 0
    for (i in openIndex until text.length) {
        if (mask[i]) continue
        when (text[i]) {
            open -> depth++
            close -> {
                depth--
                if (depth == 0) return BracketPair(openIndex, i)
            }
        }
    }
    return null
}

private fun findOpener(
    text: String,
    closeIndex: Int,
    close: Char,
    open: Char,
    mask: BooleanArray
): BracketPair? {
    var depth = 0
    for (i in closeIndex downTo 0) {
        if (mask[i]) continue
        when (text[i]) {
            close -> depth++
            open -> {
                depth--
                if (depth == 0) return BracketPair(i, closeIndex)
            }
        }
    }
    return null
}

/** True where a character sits inside a `"` string or a `//` line comment. */
private fun ignoredMask(text: String): BooleanArray {
    val mask = BooleanArray(text.length)
    var i = 0
    while (i < text.length) {
        val c = text[i]
        when {
            c == '/' && i + 1 < text.length && text[i + 1] == '/' -> {
                while (i < text.length && text[i] != '\n') {
                    mask[i] = true
                    i++
                }
            }
            c == '"' -> {
                mask[i] = true
                i++
                while (i < text.length && text[i] != '\n') {
                    mask[i] = true
                    if (text[i] == '"' && text[i - 1] != '\\') {
                        i++
                        break
                    }
                    i++
                }
            }
            else -> i++
        }
    }
    return mask
}
